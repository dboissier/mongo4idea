/*
 * Copyright (c) 2016 David Boissier.
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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.SshTunnelingConfiguration;
import org.codinjutsu.tools.mongo.logic.ssh.SshConnection;
import org.codinjutsu.tools.mongo.model.*;

import java.util.*;

public class MongoManager {

    private static final String DEFAULT_TUNNEL_LOCAL_HOST = "localhost";
    private static final int DEFAULT_TUNNEL_LOCAL_PORT = 9080;

    private final List<MongoServer> mongoServers = new LinkedList<>();

    public static MongoManager getInstance(Project project) {
        return ServiceManager.getService(project, MongoManager.class);
    }

    public void connect(final ServerConfiguration configuration) {
        Task task = mongoClient -> {
            String userDatabase = configuration.getUserDatabase();
            String databaseName = StringUtils.isNotEmpty(userDatabase) ? userDatabase : "test";

            mongoClient.getDatabase(databaseName).listCollectionNames().first();
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

    public List<MongoDatabase> loadDatabases(MongoServer mongoServer, ServerConfiguration configuration) {
        final List<MongoDatabase> mongoDatabases = new LinkedList<>();
        TaskWithReturnedObject<List<MongoDatabase>> perform = mongoClient -> {
            String userDatabase = configuration.getUserDatabase();

            if (StringUtils.isNotEmpty(userDatabase)) {
                com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(userDatabase);
                MongoDatabase mongoDatabase = new MongoDatabase(database.getName(), mongoServer);
                mongoDatabases.add(createMongoDatabaseAndItsCollections(mongoDatabase, database));
            } else {
                MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
                for (String databaseName : databaseNames) {
                    com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(databaseName);
                    MongoDatabase mongoDatabase = new MongoDatabase(database.getName(), mongoServer);
                    mongoDatabases.add(createMongoDatabaseAndItsCollections(mongoDatabase, database));
                }
            }
            return mongoDatabases;
        };

        return executeTask(configuration, perform);
    }

    private MongoDatabase createMongoDatabaseAndItsCollections(MongoDatabase mongoDatabase, com.mongodb.client.MongoDatabase database) {
        MongoIterable<String> collectionNames = database.listCollectionNames();
        for (String collectionName : collectionNames) {
            mongoDatabase.addCollection(new MongoCollection(collectionName, mongoDatabase));
        }
        return mongoDatabase;
    }

    public MongoCollectionResult loadCollectionValues(ServerConfiguration configuration, final MongoCollection mongoCollection, final MongoQueryOptions mongoQueryOptions) {
        TaskWithReturnedObject<MongoCollectionResult> task = mongoClient -> {
            MongoDatabase mongoDatabase = mongoCollection.getParentDatabase();

            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(mongoDatabase.getName());
            com.mongodb.client.MongoCollection<Document> collection = database.getCollection(mongoCollection.getName());

            MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(mongoCollection.getName());
            if (mongoQueryOptions.isAggregate()) {
                return aggregate(mongoQueryOptions, mongoCollectionResult, collection);
            }

            return find(mongoQueryOptions, mongoCollectionResult, collection);
        };

        return executeTask(configuration, task);
    }

    public Document findMongoDocument(ServerConfiguration configuration, final MongoCollection mongoCollection, final Object _id) {
        TaskWithReturnedObject<Document> task = mongoClient -> {
            MongoDatabase mongoDatabase = mongoCollection.getParentDatabase();
            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(mongoDatabase.getName());
            com.mongodb.client.MongoCollection<Document> collection = database.getCollection(mongoCollection.getName());
            FindIterable<Document> foundDocuments = collection.find(new BasicDBObject("_id", _id));

            return foundDocuments.first();
        };

        return executeTask(configuration, task);
    }

    public void update(ServerConfiguration configuration, final MongoCollection mongoCollection, final Document mongoDocument) {
        Task task = mongoClient -> {
            MongoDatabase mongoDatabase = mongoCollection.getParentDatabase();
            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(mongoDatabase.getName());
            com.mongodb.client.MongoCollection<Document> collection = database.getCollection(mongoCollection.getName());


            if (!mongoDocument.containsKey("_id")) {
                collection.insertOne(mongoDocument);
            } else {
                collection.findOneAndReplace(
                        new Document("_id", mongoDocument.get("_id")),
                        mongoDocument,
                        new FindOneAndReplaceOptions().upsert(true));
            }
        };

        executeTask(configuration, task);
    }

    public void delete(ServerConfiguration configuration, final MongoCollection mongoCollection, final Object _id) {
        Task task = mongoClient -> {
            MongoDatabase mongoDatabase = mongoCollection.getParentDatabase();

            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(mongoDatabase.getName());
            com.mongodb.client.MongoCollection<Document> collection = database.getCollection(mongoCollection.getName());

            collection.deleteOne(new Document("_id", _id));
        };

        executeTask(configuration, task);
    }

    public void removeCollection(ServerConfiguration configuration, final MongoCollection mongoCollection) {
        Task task = mongoClient -> {
            MongoDatabase mongoDatabase = mongoCollection.getParentDatabase();

            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(mongoDatabase.getName());
            com.mongodb.client.MongoCollection<Document> collection = database.getCollection(mongoCollection.getName());

            collection.drop();
        };
        executeTask(configuration, task);
    }

    public void removeDatabase(ServerConfiguration configuration, final MongoDatabase selectedDatabase) {
        Task task = mongoClient -> mongoClient.dropDatabase(selectedDatabase.getName());

        executeTask(configuration, task);
    }

    public List<StatInfoEntry> getCollStats(ServerConfiguration configuration, final MongoCollection mongoCollection) {
        TaskWithReturnedObject<List<StatInfoEntry>> task = mongoClient -> {
            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(mongoCollection.getParentDatabase().getName());

            return adaptToCollectionInfos(database.runCommand(new Document("collStats", mongoCollection.getName())));
        };

        return executeTask(configuration, task);
    }

    public List<StatInfoEntry> getDbStats(ServerConfiguration configuration, final MongoDatabase mongoDatabase) {
        TaskWithReturnedObject<List<StatInfoEntry>> task = mongoClient -> {
            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(mongoDatabase.getName());

            return adaptToDatabaseInfos(database.runCommand(new Document("dbStats", 1)));
        };

        return executeTask(configuration, task);
    }

    private List<StatInfoEntry> adaptToCollectionInfos(Document collectionStatsDocument) {
        List<StatInfoEntry> collectionInfoEntries = new ArrayList<>();

        for (CollectionStatInfoEnum statInfo : CollectionStatInfoEnum.values()) {
            collectionInfoEntries.add(statInfo.getDataBuilder().build(
                    statInfo.name(),
                    statInfo.getDataExtractor().extract(statInfo.name(), collectionStatsDocument)));

            if (CollectionStatInfoEnum.indexSizes.equals(statInfo)) {
                collectionInfoEntries.addAll(createIndexSizeEntryList(
                        (Document) statInfo.getDataExtractor().extract(statInfo.name(), collectionStatsDocument)));
            }
        }

        return collectionInfoEntries;
    }

    private List<StatInfoEntry> createIndexSizeEntryList(Document indexSizesDocument) {
        LinkedList<StatInfoEntry> list = new LinkedList<>();
        for (Map.Entry<String, Object> keyValueEntry : indexSizesDocument.entrySet()) {
            list.add(new StatInfoEntry.ByteSizeStatInfoEntry(keyValueEntry.getKey(), new Long(keyValueEntry.getValue().toString()))); //TODO need refactor
        }
        return list;
    }

    private List<StatInfoEntry> adaptToDatabaseInfos(Document databaseStatsDocument) {
        List<StatInfoEntry> collectionInfoEntries = new ArrayList<>();

        for (DatabaseStatInfoEnum statInfo : DatabaseStatInfoEnum.values()) {
            collectionInfoEntries.add(statInfo.getDataBuilder().build(
                    statInfo.name(),
                    statInfo.getDataExtractor().extract(statInfo.name(), databaseStatsDocument)));
        }

        return collectionInfoEntries;
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
        } catch (MongoException mongoEx) {
            throw new ConfigurationException(mongoEx);
        }
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
        } catch (MongoException ex) {
            throw new ConfigurationException(ex);
        }
    }

    private MongoCollectionResult aggregate(MongoQueryOptions mongoQueryOptions, MongoCollectionResult mongoCollectionResult, com.mongodb.client.MongoCollection<Document> collection) {
        AggregateIterable aggregate = collection.aggregate(mongoQueryOptions.getOperations());
        int index = 0;
        Iterator iterator = aggregate.iterator();
        while (iterator.hasNext() && index < mongoQueryOptions.getResultLimit()) {
            mongoCollectionResult.add((Document) iterator.next());
        }
        return mongoCollectionResult;
    }

    private MongoCollectionResult find(MongoQueryOptions mongoQueryOptions, final MongoCollectionResult mongoCollectionResult, com.mongodb.client.MongoCollection<Document> collection) {
        Document filter = mongoQueryOptions.getFilter();
        Document projection = mongoQueryOptions.getProjection();
        Document sort = mongoQueryOptions.getSort();

        FindIterable<Document> cursor = collection.find(filter);
        if (!MongoQueryOptions.EMPTY_DOCUMENT.equals(projection)) {
            cursor.projection(projection);
        }

        if (!MongoQueryOptions.EMPTY_DOCUMENT.equals(sort)) {
            cursor.sort(sort);
        }

        int resultLimit = mongoQueryOptions.getResultLimit();
        if (resultLimit > 0) {
            cursor.limit(resultLimit);
        }

        cursor.forEach((Block<Document>) mongoCollectionResult::add);

        return mongoCollectionResult;
    }

    private MongoClient createMongoClient(ServerConfiguration configuration) {
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
                .codecRegistry(MongoClient.getDefaultCodecRegistry())
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
                    getAuthenticationDatabase(configuration),
                    configuration.getPassword().toCharArray());
        } else {
            if (AuthenticationMechanism.MONGODB_CR.equals(authenticationMechanism)) {
                return MongoCredential.createMongoCRCredential(configuration.getUsername(),
                        getAuthenticationDatabase(configuration),
                        configuration.getPassword().toCharArray());
            } else if (AuthenticationMechanism.SCRAM_SHA_1.equals(authenticationMechanism)) {
                return MongoCredential.createScramSha1Credential(configuration.getUsername(),
                        getAuthenticationDatabase(configuration),
                        configuration.getPassword().toCharArray());
            }
        }

        throw new IllegalArgumentException("Unsupported authentication macanism: " + authenticationMechanism);
    }

    private static String getAuthenticationDatabase(ServerConfiguration configuration) {
        String authenticationDatabase = configuration.getAuthenticationDatabase();
        return StringUtils.isEmpty(authenticationDatabase) ? "admin" : authenticationDatabase;
    }

    private interface Task {

        void run(MongoClient mongoClient);
    }


    private interface TaskWithReturnedObject<T> {

        T run(MongoClient mongoClient);
    }
}
