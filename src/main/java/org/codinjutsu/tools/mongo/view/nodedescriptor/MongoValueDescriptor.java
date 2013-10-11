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
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.utils.StringUtils;

public class MongoValueDescriptor implements MongoNodeDescriptor {

    private final int index;
    private Object value;
    private final SimpleTextAttributes valueTextAttributes;

    private MongoValueDescriptor(int index, Object value, SimpleTextAttributes valueTextAttributes) {
        this.index = index;
        this.value = value;
        this.valueTextAttributes = valueTextAttributes;
    }

    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
        if (!isNodeExpanded) {
            cellRenderer.append(getFormattedValue(), valueTextAttributes);
        }
    }

    @Override
    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getFormattedKey(), TEXT_ATTRIBUTES_PROVIDER.getIndexAttribute());
    }

    public String getFormattedKey() {
        return String.format("[%s]", index);
    }

    @Override
    public String getFormattedValue() {
        return String.format("%s", getValueAndAbbreviateIfNecessary());
    }

    protected String getValueAndAbbreviateIfNecessary() {
        String stringifiedValue = value.toString();
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    private static class MongoStringValueDescriptor extends MongoValueDescriptor {

        private MongoStringValueDescriptor(int index, String value) {
            super(index, value, TEXT_ATTRIBUTES_PROVIDER.getStringAttribute());
        }

        @Override
        public String getFormattedValue() {
            return String.format("\"%s\"", getValueAndAbbreviateIfNecessary());
        }
    }

    private static class MongoNullValueDescriptor extends MongoValueDescriptor {

        private MongoNullValueDescriptor(int index) {
            super(index, null, TEXT_ATTRIBUTES_PROVIDER.getNullAttribute());
        }

        @Override
        public String getFormattedValue() {
            return "null";
        }

        @Override
        public String toString() {
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
            return new MongoValueDescriptor(index, value, TEXT_ATTRIBUTES_PROVIDER.getBooleanAttribute());
        } else if (value instanceof Integer) {
            return new MongoValueDescriptor(index, value, TEXT_ATTRIBUTES_PROVIDER.getIndexAttribute());
        } else if (value instanceof DBObject) {
            return new MongoValueDescriptor(index, value, TEXT_ATTRIBUTES_PROVIDER.getDBObjectAttribute());
        } else {
            return new MongoValueDescriptor(index, value, TEXT_ATTRIBUTES_PROVIDER.getStringAttribute());
        }
    }
}
