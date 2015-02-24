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
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.utils.DateUtils;
import org.codinjutsu.tools.mongo.utils.StringUtils;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MongoKeyValueDescriptor implements MongoNodeDescriptor {

    private static final String STRING_SURROUNDED = "\"%s\"";
    protected static final String TO_STRING_TEMPLATE = "\"%s\" : %s";

    protected final String key;
    protected Object value;

    private final SimpleTextAttributes valueTextAttributes;

    public static MongoKeyValueDescriptor createDescriptor(String key, Object value) {
        if (value == null) {
            return new MongoKeyNullValueDescriptor(key);
        }

        if (value instanceof Boolean) {
            return new MongoKeyValueDescriptor(key, value, StyleAttributesProvider.getBooleanAttribute()) {
                @Override
                public void setValue(Object value) {
                    this.value = Boolean.valueOf((String) value);
                }
            };
        } else if (value instanceof Integer) {
            return new MongoKeyValueDescriptor(key, value, StyleAttributesProvider.getNumberAttribute()) {
                @Override
                public void setValue(Object value) {
                    this.value = Integer.valueOf((String) value);
                }
            };
        } else if (value instanceof Double) {
            return new MongoKeyValueDescriptor(key, value, StyleAttributesProvider.getNumberAttribute()) {
                @Override
                public void setValue(Object value) {
                    this.value = Double.valueOf((String) value);
                }
            };
        } else if (value instanceof Long) {
            return new MongoKeyValueDescriptor(key, value, StyleAttributesProvider.getNumberAttribute()) {
                @Override
                public void setValue(Object value) {
                    this.value = Long.valueOf((String) value);
                }
            };
        } else if (value instanceof String) {
            return new MongoKeyStringValueDescriptor(key, (String) value);
        } else if (value instanceof Date) {
            return new MongoKeyDateValueDescriptor(key, (Date) value);
        } else if (value instanceof ObjectId) {
            return new MongoKeyValueDescriptor(key, value, StyleAttributesProvider.getObjectIdAttribute());
        } else if (value instanceof DBObject) {
            return new MongoKeyValueDescriptor(key, value, StyleAttributesProvider.getDBObjectAttribute());
        } else {
            return new MongoKeyValueDescriptor(key, value, StyleAttributesProvider.getStringAttribute());
        }
    }

    private MongoKeyValueDescriptor(String key, Object value, SimpleTextAttributes valueTextAttributes) {
        this.key = key;
        this.value = value;
        this.valueTextAttributes = valueTextAttributes;
    }

    public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
        if (!isNodeExpanded) {
            cellRenderer.append(getValueAndAbbreviateIfNecessary(), valueTextAttributes);
        }
    }

    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getFormattedKey(), StyleAttributesProvider.getKeyValueAttribute());
    }

    public String getFormattedKey() {
        return String.format(STRING_SURROUNDED, key);
    }

    public String getFormattedValue() {
        return getValueAndAbbreviateIfNecessary();
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_TEMPLATE, key, value);
    }

    protected String getValueAndAbbreviateIfNecessary() {
        String stringifiedValue = value.toString();
        if (stringifiedValue.length() > MAX_LENGTH) {
            return StringUtils.abbreviateInCenter(stringifiedValue, MAX_LENGTH);
        }
        return stringifiedValue;
    }

    private static class MongoKeyNullValueDescriptor extends MongoKeyValueDescriptor {

        private MongoKeyNullValueDescriptor(String key) {
            super(key, null, StyleAttributesProvider.getNullAttribute());
        }

        protected String getValueAndAbbreviateIfNecessary() {
            return "null";
        }
    }

    private static class MongoKeyStringValueDescriptor extends MongoKeyValueDescriptor {

        private static final String TO_STRING_FOR_STRING_VALUE_TEMPLATE = "\"%s\" : \"%s\"";

        private MongoKeyStringValueDescriptor(String key, String value) {
            super(key, value, StyleAttributesProvider.getStringAttribute());
        }

        @Override
        protected String getValueAndAbbreviateIfNecessary() {
            return String.format(STRING_SURROUNDED, value);
        }

        @Override
        public String toString() {
            return String.format(TO_STRING_FOR_STRING_VALUE_TEMPLATE, key, value);
        }
    }

    private static class MongoKeyDateValueDescriptor extends MongoKeyValueDescriptor {

        private static final DateFormat DATE_FORMAT = DateUtils.utcDateTime(Locale.getDefault());

        private static final String TO_STRING_FOR_DATE_VALUE_TEMPLATE = "\"%s\" : \"%s\"";

        private MongoKeyDateValueDescriptor(String key, Date value) {
            super(key, value, StyleAttributesProvider.getStringAttribute());
        }

        @Override
        protected String getValueAndAbbreviateIfNecessary() {
            return getFormattedDate();
        }

        @Override
        public String toString() {
            return String.format(TO_STRING_FOR_DATE_VALUE_TEMPLATE, key, getFormattedDate());
        }

        private String getFormattedDate() {
            return DATE_FORMAT.format(value);
        }
    }
}
