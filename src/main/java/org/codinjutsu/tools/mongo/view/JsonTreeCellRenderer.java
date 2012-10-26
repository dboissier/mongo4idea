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
import org.codinjutsu.tools.mongo.view.model.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.model.ResultNode;
import org.codinjutsu.tools.mongo.view.model.nodedescriptor.MongoValueDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class JsonTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(final JTree tree,
                                      final Object value,
                                      final boolean selected,
                                      final boolean expanded,
                                      final boolean leaf,
                                      final int row,
                                      final boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            final Object userObject = node.getUserObject();

            if (userObject instanceof ResultNode) {
                append(userObject.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            } else if (userObject instanceof MongoValueDescriptor) {
                MongoValueDescriptor mongoValueDescriptor = (MongoValueDescriptor) userObject;
                append(mongoValueDescriptor.getDescription(), TextAttributesUtils.STRING_VALUE);
            } else if (userObject instanceof MongoKeyValueDescriptor) {
                MongoKeyValueDescriptor mongoValueDescriptor = (MongoKeyValueDescriptor) userObject;
                append(mongoValueDescriptor.getDescription(), TextAttributesUtils.STRING_VALUE);
            }
        }
    }
}
