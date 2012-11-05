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
import org.codinjutsu.tools.mongo.view.TextAttributesUtils;

import static org.codinjutsu.tools.mongo.view.TextAttributesUtils.*;

public class MongoKeyValueDescriptor implements MongoNodeDescriptor {

    private static final String STRING_SURROUNDED = "\"%s\"";
    private static final String TO_STRING_TEMPLATE = "{ \"%s\" : %s}";
    private static final String TO_STRING_FOR_STRING_VALUE_TEMPLATE = "{ \"%s\" : \"%s\"}";

    protected final String key;
    protected final Object value;

    private final SimpleTextAttributes textAttributes;

    private MongoKeyValueDescriptor(String key, Object value, SimpleTextAttributes textAttributes) {
        this.key = key;
        this.value = value;
        this.textAttributes = textAttributes;
    }

    public void appendText(ColoredTreeCellRenderer cellRenderer, boolean isNodeExpanded) {
        cellRenderer.append(String.format(STRING_SURROUNDED, key), TextAttributesUtils.KEY_VALUE);
        if (!isNodeExpanded) {
            cellRenderer.append(": ");
            cellRenderer.append(getDescription(), getTextAttributes());
        }
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_TEMPLATE, key, value);
    }

    protected String getDescription() {
        String stringifiedValue = value.toString();
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }

    private SimpleTextAttributes getTextAttributes() {
        return this.textAttributes;
    }


    private static class MongoKeyNullValueDescriptor extends MongoKeyValueDescriptor {

        private MongoKeyNullValueDescriptor(String key) {
            super(key, null, NULL_TEXT_ATTRIBUTE);
        }

        protected String getDescription() {
            return "null";
        }
    }


    private static class MongoKeyStringValueDescriptor extends MongoKeyValueDescriptor {

        private MongoKeyStringValueDescriptor(String key, String value) {
            super(key, value, STRING_TEXT_ATTRIBUTE);
        }

        protected String getDescription() {
            return String.format(STRING_SURROUNDED, value);
        }

        @Override
        public String toString() {
            return String.format(TO_STRING_FOR_STRING_VALUE_TEMPLATE, key, value);
        }
    }

    public static MongoKeyValueDescriptor createDescriptor(String key, Object value) {
        if (value == null) {
            return new MongoKeyNullValueDescriptor(key);
        }

        if (value instanceof String) {
            return new MongoKeyStringValueDescriptor(key, (String) value);
        } else if (value instanceof Boolean) {
            return new MongoKeyValueDescriptor(key, value, BOOLEAN_TEXT_ATTRIBUTE);
        } else if (value instanceof Integer) {
            return new MongoKeyValueDescriptor(key, value, INTEGER_TEXT_ATTRIBUTE);
        } else if (value instanceof DBObject) {
            return new MongoKeyValueDescriptor(key, value, DBOBJECT_TEXT_ATTRIBUTE);
        } else {
            return new MongoKeyValueDescriptor(key, value, STRING_TEXT_ATTRIBUTE);
        }
    }
}
