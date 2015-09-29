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

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class MongoManager {

    private static final Logger LOG = Logger.getLogger(MongoManager.class);
    private final List<MongoServer> mongoServers = new LinkedList<MongoServer>();

    public static MongoManager getInstance(Project project) {
        return ServiceManager.getService(project, MongoManager.class);
    }

    public void connect(ServerConfiguration configuration) {
        MongoClient mongo = null;
        try {
            String userDatabase = configuration.getUserDatabase();
            mongo = createMongoClient(configuration);

            if (StringUtils.isNotEmpty(userDatabase)) {
                mongo.getDatabase(userDatabase);
            } else {
                mongo.getDatabase("test");
            }
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

    List<MongoDatabase> loadDatabaseCollections(ServerConfiguration configuration) {
        MongoClient mongo = null;
        List<MongoDatabase> mongoDatabases = new LinkedList<MongoDatabase>();
        try {
            String userDatabase = configuration.getUserDatabase();

            mongo = createMongoClient(configuration);

            if (StringUtils.isNotEmpty(userDatabase) && configuration.isUserDatabaseAsMySingleDatabase()) {
                DB database = mongo.getDB(userDatabase);
                mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
            } else {
                List<String> databaseNames = mongo.getDatabaseNames();
                Collections.sort(databaseNames);
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

    public void dropDatabase(ServerConfiguration configuration, MongoDatabase selectedDatabase) {
        MongoClient mongo = null;
        try {
            mongo = createMongoClient(configuration);
            mongo.dropDatabase(selectedDatabase.getName());
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
        DBObject projection = mongoQueryOptions.getProjection();
        DBObject sort = mongoQueryOptions.getSort();

        DBCursor cursor;
        if (projection == null) {
            cursor = collection.find(filter);
        } else {
            cursor = collection.find(filter, projection);
        }

        if (sort != null) {
            cursor = cursor.sort(sort);
        }

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

    private MongoClient createMongoClient(ServerConfiguration configuration) throws UnknownHostException {
        List<String> serverUrls = configuration.getServerUrls();
        if (serverUrls.isEmpty()) {
            throw new ConfigurationException("server host is not set");
        }

        MongoClientBuilder builder = MongoClientBuilder.builder()
                .setServerAddresses(serverUrls);

        if (configuration.isSslConnection()) {
            builder.setSSLOptions();
        }

        String username = configuration.getUsername();
        String password = configuration.getPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            builder.setCredential(username, password, configuration.getUserDatabase(), configuration.getAuthentificationMethod());

        }
        return builder.build();
    }

    private static class MongoClientBuilder {


        private final LinkedList<ServerAddress> serverAddresses;
        private final MongoClientOptions.Builder optionsBuilder;
        private MongoCredential credential;

        private MongoClientBuilder() {
            this.serverAddresses = new LinkedList<ServerAddress>();
            this.optionsBuilder = MongoClientOptions.builder();
            this.credential = null;
        }

        public static MongoClientBuilder builder() {
            return new MongoClientBuilder();
        }

        public MongoClientBuilder setServerAddresses(List<String> serverUrls) {
            for (String serverUrl : serverUrls) {
                serverAddresses.add(new ServerAddress(serverUrl));
            }
            return this;
        }

        public MongoClientBuilder setSSLOptions() {
            optionsBuilder.socketFactory(SSLSocketFactory.getDefault());
            return this;
        }

        public MongoClientBuilder setCredential(String username, String password, String database, MongoServer.AuthentificationMethod authenticationMechanism) {
            if (MongoServer.AuthentificationMethod.MONGODB_CR.equals(authenticationMechanism)) {
                credential = MongoCredential.createMongoCRCredential(username, database, password.toCharArray());
            } else {
                credential = MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
            }
            return this;
        }

        public MongoClient build() {
            if (serverAddresses.size() == 1) {
                if (credential != null) {
                    String uri = String.format("mongodb://%s:%s@%s:%s/%s",
                            credential.getUserName(),
                            String.valueOf(credential.getPassword()),
                            serverAddresses.get(0).getHost(),
                            serverAddresses.get(0).getPort(),
                            credential.getSource()
                    );
                    return new MongoClient(new MongoClientURI(uri));
                }
                return new MongoClient(serverAddresses.get(0), optionsBuilder.build());
            } else {
                if (credential != null) {
                    return new MongoClient(serverAddresses, Arrays.asList(credential), optionsBuilder.build());
                }
                return new MongoClient(serverAddresses, optionsBuilder.build());
            }
        }
    }
}
