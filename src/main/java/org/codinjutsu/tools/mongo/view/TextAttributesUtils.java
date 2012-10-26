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

package org.codinjutsu.tools.mongo.view;

import com.intellij.ui.SimpleTextAttributes;

import java.awt.*;

public class TextAttributesUtils {

    private static final Color LIGNT_GREEN = new Color(0, 128, 0);

    public static final SimpleTextAttributes KEY_VALUE = new SimpleTextAttributes(Font.BOLD, Color.BLACK);
    public static final SimpleTextAttributes INTEGER_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, Color.BLUE);
    public static final SimpleTextAttributes BOOLEAN_TEXT_ATTRIBUTE = INTEGER_TEXT_ATTRIBUTE;
    public static final SimpleTextAttributes STRING_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.PLAIN, Color.RED);
    public static final SimpleTextAttributes NULL_TEXT_ATTRIBUTE = new SimpleTextAttributes(Font.ITALIC, Color.LIGHT_GRAY);
    public static final SimpleTextAttributes DBOBJECT_TEXT_ATTRIBUTE = new SimpleTextAttributes(SimpleTextAttributes.STYLE_UNDERLINE | SimpleTextAttributes.STYLE_BOLD, LIGNT_GREEN);
}
