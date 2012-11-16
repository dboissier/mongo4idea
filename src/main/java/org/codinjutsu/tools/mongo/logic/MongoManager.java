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
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.model.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

public class MongoManager {

    public String connect(String serverName, int serverPort, String username, String password) {
        try {
            Mongo mongo = new Mongo(serverName, serverPort);
            List<String> databaseNames = mongo.getDatabaseNames();
            if (databaseNames.isEmpty()) {
                throw new ConfigurationException("No databases were found");
            }


            DB databaseForTesting = mongo.getDB(databaseNames.get(0));
            if (StringUtils.isNotBlank(username)) {
                databaseForTesting.authenticate(username, password.toCharArray());
            }
            return (String) databaseForTesting.eval("db.version();");

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        }
    }

    public MongoServer loadDatabaseCollections(MongoConfiguration configuration) {
        try {
            Mongo mongo = new Mongo(configuration.getServerName(), configuration.getServerPort());

            MongoServer mongoServer = new MongoServer(configuration.getServerName(), configuration.getServerPort());

            List<String> databaseNames = mongo.getDatabaseNames();
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
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        }
    }

    public MongoCollectionResult loadCollectionValues(MongoConfiguration configuration, MongoCollection mongoCollection) {
        return loadCollectionValues(configuration, mongoCollection, new MongoQueryOptions());
    }

    public MongoCollectionResult loadCollectionValues(MongoConfiguration configuration, MongoCollection mongoCollection, MongoQueryOptions mongoQueryOptions) {
        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(mongoCollection.getName());
        try {
            Mongo mongo = new Mongo(configuration.getServerName(), configuration.getServerPort());
            DB database = mongo.getDB(mongoCollection.getDatabaseName());
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
        for (DBObject dbObject : aggregate.results()) {
            mongoCollectionResult.add(dbObject);
        }
        return mongoCollectionResult;
    }

    private MongoCollectionResult find(MongoQueryOptions mongoQueryOptions, MongoCollectionResult mongoCollectionResult, DBCollection collection) {
        DBObject filter = mongoQueryOptions.getFilter();
        long count = collection.count(filter);
        long maxResults;
        if (count > 200) {
            maxResults = 200;
        } else {
            maxResults = count;
        }
        DBCursor cursor = collection.find(filter);
        try {
            int index = 0;
            while (cursor.hasNext() && index < maxResults) {
                mongoCollectionResult.add(cursor.next());
                index++;
            }
        } finally {
            cursor.close();
        }
        return mongoCollectionResult;
    }

}
