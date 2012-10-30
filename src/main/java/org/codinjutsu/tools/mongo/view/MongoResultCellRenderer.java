/*
 * Copyright (c) 2012 David Boissier
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

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.mongo.utils.GuiUtil;
import org.codinjutsu.tools.mongo.view.model.ResultNode;
import org.codinjutsu.tools.mongo.view.model.nodedescriptor.MongoNodeDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class MongoResultCellRenderer extends ColoredTreeCellRenderer {

    private static final Icon MONGO_ICON = GuiUtil.loadIcon("mongo_16x16.png");

    @Override
    public void customizeCellRenderer(final JTree tree,
                                      final Object value,
                                      final boolean selected,
                                      final boolean expanded,
                                      final boolean leaf,
                                      final int row,
                                      final boolean hasFocus) {

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final Object userObject = node.getUserObject();

        if (userObject instanceof ResultNode) {
            append(userObject.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(MONGO_ICON);
        } else if (userObject instanceof MongoNodeDescriptor) {
            MongoNodeDescriptor mongoValueDescriptor = (MongoNodeDescriptor) userObject;
            mongoValueDescriptor.appendText(this);
        }
    }
}