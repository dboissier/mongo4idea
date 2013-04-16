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
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class MongoKeyCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object obj, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        MongoNodeDescriptor descriptor = ((JsonTreeNode) obj).getDescriptor();
        String text = descriptor.getFormattedText();
        SimpleTextAttributes attributes = descriptor.getNodeTextAttributes();


        JLabel rendererComponent = (JLabel) super.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);
        rendererComponent.setForeground(attributes.getFgColor());
        rendererComponent.setFont(rendererComponent.getFont().deriveFont(attributes.getFontStyle()));
        rendererComponent.setIcon(null);
        return rendererComponent;
    }
}
