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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.PopupHandler;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.CopyResultAction;
import org.codinjutsu.tools.mongo.view.action.RerunQuery;
import org.codinjutsu.tools.mongo.view.action.SortResultsByKeysAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.MongoComparator;
import org.codinjutsu.tools.mongo.view.model.ResultNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class MongoRunnerPanel extends JPanel {

    private JPanel rootPanel;
    private JPanel toolBarPanel;
    private Splitter splitter;
    private final JsonTreeView jsonResultTree = new JsonTreeView();
    private QueryPanel queryPanel;

    private boolean sortByKey = false;
    private final MongoConfiguration configuration;
    private final MongoManager mongoManager;
    private MongoCollection currentMongoCollection;

    public MongoRunnerPanel(MongoConfiguration configuration, MongoManager mongoManager) {
        this.configuration = configuration;
        this.mongoManager = mongoManager;

        queryPanel = createQueryPanel();
        splitter.setFirstComponent(queryPanel);
        splitter.setSecondComponent(new JScrollPane(jsonResultTree));
        splitter.setProportion(0.30f);


        toolBarPanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());
        add(rootPanel);

        jsonResultTree.setVisible(false);
    }

    protected QueryPanel createQueryPanel() {
        return QueryPanel.queryPanel();
    }

    public void installActions() {

        DefaultActionGroup actionGroup = new DefaultActionGroup("MongoResultsGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new RerunQuery(this));
            actionGroup.addSeparator();
            actionGroup.add(new CopyResultAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new SortResultsByKeysAction(this));
        }
        GuiUtils.installActionGroupInToolBar(actionGroup, toolBarPanel, ActionManager.getInstance(), "MongoResultsActions");
//        TreeUtil.installActions(jsonResultTree);
        installActionGroupInPopupMenu(actionGroup, jsonResultTree, ActionManager.getInstance());

    }

    private static void installActionGroupInPopupMenu(ActionGroup group,
                                                      JComponent component,
                                                      ActionManager actionManager) {
        if (actionManager == null) {
            return;
        }
        PopupHandler.installPopupHandler(component, group, "POPUP", actionManager);
    }

    public void setSortedByKey(boolean sortedByKey) {
        sortByKey = sortedByKey;

        jsonResultTree.invalidate();
        ((DefaultTreeModel) jsonResultTree.getModel()).reload();
        jsonResultTree.repaint();
        jsonResultTree.validate();
    }

    public void showResults(MongoCollection mongoCollection) {
        currentMongoCollection = mongoCollection;
        updateResultTree(mongoManager.loadCollectionValues(configuration, currentMongoCollection));
    }

    public void reRunQuery() {
        try {
            MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(configuration, currentMongoCollection, queryPanel.getQueryOptions());
            updateResultTree(mongoCollectionResult);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void updateResultTree(MongoCollectionResult mongoCollectionResult) {
        jsonResultTree.invalidate();
        jsonResultTree.setVisible(true);
        JsonTreeModel jsonTreeModel = new JsonTreeModel(mongoCollectionResult);
        jsonTreeModel.setMongoComparator(new MongoKeyComparator());
        jsonResultTree.setModel(jsonTreeModel);
        jsonResultTree.validate();
    }

    public String getSelectedNodeStringifiedValue() {
        DefaultMutableTreeNode lastSelectedResultNode = (DefaultMutableTreeNode) jsonResultTree.getLastSelectedPathComponent();
        if (lastSelectedResultNode == null) {
            return null;
        }
        Object userObject = lastSelectedResultNode.getUserObject();
        if (userObject instanceof MongoNodeDescriptor) {
            return userObject.toString();
        } else if (userObject instanceof ResultNode) {
            return stringifyResult(lastSelectedResultNode);
        }
        return null;
    }

    private String stringifyResult(DefaultMutableTreeNode selectedResultNode) {
        List<Object> stringifiedObjects = new LinkedList<Object>();
        for (int i = 0; i < selectedResultNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedResultNode.getChildAt(i);
            stringifiedObjects.add(childNode.getUserObject());
        }

        return String.format("[ %s ]", StringUtils.join(stringifiedObjects, " , "));
    }

    private class MongoKeyComparator implements MongoComparator {
        @Override
        public boolean isApplicable() {
            return sortByKey;
        }

        @Override
        public int compare(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2) {
            Object userObjectNode1 = node1.getUserObject();
            Object userObjectNode2 = node2.getUserObject();
            if (userObjectNode1 instanceof MongoKeyValueDescriptor) {
                MongoKeyValueDescriptor mongoKeyValueDescriptorNode1 = (MongoKeyValueDescriptor) userObjectNode1;
                if (userObjectNode2 instanceof MongoKeyValueDescriptor) {
                    MongoKeyValueDescriptor mongoKeyValueDescriptorNode2 = (MongoKeyValueDescriptor) userObjectNode2;

                    return mongoKeyValueDescriptorNode1.getKey().compareTo(mongoKeyValueDescriptorNode2.getKey());
                }

                return 1;
            }

            if (userObjectNode2 instanceof MongoKeyValueDescriptor) {
                return -1;
            }

            return 0;
        }
    }
}
