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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.EditMongoDocumentAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
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

    private final Project project;
    private final MongoPanel.MongoDocumentOperations mongoDocumentOperations;
    private JPanel mainPanel;
    private JPanel containerPanel;
    private JPanel toolbar;
    private Splitter splitter;
    private JPanel resultTreePanel;
    private MongoEditionPanel mongoEditionPanel;

    JsonTreeTableView resultTableView;


    public MongoResultPanel(Project project, MongoPanel.MongoDocumentOperations mongoDocumentOperations) {
        this.project = project;
        this.mongoDocumentOperations = mongoDocumentOperations;
        toolbar.setLayout(new BorderLayout());
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
                JBPopupFactory.getInstance().createBalloonBuilder(new JLabel(message))
                        .setFillColor(info.getPopupBackground())
                        .createBalloon()
                        .show(new RelativePoint(MongoResultPanel.this.resultTreePanel, new Point(0, 0)), Balloon.Position.above);
            }
        });
    }

    public void updateResultTableTree(MongoCollectionResult mongoCollectionResult) {
        resultTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree(mongoCollectionResult), JsonTreeTableView.COLUMNS_FOR_READING);
        resultTableView.setName("resultTreeTable");

        resultTableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && MongoResultPanel.this.isSelectedNodeId()) {
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
        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            if (StringUtils.equals(keyValueDescriptor.getKey(), "_id")) {
                return mongoDocumentOperations.getMongoDocument(keyValueDescriptor.getValue());
            }
        }

        return null;
    }

    public boolean isSelectedNodeId() {
        TreeTableTree tree = resultTableView.getTree();
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
        TreeUtil.expandAll(resultTableView.getTree());
    }

    void collapseAll() {
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

    public JPanel getToolbar() {
        return toolbar;
    }

    public interface ActionCallback {

        void onOperationSuccess(String message);

        void onOperationFailure(Exception exception);
    }
}
