package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.utils.MongoUtils;
import org.codinjutsu.tools.mongo.view.action.edition.AddKeyAction;
import org.codinjutsu.tools.mongo.view.action.edition.AddValueAction;
import org.codinjutsu.tools.mongo.view.action.edition.DeleteKeyAction;
import org.codinjutsu.tools.mongo.view.model.JsonDataType;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
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

        mainPanel.setBorder(IdeBorderFactory.createTitledBorder("Edition", false));
        add(mainPanel);
        editionTreePanel.setLayout(new BorderLayout());

        saveButton.setName("saveButton");
        cancelButton.setName("cancelButton");
        deleteButton.setName("deleteButton");
    }

    public MongoEditionPanel init(final MongoRunnerPanel.MongoDocumentOperations mongoDocumentOperations, final MongoResultPanel.ActionCallback actionCallback) {

        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                actionCallback.afterOperation();
            }
        });

        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mongoDocumentOperations.updateMongoDocument(buildMongoDocument());
                actionCallback.afterOperation();
            }
        });

        deleteButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mongoDocumentOperations.deleteMongoDocument(getMongoDocument());
                actionCallback.afterOperation();
            }
        });

        return this;
    }

    public void updateEditionTree(DBObject mongoDocument) {
        editTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree(mongoDocument), JsonTreeTableView.COLUMNS_FOR_WRITING);
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
        Enumeration children = parentNode.children();
        while(children.hasMoreElements()) {
            JsonTreeNode childNode = (JsonTreeNode) children.nextElement();
            MongoNodeDescriptor descriptor = childNode.getDescriptor();
            if(descriptor instanceof MongoKeyValueDescriptor) {
                MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
                if (StringUtils.equals(key, keyValueDescriptor.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addKey(String key, JsonDataType jsonDataType, String value) {

        List<TreeNode> node = new LinkedList<TreeNode>();
        Object mongoObject = MongoUtils.parseValue(jsonDataType, value);
        JsonTreeNode treeNode = new JsonTreeNode(MongoKeyValueDescriptor.createDescriptor(key, mongoObject));

        if (mongoObject instanceof DBObject) {
             JsonTreeModel.processDbObject(treeNode, (DBObject) mongoObject);
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

    public void addValue(JsonDataType jsonDataType, String value) {
        List<TreeNode> node = new LinkedList<TreeNode>();
        Object mongoObject = MongoUtils.parseValue(jsonDataType, value);

        JsonTreeNode parentNode = getParentNode();

        JsonTreeNode treeNode = new JsonTreeNode(MongoValueDescriptor.createDescriptor(parentNode.getChildCount(), mongoObject));
        if (mongoObject instanceof DBObject) {
            JsonTreeModel.processDbObject(treeNode, (DBObject) mongoObject);
        }

        node.add(treeNode);

        DefaultTreeModel treeModel = (DefaultTreeModel) editTableView.getTree().getModel();
        TreeUtil.addChildrenTo(parentNode, node);
        treeModel.reload(parentNode);
    }

    private JsonTreeNode getParentNode() {
        return (JsonTreeNode) ((JsonTreeNode) editTableView.getTree().getLastSelectedPathComponent()).getParent();
    }

    public boolean canAddKey() {
        JsonTreeNode selectedNode = (JsonTreeNode) editTableView.getTree().getLastSelectedPathComponent();
        if (selectedNode == null) {
            return false;
        }
        return selectedNode.getDescriptor() instanceof MongoKeyValueDescriptor;
    }

    public boolean canAddValue() {
        JsonTreeNode selectedNode = (JsonTreeNode) editTableView.getTree().getLastSelectedPathComponent();
        if (selectedNode == null) {
            return false;
        }
        return selectedNode.getDescriptor() instanceof MongoValueDescriptor;
    }

    public void removeSelectedKey() {
        JsonTreeNode lastSelectedResultNode = (JsonTreeNode) editTableView.getTree().getLastSelectedPathComponent();
        if (lastSelectedResultNode == null) {
            return;
        }
        TreeUtil.removeSelected(editTableView.getTree());

    }

    private DBObject buildMongoDocument() {
        JsonTreeNode rootNode = (JsonTreeNode) editTableView.getTree().getModel().getRoot();
        return JsonTreeModel.buildDBObject(rootNode);
    }

    @Override
    public void dispose() {
        editTableView = null;
    }

    private ObjectId getMongoDocument() {
        JsonTreeNode rootNode = (JsonTreeNode) editTableView.getTree().getModel().getRoot();

        return (ObjectId) findObjectIdNodeDescriptor(rootNode).getDescriptor().getValue();
    }

    private JsonTreeNode findObjectIdNodeDescriptor(JsonTreeNode rootNode) {
        return ((JsonTreeNode) rootNode.getChildAt(0));//TODO crappy
    }
}
