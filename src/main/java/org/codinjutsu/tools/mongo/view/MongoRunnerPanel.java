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

import javax.swing.*;
import java.awt.*;

public class MongoRunnerPanel extends JPanel {
    private JPanel rootPanel;
    private JPanel toolBarPanel;
    private JPanel resultPanel;
    private final JsonTreeView jsonResultTree = new JsonTreeView();

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
        jsonResultTree.setModel(new JsonTreeModel(mongoCollectionResult));
        jsonResultTree.validate();
    }
}
