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

import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.renderer.MongoKeyCellRenderer;
import org.codinjutsu.tools.mongo.view.renderer.MongoValueCellRenderer;
import org.codinjutsu.tools.mongo.view.table.MongoDatePickerCellEditor;
import org.codinjutsu.tools.mongo.view.table.MongoValueCellEditor;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

class JsonTreeTableView extends TreeTable {

    private static final ColumnInfo KEY = new ColumnInfo("Key") {

        public Object valueOf(Object obj) {
            JsonTreeNode node = (JsonTreeNode) obj;
            return node.getDescriptor();
        }

        @Override
        public Class getColumnClass() {
            return TreeTableModel.class;
        }

        @Override
        public boolean isCellEditable(Object o) {
            return false;
        }
    };

    private static final ColumnInfo READONLY_VALUE = new ReadOnlyValueColumnInfo();

    private static final ColumnInfo WRITABLE_VALUE = new WritableColumnInfo();

    public static final ColumnInfo[] COLUMNS_FOR_READING = new ColumnInfo[]{KEY, READONLY_VALUE};
    public static final ColumnInfo[] COLUMNS_FOR_WRITING = new ColumnInfo[]{KEY, WRITABLE_VALUE};

    private final ColumnInfo[] columns;

    public JsonTreeTableView(TreeNode rootNode, ColumnInfo[] columnInfos) {
        super(new ListTreeTableModelOnColumns(rootNode, columnInfos));
        this.columns = columnInfos;

        final TreeTableTree tree = getTree();

        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        UIUtil.setLineStyleAngled(tree);
        setTreeCellRenderer(new MongoKeyCellRenderer());

        TreeUtil.expand(tree, 2);

        new TreeTableSpeedSearch(this, new Convertor<TreePath, String>() {
            @Override
            public String convert(final TreePath path) {
                final JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
                MongoNodeDescriptor descriptor = node.getDescriptor();
                return descriptor.getFormattedKey();
            }
        });
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        TreePath treePath = getTree().getPathForRow(row);
        if (treePath == null) return super.getCellRenderer(row, column);

        JsonTreeNode node = (JsonTreeNode) treePath.getLastPathComponent();

        TableCellRenderer renderer = this.columns[column].getRenderer(node);
        return renderer == null ? super.getCellRenderer(row, column) : renderer;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        TreePath treePath = getTree().getPathForRow(row);
        if (treePath == null) return super.getCellEditor(row, column);

        JsonTreeNode node = (JsonTreeNode) treePath.getLastPathComponent();
        TableCellEditor editor = columns[column].getEditor(node);
        return editor == null ? super.getCellEditor(row, column) : editor;
    }

    private static class ReadOnlyValueColumnInfo extends ColumnInfo<JsonTreeNode, MongoNodeDescriptor> {
        private final TableCellRenderer myRenderer = new MongoValueCellRenderer();

        public ReadOnlyValueColumnInfo() {
            super("Value");
        }

        public MongoNodeDescriptor valueOf(JsonTreeNode treeNode) {
            return treeNode.getDescriptor();
        }

        @Override
        public TableCellRenderer getRenderer(JsonTreeNode o) {
            return myRenderer;
        }

        @Override
        public boolean isCellEditable(JsonTreeNode o) {
            return false;
        }
    }

    private static class WritableColumnInfo extends ColumnInfo<JsonTreeNode, Object> {

        private final TableCellRenderer myRenderer = new MongoValueCellRenderer();
        private final TableCellEditor defaultEditor = new MongoValueCellEditor();


        public WritableColumnInfo() {
            super("Value");
        }

        @Override
        public TableCellRenderer getRenderer(JsonTreeNode o) {
            return myRenderer;
        }


        @Override
        public boolean isCellEditable(JsonTreeNode treeNode) {
            Object value = treeNode.getDescriptor().getValue();
            if (value instanceof Document || value instanceof List) {
                return false;
            }

            if (value instanceof ObjectId) {
                return false;
            }

            return true;
        }

        @Nullable
        @Override
        public TableCellEditor getEditor(final JsonTreeNode treeNode) {
            Object value = treeNode.getDescriptor().getValue();
            if (value instanceof Date) {
                return buildDateCellEditor(treeNode);
            }
            return defaultEditor;
        }

        private static MongoDatePickerCellEditor buildDateCellEditor(final JsonTreeNode treeNode) {
            final MongoDatePickerCellEditor dateEditor = new MongoDatePickerCellEditor();

//  Note from dev: Quite ugly because when clicking on the button to open popup calendar, stopCellEdition is invoked.
//                 From that point, impossible to set the selected data in the node description
            dateEditor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    treeNode.getDescriptor().setValue(dateEditor.getCellEditorValue());
                }
            });
            return dateEditor;
        }

        public Object valueOf(JsonTreeNode treeNode) {
            return treeNode.getDescriptor();

        }

        @Override
        public void setValue(JsonTreeNode treeNode, Object value) {
            treeNode.getDescriptor().setValue(value);
        }
    }
}