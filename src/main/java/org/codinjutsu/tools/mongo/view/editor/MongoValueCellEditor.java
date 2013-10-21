package org.codinjutsu.tools.mongo.view.editor;

import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.AbstractTableCellEditor;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.jdesktop.swingx.JXTextField;

import javax.swing.*;
import java.awt.*;

public class MongoValueCellEditor extends DefaultCellEditor {


    public MongoValueCellEditor() {
        super(new JTextField());
    }


    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JTextField stringEditor = (JTextField) getComponent();
        final JsonTreeNode jsonNode = (JsonTreeNode)((TreeTable)table).getTree().
                getPathForRow(row).getLastPathComponent();

        stringEditor.setText(String.valueOf(jsonNode.getDescriptor().getValue()));

        return stringEditor;
    }

    @Override
    public Object getCellEditorValue() {
        return ((JTextField) getComponent()).getText();
    }
}
