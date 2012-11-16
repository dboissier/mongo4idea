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
import com.mongodb.util.JSON;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.model.MongoAggregateOperator;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class MongoManagerTest {

    private DBCollection dummyCollection;
    private MongoManager mongoManager;
    private MongoConfiguration mongoConfiguration;


    @Test
    public void loadCollectionsWithEmptyFilter() throws Exception {
        MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(mongoConfiguration, new MongoCollection("dummyCollection", "test"));
        Assert.assertNotNull(mongoCollectionResult);
        Assert.assertEquals(5, mongoCollectionResult.getMongoObjects().size());
    }

    @Test
    public void loadCollectionsWithMatchOperator() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.addQuery(MongoAggregateOperator.MATCH, "{ 'price': 15}");
        mongoQueryOptions.addQuery(MongoAggregateOperator.PROJECT, "{ 'label': 1, 'price': 1}");
        mongoQueryOptions.addQuery(MongoAggregateOperator.GROUP, "{ '_id': '$label', 'total': {'$sum': '$price'}}");
        MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(mongoConfiguration, new MongoCollection("dummyCollection", "test"), mongoQueryOptions);
        Assert.assertNotNull(mongoCollectionResult);
        Assert.assertEquals(2, mongoCollectionResult.getMongoObjects().size());
        System.out.println("mongoCollectionResult.getMongoObjects() = " + mongoCollectionResult.getMongoObjects());
    }

    @Before
    public void setUp() throws Exception {
        Mongo mongo = new Mongo();
        DB db = mongo.getDB("test");
        dummyCollection = db.getCollection("dummyCollection");

        clearCollection(dummyCollection);
        fillCollectionWithJsonData(dummyCollection, IOUtils.toString(getClass().getResourceAsStream("dummyCollection.json")));

        mongoManager = new MongoManager();
        mongoConfiguration = new MongoConfiguration();
    }

    private static void fillCollectionWithJsonData(DBCollection collection, String jsonResource) throws IOException {
        Object jsonParsed = JSON.parse(jsonResource);
        if (jsonParsed instanceof BasicDBList) {
            BasicDBList jsonObject = (BasicDBList) jsonParsed;
            for (Object o : jsonObject) {
                DBObject dbObject = (DBObject) o;
                collection.save(dbObject);
            }
        } else {
            collection.save((DBObject) jsonParsed);
        }
    }

    private static void clearCollection(DBCollection collection) {
        DBCursor cursor = collection.find();
        while (cursor.hasNext()) {
            collection.remove(cursor.next());
        }
    }

}

