package org.codinjutsu.tools.mongo.view.editor;

import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.AbstractTableCellEditor;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;

import javax.swing.*;
import java.awt.*;

public class MongoValueCellEditor extends AbstractTableCellEditor {

    private final JTextField stringEditor = new JTextField();


    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        final JsonTreeNode jsonNode = (JsonTreeNode)((TreeTable)table).getTree().
                getPathForRow(row).getLastPathComponent();

        stringEditor.setText(String.valueOf(jsonNode.getDescriptor().getValue()));

        return null;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
}
