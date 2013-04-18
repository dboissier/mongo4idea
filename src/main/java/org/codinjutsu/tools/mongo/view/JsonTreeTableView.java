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
import com.intellij.ui.dualView.TreeTableView;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.model.MongoComparator;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.renderer.MongoKeyCellRenderer;
import org.codinjutsu.tools.mongo.view.renderer.MongoValueCellRenderer;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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

    private static final ColumnInfo VALUE = new ColumnInfo("Value") {
        private final TableCellRenderer myRenderer = new MongoValueCellRenderer();

        public Object valueOf(Object obj) {
            JsonTreeNode node = (JsonTreeNode) obj;
            return node.getDescriptor();
        }

        @Override
        public TableCellRenderer getRenderer(Object o) {
            return myRenderer;
        }

        @Override
        public boolean isCellEditable(Object o) {
            return false;
        }
    };
    private static final ColumnInfo[] COLUMNS = new ColumnInfo[]{KEY, VALUE};

    public JsonTreeTableView(TreeNode rootNode) {
        super(new ListTreeTableModelOnColumns(rootNode, COLUMNS));
        final TreeTableTree tree = getTree();

        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        UIUtil.setLineStyleAngled(tree);
        setTreeCellRenderer(new MongoKeyCellRenderer());

        TreeUtil.expand(tree, 2);

        int maxWidth = tree.getPreferredScrollableViewportSize().width + 10;
        final TableColumn keyColumn = getColumnModel().getColumn(0);
        keyColumn.setPreferredWidth(maxWidth);
        keyColumn.setMinWidth(maxWidth);
        keyColumn.setMaxWidth(maxWidth);

        new TreeTableSpeedSearch(this, new Convertor<TreePath, String>() {
            @Override
            public String convert(final TreePath path) {
                final JsonTreeNode node = (JsonTreeNode)path.getLastPathComponent();
                MongoNodeDescriptor descriptor = node.getDescriptor();
                return descriptor.getNodeText();
            }
        });
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        TreePath treePath = getTree().getPathForRow(row);
        if (treePath == null) return super.getCellRenderer(row, column);

        Object node = treePath.getLastPathComponent();

        TableCellRenderer renderer = COLUMNS[column].getRenderer(node);
        return renderer == null ? super.getCellRenderer(row, column) : renderer;
    }
}