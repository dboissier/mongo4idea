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

package org.codinjutsu.tools.mongo.view.model;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.List;

public class JsonTreeModel extends DefaultTreeModel {

    public JsonTreeModel(List<DBObject> dbObjects) {

        super(buildJsonTree(dbObjects));
    }

    private static TreeNode buildJsonTree(List<DBObject> dbObjects) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new ResultNode());

        int i = 0;
        for (DBObject dbObject : dbObjects) {
            if (dbObject instanceof BasicDBList) {
                BasicDBList dbList = (BasicDBList) dbObject;
                int j = 0;
                for (Object obj : dbList) {
                    DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode(new MongoValueDescriptor(String.format("[%s] Object", j++), dbObject));
                    fillJsonNode(objectNode, (DBObject) obj);
                    rootNode.add(objectNode);
                }
            }
            else {
                DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode(new MongoValueDescriptor(String.format("[%s] Object", i++), dbObject));
                fillJsonNode(objectNode, dbObject);
                rootNode.add(objectNode);
            }
        }

        return rootNode;
    }

    private static void fillJsonNode(DefaultMutableTreeNode nodeToFill, DBObject dbObject) {
        if (dbObject instanceof BasicDBList) {
            BasicDBList dbList = (BasicDBList) dbObject;
            for (Object obj : dbList) {
                if (obj instanceof DBObject) {
                    fillJsonNode(nodeToFill, (DBObject) obj);
                } else {
                    MongoValueDescriptor mongoValueDescriptor = createMongoValueDescriptor("", obj);
                    DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode(mongoValueDescriptor);
                    nodeToFill.add(propertyNode);
                }
            }
        } else {
            for (String key : dbObject.keySet()) {
                Object value = dbObject.get(key);
                MongoValueDescriptor mongoValueDescriptor = createMongoValueDescriptor(key, value);
                DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode(mongoValueDescriptor);
                if (value instanceof DBObject) {
                    fillJsonNode(propertyNode, (DBObject) value);
                }
                nodeToFill.add(propertyNode);
            }
        }
    }

    private static MongoValueDescriptor createMongoValueDescriptor(String key, Object value) {
        MongoValueDescriptor mongoValueDescriptor;
        if (value == null) {
            mongoValueDescriptor = new MongoValueDescriptor(key);
        } else {
            if (value instanceof String) {
                mongoValueDescriptor = new MongoValueDescriptor(key, value);
            } else if (value instanceof Boolean) {
                mongoValueDescriptor = new MongoValueDescriptor(key, value);
            } else if (value instanceof Integer) {
                mongoValueDescriptor = new MongoValueDescriptor(key, value);
            } else {
                mongoValueDescriptor = new MongoValueDescriptor(key, value);
            }
        }
        return mongoValueDescriptor;
    }

}
