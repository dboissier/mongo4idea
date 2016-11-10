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

package org.codinjutsu.tools.mongo.view.renderer;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;

import javax.swing.*;

public class MongoTableCellRenderer extends ColoredTableCellRenderer {

    @Override
    protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {

        if (value == null) {
            append("null", StyleAttributesProvider.getNullAttribute());
        } else {
            SimpleTextAttributes styleAttribute;
            if (value instanceof Number) {
                styleAttribute = StyleAttributesProvider.getNumberAttribute();
            } else if (value instanceof Boolean) {
                styleAttribute = StyleAttributesProvider.getBooleanAttribute();
            } else if (value instanceof Document) {
                styleAttribute = StyleAttributesProvider.getDocumentAttribute();
            } else if (value instanceof ObjectId) {
                styleAttribute = StyleAttributesProvider.getObjectIdAttribute();
            } else {
                styleAttribute = StyleAttributesProvider.getStringAttribute();
            }
            append(String.valueOf(value), styleAttribute);
        }
    }
}
