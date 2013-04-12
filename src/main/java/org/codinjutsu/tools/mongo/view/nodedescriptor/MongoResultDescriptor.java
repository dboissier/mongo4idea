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

package org.codinjutsu.tools.mongo.view.nodedescriptor;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;

public class MongoResultDescriptor implements MongoNodeDescriptor {

    private static final Icon MONGO_ICON = GuiUtils.loadIcon("mongo_logo.png");

    private final String formattedText;

    public MongoResultDescriptor(String collectionName) {
        formattedText = String.format("results of '%s'", collectionName);
    }
    @Override
    public void appendText(ColoredTreeCellRenderer cellRenderer, boolean isNodeExpanded) {
        cellRenderer.append(formattedText, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        cellRenderer.setIcon(MONGO_ICON);
    }

    @Override
    public void renderTextValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
    }

    @Override
    public void renderTextKey(ColoredTreeCellRenderer cellRenderer) {
    }
}
