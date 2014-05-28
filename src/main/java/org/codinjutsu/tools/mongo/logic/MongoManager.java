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
import org.bson.types.ObjectId;
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

    public void connect(ServerConfiguration configuration) {
        connect(configuration.getServerUrls(), configuration.getUsername(), configuration.getPassword(), configuration.getUserDatabase());
    }

    public void connect(List<String> serverUrls, String username, String password, String userDatabase) {

        MongoClient mongo = null;
        try {
            mongo = createMongoClient(serverUrls, userDatabase);

            DB databaseForTesting;
            if (StringUtils.isNotEmpty(userDatabase)) {
                databaseForTesting = mongo.getDB(userDatabase);
            } else {
                databaseForTesting = mongo.getDB("test");
            }

            databaseForTesting.getLastError();

            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                databaseForTesting.authenticate(username, password.toCharArray());
            }

        } catch (IOException ex) {
            throw new ConfigurationException(ex);
        } catch (MongoException ex) {
            LOG.error("Error when accessing Mongo server", ex);
            throw new ConfigurationException(ex.getMessage());
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public MongoServer loadDatabaseCollections(MongoServer mongoServer) {
        MongoClient mongo = null;
        try {
            String userDatabase = mongoServer.getConfiguration().getUserDatabase();

            mongo = createMongoClient(mongoServer.getServerUrls(), userDatabase);

            String username = mongoServer.getUsername();
            String password = mongoServer.getPassword();
            if (StringUtils.isNotEmpty(userDatabase)) {
                DB database = mongo.getDB(userDatabase);
                if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && !database.isAuthenticated()) {
                    database.authenticate(username, password.toCharArray());
                }
                mongoServer.addDatabase(createMongoDatabaseAndItsCollections(database));
            } else {
                List<String> databaseNames = getDatabaseNames(mongo, username, password);
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

    public void update(ServerConfiguration configuration, MongoCollection mongoCollection, DBObject mongoDocument) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration.getServerUrls(), databaseName);

            DB database = mongo.getDB(databaseName);

            String username = configuration.getUsername();
            String password = configuration.getPassword();
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                database.authenticate(username, password.toCharArray());
            }

            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.save(mongoDocument);
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void delete(ServerConfiguration configuration, MongoCollection mongoCollection, Object _id) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration.getServerUrls(), databaseName);

            DB database = mongo.getDB(databaseName);

            String username = configuration.getUsername();
            String password = configuration.getPassword();
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                database.authenticate(username, password.toCharArray());
            }

            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.remove(new BasicDBObject("_id", _id));
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void dropCollection(ServerConfiguration configuration, MongoCollection mongoCollection) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration.getServerUrls(), databaseName);

            DB database = mongo.getDB(databaseName);

            String username = configuration.getUsername();
            String password = configuration.getPassword();
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                database.authenticate(username, password.toCharArray());
            }

            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.drop();
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public MongoCollectionResult loadCollectionValues(ServerConfiguration configuration, MongoCollection mongoCollection, MongoQueryOptions mongoQueryOptions) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration.getServerUrls(), databaseName);

            DB database = mongo.getDB(databaseName);

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

    public DBObject findMongoDocument(ServerConfiguration configuration, MongoCollection mongoCollection, Object _id) {
        MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration.getServerUrls(), databaseName);

            DB database = mongo.getDB(databaseName);

            String username = configuration.getUsername();
            String password = configuration.getPassword();
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                database.authenticate(username, password.toCharArray());
            }

            DBCollection collection = database.getCollection(mongoCollection.getName());
            return collection.findOne(new BasicDBObject("_id", _id));

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private MongoClient createMongoClient(List<String> serverUrls, String userDatabase) throws UnknownHostException {
        String textURI;

        if (serverUrls.isEmpty()) {
            throw new ConfigurationException("server host is not set");
        }

        if (serverUrls.size() == 1) {
            String serverUrl = serverUrls.get(0);
            if (StringUtils.isEmpty(userDatabase)) {
                textURI = String.format("mongodb://%s/", serverUrl);
            } else {
                textURI = String.format("mongodb://%s/%s", serverUrl, userDatabase);
            }
            MongoClientURI mongoClientURI = new MongoClientURI(textURI);
            return new MongoClient(mongoClientURI);
        } else {
            List<ServerAddress> serverAddresses = new LinkedList<ServerAddress>();
            for (String serverUrl : serverUrls) {
                String[] host_port = serverUrl.split(":");
                serverAddresses.add(new ServerAddress(host_port[0], Integer.valueOf(host_port[1])));
            }
            return new MongoClient(serverAddresses);
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
