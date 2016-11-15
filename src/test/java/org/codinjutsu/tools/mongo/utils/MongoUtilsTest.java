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

package org.codinjutsu.tools.mongo.utils;

import org.bson.Document;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class MongoUtilsTest {

    @Test
    public void stringifyListOfSimpleObjects() throws Exception {
        List<Object> list = new LinkedList<>();
        list.add("foo");
        list.add(123);
        list.add(null);
        list.add(new Document("key", "value"));

        assertEquals("[\"foo\", 123, null, { \"key\" : \"value\" }]", MongoUtils.stringifyList(list));

    }

    @Test
    public void stringifyListOfInnerList() throws Exception {
        List<Object> innerList = new LinkedList<>();
        innerList.add("foo");
        innerList.add(new Document("key", "value1"));
        List<Object> list = new LinkedList<>();
        list.add(innerList);
        list.add(new Document("bar", 12));

        assertEquals("[[\"foo\", { \"key\" : \"value1\" }], { \"bar\" : 12 }]", MongoUtils.stringifyList(list));

    }
}