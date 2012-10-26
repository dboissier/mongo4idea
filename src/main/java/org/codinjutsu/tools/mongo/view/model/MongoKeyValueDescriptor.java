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

package org.codinjutsu.tools.mongo.view.model;

public class MongoKeyValueDescriptor<T> {

    protected final String key;
    protected final T value;

    public MongoKeyValueDescriptor(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public String getDescription() {
        return String.format("\"%s\": %s", key, value);
    }


    private static class MongoKeyStringValueDescriptor extends MongoKeyValueDescriptor<String> {

        public MongoKeyStringValueDescriptor(String key, String value) {
            super(key, value);
        }

        public String getDescription() {
            return String.format("\"%s\": \"%s\"", key, value);
        }
    }


    private static class MongoKeyBooleanValueDescriptor extends MongoKeyValueDescriptor<Boolean> {

        public MongoKeyBooleanValueDescriptor(String key, Boolean value) {
            super(key, value);
        }
    }

    private static class MongoKeyIntegerValueDescriptor extends MongoKeyValueDescriptor<Integer> {

        public MongoKeyIntegerValueDescriptor(String key, Integer value) {
            super(key, value);
        }
    }



    public static MongoKeyValueDescriptor createDescriptor(String key, Object value) {
        if (value instanceof String) {
            return new MongoKeyStringValueDescriptor(key, (String) value);
        } else if (value instanceof Boolean) {
            return new MongoKeyBooleanValueDescriptor(key, (Boolean) value);
        } else if (value instanceof Integer) {
            return new MongoKeyIntegerValueDescriptor(key, (Integer) value);
        }
        throw new IllegalArgumentException("unsupported " + value.getClass());
    }
}
