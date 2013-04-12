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

package org.codinjutsu.tools.mongo.view.renderer;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class MongoTableValueCellRenderer extends ColoredTableCellRenderer {

    @Override
    protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {

        TreeTableTree tree = ((TreeTable) table).getTree();
        TreePath pathForRow = tree.getPathForRow(row);

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
        if (!(node instanceof JsonTreeNode)) {
            append("Pouet Pouet");
        }

        JsonTreeNode jsonTreeNode = (JsonTreeNode) node;

        MongoNodeDescriptor descriptor = (MongoNodeDescriptor) jsonTreeNode.getValue();
        descriptor.renderTextValue(this, tree.isExpanded(pathForRow));
    }
}
