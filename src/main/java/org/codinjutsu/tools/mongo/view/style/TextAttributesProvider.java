package org.codinjutsu.tools.mongo.view.style;

import com.intellij.ui.SimpleTextAttributes;

public interface TextAttributesProvider {

    SimpleTextAttributes getIndexAttribute();

    SimpleTextAttributes getKeyValueAttribute();

    SimpleTextAttributes getIntegerAttribute();

    SimpleTextAttributes getBooleanAttribute();

    SimpleTextAttributes getStringAttribute();

    SimpleTextAttributes getNullAttribute();

    SimpleTextAttributes getDBObjectAttribute();
}
