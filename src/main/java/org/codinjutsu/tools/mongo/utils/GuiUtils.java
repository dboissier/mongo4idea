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

package org.codinjutsu.tools.mongo.utils;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class GuiUtils {

    private static final String ICON_FOLDER = "/icons/";
    public static final Color ERROR_COLOR = new Color(255, 220, 220);

    public static Icon loadIcon(String iconFilename) {
        return IconLoader.findIcon(ICON_FOLDER + iconFilename);
    }

    public static URL getIconResource(String iconFilename) {
        return GuiUtils.class.getResource(ICON_FOLDER + iconFilename);
    }

    public static void installActionGroupInToolBar(DefaultActionGroup actionGroup, JPanel toolBarPanel, ActionManager actionManager, String toolbarName, boolean horizontal) {
        if (actionManager == null) {
            return;
        }

        JComponent actionToolbar = ActionManager.getInstance()
                .createActionToolbar(toolbarName, actionGroup, horizontal).getComponent();
        toolBarPanel.add(actionToolbar, BorderLayout.CENTER);
    }

    public static boolean isUnderDarcula() {
        return UIManager.getLookAndFeel().getName().contains("Darcula");
    }
}
