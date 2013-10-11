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

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.CopyResultAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoResultDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class MongoResultPanel extends JPanel implements Disposable {

    private final MongoRunnerPanel.UpdateCallback updateCallback;
    private JPanel resultToolbar;
    private JPanel mainPanel;
    private JPanel treePanel;
    private JPanel editionPanel;

    JsonTreeTableView resultTableView;
    JsonTreeTableView editTableView;

    public MongoResultPanel(Project project, MongoRunnerPanel.UpdateCallback updateCallback) {
        this.updateCallback = updateCallback;
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        treePanel.setLayout(new BorderLayout());

        resultToolbar.setLayout(new BorderLayout());
        Disposer.register(project, this);
    }


    public void updateResultTableTree(MongoCollectionResult mongoCollectionResult) {
        resultTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree(mongoCollectionResult), JsonTreeTableView.COLUMNS_FOR_READING);
        resultTableView.setName("resultTreeTable");
        treePanel.invalidate();
        treePanel.removeAll();
        treePanel.add(new JBScrollPane(resultTableView));

        treePanel.validate();
    }

    public void editMongoDocument(DBObject mongoDocument) {
        editTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree("", mongoDocument), JsonTreeTableView.COLUMNS_FOR_READING);

    }

    public void installActions() {
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("MongoResultGroup", true);
        actionResultGroup.add(new CopyResultAction(this));

        final TreeExpander treeExpander = new TreeExpander() {
            @Override
            public void expandAll() {
                MongoResultPanel.this.expandAll();
            }

            @Override
            public boolean canExpand() {
                return true;
            }

            @Override
            public void collapseAll() {
                MongoResultPanel.this.collapseAll();
            }

            @Override
            public boolean canCollapse() {
                return true;
            }
        };

        CommonActionsManager actionsManager = CommonActionsManager.getInstance();

        final AnAction expandAllAction = actionsManager.createExpandAllAction(treeExpander, mainPanel);
        final AnAction collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, mainPanel);

        Disposer.register(this, new Disposable() {
            @Override
            public void dispose() {
                collapseAllAction.unregisterCustomShortcutSet(mainPanel);
                expandAllAction.unregisterCustomShortcutSet(mainPanel);
            }
        });

        actionResultGroup.add(expandAllAction);
        actionResultGroup.add(collapseAllAction);

        GuiUtils.installActionGroupInToolBar(actionResultGroup, resultToolbar, ActionManager.getInstance(), "MongoQueryGroupActions", false);
    }

    private void expandAll() {
        TreeUtil.expandAll(resultTableView.getTree());
    }

    private void collapseAll() {
        TreeTableTree tree = resultTableView.getTree();
        TreeUtil.collapseAll(tree, 1);
    }

    public String getSelectedNodeStringifiedValue() {
        JsonTreeNode lastSelectedResultNode = (JsonTreeNode) resultTableView.getTree().getLastSelectedPathComponent();
        if (lastSelectedResultNode == null) {
            lastSelectedResultNode = (JsonTreeNode) resultTableView.getTree().getModel().getRoot();
        }
        MongoNodeDescriptor userObject = lastSelectedResultNode.getDescriptor();
        if (userObject instanceof MongoResultDescriptor) {
            return stringifyResult(lastSelectedResultNode);
        }

        return userObject.toString();
    }


    private String stringifyResult(DefaultMutableTreeNode selectedResultNode) {
        List<Object> stringifiedObjects = new LinkedList<Object>();
        for (int i = 0; i < selectedResultNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedResultNode.getChildAt(i);
            stringifiedObjects.add(childNode.getUserObject());
        }

        return String.format("[ %s ]", StringUtils.join(stringifiedObjects, " , "));
    }

    @Override
    public void dispose() {
        resultTableView = null;
        editTableView = null;
    }
}
