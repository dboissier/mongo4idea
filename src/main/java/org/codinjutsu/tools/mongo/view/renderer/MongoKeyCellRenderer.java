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

import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.mongo.view.model.JsonTreeNode;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoResultDescriptor;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoValueDescriptor;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

//TODO does not with ColoredTreeCellRenderer (text is truncated, I do not know why: Has to do it manually and ugly :(
public class MongoKeyCellRenderer extends DefaultTreeCellRenderer {


    private static final SimpleTextAttributes KEY_ATTRIBUTE = StyleAttributesUtils.getInstance().getKeyValueAttribute();
    private static final SimpleTextAttributes INDEX_ATTRIBUTE = StyleAttributesUtils.getInstance().getIndexAttribute();

//
//    @Override
//    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//
//        TreePath pathForRow = tree.getPathForRow(row);
//        if (pathForRow == null) {
//            return;
//        }
//        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
//        if (node == null || !(node instanceof JsonTreeNode)) {
//            return;
//        }
//
//        JsonTreeNode jsonTreeNode = (JsonTreeNode) node;
//
//        MongoNodeDescriptor descriptor = jsonTreeNode.getDescriptor();
//        descriptor.renderTextKey(this);
//    }


    @Override
    public Component getTreeCellRendererComponent(JTree jTree, Object obj, boolean b, boolean b2, boolean b3, int i, boolean b4) {
        MongoNodeDescriptor descriptor = ((JsonTreeNode) obj).getDescriptor();
        String text;
        SimpleTextAttributes attributes;
        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            text = keyValueDescriptor.renderedKeyText();
            attributes = KEY_ATTRIBUTE;
        } else if (descriptor instanceof MongoValueDescriptor) {
            MongoValueDescriptor valueDescriptor = (MongoValueDescriptor) descriptor;
            text = valueDescriptor.getFormattedText();
            attributes = INDEX_ATTRIBUTE;
        } else {
            MongoResultDescriptor valueDescriptor = (MongoResultDescriptor) descriptor;
            text = valueDescriptor.getRenderedText();
            attributes = valueDescriptor.getTextAttributes();
        }


        JLabel rendererComponent = (JLabel) super.getTreeCellRendererComponent(jTree, text, b, b2, b3, i, b4);
        rendererComponent.setForeground(attributes.getFgColor());
        rendererComponent.setFont(rendererComponent.getFont().deriveFont(attributes.getFontStyle()));
        rendererComponent.setIcon(null);
        return rendererComponent;
    }
}
