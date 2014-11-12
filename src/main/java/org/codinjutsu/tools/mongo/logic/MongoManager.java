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
    private List<MongoServer> mongoServers = new LinkedList<MongoServer>();

    public static MongoManager getInstance(Project project) {
        return ServiceManager.getService(project, MongoManager.class);
    }

    public void connect(ServerConfiguration configuration) {

        MongoClient mongo = null;
        try {
            String userDatabase = configuration.getUserDatabase();
            mongo = createMongoClient(configuration);

            DB databaseForTesting;
            if (StringUtils.isNotEmpty(userDatabase)) {
                databaseForTesting = mongo.getDB(userDatabase);
            } else {
                databaseForTesting = mongo.getDB("test");
            }

            databaseForTesting.getLastError();

        } catch (IOException ex) {
            throw new MongoConnectionException(ex);
        } catch (MongoException ex) {
            LOG.error("Error when accessing Mongo server", ex);
            throw new MongoConnectionException(ex.getMessage());
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public List<MongoServer> loadServers(List<ServerConfiguration> serverConfigurations, boolean loadOnStartup) {
        if (loadOnStartup) {
            mongoServers.clear();
        }

        if (!mongoServers.isEmpty()) {
            return mongoServers;
        }

        for (ServerConfiguration serverConfiguration : serverConfigurations) {
            MongoServer mongoServer = new MongoServer(serverConfiguration);
            mongoServers.add(mongoServer);

            if (loadOnStartup && !mongoServer.getConfiguration().isConnectOnIdeStartup()) {
                continue;
            }
            loadServer(mongoServer);

        }
        return mongoServers;
    }

    public void loadServer(MongoServer mongoServer) {
        try {
            List<MongoDatabase> mongoDatabases = loadDatabaseCollections(mongoServer.getConfiguration());
            mongoServer.setDatabases(mongoDatabases);
            mongoServer.setStatus(MongoServer.Status.OK);
        } catch (ConfigurationException e) {
            mongoServer.setStatus(MongoServer.Status.ERROR);
        }
    }

    public List<MongoDatabase> loadDatabaseCollections(ServerConfiguration configuration) {
        MongoClient mongo = null;
        List<MongoDatabase> mongoDatabases = new LinkedList<MongoDatabase>();
        try {
            String userDatabase = configuration.getUserDatabase();

            mongo = createMongoClient(configuration);

            if (StringUtils.isNotEmpty(userDatabase)) {
                DB database = mongo.getDB(userDatabase);
                mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
            } else {
                List<String> databaseNames = getDatabaseNames(mongo);
                for (String databaseName : databaseNames) {
                    DB database = mongo.getDB(databaseName);
                    mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
                }
            }

            return mongoDatabases;
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
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
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
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
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
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
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
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
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
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
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

    private MongoClient createMongoClient(ServerConfiguration configuration) throws UnknownHostException {
        List<String> serverUrls = configuration.getServerUrls();
        String username = configuration.getUsername();
        String password = configuration.getPassword();
        String userDatabase = configuration.getUserDatabase();
        if (serverUrls.isEmpty()) {
            throw new ConfigurationException("server host is not set");
        }

        if (serverUrls.size() == 1) {

            String serverUrl = serverUrls.get(0);
            MongoClientURI mongoClientURI = new MongoClientURI(buildMongoURI(serverUrl, username, password, userDatabase));
            return new MongoClient(mongoClientURI);
        } else {
            List<ServerAddress> serverAddresses = new LinkedList<ServerAddress>();
            for (String serverUrl : serverUrls) {
                String[] host_port = serverUrl.split(":");
                serverAddresses.add(new ServerAddress(host_port[0], Integer.valueOf(host_port[1])));
            }
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                MongoCredential credential = MongoCredential.createMongoCRCredential(username, userDatabase, password.toCharArray());
                return new MongoClient(serverAddresses, Arrays.asList(credential));
            }
            return new MongoClient(serverAddresses);
        }
    }

    private String buildMongoURI(String serverUrl, String username, String password, String userDatabase) {
        StringBuilder uri = new StringBuilder("mongodb://");
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            uri.append(String.format("%s:%s@", username, password));
        }
        uri.append(serverUrl).append("/");
        if (StringUtils.isNotEmpty(userDatabase)) {
            uri.append(userDatabase);
        }
        return uri.toString();
    }

    private MongoCollectionResult aggregate(MongoQueryOptions mongoQueryOptions, MongoCollectionResult mongoCollectionResult, DBCollection collection) {
        AggregationOutput aggregate = collection.aggregate(mongoQueryOptions.getOperations());
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
    List<String> getDatabaseNames(MongoClient mongo) {

        BasicDBObject cmd = new BasicDBObject();
        cmd.put("listDatabases", 1);

        DB adminDb = mongo.getDB("admin");

        CommandResult res = adminDb.command(cmd, ReadPreference.primaryPreferred());
        try {
            res.throwOnError();
        } catch (MongoException e) {
            throw new ConfigurationException("Invalid credentials");
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
