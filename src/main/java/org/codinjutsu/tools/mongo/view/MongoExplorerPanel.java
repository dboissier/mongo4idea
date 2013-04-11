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

package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.MongoConsoleAction;
import org.codinjutsu.tools.mongo.view.action.OpenPluginSettingsAction;
import org.codinjutsu.tools.mongo.view.action.RefreshServerAction;
import org.codinjutsu.tools.mongo.view.action.ViewCollectionValuesAction;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

public class MongoExplorerPanel extends JPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtils.isUnderDarcula() ? GuiUtils.getIconResource("pluginSettings_dark.png") : GuiUtils.getIconResource("pluginSettings.png");

    private JPanel rootPanel;

    private JPanel treePanel;
    private final Tree mongoTree;

    private JPanel toolBarPanel;

    private final Project project;
    private final MongoManager mongoManager;
    private List<ServerConfiguration> serverConfigurations;

    public MongoExplorerPanel(Project project, MongoManager mongoManager) {
        this.project = project;
        this.mongoManager = mongoManager;

        treePanel.setLayout(new BorderLayout());

        mongoTree = createTree();
        mongoTree.setCellRenderer(new MongoTreeRenderer());
        mongoTree.setName("mongoTree");

        JBScrollPane mongoTreeScrollPane = new JBScrollPane(mongoTree);

        setLayout(new BorderLayout());
        treePanel.add(mongoTreeScrollPane, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        reloadAllServerConfigurations(true);
    }

    public void reloadConfiguration(boolean clearSelection) {
        if (clearSelection) {
            TreeUtil.removeSelected(mongoTree);
        }
        ServerConfiguration serverConfiguration = getConfiguration();
        if (serverConfiguration != null) {
            reloadSelectedServerConfiguration();
        } else {
            reloadAllServerConfigurations(false);
        }
    }

    private void reloadSelectedServerConfiguration() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {

            @Override
            public void run() {
                mongoTree.invalidate();

                DefaultMutableTreeNode serverNode = getSelectedServerNode();
                if (serverNode == null) {
                    return;
                }

                serverNode.removeAllChildren();

                MongoServer mongoServer = (MongoServer) serverNode.getUserObject();
                mongoServer.cleanAllDatabases();
                ServerConfiguration serverConfiguration = mongoServer.getConfiguration();
                serverConfiguration.setServerVersion(mongoManager.connect(mongoServer.getConfiguration()));
                mongoServer = mongoManager.loadDatabaseCollections(mongoServer);

                if (mongoServer.hasDatabases()) {
                    for (MongoDatabase mongoDatabase : mongoServer.getDatabases()) {
                        DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(mongoDatabase);
                        for (MongoCollection collection : mongoDatabase.getCollections()) {
                            if (shouldNotIgnore(collection, serverConfiguration)) {
                                databaseNode.add(new DefaultMutableTreeNode(collection));
                            }
                        }
                        serverNode.add(databaseNode);
                    }
                }

                ((DefaultTreeModel) mongoTree.getModel()).reload(serverNode);

                mongoTree.revalidate();

                TreeUtil.expand(mongoTree, 3);
            }
        });
    }


    private void reloadAllServerConfigurations(final boolean loadOnStartup) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    mongoTree.setRootVisible(false);
                    final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

                    serverConfigurations = MongoConfiguration.getInstance(project).getServerConfigurations();
                    for (ServerConfiguration serverConfiguration : serverConfigurations) {

                        MongoServer mongoServer = new MongoServer(serverConfiguration);
                        final DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(mongoServer);
                        rootNode.add(serverNode);

                        if (loadOnStartup && !serverConfiguration.isConnectOnIdeStartup()) {
                            continue;
                        }
                        serverConfiguration.setServerVersion(mongoManager.connect(mongoServer.getConfiguration()));
                        mongoServer = mongoManager.loadDatabaseCollections(mongoServer);
                        if (mongoServer.hasDatabases()) {
                            for (MongoDatabase mongoDatabase : mongoServer.getDatabases()) {
                                DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(mongoDatabase);
                                for (MongoCollection collection : mongoDatabase.getCollections()) {
                                    if (shouldNotIgnore(collection, serverConfiguration)) {
                                        databaseNode.add(new DefaultMutableTreeNode(collection));
                                    }
                                }
                                serverNode.add(databaseNode);
                            }
                        }
                    }

                    mongoTree.invalidate();
                    mongoTree.setModel(new DefaultTreeModel(rootNode));
                    mongoTree.revalidate();

                    TreeUtil.expand(mongoTree, 3);

                } catch (ConfigurationException confEx) {
                    serverConfigurations = null;
                    mongoTree.setModel(null);
                    mongoTree.setRootVisible(false);
                }
            }
        });
    }

    private static boolean shouldNotIgnore(MongoCollection collection, ServerConfiguration configuration) {
        return !configuration.getCollectionsToIgnore().contains(collection.getName());
    }

    public void installActions() {

        DefaultActionGroup actionGroup = new DefaultActionGroup("MongoExplorerGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new RefreshServerAction(this));
            actionGroup.add(new ViewCollectionValuesAction(this));
            actionGroup.add(new MongoConsoleAction(this));
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

                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
                if (treeNode == null) {
                    return;
                }

                if (mouseEvent.getClickCount() == 2) {

                    if (treeNode.getUserObject() instanceof MongoCollection) {
                        loadSelectedCollectionValues();
                    } else if (treeNode.getUserObject() instanceof MongoServer && treeNode.getChildCount() == 0) {
                        reloadConfiguration(false);
                    }
                }
            }
        });
    }

    @Override
    public void dispose() {

    }

    public DefaultMutableTreeNode getSelectedCollectionNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
                return treeNode;
            }
        }
        return null;
    }


    public DefaultMutableTreeNode getSelectedServerNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
                return (DefaultMutableTreeNode) treeNode.getParent().getParent();
            }

            if (userObject instanceof MongoDatabase) {
                return (DefaultMutableTreeNode) treeNode.getParent();
            }

            if (userObject instanceof MongoServer) {
                return treeNode;
            }
        }
        return null;
    }

    public ServerConfiguration getConfiguration() {

        DefaultMutableTreeNode serverNode = getSelectedServerNode();
        if (serverNode == null) {
            return null;
        }

        return ((MongoServer) serverNode.getUserObject()).getConfiguration();
    }

    MongoCollection getSelectedCollectionValues() {
        DefaultMutableTreeNode collectionNode = getSelectedCollectionNode();
        if (collectionNode == null) {
            return null;
        }

        return (MongoCollection) collectionNode.getUserObject();
    }

    public void loadSelectedCollectionValues() {
        MongoWindowManager.getInstance(project).addResultContent(getConfiguration(), getSelectedCollectionValues());
        MongoWindowManager.getInstance(project).showToolWindow();
    }

    private Tree createTree() {

        SimpleTree tree = new SimpleTree() {

            private final JLabel myLabel = new JLabel(
                    String.format("<html><center>No Mongo server available<br><br>You may use <img src=\"%s\"> to add or fix configuration</center></html>", pluginSettingsUrl)
            );

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (serverConfigurations != null && !serverConfigurations.isEmpty()) return;

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
