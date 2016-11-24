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
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MongoManagerTest {

    private MongoManager mongoManager;
    private ServerConfiguration serverConfiguration;
    private com.mongodb.client.MongoCollection<Document> peopleCollection;

    @Before
    public void setUp() throws Exception {
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
    public void loadServer() throws Exception {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setUserDatabase("test");
        MongoServer mongoServer = new MongoServer(configuration);
        mongoManager.loadServer(mongoServer);

        org.codinjutsu.tools.mongo.model.MongoDatabase expectedDatabase =
                new org.codinjutsu.tools.mongo.model.MongoDatabase("test");
        expectedDatabase.addCollection(new MongoCollection("people", "test"));
        expectedDatabase.addCollection(new MongoCollection("system.indexes", "test"));

        assertThat(mongoServer.getDatabases()).containsExactly(expectedDatabase);
    }

    @Test
    public void loadCollectionsWithEmptyFilterAndLimitToThreeDocuments() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setResultLimit(3);

        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        new MongoCollection("people", "test"), mongoQueryOptions);

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
    public void loadCollectionsWithFilterAndProjection() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter(
                new Document("position", "developer").toJson());
        mongoQueryOptions.setProjection(
                new Document("name", 1)
                        .append("_id", 0).toJson());
        mongoQueryOptions.setResultLimit(3);

        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        new MongoCollection("people", "test"), mongoQueryOptions);

        assertThat(mongoCollectionResult.getDocuments())
                .containsExactly(
                        new Document("name", "Paul"),
                        new Document("name", "Melissa"));
    }

    @Test
    public void loadCollectionsWithFilterAndProjectionAndSort() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{'position': 'developer'}");
        mongoQueryOptions.setProjection("{'name': 1, 'age': 1, '_id': 0}");
        mongoQueryOptions.setSort("{'age': -1}");

        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        new MongoCollection("people", "test"), mongoQueryOptions);

        assertThat(mongoCollectionResult.getDocuments())
                .containsExactly(
                        new Document("name", "Melissa")
                                .append("age", 26),
                        new Document("name", "Paul")
                                .append("age", 25));
    }


    @Test
    public void loadCollectionsWithAggregateOperators() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setOperations("[{'$match': {'position': 'developer'}}, {'$project': {'name': 1, 'age': 1}}, {'$group': {'_id': '$name', 'total': {'$sum': '$age'}}}]");
        MongoCollectionResult mongoCollectionResult =
                mongoManager.loadCollectionValues(serverConfiguration,
                        new MongoCollection("people", "test"), mongoQueryOptions);

        assertThat(mongoCollectionResult.getDocuments()).containsExactly(
                new Document("_id", "Melissa")
                        .append("total", 26),
                new Document("_id", "Paul")
                        .append("total", 25));
    }


    @Test
    public void updateMongoDocument() throws Exception {
        Document documentToUpdate = peopleCollection.find(new Document("name", "Paul")).first();

        documentToUpdate.put("surname", "Paulo les Gaz");
        mongoManager.update(serverConfiguration, new MongoCollection("people", "test"), documentToUpdate);

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
    public void deleteMongoDocument() throws Exception {
        MongoCollection mongoCollection = new MongoCollection("people", "test");
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
    public void findMongoDocument() throws Exception {

        Document actualDocument = mongoManager.findMongoDocument(
                ServerConfiguration.byDefault(),
                new MongoCollection("people", "test"),
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
    public void dropCollection() throws Exception {
        mongoManager.dropCollection(ServerConfiguration.byDefault(),
                new MongoCollection("people", "test"));

        MongoDatabase testDatabase = new MongoClient("localhost:27017").getDatabase("test");
        assertThat(testDatabase.listCollectionNames()).containsExactly("system.indexes");
    }
}

