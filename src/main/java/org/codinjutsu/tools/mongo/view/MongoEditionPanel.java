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
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.codinjutsu.tools.mongo.view.action.edition.AddKeyAction;
import org.codinjutsu.tools.mongo.view.action.edition.AddValueAction;
import org.codinjutsu.tools.mongo.view.action.edition.DeleteKeyAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.model.JsonTreeUtils;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoValueDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class MongoEditionPanel extends JPanel implements Disposable {
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel editionTreePanel;
    private JPanel mainPanel;
    private JButton deleteButton;

    private JsonTreeTableView editTableView;


    public MongoEditionPanel() {
        super(new BorderLayout());

        add(mainPanel);
        editionTreePanel.setLayout(new BorderLayout());

        saveButton.setName("saveButton");
        cancelButton.setName("cancelButton");
        deleteButton.setName("deleteButton");
    }

    public MongoEditionPanel init(final MongoPanel.MongoDocumentOperations mongoDocumentOperations, final MongoResultPanel.ActionCallback actionCallback) {

        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                actionCallback.onOperationCancelled("Modification canceled...");
            }
        });

        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Document mongoDocument = buildMongoDocument();
                    mongoDocumentOperations.updateMongoDocument(mongoDocument);
                    actionCallback.onOperationSuccess("Document " + mongoDocument.toString() + " saved...");

                } catch (Exception exception) {
                    actionCallback.onOperationFailure(exception);
                }
            }
        });

        deleteButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Object documentId = getDocumentId();
                    mongoDocumentOperations.deleteMongoDocument(documentId);
                    actionCallback.onOperationSuccess("Document with _id=" + documentId + " deleted...");
                } catch (Exception exception) {
                    actionCallback.onOperationFailure(exception);
                }
            }
        });

        return this;
    }

    public void updateEditionTree(Document mongoDocument) {
        String panelTitle = "New document";
        if (mongoDocument != null) {
            panelTitle = "Edition";
        }

        mainPanel.setBorder(IdeBorderFactory.createTitledBorder(panelTitle, true));
        editTableView = new JsonTreeTableView(JsonTreeUtils.buildJsonTree(mongoDocument), JsonTreeTableView.COLUMNS_FOR_WRITING);
        editTableView.setName("editionTreeTable");

        editionTreePanel.invalidate();
        editionTreePanel.removeAll();
        editionTreePanel.add(new JBScrollPane(editTableView));
        editionTreePanel.validate();

        buildPopupMenu();
    }

    void buildPopupMenu() {
        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoEditorPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(new AddKeyAction(this));
            actionPopupGroup.add(new AddValueAction(this));
            actionPopupGroup.add(new DeleteKeyAction(this));
        }

        PopupHandler.installPopupHandler(editTableView, actionPopupGroup, "POPUP", ActionManager.getInstance());
    }

    public boolean containsKey(String key) {
        JsonTreeNode parentNode = getParentNode();
        if (parentNode == null) {
            return false;
        }

        Enumeration children = parentNode.children();
        while (children.hasMoreElements()) {
            JsonTreeNode childNode = (JsonTreeNode) children.nextElement();
            MongoNodeDescriptor descriptor = childNode.getDescriptor();
            if (descriptor instanceof MongoKeyValueDescriptor) {
                MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
                if (StringUtils.equals(key, keyValueDescriptor.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addKey(String key, Object value) {

        List<TreeNode> node = new LinkedList<>();
        JsonTreeNode treeNode = new JsonTreeNode(MongoKeyValueDescriptor.createDescriptor(key, value));

        if (value instanceof Document) {
            JsonTreeUtils.processDocument(treeNode, (Document) value);
        }

        node.add(treeNode);

        DefaultTreeModel treeModel = (DefaultTreeModel) editTableView.getTree().getModel();
        JsonTreeNode parentNode = getParentNode();
        if (parentNode == null) {
            parentNode = (JsonTreeNode) treeModel.getRoot();
        }
        TreeUtil.addChildrenTo(parentNode, node);
        treeModel.reload(parentNode);
    }

    public void addValue(Object value) {
        List<TreeNode> node = new LinkedList<>();

        JsonTreeNode parentNode = getParentNode();

        JsonTreeNode treeNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(parentNode.getChildCount(), value));
        if (value instanceof Document) {
            JsonTreeUtils.processDocument(treeNode, (Document) value);
        }

        node.add(treeNode);

        DefaultTreeModel treeModel = (DefaultTreeModel) editTableView.getTree().getModel();
        TreeUtil.addChildrenTo(parentNode, node);
        treeModel.reload(parentNode);
    }

    private JsonTreeNode getParentNode() {
        JsonTreeNode lastPathComponent = getSelectedNode();
        if (lastPathComponent == null) {
            return null;
        }
        return (JsonTreeNode) lastPathComponent.getParent();
    }

    public JsonTreeNode getSelectedNode() {
        return (JsonTreeNode) editTableView.getTree().getLastSelectedPathComponent();
    }

    public boolean canAddKey() {
        JsonTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null) {
            return false;
        }
        return selectedNode.getDescriptor() instanceof MongoKeyValueDescriptor;
    }

    public boolean canAddValue() {
        JsonTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null) {
            return false;
        }
        return selectedNode.getDescriptor() instanceof MongoValueDescriptor;
    }

    public void removeSelectedKey() {
        JsonTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null) {
            return;
        }
        TreeUtil.removeSelected(editTableView.getTree());

    }

    private Document buildMongoDocument() {
        JsonTreeNode rootNode = (JsonTreeNode) editTableView.getTree().getModel().getRoot();
        return JsonTreeUtils.buildDocumentObject(rootNode);
    }

    @Override
    public void dispose() {
        editTableView = null;
    }

    private Object getDocumentId() {
        JsonTreeNode rootNode = (JsonTreeNode) editTableView.getTree().getModel().getRoot();

        return findObjectIdNodeDescriptor(rootNode).getDescriptor().getValue();
    }

    private JsonTreeNode findObjectIdNodeDescriptor(JsonTreeNode rootNode) {
        return ((JsonTreeNode) rootNode.getChildAt(0));//TODO crappy
    }
}
