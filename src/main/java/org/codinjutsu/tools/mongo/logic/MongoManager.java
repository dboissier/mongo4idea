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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.model.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

public class MongoManager {

    public void connect(String serverName, int serverPort, String username, String password) {
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
            DBCursor cursor;
            if (mongoQueryOptions.isNotEmpty()) {
                cursor = collection.find(mongoQueryOptions.getFilter());
            } else {
                cursor = collection.find();
            }
            try {
                while (cursor.hasNext()) {
                    mongoCollectionResult.add(cursor.next());
                }
            } finally {
                cursor.close();
            }
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        }
        return mongoCollectionResult;
    }
}
