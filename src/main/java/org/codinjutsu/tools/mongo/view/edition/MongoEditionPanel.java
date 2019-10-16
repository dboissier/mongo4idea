/*
 * Copyright (c) 2018 David Boissier.
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

package org.codinjutsu.tools.mongo.view.edition;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.codinjutsu.tools.mongo.view.JsonTreeTableView;
import org.codinjutsu.tools.mongo.view.MongoPanel;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;
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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import static org.codinjutsu.tools.mongo.utils.MongoUtils.DOCUMENT_CODEC;

public class MongoEditionPanel extends JPanel {
    private final MongoPanel.MongoDocumentOperations mongoDocumentOperations;
    private final MongoResultPanel.ActionCallback actionCallback;

    private JsonTreeTableView editTableView;


    MongoEditionPanel(final MongoPanel.MongoDocumentOperations mongoDocumentOperations, final MongoResultPanel.ActionCallback actionCallback) {
        super(new BorderLayout());
        this.mongoDocumentOperations = mongoDocumentOperations;
        this.actionCallback = actionCallback;
    }

    public boolean save() {
        try {
            Document mongoDocument = buildMongoDocument();
            mongoDocumentOperations.updateMongoDocument(mongoDocument);
            actionCallback.onOperationSuccess("Document saved", "Document " +
                    mongoDocument.toJson(DOCUMENT_CODEC) + " saved.");
            return true;

        } catch (Exception exception) {
            actionCallback.onOperationFailure(exception);
            return false;
        }
    }

    public void updateEditionTree(Document mongoDocument) {
        editTableView = new JsonTreeTableView(JsonTreeUtils.buildJsonTree(mongoDocument), JsonTreeTableView.COLUMNS_FOR_WRITING);
        editTableView.setName("editionTreeTable");

        TreeUtil.expand(editTableView.getTree(), 2);

        add(new JBScrollPane(editTableView), BorderLayout.CENTER);

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

    boolean containsKey(String key) {
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
        } else if (value instanceof List) {
            JsonTreeUtils.processObjectList(treeNode, (List) value);
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

        JsonTreeNode currentSelectionNode = getSelectedNode();
        if (currentSelectionNode == null) {
            return;
        }

        JsonTreeNode nodeToAttach;
        if (doesKeyDescriptionHaveEmptyArrayValue(currentSelectionNode.getDescriptor())) {
            nodeToAttach = currentSelectionNode;
        } else {
            nodeToAttach = (JsonTreeNode) currentSelectionNode.getParent();
        }

        JsonTreeNode treeNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(nodeToAttach.getChildCount(), value));
        if (value instanceof Document) {
            JsonTreeUtils.processDocument(treeNode, (Document) value);
        }

        node.add(treeNode);

        DefaultTreeModel treeModel = (DefaultTreeModel) editTableView.getTree().getModel();
        TreeUtil.addChildrenTo(nodeToAttach, node);
        treeModel.reload(nodeToAttach);
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

    public boolean canAddValue() {
        JsonTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null) {
            return false;
        }
        MongoNodeDescriptor descriptor = selectedNode.getDescriptor();
        return descriptor instanceof MongoValueDescriptor ||
                doesKeyDescriptionHaveEmptyArrayValue(descriptor);
    }

    private boolean doesKeyDescriptionHaveEmptyArrayValue(MongoNodeDescriptor descriptor) {
        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            Object value = keyValueDescriptor.getValue();
            if (value instanceof List) {
                List list = (List) value;
                return list.isEmpty();
            }
            return false;
        }
        return false;

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
}
