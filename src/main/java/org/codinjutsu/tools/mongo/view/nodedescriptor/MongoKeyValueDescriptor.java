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

package org.codinjutsu.tools.mongo.view.nodedescriptor;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.utils.DateUtils;
import org.codinjutsu.tools.mongo.utils.MongoUtils;
import org.codinjutsu.tools.mongo.utils.StringUtils;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MongoKeyValueDescriptor implements MongoNodeDescriptor {

    private static final String STRING_SURROUNDED = "\"%s\"";
    private static final String TO_STRING_TEMPLATE = "\"%s\" : %s";

    final String key;
    Object value;

    private final SimpleTextAttributes valueTextAttributes;

    public static MongoKeyValueDescriptor createDescriptor(String key, Object value) { //TODO refactor needed
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
        } else if (value instanceof Document) {
            return new MongoKeyDocumentValueDescriptor(key, value);
        } else if (value instanceof List) {
            return new MongoKeyListValueDescriptor(key, value);
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
            cellRenderer.append(getFormattedValue(), valueTextAttributes);
        }
    }

    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getFormattedKey(), StyleAttributesProvider.getKeyValueAttribute());
    }

    public String getFormattedKey() {
        return String.format(STRING_SURROUNDED, key);
    }

    public String getFormattedValue() {
        return StringUtils.abbreviateInCenter(value.toString(), MAX_LENGTH);
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

    private static class MongoKeyNullValueDescriptor extends MongoKeyValueDescriptor {

        private MongoKeyNullValueDescriptor(String key) {
            super(key, null, StyleAttributesProvider.getNullAttribute());
        }

        @Override
        public String getFormattedValue() {
            return "null";
        }
    }

    private static class MongoKeyStringValueDescriptor extends MongoKeyValueDescriptor {

        private static final String TO_STRING_FOR_STRING_VALUE_TEMPLATE = "\"%s\" : \"%s\"";

        private MongoKeyStringValueDescriptor(String key, String value) {
            super(key, value, StyleAttributesProvider.getStringAttribute());
        }

        @Override
        public String getFormattedValue() {
            return value.toString();
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
        public String getFormattedValue() {
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

    private static class MongoKeyDocumentValueDescriptor extends MongoKeyValueDescriptor {

        MongoKeyDocumentValueDescriptor(String key, Object value) {
            super(key, value, StyleAttributesProvider.getDocumentAttribute());
        }

        @Override
        public String getFormattedValue() {
            Document document = (Document) this.value;
            return StringUtils.abbreviateInCenter(document.toJson(), MAX_LENGTH);
        }

        @Override
        public String toString() {
            return String.format(TO_STRING_TEMPLATE, key, getFormattedDocument());
        }

        private String getFormattedDocument() {
            return ((Document) value).toJson();
        }
    }

    private static class MongoKeyListValueDescriptor extends MongoKeyValueDescriptor {
        MongoKeyListValueDescriptor(String key, Object value) {
            super(key, value, StyleAttributesProvider.getDocumentAttribute());
        }

        @Override
        public String getFormattedValue() {
            return StringUtils.abbreviateInCenter(getFormattedList(), MAX_LENGTH);
        }

        @Override
        public String toString() {
            return String.format(TO_STRING_TEMPLATE, key, getFormattedList());
        }

        private String getFormattedList() {
            return MongoUtils.stringifyList((List) value);
        }
    }
}
