package org.codinjutsu.tools.mongo.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

@SuppressWarnings("Convert2MethodRef")
public abstract class StatInfoEntry<T> {

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
            return value.toString();
        }
    }

    public static class ByteSizeStatInfoEntry extends StatInfoEntry<Number> {

        public ByteSizeStatInfoEntry(String key, Number value) {
            super(key, value);
        }

        @Override
        public String getStringifiedValue() {
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
