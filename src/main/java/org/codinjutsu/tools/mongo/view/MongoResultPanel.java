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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.CopyResultAction;
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

public class MongoResultPanel extends JPanel implements Disposable {

    private JPanel resultToolbar;
    private JPanel mainPanel;
    private JPanel treePanel;

    private boolean sortByKey = false;

    private JsonTreeView jsonResultTree = new JsonTreeView();

    public MongoResultPanel(Project project) {
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        treePanel.setLayout(new BorderLayout());
        treePanel.add(new JBScrollPane(jsonResultTree));

        resultToolbar.setLayout(new BorderLayout());
        Disposer.register(project, this);
    }


    public void setSortedByKey(boolean sortedByKey) {
        sortByKey = sortedByKey;

        jsonResultTree.invalidate();
        ((DefaultTreeModel) jsonResultTree.getModel()).reload();
        jsonResultTree.repaint();
        jsonResultTree.validate();
    }


    public void updateResultTree(MongoCollectionResult mongoCollectionResult) {
        jsonResultTree.invalidate();
        jsonResultTree.setVisible(true);
        JsonTreeModel jsonTreeModel = new JsonTreeModel(mongoCollectionResult);
        jsonTreeModel.setMongoComparator(new MongoKeyComparator());
        jsonResultTree.setModel(jsonTreeModel);
        jsonResultTree.validate();
    }


    public void installActions() {
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("MongoResultGroup", true);
        actionResultGroup.add(new SortResultsByKeysAction(this));
        actionResultGroup.addSeparator();
        actionResultGroup.add(new CopyResultAction(this));

        GuiUtils.installActionGroupInToolBar(actionResultGroup, resultToolbar, ActionManager.getInstance(), "MongoQueryGroupActions", false);

        installActionGroupInPopupMenu(actionResultGroup, jsonResultTree, ActionManager.getInstance());
    }

    public void shouldShowTreeResult(boolean visible) {
        jsonResultTree.setVisible(visible);
    }

    private static void installActionGroupInPopupMenu(ActionGroup group,
                                                      JComponent component,
                                                      ActionManager actionManager) {
        if (actionManager == null) {
            return;
        }
        PopupHandler.installPopupHandler(component, group, "POPUP", actionManager);
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

    public boolean isNotEmpty() {
        return jsonResultTree.isVisible();
    }

    @Override
    public void dispose() {
        jsonResultTree = null;
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
