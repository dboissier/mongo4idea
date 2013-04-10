/*
 * Copyright (c) 2013 David Boissier
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

package org.codinjutsu.tools.mongo.view.style;

import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class DefaultStyleAttributesProvider implements StyleAttributesProvider {

    private static final Color LIGNT_GREEN = new Color(0, 128, 0);
    private static final Color PURPLE = new Color(102, 14, 122);
    private static final Color LIGHT_GRAY = new Color(128, 128, 128);

    private static final SimpleTextAttributes INDEX = new SimpleTextAttributes(Font.BOLD, Color.BLACK);
    private static final SimpleTextAttributes KEY_VALUE = new SimpleTextAttributes(Font.BOLD, PURPLE);
    private static final SimpleTextAttributes INTEGER_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, Color.BLUE);
    private static final SimpleTextAttributes BOOLEAN_TEXT_ATTRIBUTE = INTEGER_TEXT_ATTRIBUTE;
    private static final SimpleTextAttributes STRING_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, LIGNT_GREEN);
    private static final SimpleTextAttributes NULL_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.ITALIC, LIGHT_GRAY);
    private static final SimpleTextAttributes DBOBJECT_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.BOLD, LIGHT_GRAY);

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

    @Override
    public Icon getAddIcon() {
        return GuiUtils.loadIcon("add.png");
    }

    @Override
    public Icon getCloseIcon() {
        return GuiUtils.loadIcon("close.png");
    }

    @Override
    public Icon getCopyIcon() {
        return GuiUtils.loadIcon("copy.png");
    }

    @Override
    public Icon getExecuteIcon() {
        return GuiUtils.loadIcon("execute.png");
    }

    @Override
    public Icon getSettingsIcon() {
        return GuiUtils.loadIcon("pluginSettings.png");
    }

    @Override
    public Icon getRefreshIcon() {
        return GuiUtils.loadIcon("refresh.png");
    }
}
