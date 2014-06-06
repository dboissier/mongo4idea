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

package org.codinjutsu.tools.mongo.view.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoResultDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoValueDescriptor;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.List;

public class JsonTreeModel extends DefaultTreeModel {


    public JsonTreeModel(MongoCollectionResult mongoCollectionResult) {
        super(buildJsonTree(mongoCollectionResult));
    }


    public static TreeNode buildJsonTree(MongoCollectionResult mongoCollectionResult) {
        JsonTreeNode rootNode = new JsonTreeNode(new MongoResultDescriptor(mongoCollectionResult.getCollectionName()));

        List<DBObject> mongoObjects = mongoCollectionResult.getMongoObjects();
        int i = 0;
        for (DBObject mongoObject : mongoObjects) {
            if (mongoObject instanceof BasicDBList) {
                processDbObject(rootNode, mongoObject);
            } else if (mongoObject instanceof BasicDBObject) {
                JsonTreeNode currentNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(i++, mongoObject));
                processDbObject(currentNode, mongoObject);
                rootNode.add(currentNode);
            }
        }
        return rootNode;
    }

    public static TreeNode buildJsonTree(DBObject mongoObject) {
        JsonTreeNode rootNode = new JsonTreeNode(new MongoResultDescriptor());//TODO crappy
        processDbObject(rootNode, mongoObject);
        return rootNode;
    }

    public static void processDbObject(JsonTreeNode parentNode, DBObject mongoObject) {
        if (mongoObject instanceof BasicDBList) {
            BasicDBList mongoObjectList = (BasicDBList) mongoObject;
            for (int i = 0; i < mongoObjectList.size(); i++) {
                Object mongoObjectOfList = mongoObjectList.get(i);
                JsonTreeNode currentNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(i, mongoObjectOfList));
                if (mongoObjectOfList instanceof DBObject) {
                    processDbObject(currentNode, (DBObject) mongoObjectOfList);
                }
                parentNode.add(currentNode);
            }
        } else if (mongoObject instanceof BasicDBObject) {
            BasicDBObject basicDBObject = (BasicDBObject) mongoObject;
            for (String key : basicDBObject.keySet()) {
                Object value = basicDBObject.get(key);
                JsonTreeNode currentNode = new JsonTreeNode(MongoKeyValueDescriptor.createDescriptor(key, value));
                if (value instanceof DBObject) {
                    processDbObject(currentNode, (DBObject) value);
                }
                parentNode.add(currentNode);
            }
        }
    }

    public static DBObject buildDBObject(JsonTreeNode rootNode) {
        BasicDBObject basicDBObject = new BasicDBObject();
        Enumeration children = rootNode.children();
        while (children.hasMoreElements()) {
            JsonTreeNode node = (JsonTreeNode) children.nextElement();
            MongoKeyValueDescriptor descriptor = (MongoKeyValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();
            if (value instanceof DBObject) {
                if (value instanceof BasicDBList) {
                    basicDBObject.put(descriptor.getKey(), buildDBList(node));
                } else {
                    basicDBObject.put(descriptor.getKey(), buildDBObject(node));
                }
            } else {
                basicDBObject.put(descriptor.getKey(), value);
            }
        }

        return basicDBObject;
    }

    private static DBObject buildDBList(JsonTreeNode parentNode) {
        BasicDBList basicDBList = new BasicDBList();
        Enumeration children = parentNode.children();
        while (children.hasMoreElements()) {
            JsonTreeNode node = (JsonTreeNode) children.nextElement();
            MongoValueDescriptor descriptor = (MongoValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();
            if (value instanceof DBObject) {
                if (value instanceof BasicDBList) {
                    basicDBList.add(buildDBList(node));
                } else {
                    basicDBList.add(buildDBObject(node));
                }
            } else {
                basicDBList.add(value);
            }
        }
        return basicDBList;
    }
}
