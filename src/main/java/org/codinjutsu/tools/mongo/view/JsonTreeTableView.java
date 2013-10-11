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

import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.SortableColumnModel;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.editor.MongoValueCellEditor;import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.renderer.MongoKeyCellRenderer;
import org.codinjutsu.tools.mongo.view.renderer.MongoValueCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Date;

public class JsonTreeTableView extends TreeTable {

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

        int maxWidth = tree.getPreferredScrollableViewportSize().width + 10;
        final TableColumn keyColumn = getColumnModel().getColumn(0);
        keyColumn.setMinWidth(maxWidth);
        keyColumn.setMaxWidth(maxWidth);
        keyColumn.setPreferredWidth(maxWidth);

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

        Object node = treePath.getLastPathComponent();

        TableCellRenderer renderer = this.columns[column].getRenderer(node);
        return renderer == null ? super.getCellRenderer(row, column) : renderer;
    }

    private static class ReadOnlyValueColumnInfo extends ColumnInfo {
        private final TableCellRenderer myRenderer = new MongoValueCellRenderer();

        public ReadOnlyValueColumnInfo() {
            super("Value");
        }

        public Object valueOf(Object obj) {
            JsonTreeNode node = (JsonTreeNode) obj;
            return node.getDescriptor();
        }

        @Override
        public TableCellRenderer getRenderer(Object o) {
            return myRenderer;
        }

        @Nullable
        @Override
        public boolean isCellEditable(Object o) {
            return false;
        }
    }

    private static class WritableColumnInfo extends ReadOnlyValueColumnInfo {
        private final TableCellEditor myEditor = new MongoValueCellEditor();

        @Override
        public boolean isCellEditable(Object o) {
            JsonTreeNode treeNode = (JsonTreeNode) o;
            Object value = treeNode.getDescriptor().getValue();
            if (value instanceof DBObject) {
                return false;
            }

            if (value instanceof Date) {
                return false;
            }

            if (value instanceof ObjectId) {
                return false;
            }

            return true;
        }

        @Nullable
        @Override
        public TableCellEditor getEditor(Object o) {
            return myEditor;
        }

        @Override
        public void setValue(Object o, Object value) {
            System.out.println("node = " + o + " - value =" + String.valueOf(value));
        }
    }
}