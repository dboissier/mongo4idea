/*
 * Copyright (c) 2012 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.mongo.logic;

import com.mongodb.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.model.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MongoManager {

    private static final Logger LOG = Logger.getLogger(MongoManager.class);

    public String connect(String serverName, int serverPort, String username, String password) {
        try {
            MongoClient mongo = new MongoClient(serverName, serverPort);
            List<String> databaseNames = getDatabaseNames(mongo, username, password);
            if (databaseNames.isEmpty()) {
                throw new ConfigurationException("No databases were found");
            }


            DB databaseForTesting = mongo.getDB("admin");
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                databaseForTesting.authenticate(username, password.toCharArray());
            }

            return (String) databaseForTesting.eval("db.version();");

        } catch (IOException ex) {
            throw new ConfigurationException(ex);
        } catch (MongoException ex) {
            LOG.error("Error when accessing Mongo server", ex);
            throw new ConfigurationException(ex);
        }
    }

    public MongoServer loadDatabaseCollections(MongoConfiguration configuration) {
        try {
            MongoClient mongo = new MongoClient(configuration.getServerName(), configuration.getServerPort());

            MongoServer mongoServer = new MongoServer(configuration.getServerName(), configuration.getServerPort());

            List<String> databaseNames = getDatabaseNames(mongo, configuration.getUsername(), configuration.getPassword());
            for (String databaseName : databaseNames) {
                DB database = mongo.getDB(databaseName);
                MongoDatabase mongoDatabase = new MongoDatabase(database.getName());

                Set<String> collectionNames = database.getCollectionNames();
                for (String collectionName : collectionNames) {
                    mongoDatabase.addCollection(new MongoCollection(collectionName, database.getName()));
                }
                mongoServer.addDatabase(mongoDatabase);
            }
            return mongoServer;
        } catch (Exception ex) {
            LOG.error("Error when collecting Mongo databases", ex);
            throw new ConfigurationException(ex);
        }
    }

    public MongoCollectionResult loadCollectionValues(MongoConfiguration configuration, MongoCollection mongoCollection, MongoQueryOptions mongoQueryOptions) {
        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(mongoCollection.getName());
        try {
            Mongo mongo = new Mongo(configuration.getServerName(), configuration.getServerPort());
            DB database = mongo.getDB(mongoCollection.getDatabaseName());

            String username = configuration.getUsername();
            String password = configuration.getPassword();
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                database.authenticate(username, password.toCharArray());
            }

            DBCollection collection = database.getCollection(mongoCollection.getName());

            if (mongoQueryOptions.isAggregate()) {
                return aggregate(mongoQueryOptions, mongoCollectionResult, collection);
            }

            return find(mongoQueryOptions, mongoCollectionResult, collection);

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        }

    }

    private MongoCollectionResult aggregate(MongoQueryOptions mongoQueryOptions, MongoCollectionResult mongoCollectionResult, DBCollection collection) {
        List<DBObject> otherOperations = mongoQueryOptions.getOperationsExceptTheFirst();
        AggregationOutput aggregate = collection.aggregate(mongoQueryOptions.getFirstOperation(), otherOperations.toArray(new DBObject[otherOperations.size()]));
        int index = 0;
        Iterator<DBObject> iterator = aggregate.results().iterator();
        while (iterator.hasNext() && index < mongoQueryOptions.getResultLimit()) {
            mongoCollectionResult.add(iterator.next());
        }
        return mongoCollectionResult;
    }

    private MongoCollectionResult find(MongoQueryOptions mongoQueryOptions, MongoCollectionResult mongoCollectionResult, DBCollection collection) {
        DBObject filter = mongoQueryOptions.getFilter();
        DBCursor cursor = collection.find(filter);
        try {
            int index = 0;
            while (cursor.hasNext() && index < mongoQueryOptions.getResultLimit()) {
                mongoCollectionResult.add(cursor.next());
                index++;
            }
        } finally {
            cursor.close();
        }
        return mongoCollectionResult;
    }


    //Note : Hack of MongoClient#getDatabaseNames to retry with provided credentials
    public List<String> getDatabaseNames(MongoClient mongo, String username, String password){

        BasicDBObject cmd = new BasicDBObject();
        cmd.put("listDatabases", 1);

        DB adminDb = mongo.getDB("admin");

        CommandResult res = adminDb.command(cmd, mongo.getOptions());
        try {
            res.throwOnError();
        } catch (MongoException e) {
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                boolean authenticate = adminDb.authenticate(username, password.toCharArray());
                if (!authenticate) {
                    throw new ConfigurationException("Invalid creadentials");
                }
                res = adminDb.command(cmd, mongo.getOptions());
            }
        }

        res.throwOnError();

        List l = (List)res.get("databases");

        List<String> list = new ArrayList<String>();

        for (Object o : l) {
            list.add(((BasicDBObject)o).getString("name"));
        }
        return list;
    }
}
