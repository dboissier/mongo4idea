/*
 * Copyright (c) 2016 David Boissier.
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

package org.codinjutsu.tools.mongo.view.action.result;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.SystemInfo;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;

import java.awt.*;
import java.awt.event.KeyEvent;

public class DeleteMongoDocumentAction extends AnAction implements DumbAware {

    private final MongoResultPanel resultPanel;

    public DeleteMongoDocumentAction(MongoResultPanel resultPanel) {
        super("Delete", "Delete this document", AllIcons.Actions.Delete);
        this.resultPanel = resultPanel;

        int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if (SystemInfo.isMac) {
            registerCustomShortcutSet(KeyEvent.VK_BACK_SPACE, menuShortcutKeyMask, resultPanel);
        } else {
            registerCustomShortcutSet(KeyEvent.VK_DELETE,menuShortcutKeyMask, resultPanel);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        resultPanel.deleteSelectedMongoDocument();
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setEnabled(resultPanel.isSelectedNodeId());
    }
}
