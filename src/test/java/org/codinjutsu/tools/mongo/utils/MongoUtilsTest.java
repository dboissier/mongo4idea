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

package org.codinjutsu.tools.mongo.utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.codinjutsu.tools.mongo.view.model.JsonDataType;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class MongoUtilsTest {
    @Test
    public void testParseValue() throws Exception {
        assertNull(MongoUtils.parseValue(JsonDataType.NULL, null));

        assertEquals("pouet", MongoUtils.parseValue(JsonDataType.STRING, "pouet"));
        assertEquals(Boolean.TRUE, MongoUtils.parseValue(JsonDataType.BOOLEAN, "true"));
        assertEquals(Boolean.FALSE, MongoUtils.parseValue(JsonDataType.BOOLEAN, "truie"));
        assertEquals(Boolean.FALSE, MongoUtils.parseValue(JsonDataType.BOOLEAN, "false"));

        assertEquals(1.1d, MongoUtils.parseValue(JsonDataType.NUMBER, "1.1"));
        assertEquals(1, MongoUtils.parseValue(JsonDataType.NUMBER, "1"));


        assertEquals(new BasicDBObject("key", "val"), MongoUtils.parseValue(JsonDataType.OBJECT, "{ 'key': 'val'}"));

        BasicDBList dbList = new BasicDBList();
        dbList.add("pouet");
        dbList.add(1.0d);
        dbList.add(Boolean.TRUE);

        assertEquals(dbList, MongoUtils.parseValue(JsonDataType.ARRAY, "[ 'pouet', 1.0, true ]"));
    }
}
