package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.action.DropCollectionAction;
import org.codinjutsu.tools.mongo.view.action.RefreshServerAction;
import org.codinjutsu.tools.mongo.view.action.edition.AddKeyAction;
import org.codinjutsu.tools.mongo.view.action.edition.DeleteKeyAction;
import org.codinjutsu.tools.mongo.view.action.edition.EditKeyAction;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
//            actionPopupGroup.add(new EditKeyAction(this));
            actionPopupGroup.add(new DeleteKeyAction(this));
//            actionPopupGroup.add(new AddKeyAction(this));
        }

        PopupHandler.installPopupHandler(editTableView, actionPopupGroup, "POPUP", ActionManager.getInstance());
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
