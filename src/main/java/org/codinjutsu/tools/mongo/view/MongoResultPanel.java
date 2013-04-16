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
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.CopyResultAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.model.MongoComparator;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoResultDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class MongoResultPanel extends JPanel implements Disposable {

    private final MongoComparator resultComparator = new MongoKeyComparator();

    private JPanel resultToolbar;
    private JPanel mainPanel;
    private JPanel treePanel;

    private boolean sortByKey = false;

    private JsonTreeTableView jsonTreeTableView;

    public MongoResultPanel(Project project) {
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        treePanel.setLayout(new BorderLayout());

        resultToolbar.setLayout(new BorderLayout());
        Disposer.register(project, this);
    }


    public void setSortedByKey(boolean sortedByKey) {
        sortByKey = sortedByKey;

        TreeUtil.sort((DefaultTreeModel) jsonTreeTableView.getTree().getModel(), resultComparator);
    }


    public void updateResultTableTree(MongoCollectionResult mongoCollectionResult) {
        jsonTreeTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree(mongoCollectionResult));
        jsonTreeTableView.setName("treeTable");
        treePanel.invalidate();
        treePanel.removeAll();
        treePanel.add(new JBScrollPane(jsonTreeTableView));

        treePanel.validate();
    }


    public void installActions() {
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("MongoResultGroup", true);
//        actionResultGroup.add(new SortResultsByKeysAction(this));
//        actionResultGroup.addSeparator();
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
        TreeUtil.expandAll(jsonTreeTableView.getTree());
    }

    private void collapseAll() {
        TreeTableTree tree = jsonTreeTableView.getTree();
        TreeUtil.collapseAll(tree, 1);
    }

    public String getSelectedNodeStringifiedValue() {
        JsonTreeNode lastSelectedResultNode = (JsonTreeNode) jsonTreeTableView.getTree().getLastSelectedPathComponent();
        if (lastSelectedResultNode == null) {
            lastSelectedResultNode = (JsonTreeNode) jsonTreeTableView.getTree().getModel().getRoot();
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
        jsonTreeTableView = null;
    }


    private class MongoKeyComparator implements MongoComparator {
        @Override
        public boolean isApplicable() {
            return sortByKey;
        }

        @Override
        public int compare(JsonTreeNode node1, JsonTreeNode node2) {
            MongoNodeDescriptor descriptorNode1 = node1.getDescriptor();
            MongoNodeDescriptor descriptorNode2 = node2.getDescriptor();
            if (descriptorNode1 instanceof MongoKeyValueDescriptor) {
                MongoKeyValueDescriptor mongoKeyValueDescriptorNode1 = (MongoKeyValueDescriptor) descriptorNode1;
                if (descriptorNode2 instanceof MongoKeyValueDescriptor) {
                    MongoKeyValueDescriptor mongoKeyValueDescriptorNode2 = (MongoKeyValueDescriptor) descriptorNode2;

                    return mongoKeyValueDescriptorNode1.getKey().compareTo(mongoKeyValueDescriptorNode2.getKey());
                }

                return 1;
            }

            if (descriptorNode2 instanceof MongoKeyValueDescriptor) {
                return -1;
            }

            return 0;
        }
    }
}
