package org.codinjutsu.tools.mongo.view.style;

import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;

public interface StyleAttributesProvider {

    SimpleTextAttributes getIndexAttribute();

    SimpleTextAttributes getKeyValueAttribute();

    SimpleTextAttributes getIntegerAttribute();

    SimpleTextAttributes getBooleanAttribute();

    SimpleTextAttributes getStringAttribute();

    SimpleTextAttributes getNullAttribute();

    SimpleTextAttributes getDBObjectAttribute();

    Icon getAddIcon();

    Icon getCloseIcon();

    Icon getCopyIcon();

    Icon getExecuteIcon();

    Icon getSettingsIcon();

    Icon getRefreshIcon();
}
