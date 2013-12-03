package org.codinjutsu.tools.mongo.view.model;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

public class JsonTreeModelTest {

    @Test
    public void buildDBObjectFromSimpleTree() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocument.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeModel.buildJsonTree(jsonObject);
        JsonTreeNode labelNode = (JsonTreeNode) treeNode.getChildAt(1);
        labelNode.getDescriptor().setValue("tata");


        DBObject dbObject = JsonTreeModel.buildDBObject(treeNode);

        Assert.assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"label\" : \"tata\" , \"visible\" : false , \"image\" :  null }",
                dbObject.toString());
    }

    @Test
    public void buildDBObjectFromTreeWithSubNodes() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithInnerNodes.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeModel.buildJsonTree(jsonObject);

//      Simulate updating from the treeNode
        JsonTreeNode innerDocNode = (JsonTreeNode) treeNode.getChildAt(4);
        JsonTreeNode soldOutNode = (JsonTreeNode) innerDocNode.getChildAt(2);
        soldOutNode.getDescriptor().setValue("false");

        DBObject dbObject = JsonTreeModel.buildDBObject(treeNode);

        Assert.assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"label\" : \"toto\" , \"visible\" : false , \"image\" :  null  , \"innerdoc\" : { \"title\" : \"What?\" , \"numberOfPages\" : 52 , \"soldOut\" : false}}",
                dbObject.toString());
    }

    @Test
    public void buildDBObjectFromTreeWithSubList() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithSubList.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeModel.buildJsonTree(jsonObject);
        JsonTreeNode tagsNode = (JsonTreeNode) treeNode.getChildAt(2);
        JsonTreeNode agileTagNode = (JsonTreeNode) tagsNode.getChildAt(2);
        agileTagNode.getDescriptor().setValue("a gilles");

        DBObject dbObject = JsonTreeModel.buildDBObject(treeNode);

        Assert.assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"title\" : \"XP by example\" , \"tags\" : [ \"pair programming\" , \"tdd\" , \"a gilles\"] , \"innerList\" : [ [ 1 , 2 , 3 , 4] , [ false , true] , [ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]]}",
                dbObject.toString());
    }
}
