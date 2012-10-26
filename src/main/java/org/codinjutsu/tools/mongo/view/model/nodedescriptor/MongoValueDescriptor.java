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

public class MongoValueDescriptor<T> {

    private final int index;
    protected final T value;

    public MongoValueDescriptor(int index, T value) {
        this.index = index;
        this.value = value;
    }

    public String getDescription() {
        return value.toString();
    }

    //voir comment afficher l'info lors du rendu
    public String getIndex() {
        return  String.format("[%s]", index);
    }



    public static class MongoBooleanValueDescriptor extends MongoValueDescriptor<Boolean>{

        public MongoBooleanValueDescriptor(int index, Boolean booleanValue) {
            super(index, booleanValue);
        }
    }

    public static class MongoIntegerValueDescriptor extends MongoValueDescriptor<Integer>{

        public MongoIntegerValueDescriptor(int index, Integer integerValue) {
            super(index, integerValue);
        }
    }

    public static class MongoStringValueDescriptor extends MongoValueDescriptor<String> {

        public MongoStringValueDescriptor(int index, String value) {
            super(index, value);
        }

        public String getDescription() {
            return String.format("\"%s\"", value);
        }
    }
    
    public static MongoValueDescriptor createDescriptor(int index, Object value) {
        if (value instanceof String) {
            return new MongoStringValueDescriptor(index, (String) value);
        } else if (value instanceof Boolean) {
            return new MongoBooleanValueDescriptor(index, (Boolean) value);
        } else if (value instanceof Integer) {
            return new MongoIntegerValueDescriptor(index, (Integer) value);
        }
        throw new IllegalArgumentException("unsupported " + value.getClass());
    }
}
