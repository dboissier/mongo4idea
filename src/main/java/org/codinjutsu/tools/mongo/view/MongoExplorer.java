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

package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.Disposable;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.model.MongoServer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class MongoExplorer extends JPanel implements Disposable {


    private JTree mongoTree;
    private JPanel rootPanel;
    private final MongoManager mongoManager;
    private final MongoConfiguration configuration;

    public MongoExplorer(MongoManager mongoManager, MongoConfiguration configuration) {
        this.mongoManager = mongoManager;
        this.configuration = configuration;
        mongoTree.setCellRenderer(new MongoTreeRenderer());
        mongoTree.setName("mongoTree");

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        init();
    }

    private void init() {
        MongoServer mongoServer = mongoManager.loadDatabaseCollections(configuration);
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(mongoServer);
        if (mongoServer.hasDatabases()) {
            for (MongoDatabase mongoDatabase : mongoServer.getDatabases()) {
                DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(mongoDatabase);
                for (String collection : mongoDatabase.getCollections()) {
                    databaseNode.add(new DefaultMutableTreeNode(collection));
                }
                rootNode.add(databaseNode);
            }
        }
        mongoTree.setModel(new DefaultTreeModel(rootNode));
    }


    @Override
    public void dispose() {

    }
}
