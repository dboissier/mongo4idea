/*
 * Copyright (c) 2018 David Boissier.
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

package org.codinjutsu.tools.mongo.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.text.NumberFormat;

@SuppressWarnings("Convert2MethodRef")
public abstract class StatInfoEntry<T> {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private final String key;
    protected final T value;

    static DataExtractor<Number> NUMBER_EXTRACTOR = (key, document) -> {
        if (document.get(key) instanceof Double) {
            return document.getDouble(key);
        } else {
            return document.getInteger(key);
        }
    };
    static DataExtractor<Boolean> BOOLEAN_EXTRACTOR = (key, document) -> document.getBoolean(key, false);
    static DataExtractor<Document> DOCUMENT_EXTRACTOR = (key, document) -> (Document) document.get(key);

    static DataBuilder<Number> NUMBER_DATA_BUILDER = (key, value) -> new NumberStatInfoEntry(key, value);
    static DataBuilder<Number> BYTE_SIZE_DATA_BUILDER = (key, value) -> new ByteSizeStatInfoEntry(key, value);
    static DataBuilder<Boolean> BOOLEAN_DATA_BUILDER = (key, value) -> new BooleanStatInfoEntry(key, value);
    static DataBuilder<Document> EMPTY_DATA_BUILDER = (key, value) -> new EmptyInfoEntry(key, value);

    StatInfoEntry(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public abstract String getStringifiedValue();

    public static class NumberStatInfoEntry extends StatInfoEntry<Number> {

        NumberStatInfoEntry(String key, Number value) {
            super(key, value);
        }

        @Override
        public String getStringifiedValue() {
            if (value == null) {
                return "null";
            }
            return NUMBER_FORMAT.format(value);
        }
    }

    public static class ByteSizeStatInfoEntry extends StatInfoEntry<Number> {

        public ByteSizeStatInfoEntry(String key, Number value) {
            super(key, value);
        }

        @Override
        public String getStringifiedValue() {
            if (value == null) {
                return "null";
            }
            return FileUtils.byteCountToDisplaySize(value.longValue());
        }
    }

    public static class BooleanStatInfoEntry extends StatInfoEntry<Boolean> {
        BooleanStatInfoEntry(String key, Boolean value) {
            super(key, value);
        }

        @Override
        public String getStringifiedValue() {
            return value.toString();
        }
    }

    private static class EmptyInfoEntry extends StatInfoEntry<Document> {
        EmptyInfoEntry(String key, Document value) {
            super(key, value);
        }

        @Override
        public String getStringifiedValue() {
            return StringUtils.EMPTY;
        }


    }

    public interface DataExtractor<T> {
        T extract(String key, Document document);

    }

    public interface DataBuilder<T> {
        StatInfoEntry build(String key, T value);

    }
}
