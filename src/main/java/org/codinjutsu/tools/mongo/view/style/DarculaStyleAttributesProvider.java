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

public class DarculaStyleAttributesProvider implements StyleAttributesProvider {

    private static final Color GREEN = new Color(165, 194, 97);
    public static final Color ORANGE = new Color(204, 120, 50);
    private static final Color GRAY = new Color(128, 128, 128);
    public static final Color BLUE = new Color(104, 151, 187);

    private static final SimpleTextAttributes INDEX = new SimpleTextAttributes(Font.BOLD, Color.WHITE);
    private static final SimpleTextAttributes KEY_VALUE = new SimpleTextAttributes(Font.BOLD, ORANGE);
    private static final SimpleTextAttributes NUMBER_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, BLUE);
    private static final SimpleTextAttributes BOOLEAN_TEXT_ATTRIBUTE = NUMBER_TEXT_ATTRIBUTE;
    private static final SimpleTextAttributes STRING_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, GREEN);
    private static final SimpleTextAttributes NULL_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.ITALIC, GRAY);
    private static final SimpleTextAttributes DBOBJECT_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.BOLD, GRAY);
    private static final SimpleTextAttributes OBJECT_ID_TEXT_ATTRIBUTE = NUMBER_TEXT_ATTRIBUTE;

    @Override
    public SimpleTextAttributes getIndexAttribute() {
        return INDEX;
    }

    @Override
    public SimpleTextAttributes getKeyValueAttribute() {
        return KEY_VALUE;
    }

    @Override
    public SimpleTextAttributes getNumberAttribute() {
        return NUMBER_TEXT_ATTRIBUTE;
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
    public SimpleTextAttributes getObjectIdAttribute() {
        return OBJECT_ID_TEXT_ATTRIBUTE;
    }

    @Override
    public Icon getAddIcon() {
        return GuiUtils.loadIcon("add_dark.png");
    }

    @Override
    public Icon getCloseIcon() {
        return GuiUtils.loadIcon("close_dark.png");
    }

    @Override
    public Icon getCopyIcon() {
        return GuiUtils.loadIcon("copy_dark.png");
    }

    @Override
    public Icon getExecuteIcon() {
        return GuiUtils.loadIcon("execute_dark.png");
    }

    @Override
    public Icon getSettingsIcon() {
        return GuiUtils.loadIcon("pluginSettings_dark.png");
    }

    @Override
    public Icon getRefreshIcon() {
        return GuiUtils.loadIcon("refresh_dark.png");
    }

    @Override
    public Icon getEditIcon() {
        return GuiUtils.loadIcon("edit_dark.png");
    }

    @Override
    public Icon getClearAllIcon() {
        return GuiUtils.loadIcon("clearAll_dark.png");
    }

    @Override
    public Icon getDeleteIcon() {
        return GuiUtils.loadIcon("delete_dark.png");
    }

    @Override
    public Icon getFindIcon() {
        return GuiUtils.loadIcon("find_dark.png");
    }
}
