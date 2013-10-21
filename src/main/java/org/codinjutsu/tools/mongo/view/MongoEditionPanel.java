package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBScrollPane;
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
    }

    public void updateEditionTree(DBObject mongoDocument) {
        editTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree(mongoDocument), JsonTreeTableView.COLUMNS_FOR_WRITING);
        editTableView.setName("editionTreeTable");

        editionTreePanel.invalidate();
        editionTreePanel.removeAll();
        editionTreePanel.add(new JBScrollPane(editTableView));
        editionTreePanel.validate();
    }

    @Override
    public void dispose() {
        editTableView = null;
    }

    public void init(final MongoRunnerPanel.MongoDocumentOperations mongoDocumentOperations, final MongoResultPanel.ActionCallback actionCallback) {

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
    }

    private DBObject buildMongoDocument() {
        JsonTreeNode rootNode = (JsonTreeNode) editTableView.getTree().getModel().getRoot();
        return JsonTreeModel.buildDBObject(rootNode);
    }

    private DBObject getMongoDocument() {
        JsonTreeNode rootNode = (JsonTreeNode) editTableView.getTree().getModel().getRoot();
        return (DBObject) rootNode.getDescriptor().getValue();
    }
}
