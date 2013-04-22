/*
 * Copyright (c) 2013 David Boissier
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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.mongodb.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class MongoManager {

    private static final Logger LOG = Logger.getLogger(MongoManager.class);

    public static MongoManager getInstance(Project project) {
        return ServiceManager.getService(project, MongoManager.class);
    }

    public String connect(ServerConfiguration configuration) {
        return connectAndReturnServerVersion(configuration.getServerName(), configuration.getServerPort(), configuration.getUsername(), configuration.getPassword(), configuration.getUserDatabase());
    }

    public String connectAndReturnServerVersion(String serverName, int serverPort, String username, String password, String userDatabase) {

        MongoClient mongo = null;
        try {
            mongo = createMongoClient(serverName, serverPort);

            DB databaseForTesting;
            if (StringUtils.isNotEmpty(userDatabase)) {
                databaseForTesting = mongo.getDB(userDatabase);
            } else {
                databaseForTesting = mongo.getDB("admin");
            }

            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                databaseForTesting.authenticate(username, password.toCharArray());
            }

            return (String) databaseForTesting.eval("db.version();");

        } catch (IOException ex) {
            throw new ConfigurationException(ex);
        } catch (MongoException ex) {
            LOG.error("Error when accessing Mongo server", ex);
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public MongoServer loadDatabaseCollections(MongoServer mongoServer) {
        MongoClient mongo = null;
        try {
            mongo = createMongoClient(mongoServer.getServerName(), mongoServer.getServerPort());

            String userDatabase = mongoServer.getConfiguration().getUserDatabase();
            if (StringUtils.isNotEmpty(userDatabase)) {
                DB database = mongo.getDB(userDatabase);
                if (!database.isAuthenticated()) {
                    database.authenticate(mongoServer.getUsername(), mongoServer.getPassword().toCharArray());
                }
                mongoServer.addDatabase(createMongoDatabaseAndItsCollections(database));
            } else {
                List<String> databaseNames = getDatabaseNames(mongo, mongoServer.getUsername(), mongoServer.getPassword());
                for (String databaseName : databaseNames) {
                    DB database = mongo.getDB(databaseName);
                    mongoServer.addDatabase(createMongoDatabaseAndItsCollections(database));
                }
            }

            return mongoServer;
        } catch (Exception ex) {
            LOG.error("Error when collecting Mongo databases", ex);
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private MongoDatabase createMongoDatabaseAndItsCollections(DB database) {
        MongoDatabase mongoDatabase = new MongoDatabase(database.getName());


        Set<String> collectionNames = database.getCollectionNames();
        for (String collectionName : collectionNames) {
            mongoDatabase.addCollection(new MongoCollection(collectionName, database.getName()));
        }
        return mongoDatabase;
    }

    public MongoCollectionResult loadCollectionValues(ServerConfiguration configuration, MongoCollection mongoCollection, MongoQueryOptions mongoQueryOptions) {
        MongoClient mongo = null;
        try {
            mongo = createMongoClient(configuration.getServerName(), configuration.getServerPort());
            DB database = mongo.getDB(mongoCollection.getDatabaseName());

            String username = configuration.getUsername();
            String password = configuration.getPassword();
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                database.authenticate(username, password.toCharArray());
            }

            DBCollection collection = database.getCollection(mongoCollection.getName());

            MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(mongoCollection.getName());
            if (mongoQueryOptions.isAggregate()) {
                return aggregate(mongoQueryOptions, mongoCollectionResult, collection);
            }

            return find(mongoQueryOptions, mongoCollectionResult, collection);

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }

    }

    private MongoClient createMongoClient(String serverName, int serverPort) throws UnknownHostException {
        String textURI = String.format("mongodb://%s:%s", serverName, serverPort);
        MongoClientURI mongoClientURI = new MongoClientURI(textURI);
        return new MongoClient(mongoClientURI);
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
    List<String> getDatabaseNames(MongoClient mongo, String username, String password) {

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
                    throw new ConfigurationException("Invalid credentials");
                }
                res = adminDb.command(cmd, mongo.getOptions());
            }
        }

        res.throwOnError();

        List l = (List) res.get("databases");

        List<String> list = new ArrayList<String>();

        for (Object o : l) {
            list.add(((BasicDBObject) o).getString("name"));
        }
        return list;
    }
}
