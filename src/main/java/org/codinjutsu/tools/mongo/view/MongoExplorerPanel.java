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
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
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
import org.codinjutsu.tools.mongo.view.model.MongoTreeBuilder;
import org.codinjutsu.tools.mongo.view.model.navigation.Navigation;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.codinjutsu.tools.mongo.utils.GuiUtils.showNotification;

public class MongoExplorerPanel extends JPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtils.class.getResource("/general/add.png");

    private JPanel rootPanel;

    private JPanel treePanel;
    private final Tree mongoTree;

    private JPanel toolBarPanel;

    private final Project project;
    private final MongoManager mongoManager;
    private final Notifier notifier;

    private final MongoTreeBuilder mongoTreeBuilder;

    public MongoExplorerPanel(Project project, MongoManager mongoManager, Notifier notifier) {
        this.project = project;
        this.mongoManager = mongoManager;
        this.notifier = notifier;

        treePanel.setLayout(new BorderLayout());

        mongoTree = createTree();
        mongoTreeBuilder = new MongoTreeBuilder(mongoTree);

        JBScrollPane mongoTreeScrollPane = new JBScrollPane(mongoTree);

        setLayout(new BorderLayout());
        treePanel.add(mongoTreeScrollPane, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        loadAllServerConfigurations();

        installActions();
    }

    private void loadAllServerConfigurations() {
        this.mongoManager.cleanUpServers();

        List<ServerConfiguration> serverConfigurations = getServerConfigurations();
        for (ServerConfiguration serverConfiguration : serverConfigurations) {
            addConfiguration(serverConfiguration);
        }
    }

    public void addConfiguration(ServerConfiguration serverConfiguration) {
        mongoTreeBuilder.addConfiguration(serverConfiguration);
    }

    public void loadServerConfiguration(final MongoServer mongoServer) {
        mongoTree.setPaintBusy(true);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                List<MongoDatabase> mongoDatabases = mongoManager.loadDatabases(mongoServer, mongoServer.getConfiguration());
                if (mongoDatabases.isEmpty()) {
                    return;
                }
                mongoServer.setDatabases(mongoDatabases);
                mongoTreeBuilder.queueUpdateFrom(mongoServer, true)
                        .doWhenDone(() -> mongoTreeBuilder.expand(mongoServer, null));

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

    private List<ServerConfiguration> getServerConfigurations() {
        return MongoConfiguration.getInstance(project).getServerConfigurations();
    }

    private void installActions() {

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
            actionPopupGroup.add(new DeleteAction(this));
            actionPopupGroup.addSeparator();
            actionPopupGroup.add(viewCollectionValuesAction);
        }

        PopupHandler.installPopupHandler(mongoTree, actionPopupGroup, "POPUP", ActionManager.getInstance());

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent event) {
                if (!(event.getSource() instanceof JTree)) {
                    return false;
                }

                MongoServer selectedMongoServer = getSelectedServer();
                if (selectedMongoServer != null) {
                    loadServerConfiguration(selectedMongoServer);
                    return true;
                }
                MongoCollection selectedCollection = getSelectedCollection();
                if (selectedCollection != null) {
                    loadSelectedCollectionValues(selectedCollection);
                    return true;
                }
                return false;
            }
        }.installOn(mongoTree);
    }

    private void expandAll() {
        mongoTreeBuilder.expandAll();
    }

    private void collapseAll() {
        mongoTreeBuilder.collapseAll();
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
        tree.setName("mongoTree");
        tree.setRootVisible(false);

        new TreeSpeedSearch(tree, treePath -> {
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

        return tree;
    }

    @Override
    public void dispose() {

    }

    public ServerConfiguration getConfiguration() {
        MongoCollection selectedCollection = getSelectedCollection();
        if (selectedCollection != null) {
            return selectedCollection.getParentDatabase().getParentServer().getConfiguration();
        }

        MongoDatabase selectedDatabase = getSelectedDatabase();
        if (selectedDatabase != null) {
            return selectedDatabase.getParentServer().getConfiguration();
        }

        MongoServer mongoServer = getSelectedServer();
        if (mongoServer != null) {
            return mongoServer.getConfiguration();
        }

        return null;
    }

    public Object getSelectedItem() {
        Set<Object> selectedElements = mongoTreeBuilder.getSelectedElements();
        if (selectedElements.isEmpty()) {
            return null;
        }
        return selectedElements.iterator().next();
    }

    public MongoServer getSelectedServer() {
        Set<MongoServer> selectedElements = mongoTreeBuilder.getSelectedElements(MongoServer.class);
        if (selectedElements.isEmpty()) {
            return null;
        }
        return selectedElements.iterator().next();
    }

    public MongoDatabase getSelectedDatabase() {
        Set<MongoDatabase> selectedElements = mongoTreeBuilder.getSelectedElements(MongoDatabase.class);
        if (selectedElements.isEmpty()) {
            return null;
        }
        return selectedElements.iterator().next();

    }

    public MongoCollection getSelectedCollection() {
        Set<MongoCollection> selectedElements = mongoTreeBuilder.getSelectedElements(MongoCollection.class);
        if (selectedElements.isEmpty()) {
            return null;
        }
        return selectedElements.iterator().next();
    }

    public void loadSelectedCollectionValues(MongoCollection mongoCollection) {
        Navigation navigation = new Navigation();
        navigation.addNewWayPoint(mongoCollection, new MongoQueryOptions());

        MongoServer parentServer = mongoCollection.getParentDatabase()
                .getParentServer();
        MongoFileSystem.getInstance().openEditor(
                new MongoObjectFile(project, parentServer.getConfiguration(), navigation));
    }

    public void removeSelectedServer() {
        MongoServer mongoServer = getSelectedServer();
        if (mongoServer == null) {
            return;
        }

        MongoConfiguration mongoConfiguration = MongoConfiguration.getInstance(project);
        mongoConfiguration.removeServerConfiguration(mongoServer.getConfiguration());

        mongoTreeBuilder.removeConfiguration(mongoServer);

    }

    public void dropCollection(MongoCollection mongoCollection) {
        ServerConfiguration configuration = mongoCollection.getParentDatabase().getParentServer().getConfiguration();
        mongoManager.dropCollection(configuration, mongoCollection);
        notifier.notifyInfo("Collection " + mongoCollection.getName() + " dropped");

        mongoTreeBuilder.removeCollection(mongoCollection);
    }

    public void dropDatabase(MongoDatabase mongoDatabase) {
        ServerConfiguration configuration = mongoDatabase.getParentServer().getConfiguration();
        mongoManager.dropDatabase(configuration, mongoDatabase);
        notifier.notifyInfo("Datatabase " + mongoDatabase.getName() + " dropped");

        mongoTreeBuilder.removeDatabase(mongoDatabase);
    }

    public MongoManager getMongoManager() {
        return mongoManager;
    }
}
