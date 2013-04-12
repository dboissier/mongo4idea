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

package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class CopyResultAction extends AnAction implements DumbAware {

    private static final Icon COPY_ICON = StyleAttributesUtils.getInstance().getCopyIcon();
    private final MongoResultPanel mongoResultPanel;

    public CopyResultAction(MongoResultPanel mongoResultPanel) {
        super("Copy", "Copy results to clipboard", COPY_ICON);
        this.mongoResultPanel = mongoResultPanel;

        registerCustomShortcutSet(KeyEvent.VK_C, InputEvent.CTRL_MASK, mongoResultPanel);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        CopyPasteManager.getInstance().setContents(new StringSelection(mongoResultPanel.getSelectedNodeStringifiedValue()));
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(mongoResultPanel.isNotEmpty());
    }
}
