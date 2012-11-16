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
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import org.codinjutsu.tools.mongo.MongoComponent;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.OpenPluginSettingsAction;
import org.codinjutsu.tools.mongo.view.action.ViewCollectionValuesAction;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MongoExplorerPanel extends JPanel implements Disposable {


    private JTree mongoTree;
    private JPanel rootPanel;
    private JPanel toolBarPanel;
    private final MongoManager mongoManager;
    private final MongoConfiguration configuration;
    private final MongoRunnerPanel mongoRunnerPanel;
    private final MongoComponent.RunnerCallback runnerCallback;

    public MongoExplorerPanel(MongoManager mongoManager, MongoConfiguration configuration, MongoRunnerPanel mongoRunnerPanel, MongoComponent.RunnerCallback runnerCallback) {
        this.mongoManager = mongoManager;
        this.configuration = configuration;
        this.mongoRunnerPanel = mongoRunnerPanel;
        this.runnerCallback = runnerCallback;

        mongoTree.setCellRenderer(new MongoTreeRenderer());
        mongoTree.setName("mongoTree");

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        init();
    }

    public void reloadConfiguration() {
        init();
    }


    private void init() {
        MongoServer mongoServer = mongoManager.loadDatabaseCollections(configuration);
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(mongoServer);
        if (mongoServer.hasDatabases()) {
            for (MongoDatabase mongoDatabase : mongoServer.getDatabases()) {
                DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(mongoDatabase);
                for (MongoCollection collection : mongoDatabase.getCollections()) {
                    if (shouldNotIgnore(collection, configuration)) {
                        databaseNode.add(new DefaultMutableTreeNode(collection));
                    }
                }
                rootNode.add(databaseNode);
            }
        }
        mongoTree.invalidate();
        mongoTree.setModel(new DefaultTreeModel(rootNode));
        mongoTree.revalidate();
    }

    private static boolean shouldNotIgnore(MongoCollection collection, MongoConfiguration configuration) {
        return !configuration.getCollectionsToIgnore().contains(collection.getName());
    }

    public void installActions() {

        DefaultActionGroup actionGroup = new DefaultActionGroup("MongoExplorerGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new ViewCollectionValuesAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new OpenPluginSettingsAction());
        }
        GuiUtils.installActionGroupInToolBar(actionGroup, toolBarPanel, ActionManager.getInstance(), "MongoExplorerActions", true);

        mongoTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (!(mouseEvent.getSource() instanceof JTree)) {
                    return;
                }
                DefaultMutableTreeNode lastSelectedNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
                if (lastSelectedNode == null) {
                    return;
                }

                if (!(lastSelectedNode.getUserObject() instanceof MongoCollection)) {
                    return;
                }

                if (mouseEvent.getClickCount() == 2) {
                    loadSelectedCollectionValues();
                }
            }
        });
    }

    @Override
    public void dispose() {

    }

    private MongoCollection getSelectedCollection() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
                return (MongoCollection) userObject;
            }
        }
        return null;
    }


    public MongoCollection getSelectedCollectionValues() {
        MongoCollection selectedCollection = getSelectedCollection();
        if (selectedCollection == null) {
            return null;
        }

        return selectedCollection;
    }

    public void loadSelectedCollectionValues() {
        runnerCallback.execute(getSelectedCollectionValues());
    }
}
