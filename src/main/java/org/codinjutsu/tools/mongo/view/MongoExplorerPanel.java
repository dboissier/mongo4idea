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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import org.codinjutsu.tools.mongo.MongoComponent;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
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
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class MongoExplorerPanel extends JPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtils.getIconResource("pluginSettings.png");

    private JPanel rootPanel;

    private JPanel treePanel;
    private Tree mongoTree;

    private JPanel toolBarPanel;

    private final MongoManager mongoManager;
    private final MongoConfiguration configuration;
    private final MongoComponent.RunnerCallback runnerCallback;
    private MongoServer mongoServer;

    public MongoExplorerPanel(MongoManager mongoManager, MongoConfiguration configuration, MongoComponent.RunnerCallback runnerCallback) {
        this.mongoManager = mongoManager;
        this.configuration = configuration;
        this.runnerCallback = runnerCallback;

        treePanel.setLayout(new BorderLayout());

        mongoTree = createTree();
        mongoTree.setCellRenderer(new MongoTreeRenderer());
        mongoTree.setName("mongoTree");

        JBScrollPane mongoTreeScrollPane = new JBScrollPane(mongoTree);

        setLayout(new BorderLayout());
        treePanel.add(mongoTreeScrollPane, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        init();
    }

    public void reloadConfiguration() {
        init();
    }


    private void init() {
        try {
            mongoTree.setRootVisible(true);
            mongoServer = mongoManager.loadDatabaseCollections(configuration);
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

        } catch (ConfigurationException confEx) {
            mongoServer = null;
            mongoTree.setModel(null);
            mongoTree.setRootVisible(false);
        }
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

    private Tree createTree() {

        SimpleTree tree = new SimpleTree() {

            private final JLabel myLabel = new JLabel(
                    String.format("<html><center>No mongo server available<br><br>You may use <img src=\"%s\"> to set or fix Mongo configuration</center></html>", pluginSettingsUrl)
            );

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mongoServer != null && mongoServer.hasDatabases()) return;

                myLabel.setFont(getFont());
                myLabel.setBackground(getBackground());
                myLabel.setForeground(getForeground());
                Rectangle bounds = getBounds();
                Dimension size = myLabel.getPreferredSize();
                myLabel.setBounds(0, 0, size.width, size.height);

                int x = (bounds.width - size.width) / 2;
                Graphics g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height);
                try {
                    myLabel.paint(g2);
                } finally {
                    g2.dispose();
                }
            }
        };

        tree.getEmptyText().clear();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        return tree;
    }
}
