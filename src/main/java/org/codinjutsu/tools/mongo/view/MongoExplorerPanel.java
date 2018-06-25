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

package org.codinjutsu.tools.mongo.view;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.SshTunnelingConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.logic.Notifier;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.explorer.*;
import org.codinjutsu.tools.mongo.view.editor.MongoFileSystem;
import org.codinjutsu.tools.mongo.view.editor.MongoObjectFile;
import org.codinjutsu.tools.mongo.view.model.navigation.Navigation;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.*;
import java.util.List;

import static org.codinjutsu.tools.mongo.utils.GuiUtils.showNotification;

public class MongoExplorerPanel extends JPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtils.class.getResource("/general/add.png");

    private JPanel rootPanel;

    private JPanel treePanel;
    private Tree mongoTree;

    private JPanel toolBarPanel;

    private final Project project;
    private final MongoManager mongoManager;
    private final Notifier notifier;
    private boolean sortedByName = false;


    private static final Comparator<DefaultMutableTreeNode> sortByNameComparator =
            (treeNodeLeft, treeNodeRight) -> {

                Object userObjectLeft = treeNodeLeft.getUserObject();
                Object userObjectRight = treeNodeRight.getUserObject();
                if (userObjectLeft instanceof MongoDatabase) {
                    MongoDatabase mongoDatabaseLeft = (MongoDatabase) userObjectLeft;
                    MongoDatabase mongoDatabaseRight = (MongoDatabase) userObjectRight;
                    return mongoDatabaseLeft.getName().compareTo(mongoDatabaseRight.getName());
                }

                if (userObjectLeft instanceof MongoCollection) {
                    MongoCollection mongoCollectionLeft = (MongoCollection) userObjectLeft;
                    MongoCollection mongoCollectionRight = (MongoCollection) userObjectRight;
                    return mongoCollectionLeft.getName().compareTo(mongoCollectionRight.getName());
                }

                return 0;
            };

    public MongoExplorerPanel(Project project, MongoManager mongoManager, Notifier notifier) {
        this.project = project;
        this.mongoManager = mongoManager;
        this.notifier = notifier;

        treePanel.setLayout(new BorderLayout());

        mongoTree = createTree();
        mongoTree.setCellRenderer(new MongoTreeRenderer());
        mongoTree.setName("mongoTree");

        JBScrollPane mongoTreeScrollPane = new JBScrollPane(mongoTree);

        setLayout(new BorderLayout());
        treePanel.add(mongoTreeScrollPane, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        ApplicationManager.getApplication().invokeLater(this::reloadAllServerConfigurations);
    }

    public void reloadAllServerConfigurations() {
        this.mongoManager.cleanUpServers();
        mongoTree.setRootVisible(false);

        List<ServerConfiguration> serverConfigurations = getServerConfigurations();
        if (serverConfigurations.size() == 0) {
            mongoTree.setModel(null);
            return;
        }

        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        mongoTree.setModel(new DefaultTreeModel(rootNode));

        createServerTreeNodes(serverConfigurations, rootNode);

        new TreeSpeedSearch(mongoTree, treePath -> {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            final Object userObject = node.getUserObject();
            if (userObject instanceof MongoDatabase) {
                return ((MongoDatabase) userObject).getName();
            }
            if (userObject instanceof MongoCollection) {
                return ((MongoCollection) userObject).getName();
            }
            return "<empty>";
        });

        sortTreeNodes();

        TreeUtil.expand(mongoTree, 2);
    }

    private void createServerTreeNodes(List<ServerConfiguration> serverConfigurations, DefaultMutableTreeNode rootNode) {
        for (ServerConfiguration serverConfiguration : serverConfigurations) {
            MongoServer mongoServer = new MongoServer(serverConfiguration);
            this.mongoManager.registerServer(mongoServer);
            DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(mongoServer);
            rootNode.add(serverNode);
            if (serverConfiguration.isConnectOnIdeStartup()) {
                this.reloadServerConfiguration(serverNode, false);
            }
        }
    }


    public void reloadServerConfiguration(final DefaultMutableTreeNode serverNode, final boolean expandAfterLoading) {
        final MongoServer mongoServer = (MongoServer) serverNode.getUserObject();
        SshTunnelingConfiguration sshTunnelingConfiguration = mongoServer.getConfiguration().getSshTunnelingConfiguration();
        if (!SshTunnelingConfiguration.isEmpty(sshTunnelingConfiguration)
                && sshTunnelingConfiguration.isAskPassphrase()) {
            SshPassphraseDialog dialog = SshPassphraseDialog.createDialog(this);
            dialog.show();

            if (!dialog.isOK()) {
                return;
            }
            sshTunnelingConfiguration.setProxyPassword(dialog.getPassphrase());
            return;
        }

        mongoTree.setPaintBusy(true);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                mongoManager.loadServer(mongoServer);

                GuiUtils.runInSwingThread(() -> {
                    mongoTree.invalidate();

                    serverNode.removeAllChildren();
                    addDatabasesIfAny(mongoServer, serverNode);

                    ((DefaultTreeModel) mongoTree.getModel()).reload(serverNode);

                    mongoTree.revalidate();

                    if (expandAfterLoading) {
                        GuiUtils.expand(mongoTree, TreeUtil.getPathFromRoot(serverNode), 1);
                    }

                });

            } catch (ConfigurationException confEx) {
                mongoServer.setStatus(MongoServer.Status.ERROR);
                String errorMessage = String.format("Error when connecting to %s", mongoServer.getLabel());
                notifier.notifyError(errorMessage + ": " + confEx.getMessage());
                showNotification(treePanel, MessageType.ERROR, errorMessage, Balloon.Position.atLeft);
            } finally {
                mongoTree.setPaintBusy(false);
            }
        });
    }

    private void addDatabasesIfAny(MongoServer mongoServer, DefaultMutableTreeNode serverNode) {
        for (MongoDatabase mongoDatabase : mongoServer.getDatabases()) {
            DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(mongoDatabase);
            for (MongoCollection collection : mongoDatabase.getCollections()) {
                if (shouldNotIgnore(collection, mongoServer.getConfiguration())) {
                    databaseNode.add(new DefaultMutableTreeNode(collection));
                }
            }
            serverNode.add(databaseNode);
        }
    }

    private List<ServerConfiguration> getServerConfigurations() {
        return MongoConfiguration.getInstance(project).getServerConfigurations();
    }

    private static boolean shouldNotIgnore(MongoCollection collection, ServerConfiguration configuration) {
        return !configuration.getCollectionsToIgnore().contains(collection.getName());
    }

    public void installActions() {

        final TreeExpander treeExpander = new TreeExpander() {
            @Override
            public void expandAll() {
                MongoExplorerPanel.this.expandAll();
            }

            @Override
            public boolean canExpand() {
                return !getServerConfigurations().isEmpty();
            }

            @Override
            public void collapseAll() {
                MongoExplorerPanel.this.collapseAll();
            }

            @Override
            public boolean canCollapse() {
                return !getServerConfigurations().isEmpty();
            }
        };

        CommonActionsManager actionsManager = CommonActionsManager.getInstance();

        final AnAction expandAllAction = actionsManager.createExpandAllAction(treeExpander, rootPanel);
        final AnAction collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, rootPanel);

        Disposer.register(this, () -> {
            collapseAllAction.unregisterCustomShortcutSet(rootPanel);
            expandAllAction.unregisterCustomShortcutSet(rootPanel);
        });


        DefaultActionGroup actionGroup = new DefaultActionGroup("MongoExplorerGroup", false);
        ViewCollectionValuesAction viewCollectionValuesAction = new ViewCollectionValuesAction(this);
        RefreshServerAction refreshServerAction = new RefreshServerAction(this);
        AddMongoServerAction addMongoServerAction = new AddMongoServerAction(this);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(addMongoServerAction);
//            actionGroup.add(new SortByNameAction(this));
            actionGroup.addSeparator();
            actionGroup.add(refreshServerAction);
            actionGroup.add(new MongoConsoleAction(this));
            actionGroup.add(viewCollectionValuesAction);
            actionGroup.add(expandAllAction);
            actionGroup.add(collapseAllAction);
            actionGroup.addSeparator();
            actionGroup.add(new OpenPluginSettingsAction());
        }

        GuiUtils.installActionGroupInToolBar(actionGroup, toolBarPanel, ActionManager.getInstance(), "MongoExplorerActions", true);

        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoExplorerPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(refreshServerAction);
            actionPopupGroup.add(new EditMongoServerAction(this));
            actionPopupGroup.add(new DeleteMongoServerAction(this));
            actionPopupGroup.addSeparator();
            actionPopupGroup.add(viewCollectionValuesAction);
            actionPopupGroup.add(new DropCollectionAction(this));
            actionPopupGroup.add(new DropDatabaseAction(this));
        }

        PopupHandler.installPopupHandler(mongoTree, actionPopupGroup, "POPUP", ActionManager.getInstance());

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
                    if (treeNode.getUserObject() instanceof MongoServer && treeNode.getChildCount() == 0) {
                        reloadServerConfiguration(getSelectedServerNode(), true);
                    }
                    if (treeNode.getUserObject() instanceof MongoCollection) {
                        loadSelectedCollectionValues();
                    }
                }
            }
        });
    }

    private void expandAll() {
        TreeUtil.expandAll(mongoTree);
    }

    private void collapseAll() {
        TreeUtil.collapseAll(mongoTree, 1);
    }

    @Override
    public void dispose() {
        mongoTree = null;
    }

    public DefaultMutableTreeNode getSelectedServerNode() {//TODO need refactor
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

    private DefaultMutableTreeNode getParentServerNode() {//TODO need refactor
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
                return (DefaultMutableTreeNode) treeNode.getParent().getParent();
            }

            if (userObject instanceof MongoDatabase) {
                return (DefaultMutableTreeNode) treeNode.getParent();
            }
        }
        return null;
    }


    private DefaultMutableTreeNode getSelectedDatabaseNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoDatabase) {
                return treeNode;
            }
        }

        return null;
    }

    private DefaultMutableTreeNode getSelectedCollectionNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof MongoCollection) {
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

    public MongoDatabase getSelectedDatabase() {
        DefaultMutableTreeNode databaseNode = getSelectedDatabaseNode();
        if (databaseNode == null) {
            return null;
        }

        return (MongoDatabase) databaseNode.getUserObject();

    }

    public MongoCollection getSelectedCollection() {
        DefaultMutableTreeNode collectionNode = getSelectedCollectionNode();
        if (collectionNode == null) {
            return null;
        }

        return (MongoCollection) collectionNode.getUserObject();
    }


    public void loadSelectedCollectionValues() {
        Navigation navigation = new Navigation();
        navigation.addNewWayPoint(getSelectedCollection(), new MongoQueryOptions());

        MongoFileSystem.getInstance().openEditor(new MongoObjectFile(project, getConfiguration(), navigation));
    }

    public void removeSelectedServerNode() {
        DefaultMutableTreeNode serverNode = getSelectedServerNode();
        if (serverNode == null) {
            return;
        }

        TreeUtil.removeSelected(mongoTree);

    }

    public void dropCollection() {
        MongoCollection selectedCollection = getSelectedCollection();
        mongoManager.dropCollection(getConfiguration(), selectedCollection);
        notifier.notifyInfo("Collection " + selectedCollection.getName() + " dropped");

        reloadServerConfiguration(getParentServerNode(), true);
    }

    public void dropDatabase() {
        MongoDatabase selectedDatabase = getSelectedDatabase();
        mongoManager.dropDatabase(getConfiguration(), selectedDatabase);
        notifier.notifyInfo("Datatabase " + selectedDatabase.getName() + " dropped");

        reloadServerConfiguration(getParentServerNode(), true);
    }

    public void sortTreeNodes() {
        final DefaultTreeModel model = (DefaultTreeModel) mongoTree.getModel();
        TreeUtil.sort(model, sortByNameComparator);

        GuiUtils.runInSwingThread(() -> model.nodeStructureChanged((TreeNode) model.getRoot()));
    }

    private Tree createTree() {

        Tree tree = new Tree() {

            private final JLabel myLabel = new JLabel(
                    String.format("<html><center>No Mongo server available<br><br>You may use <img src=\"%s\"> to add configuration</center></html>", pluginSettingsUrl)
            );

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!getServerConfigurations().isEmpty()) return;

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

    public MongoManager getMongoManager() {
        return mongoManager;
    }
}
