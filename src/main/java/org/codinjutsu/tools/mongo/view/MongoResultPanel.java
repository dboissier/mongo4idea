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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.CopyResultAction;
import org.codinjutsu.tools.mongo.view.action.EditMongoDocumentAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoResultDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

public class MongoResultPanel extends JPanel implements Disposable {

    private final Project project;
    private final MongoRunnerPanel.MongoDocumentOperations mongoDocumentOperations;
    private JPanel resultToolbar;
    private JPanel mainPanel;
    private JPanel containerPanel;
    private Splitter splitter;
    private JPanel resultTreePanel;
    private MongoEditionPanel mongoEditionPanel;

    JsonTreeTableView resultTableView;


    public MongoResultPanel(Project project, MongoRunnerPanel.MongoDocumentOperations mongoDocumentOperations) {
        this.project = project;
        this.mongoDocumentOperations = mongoDocumentOperations;
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        splitter = new Splitter(false, 0.6f);

        resultTreePanel = new JPanel(new BorderLayout());

        splitter.setFirstComponent(resultTreePanel);

        mongoEditionPanel = createMongoEditionPanel();

        containerPanel.setLayout(new BorderLayout());
        containerPanel.add(splitter);

        resultToolbar.setLayout(new BorderLayout());
        Disposer.register(project, this);
    }

    private MongoEditionPanel createMongoEditionPanel() {
        return new MongoEditionPanel().init(mongoDocumentOperations, new ActionCallback() {
            public void onOperationSuccess(String message) {
                hideEditionPanel();
                showNotification(MessageType.INFO, message);
            }

            @Override
            public void onOperationFailure(Exception exception) {
                showNotification(MessageType.ERROR, exception.getMessage());
            }
        });
    }

    private void showNotification(final MessageType info, final String message) {
        GuiUtils.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                ToolWindowManager.getInstance(project).notifyByBalloon(MongoWindowManager.MONGO_RUNNER, info, message);
            }
        });
    }

    public void updateResultTableTree(MongoCollectionResult mongoCollectionResult) {
        resultTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree(mongoCollectionResult), JsonTreeTableView.COLUMNS_FOR_READING);
        resultTableView.setName("resultTreeTable");

        resultTableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && MongoResultPanel.this.isSelectedNodeObjectId()) {
                    MongoResultPanel.this.editSelectedMongoDocument();
                }
            }
        });

        buildPopupMenu();

        resultTreePanel.invalidate();
        resultTreePanel.removeAll();
        resultTreePanel.add(new JBScrollPane(resultTableView));
        resultTreePanel.validate();
    }

    void buildPopupMenu() {
        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoResultPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(new EditMongoDocumentAction(this));
        }

        PopupHandler.installPopupHandler(resultTableView, actionPopupGroup, "POPUP", ActionManager.getInstance());
    }


    public void editSelectedMongoDocument() {

        DBObject mongoDocument = getSelectedMongoDocument();

        if (mongoDocument == null) {
            return;
        }

        mongoEditionPanel.updateEditionTree(mongoDocument);

        splitter.setSecondComponent(mongoEditionPanel);
    }

    private DBObject getSelectedMongoDocument() {
        TreeTableTree tree = resultTableView.getTree();
        JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return null;
        }

        MongoNodeDescriptor descriptor = treeNode.getDescriptor();
        Object value = descriptor.getValue();
        if (value instanceof ObjectId) {
            return mongoDocumentOperations.getMongoDocument((ObjectId) value);
        }

        return null;
    }

    public boolean isSelectedNodeObjectId() {
        TreeTableTree tree = resultTableView.getTree();
        JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return false;
        }

        Object value = treeNode.getDescriptor().getValue();

        return value instanceof ObjectId;
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

    private void hideEditionPanel() {
        splitter.setSecondComponent(null);
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
        mongoEditionPanel.dispose();
    }

    public interface ActionCallback {

        void onOperationSuccess(String message);

        void onOperationFailure(Exception exception);
    }
}
