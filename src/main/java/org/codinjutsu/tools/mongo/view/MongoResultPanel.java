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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBCardLayout;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBRef;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.logic.Notifier;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.edition.MongoEditionDialog;
import org.codinjutsu.tools.mongo.view.model.*;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MongoResultPanel extends JPanel implements Disposable {

    private final Project project;
    private final MongoPanel.MongoDocumentOperations mongoDocumentOperations;
    private final Notifier notifier;
    private JPanel mainPanel;
    private JPanel containerPanel;
    private final JPanel resultTreePanel;

    JsonTreeTableView resultTreeTableView;

    private ViewMode currentViewMode = ViewMode.TREE;
    private ActionCallback actionCallback;


    public MongoResultPanel(Project project, MongoPanel.MongoDocumentOperations mongoDocumentOperations, Notifier notifier) {
        this.project = project;
        this.mongoDocumentOperations = mongoDocumentOperations;
        this.notifier = notifier;
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        resultTreePanel = new JPanel(new BorderLayout());

        containerPanel.setLayout(new JBCardLayout());
        containerPanel.add(resultTreePanel);

        actionCallback = new ActionCallback() {
            public void onOperationSuccess(String shortMessage, String detailedMessage) {
                notifier.notifyInfo(detailedMessage);
            }

            @Override
            public void onOperationFailure(Exception exception) {
                notifier.notifyError(exception.getMessage());
            }
        };

        Disposer.register(project, this);
    }

    void updateResultView(MongoCollectionResult mongoCollectionResult, Pagination pagination) {
        if (ViewMode.TREE.equals(currentViewMode)) {
            updateResultTreeTable(mongoCollectionResult, pagination);
        } else {
            updateResultTable(mongoCollectionResult);
        }
    }

    private void updateResultTreeTable(MongoCollectionResult mongoCollectionResult, Pagination pagination) {
        resultTreeTableView = new JsonTreeTableView(JsonTreeUtils.buildJsonTree(mongoCollectionResult.getCollectionName(),
                extractDocuments(pagination, mongoCollectionResult.getDocuments()), pagination.getStartIndex()),
                JsonTreeTableView.COLUMNS_FOR_READING);
        resultTreeTableView.setName("resultTreeTable");

        displayResult(resultTreeTableView);
    }

    private static List<Document> extractDocuments(Pagination pagination, List<Document> documents) {
        if (NbPerPage.ALL.equals(pagination.getNbPerPage())) {
            return documents;
        }
        if (pagination.getNbDocumentsPerPage() >= documents.size()) {
            return documents;
        }

        int startIndex = pagination.getStartIndex();
        int endIndex = startIndex + pagination.getNbDocumentsPerPage();

        return IntStream.range(startIndex, endIndex)
                .mapToObj(documents::get)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void updateResultTable(MongoCollectionResult mongoCollectionResult) {
        displayResult(new JsonTableView(JsonTableUtils.buildJsonTable(mongoCollectionResult)));
    }

    private void displayResult(JComponent tableView) {
        resultTreePanel.invalidate();
        resultTreePanel.removeAll();
        resultTreePanel.add(new JBScrollPane(tableView));
        resultTreePanel.validate();
    }


    public void editSelectedMongoDocument() {
        Document mongoDocument = getSelectedMongoDocument();
        if (mongoDocument == null) {
            return;
        }

        MongoEditionDialog
                .create(project, mongoDocumentOperations, actionCallback)
                .initDocument(mongoDocument)
                .show();
    }


    public void addMongoDocument() {
        MongoEditionDialog
                .create(project, mongoDocumentOperations, actionCallback)
                .initDocument(null)
                .show();
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

    private MongoKeyValueDescriptor getObjectIdDescriptorFromSelectedDocument() {
        if (resultTreeTableView == null) {
            return null;
        }
        TreeTableTree tree = resultTreeTableView.getTree();
        JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return null;
        }

        MongoNodeDescriptor descriptor = treeNode.getDescriptor();
        if (!(descriptor instanceof MongoKeyValueDescriptor)) {
            return null;
        }
        MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
        if (!"_id".equals(keyValueDescriptor.getKey())
                || !(keyValueDescriptor.getValue() instanceof ObjectId)) {
            return null;
        }

        return keyValueDescriptor;
    }


    public boolean isSelectedNodeId() {
        return getObjectIdDescriptorFromSelectedDocument() != null;
    }


    public boolean isSelectedDBRef() {
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
            if (descriptor.getValue() instanceof DBRef) {
                return true;
            } else {
                JsonTreeNode parentNode = (JsonTreeNode) treeNode.getParent();
                return parentNode.getDescriptor().getValue() instanceof DBRef;
            }
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

    public String getStringifiedResult() {
        JsonTreeNode rootNode = (JsonTreeNode) resultTreeTableView.getTree().getModel().getRoot();
        return stringifyResult(rootNode);
    }

    public JsonTreeNode getSelectedNode() {
        return (JsonTreeNode) resultTreeTableView.getTree().getLastSelectedPathComponent();
    }

    public String getSelectedNodeStringifiedValue() {
        JsonTreeNode lastSelectedResultNode = getSelectedNode();
        if (lastSelectedResultNode == null) {
            return null;
        }
        MongoNodeDescriptor userObject = lastSelectedResultNode.getDescriptor();
        return userObject.toString();
    }

    public DBRef getSelectedDBRef() {
        TreeTableTree tree = resultTreeTableView.getTree();
        JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();

        MongoNodeDescriptor descriptor = treeNode.getDescriptor();
        DBRef selectedDBRef = null;
        if (descriptor instanceof MongoKeyValueDescriptor) {
            if (descriptor.getValue() instanceof DBRef) {
                selectedDBRef = (DBRef) descriptor.getValue();
            } else {
                JsonTreeNode parentNode = (JsonTreeNode) treeNode.getParent();
                MongoNodeDescriptor parentDescriptor = parentNode.getDescriptor();
                if (parentDescriptor.getValue() instanceof DBRef) {
                    selectedDBRef = (DBRef) parentDescriptor.getValue();
                }
            }
        }

        return selectedDBRef;
    }


    private String stringifyResult(DefaultMutableTreeNode selectedResultNode) {
        return IntStream.range(0, selectedResultNode.getChildCount())
                .mapToObj(i -> getDescriptor(i, selectedResultNode).toString())
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static MongoNodeDescriptor getDescriptor(int i, DefaultMutableTreeNode parentNode) {
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parentNode.getChildAt(i);
        return (MongoNodeDescriptor) childNode.getUserObject();
    }

    @Override
    public void dispose() {
        resultTreeTableView = null;
    }

    void setCurrentViewMode(ViewMode viewMode) {
        this.currentViewMode = viewMode;
    }

    ViewMode getCurrentViewMode() {
        return currentViewMode;
    }

    public Document getReferencedDocument(DBRef selectedDBRef) {
        return mongoDocumentOperations.getReferenceDocument(
                selectedDBRef.getCollectionName(), selectedDBRef.getId(), selectedDBRef.getDatabaseName()
        );
    }

    public void deleteSelectedMongoDocument() {
        MongoKeyValueDescriptor descriptor = getObjectIdDescriptorFromSelectedDocument();
        if (descriptor == null) {
            return;
        }

        ObjectId objectId = ((ObjectId) descriptor.getValue());
        mongoDocumentOperations.deleteMongoDocument(objectId);
        notifier.notifyInfo("Document with _id=" + objectId.toString() + " deleted.");
    }

    public interface ActionCallback {

        void onOperationSuccess(String label, String message);

        void onOperationFailure(Exception exception);
    }

    public enum ViewMode {
        TREE, TABLE
    }

}
