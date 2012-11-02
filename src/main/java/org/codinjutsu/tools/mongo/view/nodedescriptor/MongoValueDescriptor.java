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

package org.codinjutsu.tools.mongo.view.nodedescriptor;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.utils.StringUtils;

import static org.codinjutsu.tools.mongo.view.TextAttributesUtils.*;

public class MongoValueDescriptor implements MongoNodeDescriptor {

    protected final int index;
    protected final Object value;
    private final SimpleTextAttributes textAttributes;

    private MongoValueDescriptor(int index, Object value, SimpleTextAttributes textAttributes) {
        this.index = index;
        this.value = value;
        this.textAttributes = textAttributes;
    }

    public void appendText(ColoredTreeCellRenderer cellRenderer, boolean isNodeExpanded) {
        cellRenderer.append(String.format("[%s] ", index), INDEX);
        if (!isNodeExpanded) {
            cellRenderer.append(getDescription(), textAttributes);
        }
    }

    protected String getDescription() {
        return String.format("%s", getStringValue());
    }

    protected String getStringValue() {
        String stringifiedValue = value.toString();
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }

    private static class MongoStringValueDescriptor extends MongoValueDescriptor {

        private MongoStringValueDescriptor(int index, String value) {
            super(index, value, STRING_TEXT_ATTRIBUTE);
        }

        protected String getDescription() {
            return String.format("\"%s\"", getStringValue());
        }
    }

    private static class MongoNullValueDescriptor extends MongoValueDescriptor {

        private MongoNullValueDescriptor(int index) {
            super(index, null, NULL_TEXT_ATTRIBUTE);
        }

        protected String getDescription() {
            return "null";
        }
    }

    public static MongoValueDescriptor createDescriptor(int index, Object value) {
        if (value == null) {
            return new MongoNullValueDescriptor(index);
        }

        if (value instanceof String) {
            return new MongoStringValueDescriptor(index, (String) value);
        } else if (value instanceof Boolean) {
            return new MongoValueDescriptor(index, value, BOOLEAN_TEXT_ATTRIBUTE);
        } else if (value instanceof Integer) {
            return new MongoValueDescriptor(index, value, INTEGER_TEXT_ATTRIBUTE);
        } else if (value instanceof DBObject) {
            return new MongoValueDescriptor(index, value, DBOBJECT_TEXT_ATTRIBUTE);
        } else {
            return new MongoValueDescriptor(index, value, STRING_TEXT_ATTRIBUTE);
        }
    }
}
