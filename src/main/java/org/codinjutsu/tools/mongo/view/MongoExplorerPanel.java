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
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.PopupHandler;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.utils.GuiUtil;
import org.codinjutsu.tools.mongo.view.action.ViewCollectionValuesAction;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class MongoExplorerPanel extends JPanel implements Disposable {


    private JTree mongoTree;
    private JPanel rootPanel;
    private JPanel toolBarPanel;
    private final MongoManager mongoManager;
    private final MongoConfiguration configuration;

    public MongoExplorerPanel(MongoManager mongoManager, MongoConfiguration configuration) {
        this.mongoManager = mongoManager;
        this.configuration = configuration;
        mongoTree.setCellRenderer(new MongoTreeRenderer());
        mongoTree.setName("mongoTree");

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        init();
    }

    private void init() {
        MongoServer mongoServer = mongoManager.loadDatabaseCollections(configuration);
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(mongoServer);
        if (mongoServer.hasDatabases()) {
            for (MongoDatabase mongoDatabase : mongoServer.getDatabases()) {
                DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(mongoDatabase);
                for (MongoCollection collection : mongoDatabase.getCollections()) {
                    databaseNode.add(new DefaultMutableTreeNode(collection));
                }
                rootNode.add(databaseNode);
            }
        }
        mongoTree.setModel(new DefaultTreeModel(rootNode));
    }


    public void installActionsOnToolbar(ViewCollectionValuesAction action) {

        DefaultActionGroup actionGroup = new DefaultActionGroup("MongoExplorerGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(action);
            actionGroup.addSeparator();
        }
        GuiUtil.installActionGroupInToolBar(actionGroup, toolBarPanel, ActionManager.getInstance(), "MongoExplorerActions");
        installActionGroupInPopupMenu(actionGroup, mongoTree, ActionManager.getInstance());
    }


    private static void installActionGroupInPopupMenu(ActionGroup group,
                                                      JComponent component,
                                                      ActionManager actionManager) {
        if (actionManager == null) {
            return;
        }
        PopupHandler.installPopupHandler(component, group, "POPUP", actionManager);
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

    public MongoCollectionResult getSelectedCollectionValues() {
        MongoCollection selectedCollection = getSelectedCollection();
        if (selectedCollection == null) {
            return null;
        }

        return mongoManager.loadCollectionValues(configuration, selectedCollection);
    }
}
