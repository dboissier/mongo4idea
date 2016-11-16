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

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
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
            if (value instanceof Document) {
                processDocument(currentNode, (Document) value);
            } else if (value instanceof List) {
                processObjectList(currentNode, (List) value);
            }
            parentNode.add(currentNode);
        }
    }

    public static void processObjectList(JsonTreeNode parentNode, List objectList) {
        for (int i = 0; i < objectList.size(); i++) {
            Object object = objectList.get(i);
            JsonTreeNode subNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(i, object));
            if (object instanceof Document) {
                processDocument(subNode, (Document) object);
            } else if (object instanceof List) {
                processObjectList(subNode, (List) object);
            }
            parentNode.add(subNode);
        }
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
            } else if (value instanceof List) {
                    document.put(descriptor.getKey(), buildObjectList(node));
            } else {
                document.put(descriptor.getKey(), value);
            }
        }

        return document;
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

    static JsonTreeNode findObjectIdNode(JsonTreeNode treeNode) {
        MongoNodeDescriptor descriptor = treeNode.getDescriptor();
        if (descriptor instanceof MongoResultDescriptor) { //defensive prog?
            return null;
        }

        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            if (StringUtils.equals(keyValueDescriptor.getKey(), "_id")) {
                return treeNode;
            }
        }

        JsonTreeNode parentTreeNode = (JsonTreeNode) treeNode.getParent();
        if (parentTreeNode.getDescriptor() instanceof MongoValueDescriptor) {
            if (((JsonTreeNode) parentTreeNode.getParent()).getDescriptor() instanceof MongoResultDescriptor) {
                //find
            }
        }

        return null;
    }

}
