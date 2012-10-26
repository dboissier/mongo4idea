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
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.model.nodedescriptor.MongoValueDescriptor;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.List;

public class JsonTreeModel extends DefaultTreeModel {

    public JsonTreeModel(MongoCollectionResult mongoCollectionResult) {

        super(buildJsonTree(mongoCollectionResult));
    }

    private static TreeNode buildJsonTree(MongoCollectionResult mongoCollectionResult) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new ResultNode(mongoCollectionResult.getCollectionName()));

        List<DBObject> mongoObjects = mongoCollectionResult.getMongoObjects();
        for (DBObject mongoObject : mongoObjects) {
            if (mongoObject instanceof BasicDBList) {
                BasicDBList mongoObjectList = (BasicDBList) mongoObject;
                for (int i = 0; i < mongoObjectList.size(); i++) {
                    Object mongoObjectOfList = mongoObjectList.get(i);
                    rootNode.add(new DefaultMutableTreeNode(MongoValueDescriptor.createDescriptor(i, mongoObjectOfList)));
                }
            }
        }
        return rootNode;
    }

}
