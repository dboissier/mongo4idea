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

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.mongodb.*;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.SshTunnelingConfiguration;
import org.codinjutsu.tools.mongo.logic.ssh.SshConnection;
import org.codinjutsu.tools.mongo.model.*;

import java.net.UnknownHostException;
import java.util.*;

public class MongoManager {

    private final static NotificationGroup MONGO_GROUP = NotificationGroup.logOnlyGroup("Mongo");

    private static final String DEFAULT_TUNNEL_LOCAL_HOST = "localhost";
    private static final int DEFAULT_TUNNEL_LOCAL_PORT = 9080;

    private final List<MongoServer> mongoServers = new LinkedList<>();
    private final Project project;

    public static MongoManager getInstance(Project project) {
        return ServiceManager.getService(project, MongoManager.class);
    }

    public MongoManager(Project project) {
        this.project = project;
    }

    public void connect(final ServerConfiguration configuration) {
        Task task = new Task() {
            @Override
            public void run(MongoClient mongoClient) {
                String userDatabase = configuration.getUserDatabase();
                String databaseName = StringUtils.isNotEmpty(userDatabase) ? userDatabase : "test";

                mongoClient.getDatabase(databaseName).listCollectionNames().first();
            }
        };


        executeTask(configuration, task);
    }

    public void cleanUpServers() {
        mongoServers.clear();
    }

    public void registerServer(MongoServer mongoServer) {
        mongoServers.add(mongoServer);
    }

    public List<MongoServer> getServers() {
        return mongoServers;
    }

    public void loadServer(MongoServer mongoServer) {
        mongoServer.setStatus(MongoServer.Status.LOADING);

        List<MongoDatabase> mongoDatabases = loadDatabaseCollections(mongoServer.getConfiguration());

        mongoServer.setDatabases(mongoDatabases);
        mongoServer.setStatus(MongoServer.Status.OK);
    }

    private List<MongoDatabase> loadDatabaseCollections(final ServerConfiguration configuration) {
        final List<MongoDatabase> mongoDatabases = new LinkedList<>();
        TaskWithReturnedObject<List<MongoDatabase>> perform = new TaskWithReturnedObject<List<MongoDatabase>>() {
            @Override
            public List<MongoDatabase> run(MongoClient mongoClient) {
                String userDatabase = configuration.getUserDatabase();

                if (StringUtils.isNotEmpty(userDatabase)) {
                    DB database = mongoClient.getDB(userDatabase);
                    mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
                } else {
                    List<String> databaseNames = mongoClient.getDatabaseNames();
                    Collections.sort(databaseNames);
                    for (String databaseName : databaseNames) {
                        DB database = mongoClient.getDB(databaseName);
                        mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
                    }
                }
                return mongoDatabases;
            }
        };

        return executeTask(configuration, perform);
    }

    public MongoCollectionResult loadCollectionValues(ServerConfiguration configuration, final MongoCollection mongoCollection, final MongoQueryOptions mongoQueryOptions) {
        TaskWithReturnedObject<MongoCollectionResult> task = new TaskWithReturnedObject<MongoCollectionResult>() {
            @Override
            public MongoCollectionResult run(MongoClient mongoClient) {
                String databaseName = mongoCollection.getDatabaseName();

                DB database = mongoClient.getDB(databaseName);
                DBCollection collection = database.getCollection(mongoCollection.getName());

                MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(mongoCollection.getName());
                if (mongoQueryOptions.isAggregate()) {
                    return aggregate(mongoQueryOptions, mongoCollectionResult, collection);
                }

                return find(mongoQueryOptions, mongoCollectionResult, collection);
            }
        };

        return execute(configuration, task);
    }

    public DBObject findMongoDocument(ServerConfiguration configuration, final MongoCollection mongoCollection, final Object _id) {
        TaskWithReturnedObject<DBObject> task = new TaskWithReturnedObject<DBObject>() {
            @Override
            public DBObject run(MongoClient mongoClient) {
                String databaseName = mongoCollection.getDatabaseName();
                DB database = mongoClient.getDB(databaseName);
                DBCollection collection = database.getCollection(mongoCollection.getName());
                return collection.findOne(new BasicDBObject("_id", _id));
            }
        };

        return execute(configuration, task);
    }

    private MongoDatabase createMongoDatabaseAndItsCollections(DB database) {
        MongoDatabase mongoDatabase = new MongoDatabase(database.getName());

        Set<String> collectionNames = database.getCollectionNames();
        for (String collectionName : collectionNames) {
            mongoDatabase.addCollection(new MongoCollection(collectionName, database.getName()));
        }
        return mongoDatabase;
    }

    private <T> T executeTask(ServerConfiguration configuration, TaskWithReturnedObject<T> perform) {
        if (SshTunnelingConfiguration.isEmpty(configuration.getSshTunnelingConfiguration())) {
            return execute(configuration, perform);
        } else {
            try (SshConnection ignored = SshConnection.create(configuration)) {
                return execute(configuration, perform);
            }
        }
    }

    private <T> T execute(ServerConfiguration configuration, TaskWithReturnedObject<T> perform) {
        try (MongoClient mongo = createMongoClient(configuration)) {
            return perform.run(mongo);
        } catch (UnknownHostException | MongoException mongoEx) {
            MONGO_GROUP.createNotification(String.format("Error when connecting on %s", configuration.getLabel()),
                    MessageType.ERROR)
                    .notify(project);
            throw new ConfigurationException(mongoEx);
        }
    }

    public void update(ServerConfiguration configuration, final MongoCollection mongoCollection, final DBObject mongoDocument) {
        Task task = new Task() {
            @Override
            public void run(MongoClient mongoClient) {
                String databaseName = mongoCollection.getDatabaseName();
                DB database = mongoClient.getDB(databaseName);
                DBCollection collection = database.getCollection(mongoCollection.getName());

                collection.save(mongoDocument);
                MONGO_GROUP.createNotification("Document " + mongoDocument.toString() + " saved", MessageType.INFO)
                .notify();
            }
        };

        executeTask(configuration, task);
    }

    public void delete(ServerConfiguration configuration, final MongoCollection mongoCollection, final Object _id) {
        Task task = new Task() {
            @Override
            public void run(MongoClient mongoClient) {
                String databaseName = mongoCollection.getDatabaseName();

                DB database = mongoClient.getDB(databaseName);
                DBCollection collection = database.getCollection(mongoCollection.getName());

                collection.remove(new BasicDBObject("_id", _id));
                MONGO_GROUP.createNotification("Document with _id=" + _id + " removed", MessageType.INFO)
                        .notify(project);
            }
        };

        executeTask(configuration, task);
    }

    public void dropCollection(ServerConfiguration configuration, final MongoCollection mongoCollection) {
        Task task = new Task() {
            @Override
            public void run(MongoClient mongoClient) {
                String databaseName = mongoCollection.getDatabaseName();

                DB database = mongoClient.getDB(databaseName);
                DBCollection collection = database.getCollection(mongoCollection.getName());

                collection.drop();
                MONGO_GROUP.createNotification("Collection " + mongoCollection.getName() + " dropped", MessageType.INFO)
                        .notify(project);
            }
        };
        executeTask(configuration, task);
    }


    public void dropDatabase(ServerConfiguration configuration, final MongoDatabase selectedDatabase) {
        Task task = new Task() {
            @Override
            public void run(MongoClient mongoClient) {
                mongoClient.dropDatabase(selectedDatabase.getName());
                MONGO_GROUP.createNotification("Datatabase " + selectedDatabase.getName() + " dropped", MessageType.INFO)
                        .notify(project);
            }
        };

        executeTask(configuration, task);
    }

    private void executeTask(ServerConfiguration configuration, Task perform) {
        if (SshTunnelingConfiguration.isEmpty(configuration.getSshTunnelingConfiguration())) {
            execute(configuration, perform);
        } else {
            try (SshConnection ignored = SshConnection.create(configuration)) {
                execute(configuration, perform);
            }
        }
    }

    private void execute(ServerConfiguration configuration, Task task) {
        try (MongoClient mongoClient = createMongoClient(configuration)) {
            task.run(mongoClient);
        } catch (UnknownHostException | MongoException ex) {
            throw new ConfigurationException(ex);
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

        List<ServerAddress> serverAddresses = new LinkedList<>();
        if (SshTunnelingConfiguration.isEmpty(configuration.getSshTunnelingConfiguration())) {
            for (String serverUrl : serverUrls) {
                ServerConfiguration.HostAndPort hostAndPort = ServerConfiguration.extractHostAndPort(serverUrl);
                serverAddresses.add(new ServerAddress(hostAndPort.host, hostAndPort.port));
            }
        } else {
            serverAddresses.add(new ServerAddress(DEFAULT_TUNNEL_LOCAL_HOST, DEFAULT_TUNNEL_LOCAL_PORT));
        }

        MongoClientOptions options = MongoClientOptions.builder()
                .sslEnabled(configuration.isSslConnection())
                .readPreference(configuration.getReadPreference())
                .build();

        if (StringUtils.isEmpty(configuration.getUsername())) {
            return new MongoClient(serverAddresses, options);
        } else {
            MongoCredential credential = getMongoCredential(configuration);
            return new MongoClient(serverAddresses, Collections.singletonList(credential), options);
        }
    }


    private MongoCredential getMongoCredential(ServerConfiguration configuration) {
        AuthenticationMechanism authenticationMechanism = configuration.getAuthenticationMechanism();
        if (authenticationMechanism == null) {
            return MongoCredential.createPlainCredential(configuration.getUsername(),
                    configuration.getAuthenticationDatabase(),
                    configuration.getPassword().toCharArray());
        } else {
            if (AuthenticationMechanism.MONGODB_CR.equals(authenticationMechanism)) {
                return MongoCredential.createMongoCRCredential(configuration.getUsername(),
                        configuration.getAuthenticationDatabase(),
                        configuration.getPassword().toCharArray());
            } else if (AuthenticationMechanism.SCRAM_SHA_1.equals(authenticationMechanism)) {
                return MongoCredential.createScramSha1Credential(configuration.getUsername(),
                        configuration.getAuthenticationDatabase(),
                        configuration.getPassword().toCharArray());
            }
        }

        throw new IllegalArgumentException("Unsupported authentication macanism: " + authenticationMechanism);
    }

    private interface Task {

        void run(MongoClient mongoClient);
    }


    private interface TaskWithReturnedObject<T> {

        T run(MongoClient mongoClient);
    }
}
