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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.codinjutsu.tools.mongo.logic.Notifier;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.CopyResultAction;
import org.codinjutsu.tools.mongo.view.action.EditMongoDocumentAction;
import org.codinjutsu.tools.mongo.view.model.JsonTableUtils;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.model.JsonTreeUtils;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
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

    private final MongoPanel.MongoDocumentOperations mongoDocumentOperations;
    private final Notifier notifier;
    private JPanel mainPanel;
    private JPanel containerPanel;
    private final Splitter splitter;
    private final JPanel resultTreePanel;
    private final MongoEditionPanel mongoEditionPanel;

    JsonTreeTableView resultTreeTableView;

    JsonTableView resultTableView;

    private ViewMode currentViewMode = ViewMode.TREE;


    public MongoResultPanel(Project project, MongoPanel.MongoDocumentOperations mongoDocumentOperations, Notifier notifier) {
        this.mongoDocumentOperations = mongoDocumentOperations;
        this.notifier = notifier;
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        splitter = new Splitter(true, 0.6f);

        resultTreePanel = new JPanel(new BorderLayout());

        splitter.setFirstComponent(resultTreePanel);

        mongoEditionPanel = createMongoEditionPanel();

        containerPanel.setLayout(new BorderLayout());
        containerPanel.add(splitter);

        Disposer.register(project, this);
    }

    private MongoEditionPanel createMongoEditionPanel() {
        return new MongoEditionPanel().init(mongoDocumentOperations, new ActionCallback() {
            public void onOperationSuccess(String message) {
                hideEditionPanel();
                notifier.notifyInfo(message);
                GuiUtils.showNotification(MongoResultPanel.this.resultTreePanel, MessageType.INFO, message, Balloon.Position.above);
            }

            @Override
            public void onOperationFailure(Exception exception) {
                notifier.notifyError(exception.getMessage());
                GuiUtils.showNotification(MongoResultPanel.this.resultTreePanel, MessageType.ERROR, exception.getMessage(), Balloon.Position.above);
            }

            @Override
            public void onOperationCancelled(String message) {
                hideEditionPanel();
            }
        });
    }

    void updateResultView(MongoCollectionResult mongoCollectionResult) {
        if (ViewMode.TREE.equals(currentViewMode)) {
            updateResultTreeTable(mongoCollectionResult);
        } else {
            updateResultTable(mongoCollectionResult);
        }
    }

    private void updateResultTreeTable(MongoCollectionResult mongoCollectionResult) {
        resultTreeTableView = new JsonTreeTableView(JsonTreeUtils.buildJsonTree(mongoCollectionResult), JsonTreeTableView.COLUMNS_FOR_READING);
        resultTreeTableView.setName("resultTreeTable");

        resultTreeTableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && MongoResultPanel.this.isSelectedNodeId()) {
                    MongoResultPanel.this.editSelectedMongoDocument();
                }
            }
        });

        buildPopupMenu();

        displayResult(resultTreeTableView);
    }

    private void updateResultTable(MongoCollectionResult mongoCollectionResult) {
        resultTableView = new JsonTableView(JsonTableUtils.buildJsonTable(mongoCollectionResult));
        displayResult(resultTableView);
    }

    private void displayResult(JComponent tableView) {
        resultTreePanel.invalidate();
        resultTreePanel.removeAll();
        resultTreePanel.add(new JBScrollPane(tableView));
        resultTreePanel.validate();
    }

    void buildPopupMenu() {
        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoResultPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(new EditMongoDocumentAction(this));
            actionPopupGroup.add(new CopyResultAction(this));
        }

        PopupHandler.installPopupHandler(resultTreeTableView, actionPopupGroup, "POPUP", ActionManager.getInstance());
    }


    public void editSelectedMongoDocument() {

        Document mongoDocument = getSelectedMongoDocument();

        if (mongoDocument == null) {
            return;
        }

        mongoEditionPanel.updateEditionTree(mongoDocument);

        splitter.setSecondComponent(mongoEditionPanel);
    }


    public void addMongoDocument() {
        mongoEditionPanel.updateEditionTree(null);
        splitter.setSecondComponent(mongoEditionPanel);
    }

    private Document getSelectedMongoDocument() {
        TreeTableTree tree = resultTreeTableView.getTree();
        JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return null;
        }

        MongoNodeDescriptor descriptor = treeNode.getDescriptor();
        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            if (StringUtils.equals(keyValueDescriptor.getKey(), "_id")) {
                return mongoDocumentOperations.getMongoDocument(keyValueDescriptor.getValue());
            }
        }

        return null;
    }


    public boolean isSelectedNodeId() {
        if (resultTreeTableView == null) {
            return false;
        }
        TreeTableTree tree = resultTreeTableView.getTree();
        JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return false;
        }

        MongoNodeDescriptor descriptor = treeNode.getDescriptor();
        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            return StringUtils.equals(keyValueDescriptor.getKey(), "_id");
        }

        return false;
    }


    void expandAll() {
        TreeUtil.expandAll(resultTreeTableView.getTree());
    }

    void collapseAll() {
        TreeTableTree tree = resultTreeTableView.getTree();
        TreeUtil.collapseAll(tree, 1);
    }

    public String getSelectedNodeStringifiedValue() {
        JsonTreeNode lastSelectedResultNode = (JsonTreeNode) resultTreeTableView.getTree().getLastSelectedPathComponent();
        if (lastSelectedResultNode == null) {
            lastSelectedResultNode = (JsonTreeNode) resultTreeTableView.getTree().getModel().getRoot();
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
        List<Object> stringifiedObjects = new LinkedList<>();
        for (int i = 0; i < selectedResultNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedResultNode.getChildAt(i);
            stringifiedObjects.add(childNode.getUserObject());
        }

        return String.format("[ %s ]", StringUtils.join(stringifiedObjects, ", "));
    }

    @Override
    public void dispose() {
        resultTreeTableView = null;
        mongoEditionPanel.dispose();
    }

    void setCurrentViewMode(ViewMode viewMode) {
        this.currentViewMode = viewMode;
    }

    ViewMode getCurrentViewMode() {
        return currentViewMode;
    }

    interface ActionCallback {

        void onOperationSuccess(String message);

        void onOperationFailure(Exception exception);

        void onOperationCancelled(String message);
    }

    public enum ViewMode {
        TREE, TABLE
    }
}
