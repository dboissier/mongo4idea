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

package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.utils.GuiUtil;
import org.codinjutsu.tools.mongo.view.MongoRunnerPanel;

import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class CopyResultAction extends AnAction implements DumbAware{

    private final MongoRunnerPanel mongoRunnerPanel;

    public CopyResultAction(MongoRunnerPanel mongoRunnerPanel) {
        super("Copy (CTRL+C)", "Copy to clipboard", GuiUtil.loadIcon("copy.png"));
        this.mongoRunnerPanel = mongoRunnerPanel;

        registerCustomShortcutSet(KeyEvent.VK_C, InputEvent.CTRL_MASK, mongoRunnerPanel);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        CopyPasteManager.getInstance().setContents(new StringSelection(mongoRunnerPanel.getSelectedNodeStringifiedValue()));
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(mongoRunnerPanel.getSelectedNodeStringifiedValue() != null);
    }
}
