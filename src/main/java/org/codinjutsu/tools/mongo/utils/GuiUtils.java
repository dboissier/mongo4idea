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

package org.codinjutsu.tools.mongo.utils;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.net.URL;
import java.util.Enumeration;

public class GuiUtils {

    private static final String ICON_FOLDER = "/icons/";

    public static Icon loadIcon(String iconFilename) {
        return IconLoader.findIcon(ICON_FOLDER + iconFilename);
    }

    public static Icon loadIcon(String iconFilename, String darkIconFilename) {
        String iconPath = ICON_FOLDER;
        if (isUnderDarcula()) {
            iconPath += darkIconFilename;
        } else {
            iconPath += iconFilename;
        }
        return IconLoader.findIcon(iconPath);
    }

    public static URL getIconResource(String iconFilename) {
        return GuiUtils.class.getResource(ICON_FOLDER + iconFilename);
    }

    public static void installActionGroupInToolBar(DefaultActionGroup actionGroup, JPanel toolBarPanel, ActionManager actionManager, String toolbarName, boolean horizontal) {
        if (actionManager == null) {
            return;
        }

        JComponent actionToolbar = ActionManager.getInstance().createActionToolbar(toolbarName, actionGroup, horizontal).getComponent();
        toolBarPanel.add(actionToolbar, BorderLayout.CENTER);
    }

    public static boolean isUnderDarcula() {
        return UIManager.getLookAndFeel().getName().contains("Darcula");
    }

    public static void runInSwingThread(Runnable runnable) {
        ApplicationManager.getApplication().invokeLater(runnable);
    }

    //    Copy from private method com.intellij.util.ui.tree.TreeUtils#expand need to expand specifically some node instead of the whole tree
    public static void expand(@NotNull JTree tree, @NotNull TreePath path, int levels) {
        if (levels == 0) return;
        tree.expandPath(path);
        TreeNode node = (TreeNode) path.getLastPathComponent();
        Enumeration children = node.children();
        while (children.hasMoreElements()) {
            expand(tree, path.pathByAddingChild(children.nextElement()), levels - 1);
        }
    }

    public static Dimension enlargeWidth(Dimension preferredSize, double factor) {
        int enlargedWidth = new Double(preferredSize.width * factor).intValue();
        return new Dimension(enlargedWidth, preferredSize.height);
    }

    public static void showNotification(final JComponent component, final MessageType info, final String message, final Balloon.Position position) {
        runInSwingThread(new Runnable() {
            @Override
            public void run() {
                JBPopupFactory.getInstance().createBalloonBuilder(new JLabel(message))
                        .setFillColor(info.getPopupBackground())
                        .createBalloon()
                        .show(new RelativePoint(component, new Point(0, 0)), position);
            }
        });
    }
}
