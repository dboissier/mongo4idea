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
import org.bson.types.Binary;
import org.codinjutsu.tools.mongo.utils.DateUtils;
import org.codinjutsu.tools.mongo.utils.MongoUtils;
import org.codinjutsu.tools.mongo.utils.StringUtils;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.codinjutsu.tools.mongo.utils.MongoUtils.DOCUMENT_CODEC;
import static org.codinjutsu.tools.mongo.utils.MongoUtils.WRITER_SETTINGS;

public class MongoValueDescriptor implements MongoNodeDescriptor {

    private final int index;
    Object value;
    private final SimpleTextAttributes valueTextAttributes;

    public static MongoValueDescriptor createDescriptor(int index, Object value) { //TODO refactor this
        if (value == null) {
            return new MongoNullValueDescriptor(index);
        }

        if (value instanceof String) {
            return new MongoStringValueDescriptor(index, (String) value);
        } else if (value instanceof Boolean) {
            return new MongoValueDescriptor(index, value, StyleAttributesProvider.getBooleanAttribute()) {
                @Override
                public void setValue(Object value) {
                    this.value = Boolean.parseBoolean((String) value);
                }
            };
        } else if (value instanceof Number) {
            return new MongoValueDescriptor(index, value, StyleAttributesProvider.getNumberAttribute()) {
                @Override
                public void setValue(Object value) {
                    this.value = Integer.parseInt((String) value);
                }
            };
        } else if (value instanceof Date) {
            return new MongoDateValueDescriptor(index, (Date) value);
        } else if (value instanceof Document) {
            return new MongoDocumentValueDescriptor(index, value);
        } else if (value instanceof List) {
            return new MongoListValueDescriptor(index, value);
        } else if (value instanceof Binary) {
            return new MongoBinaryDescriptor(index, (Binary) value);
        } else {
            return new MongoValueDescriptor(index, value, StyleAttributesProvider.getStringAttribute());
        }
    }

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

    public void renderNode(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.append(getKey(), StyleAttributesProvider.getIndexAttribute());
    }

    public String getKey() {
        return String.format("[%s]", index);
    }

    public String getFormattedValue() {
        return String.format("%s", StringUtils.abbreviateInCenter(value.toString(), MAX_LENGTH));
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public String pretty() {
        return getFormattedValue();
    }

    private static class MongoStringValueDescriptor extends MongoValueDescriptor {

        private MongoStringValueDescriptor(int index, String value) {
            super(index, value, StyleAttributesProvider.getStringAttribute());
        }

        @Override
        public String getFormattedValue() {
            return StringUtils.abbreviateInCenter(value.toString(), MAX_LENGTH);
        }
    }

    private static class MongoNullValueDescriptor extends MongoValueDescriptor {

        private MongoNullValueDescriptor(int index) {
            super(index, null, StyleAttributesProvider.getNullAttribute());
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

    private static class MongoDateValueDescriptor extends MongoValueDescriptor {

        private static final DateFormat DATE_FORMAT = DateUtils.utcDateTime(Locale.getDefault());

        private MongoDateValueDescriptor(int index, Date value) {
            super(index, value, StyleAttributesProvider.getStringAttribute());
        }

        @Override
        public String getFormattedValue() {
            return getFormattedDate();
        }

        @Override
        public String toString() {
            return String.format("\"%s\"", getFormattedDate());
        }

        private String getFormattedDate() {
            return DATE_FORMAT.format(value);
        }
    }

    private static class MongoDocumentValueDescriptor extends MongoValueDescriptor {


        MongoDocumentValueDescriptor(int index, Object value) {
            super(index, value, StyleAttributesProvider.getDocumentAttribute());

        }

        @Override
        public String getFormattedValue() {
            Document document = (Document) value;
            return String.format("%s", StringUtils.abbreviateInCenter(document.toJson(DOCUMENT_CODEC), MAX_LENGTH));
        }

        @Override
        public String toString() {
            return ((Document) value).toJson(DOCUMENT_CODEC);
        }

        @Override
        public String pretty() {
            return ((Document) value).toJson(WRITER_SETTINGS);
        }
    }

    private static class MongoListValueDescriptor extends MongoValueDescriptor {
        MongoListValueDescriptor(int index, Object value) {
            super(index, value, StyleAttributesProvider.getDocumentAttribute());
        }

        @Override
        public String getFormattedValue() {
            return getFormattedList();
        }

        @Override
        public String toString() {
            return getFormattedList();
        }

        private String getFormattedList() {
            return MongoUtils.stringifyList((List) value);
        }
    }

    private static class MongoBinaryDescriptor extends MongoValueDescriptor {

        private MongoBinaryDescriptor(int index, Binary value) {
            super(index, value, StyleAttributesProvider.getNullAttribute());
        }

        @Override
        public String getFormattedValue() {
            return "Cannot display value";
        }
    }
}
