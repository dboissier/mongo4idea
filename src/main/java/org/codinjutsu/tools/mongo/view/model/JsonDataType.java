package org.codinjutsu.tools.mongo.view.model;

public enum JsonDataType {

    STRING("String"),
    NUMBER("Number"),
    BOOLEAN("Boolean"),
    ARRAY("Array"),
    OBJECT("Object"),
    NULL("Null");

    public final String type;

    private JsonDataType(String type) {
        this.type = type;
    }
}
