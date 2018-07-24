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

package org.codinjutsu.tools.mongo.view.style;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

import java.awt.*;

public class StyleAttributesProvider {

    private static final Color LIGNT_GREEN = new JBColor(new Color(0, 128, 0), new Color(165, 194, 97));
    private static final Color LIGHT_GRAY = Gray._128;
    public static final Color KEY_COLOR = new JBColor(new Color(102, 14, 122), new Color(204, 120, 50));
    public static final Color NUMBER_COLOR = JBColor.BLUE;

    private static final SimpleTextAttributes INDEX = new SimpleTextAttributes(Font.BOLD, JBColor.BLACK);
    private static final SimpleTextAttributes KEY_VALUE = new SimpleTextAttributes(Font.BOLD, KEY_COLOR);
    private static final SimpleTextAttributes INTEGER_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, NUMBER_COLOR);
    private static final SimpleTextAttributes BOOLEAN_TEXT_ATTRIBUTE = INTEGER_TEXT_ATTRIBUTE;
    private static final SimpleTextAttributes STRING_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, LIGNT_GREEN);
    private static final SimpleTextAttributes NULL_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.ITALIC, LIGHT_GRAY);
    private static final SimpleTextAttributes DOCUMENT_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.BOLD, LIGHT_GRAY);
    private static final SimpleTextAttributes OBJECT_ID_TEXT_ATTRIBUTE = INTEGER_TEXT_ATTRIBUTE;

    public static SimpleTextAttributes getIndexAttribute() {
        return INDEX;
    }

    public static SimpleTextAttributes getKeyValueAttribute() {
        return KEY_VALUE;
    }

    public static SimpleTextAttributes getNumberAttribute() {
        return INTEGER_TEXT_ATTRIBUTE;
    }

    public static SimpleTextAttributes getBooleanAttribute() {
        return BOOLEAN_TEXT_ATTRIBUTE;
    }

    public static SimpleTextAttributes getStringAttribute() {
        return STRING_TEXT_ATTRIBUTE;
    }

    public static SimpleTextAttributes getNullAttribute() {
        return NULL_TEXT_ATTRIBUTE;
    }

    public static SimpleTextAttributes getDocumentAttribute() {
        return DOCUMENT_TEXT_ATTRIBUTE;
    }

    public static SimpleTextAttributes getObjectIdAttribute() {
        return OBJECT_ID_TEXT_ATTRIBUTE;
    }
}
