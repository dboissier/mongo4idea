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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.tree.TreeUtil;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtil;
import org.codinjutsu.tools.mongo.view.action.SortResultsByKeysAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.MongoComparator;
import org.codinjutsu.tools.mongo.view.model.nodedescriptor.MongoKeyValueDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class MongoRunnerPanel extends JPanel {
    private JPanel rootPanel;
    private JPanel toolBarPanel;
    private JPanel resultPanel;
    private final JsonTreeView jsonResultTree = new JsonTreeView();

    private boolean sortByKey = false;

    public MongoRunnerPanel() {
        resultPanel.setLayout(new BorderLayout());
        resultPanel.add(new JScrollPane(jsonResultTree), BorderLayout.CENTER);

        toolBarPanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());
        add(rootPanel);

        jsonResultTree.setVisible(false);
    }

    public void installActions() {

        DefaultActionGroup actionGroup = new DefaultActionGroup("MongoResultsGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new SortResultsByKeysAction(this));
            actionGroup.addSeparator();
        }
        GuiUtil.installActionGroupInToolBar(actionGroup, toolBarPanel, ActionManager.getInstance(), "MongoResultsActions");
        TreeUtil.installActions(jsonResultTree);
    }

    public void showResults(MongoCollectionResult mongoCollectionResult) {
        jsonResultTree.invalidate();
        jsonResultTree.setVisible(true);
        JsonTreeModel jsonTreeModel = new JsonTreeModel(mongoCollectionResult);
        jsonTreeModel.setMongoComparator(new MongoKeyComparator());
        jsonResultTree.setModel(jsonTreeModel);
        jsonResultTree.validate();
    }

    public void setSortedByKey(boolean sortedByKey) {
        sortByKey = sortedByKey;
        jsonResultTree.invalidate();
        ((DefaultTreeModel) jsonResultTree.getModel()).reload();
        jsonResultTree.repaint();
        jsonResultTree.validate();
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
