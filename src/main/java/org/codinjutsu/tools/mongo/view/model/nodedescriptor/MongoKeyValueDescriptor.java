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

package org.codinjutsu.tools.mongo.view.model.nodedescriptor;

import com.intellij.ui.SimpleTextAttributes;
import com.mongodb.DBObject;

import static org.codinjutsu.tools.mongo.view.TextAttributesUtils.*;

public class MongoKeyValueDescriptor implements MongoNodeDescriptor {

    public static final String STRING_SURROUNDED = "\"%s\"";
    protected final String key;
    protected final Object value;
    private final SimpleTextAttributes textAttributes;

    public MongoKeyValueDescriptor(String key, Object value, SimpleTextAttributes textAttributes) {
        this.key = key;
        this.value = value;
        this.textAttributes = textAttributes;
    }

    public String getKey() {
        return String.format(STRING_SURROUNDED, key);
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return value.toString();
    }

    @Override
    public SimpleTextAttributes getTextAttributes() {
        return this.textAttributes;
    }


    private static class MongoKeyNullValueDescriptor extends MongoKeyValueDescriptor {

        public MongoKeyNullValueDescriptor(String key) {
            super(key, null, STRING_TEXT_ATTRIBUTE);
        }

        public String getDescription() {
            return "null";
        }
    }

    private static class MongoKeyStringValueDescriptor extends MongoKeyValueDescriptor {

        public MongoKeyStringValueDescriptor(String key, String value) {
            super(key, value, STRING_TEXT_ATTRIBUTE);
        }

        public String getDescription() {
            return String.format(STRING_SURROUNDED, value);
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
