package org.codinjutsu.tools.mongo.view.style;

import com.intellij.ui.SimpleTextAttributes;

import java.awt.*;

public class DarculaTextAttributesProvider implements TextAttributesProvider {

    private static final Color GREEN = new Color(165, 194, 97);
    private static final Color ORANGE = new Color(204, 120, 50);
    private static final Color GRAY = new Color(128, 128, 128);
    private static final Color BLUE = new Color(104, 151, 187);

    private static final SimpleTextAttributes INDEX = new SimpleTextAttributes(Font.BOLD, Color.WHITE);
    private static final SimpleTextAttributes KEY_VALUE = new SimpleTextAttributes(Font.BOLD, ORANGE);
    private static final SimpleTextAttributes INTEGER_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, BLUE);
    private static final SimpleTextAttributes BOOLEAN_TEXT_ATTRIBUTE = INTEGER_TEXT_ATTRIBUTE;
    private static final SimpleTextAttributes STRING_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, GREEN);
    private static final SimpleTextAttributes NULL_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.ITALIC, GRAY);
    private static final SimpleTextAttributes DBOBJECT_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.BOLD, GRAY);

    @Override
    public SimpleTextAttributes getIndexAttribute() {
        return INDEX;
    }

    @Override
    public SimpleTextAttributes getKeyValueAttribute() {
        return KEY_VALUE;
    }

    @Override
    public SimpleTextAttributes getIntegerAttribute() {
        return INTEGER_TEXT_ATTRIBUTE;
    }

    @Override
    public SimpleTextAttributes getBooleanAttribute() {
        return BOOLEAN_TEXT_ATTRIBUTE;
    }

    @Override
    public SimpleTextAttributes getStringAttribute() {
        return STRING_TEXT_ATTRIBUTE;
    }

    @Override
    public SimpleTextAttributes getNullAttribute() {
        return NULL_TEXT_ATTRIBUTE;
    }

    @Override
    public SimpleTextAttributes getDBObjectAttribute() {
        return DBOBJECT_TEXT_ATTRIBUTE;
    }
}
