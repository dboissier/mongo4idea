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

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MongoManagerTest {

    private MongoManager mongoManager;
    private ServerConfiguration serverConfiguration;
    private com.mongodb.client.MongoCollection<Document> peopleCollection;

    @Before
    public void setUp() {
        MongoClient mongo = new MongoClient("localhost:27017");
        MongoDatabase database = mongo.getDatabase("test");

        peopleCollection = database.getCollection("people");
        peopleCollection.deleteMany(new Document());
        peopleCollection.insertMany(Arrays.asList(
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54e58"))
                        .append("name", "Paul")
                        .append("position", "developer")
                        .append("age", 25),
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54e59"))
                        .append("name", "Melissa")
                        .append("position", "developer")
                        .append("age", 26),
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54e60"))
                        .append("name", "Roger")
                        .append("position", "manager")
                        .append("age", 27),
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54f58"))
                        .append("name", "Shirley")
                        .append("comment", "director")
                        .append("age", 28)
        ));

        mongoManager = new MongoManager();
        serverConfiguration = new ServerConfiguration();
        serverConfiguration.setServerUrls(singletonList("localhost:27017"));
    }

    @Test
    public void loadServer() {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setLabel("Server for testing");
        configuration.setUserDatabase("test");
        MongoServer mongoServer = new MongoServer(configuration);
        mongoServer.setDatabases(mongoManager.loadDatabases(mongoServer, mongoServer.getConfiguration()));

        List<org.codinjutsu.tools.mongo.model.MongoDatabase> databases = mongoServer.getDatabases();

        assertThat(databases.isEmpty()).isFalse();
        org.codinjutsu.tools.mongo.model.MongoDatabase actualDatabase = databases.get(0);
        assertThat(actualDatabase.getName()).isEqualTo("test");

        assertThat(actualDatabase.getParentServer()).isEqualTo(mongoServer);
        assertThat(actualDatabase.getCollections()).hasSize(1);

        MongoCollection actualMongoCollection = actualDatabase.getCollections().iterator().next();
        assertThat(actualMongoCollection.getParentDatabase()).isEqualTo(actualDatabase);
        assertThat(actualMongoCollection.getName()).isEqualTo("people");
    }

    @Test
    public void loadCollectionsWithEmptyFilterAndLimitToThreeDocuments() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setResultLimit(3);

        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        createMongoCollectionForTest(), mongoQueryOptions);

        assertThat(mongoCollectionResult.getDocuments()).containsExactly(
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54e58"))
                        .append("name", "Paul")
                        .append("position", "developer")
                        .append("age", 25),
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54e59"))
                        .append("name", "Melissa")
                        .append("position", "developer")
                        .append("age", 26),
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54e60"))
                        .append("name", "Roger")
                        .append("position", "manager")
                        .append("age", 27)
        );
    }

    @Test
    public void loadCollectionsWithFilterAndProjection() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter(
                new Document("position", "developer").toJson());
        mongoQueryOptions.setProjection(
                new Document("name", 1)
                        .append("_id", 0).toJson());
        mongoQueryOptions.setResultLimit(3);

        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        createMongoCollectionForTest(), mongoQueryOptions);

        assertThat(mongoCollectionResult.getDocuments())
                .containsExactly(
                        new Document("name", "Paul"),
                        new Document("name", "Melissa"));
    }

    @Test
    public void loadCollectionsWithFilterAndProjectionAndSort() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{'position': 'developer'}");
        mongoQueryOptions.setProjection("{'name': 1, 'age': 1, '_id': 0}");
        mongoQueryOptions.setSort("{'age': -1}");

        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        createMongoCollectionForTest(),
                        mongoQueryOptions);

        assertThat(mongoCollectionResult.getDocuments())
                .containsExactly(
                        new Document("name", "Melissa")
                                .append("age", 26),
                        new Document("name", "Paul")
                                .append("age", 25));
    }

    @Test
    public void loadCollectionsWithAggregateOperators() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setOperations("[{'$match': {'position': 'developer'}}, {'$project': {'name': 1, 'age': 1}}, {'$group': {'_id': '$name', 'total': {'$sum': '$age'}}}]");
        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        createMongoCollectionForTest(), mongoQueryOptions);

        assertThat(mongoCollectionResult.getDocuments()).containsExactly(
                new Document("_id", "Melissa")
                        .append("total", 26),
                new Document("_id", "Paul")
                        .append("total", 25));
    }

    @Test
    public void updateMongoDocument() {
        Document documentToUpdate = peopleCollection.find(new Document("name", "Paul")).first();

        documentToUpdate.put("surname", "Paulo les Gaz");
        mongoManager.update(serverConfiguration, createMongoCollectionForTest(), documentToUpdate);

        FindIterable<Document> iterable = peopleCollection.find().projection(new Document("_id", 0));
        assertThat(iterable).contains(
                new Document()
                        .append("name", "Paul")
                        .append("position", "developer")
                        .append("surname", "Paulo les Gaz")
                        .append("age", 25),
                new Document()
                        .append("name", "Melissa")
                        .append("position", "developer")
                        .append("age", 26),
                new Document()
                        .append("name", "Roger")
                        .append("position", "manager")
                        .append("age", 27),
                new Document()
                        .append("name", "Shirley")
                        .append("comment", "director")
                        .append("age", 28)
        );
    }

    @Test
    public void deleteMongoDocument() {
        MongoCollection mongoCollection = createMongoCollectionForTest();
        Document documentToDelete = peopleCollection.find(new Document("name", "Roger")).first();
        mongoManager.delete(serverConfiguration, mongoCollection, documentToDelete.get("_id"));

        FindIterable<Document> iterable = peopleCollection.find().projection(new Document("_id", 0));
        assertThat(iterable).containsExactly(
                new Document()
                        .append("name", "Paul")
                        .append("position", "developer")
                        .append("age", 25),
                new Document("name", "Melissa")
                        .append("position", "developer")
                        .append("age", 26),
                new Document("name", "Shirley")
                        .append("comment", "director")
                        .append("age", 28));
    }

    @Test
    public void findMongoDocument() {

        Document actualDocument = mongoManager.findMongoDocument(
                ServerConfiguration.byDefault(),
                createMongoCollectionForTest(),
                new ObjectId("582ecee28feed271b9f54e59"));
        assertThat(actualDocument).isEqualTo(
                new Document()
                        .append("_id", new ObjectId("582ecee28feed271b9f54e59"))
                        .append("name", "Melissa")
                        .append("position", "developer")
                        .append("age", 26)
        );
    }

    @Test
    public void removeCollection() {
        mongoManager.removeCollection(ServerConfiguration.byDefault(),
                createMongoCollectionForTest());

        MongoDatabase testDatabase = new MongoClient("localhost:27017").getDatabase("test");
        ArrayList<String> collections = testDatabase
                .listCollectionNames()
                .into(new ArrayList<>());

        assertThat(collections.contains("people")).isFalse();
    }

    @Test
    public void getCollectionStats() {
        List<StatInfoEntry> stats = mongoManager.getCollStats(serverConfiguration, createMongoCollectionForTest());

        HashSet<String> expectedStatNames = stats.stream().map(StatInfoEntry::getKey)
                .collect(Collectors.toCollection(HashSet::new));
        assertThat(expectedStatNames).containsExactlyInAnyOrder(
                "size",
                "count",
                "avgObjSize",
                "storageSize",
                "capped",
                "nindexes",
                "totalIndexSize",
                "indexSizes",
                "_id_"
        );
    }

    @Test
    public void getDatabaseStats() {
        List<StatInfoEntry> stats = mongoManager.getDbStats(serverConfiguration, createMongoDatabaseForTest());

        HashSet<String> expectedStatNames = stats.stream().map(StatInfoEntry::getKey)
                .collect(Collectors.toCollection(HashSet::new));

        assertThat(expectedStatNames).contains(
                "collections",
                "views",
                "objects",
                "avgObjSize",
                "dataSize",
                "storageSize",
                "numExtents",
                "indexes",
                "indexSize",
                "fsUsedSize",
                "fsTotalSize"
        );
    }

    @NotNull
    private MongoCollection createMongoCollectionForTest() {
        return new MongoCollection("people", createMongoDatabaseForTest());
    }

    @NotNull
    private org.codinjutsu.tools.mongo.model.MongoDatabase createMongoDatabaseForTest() {
        return new org.codinjutsu.tools.mongo.model.MongoDatabase("test",
                new MongoServer(serverConfiguration));
    }
}

