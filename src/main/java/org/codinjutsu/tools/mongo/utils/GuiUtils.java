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
import com.intellij.openapi.util.IconLoader;
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
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

//    Copy from private method com.intellij.util.ui.tree.TreeUtils#expand need to expand specifically some node instead of the whole tree
    public static void expand(@NotNull JTree tree, @NotNull TreePath path, int levels) {
        if (levels == 0) return;
        tree.expandPath(path);
        TreeNode node = (TreeNode)path.getLastPathComponent();
        Enumeration children = node.children();
        while (children.hasMoreElements()) {
            expand(tree, path.pathByAddingChild(children.nextElement()) , levels - 1);
        }
    }
}
