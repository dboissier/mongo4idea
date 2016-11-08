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

package org.codinjutsu.tools.mongo.view.model;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonTreeUtilsTest {

    @Test
    public void buildDBObjectFromSimpleTree() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocument.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(jsonObject);
        JsonTreeNode labelNode = (JsonTreeNode) treeNode.getChildAt(1);
        labelNode.getDescriptor().setValue("tata");


        DBObject dbObject = JsonTreeUtils.buildDBObject(treeNode);

        assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"label\" : \"tata\" , \"visible\" : false , \"image\" :  null }",
                dbObject.toString());
    }

    @Test
    public void buildDBObjectFromTreeWithSubNodes() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithInnerNodes.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(jsonObject);

//      Simulate updating from the treeNode
        JsonTreeNode innerDocNode = (JsonTreeNode) treeNode.getChildAt(4);
        JsonTreeNode soldOutNode = (JsonTreeNode) innerDocNode.getChildAt(2);
        soldOutNode.getDescriptor().setValue("false");

        DBObject dbObject = JsonTreeUtils.buildDBObject(treeNode);

        assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"label\" : \"toto\" , \"visible\" : false , \"image\" :  null  , \"innerdoc\" : { \"title\" : \"What?\" , \"numberOfPages\" : 52 , \"soldOut\" : false}}",
                dbObject.toString());
    }

    @Test
    public void buildDBObjectFromTreeWithSubList() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithSubList.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(jsonObject);
        JsonTreeNode tagsNode = (JsonTreeNode) treeNode.getChildAt(2);
        JsonTreeNode agileTagNode = (JsonTreeNode) tagsNode.getChildAt(2);
        agileTagNode.getDescriptor().setValue("a gilles");

        DBObject dbObject = JsonTreeUtils.buildDBObject(treeNode);

        assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"title\" : \"XP by example\" , \"tags\" : [ \"pair programming\" , \"tdd\" , \"a gilles\"] , \"innerList\" : [ [ 1 , 2 , 3 , 4] , [ false , true] , [ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]]}",
                dbObject.toString());
    }

    @Test
    public void getObjectIdFromANode() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithInnerNodes.json")));
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(jsonObject);
        JsonTreeNode objectIdNode = (JsonTreeNode) treeNode.getChildAt(0);
        assertEquals("\"_id\"", objectIdNode.getDescriptor().getFormattedKey());

        assertNull(JsonTreeUtils.findObjectIdNode(treeNode));
        assertEquals(objectIdNode, JsonTreeUtils.findObjectIdNode((JsonTreeNode) treeNode.getChildAt(0)));
//        assertEquals(objectIdNode, JsonTreeUtils.findObjectIdNode((JsonTreeNode) treeNode.getChildAt(3)));

    }

    @Test
    public void findDocumentFromANode() throws Exception {
        BasicDBList dbList = (BasicDBList) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("arrayOfDocuments.json")));

        DBObject first = (DBObject) dbList.get(0);
        first.put("_id", new ObjectId(String.valueOf(first.get("_id"))));

        DBObject second = (DBObject) dbList.get(1);
        second.put("_id", new ObjectId(String.valueOf(second.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(dbList);

        assertEquals(first, JsonTreeUtils.findDocument((JsonTreeNode) treeNode.getChildAt(0)));
    }
}
