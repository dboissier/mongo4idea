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

import com.mongodb.DBRef;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoResultDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoValueDescriptor;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class JsonTreeUtils {

    public static TreeNode buildJsonTree(MongoCollectionResult mongoCollectionResult) {
        JsonTreeNode rootNode = new JsonTreeNode(new MongoResultDescriptor(mongoCollectionResult.getCollectionName()));

        List<Document> mongoObjects = mongoCollectionResult.getDocuments();
        int i = 0;
        for (Document document : mongoObjects) {
            JsonTreeNode currentNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(i++, document));
            processDocument(currentNode, document);
            rootNode.add(currentNode);
        }
        return rootNode;
    }

    public static TreeNode buildJsonTree(Document document) {
        JsonTreeNode rootNode = new JsonTreeNode(new MongoResultDescriptor());//TODO crappy
        if (document != null) {
            processDocument(rootNode, document);
        }
        return rootNode;
    }

    public static void processDocument(JsonTreeNode parentNode, Document document) {
        for (String key : document.keySet()) {
            Object value = document.get(key);
            JsonTreeNode currentNode = new JsonTreeNode(MongoKeyValueDescriptor.createDescriptor(key, value));
            processValue(value, currentNode);
            parentNode.add(currentNode);
        }
    }

    public static void processObjectList(JsonTreeNode parentNode, List objectList) {
        for (int i = 0; i < objectList.size(); i++) {
            Object value = objectList.get(i);
            JsonTreeNode currentNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(i, value));
            processValue(value, currentNode);
            parentNode.add(currentNode);
        }
    }

    private static void processValue(Object value, JsonTreeNode currentNode) {
        if (value instanceof Document) {
            processDocument(currentNode, (Document) value);
        } else if (value instanceof DBRef) {
            processDBRef(currentNode, (DBRef) value);
        } else if (value instanceof List) {
            processObjectList(currentNode, (List) value);
        }
    }

    private static void processDBRef(JsonTreeNode parentNode, DBRef dbRef) {
        parentNode.add(new JsonTreeNode(MongoKeyValueDescriptor.createDescriptor("$ref", dbRef.getCollectionName())));
        parentNode.add(new JsonTreeNode(MongoKeyValueDescriptor.createDescriptor("$id", dbRef.getId())));
        parentNode.add(new JsonTreeNode(MongoKeyValueDescriptor.createDescriptor("$db", dbRef.getDatabaseName())));
    }

    public static Document buildDocumentObject(JsonTreeNode rootNode) {
        Document document = new Document();
        Enumeration children = rootNode.children();
        while (children.hasMoreElements()) {
            JsonTreeNode node = (JsonTreeNode) children.nextElement();
            MongoKeyValueDescriptor descriptor = (MongoKeyValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();
            if (value instanceof Document) {
                document.put(descriptor.getKey(), buildDocumentObject(node));
            } else if (value instanceof DBRef) {
                document.put(descriptor.getKey(), buildDBRefObject(node));
            } else if (value instanceof List) {
                document.put(descriptor.getKey(), buildObjectList(node));
            } else {
                document.put(descriptor.getKey(), value);
            }
        }

        return document;
    }

    private static DBRef buildDBRefObject(JsonTreeNode parentNode) {
        Object _id = null;
        String collectionName = null;
        String databaseName = null;
        Enumeration children = parentNode.children();
        while (children.hasMoreElements()) {
            JsonTreeNode node = (JsonTreeNode) children.nextElement();
            MongoNodeDescriptor descriptor = node.getDescriptor();
            String formattedKey = descriptor.getFormattedKey();
            switch (formattedKey) {
                case "\"$id\"":
                    _id = descriptor.getValue();
                    break;
                case "\"$ref\"":
                    collectionName = (String) descriptor.getValue();
                    break;
                case "\"$db\"":
                    databaseName = (String) descriptor.getValue();
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected key: " + formattedKey);
            }
        }
        if (collectionName == null || _id == null) {
            throw new IllegalArgumentException("When using DBRef, $ref and $id should be set.");
        }
        return new DBRef(databaseName, collectionName, _id);
    }

    private static List buildObjectList(JsonTreeNode parentNode) {
        List<Object> basicDBList = new ArrayList<>();
        Enumeration children = parentNode.children();
        while (children.hasMoreElements()) {
            JsonTreeNode node = (JsonTreeNode) children.nextElement();
            MongoValueDescriptor descriptor = (MongoValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();
            if (value instanceof Document) {
                basicDBList.add(buildDocumentObject(node));
            } else if (value instanceof List) {
                basicDBList.add(buildObjectList(node));
            } else {
                basicDBList.add(value);
            }
        }
        return basicDBList;
    }
}
