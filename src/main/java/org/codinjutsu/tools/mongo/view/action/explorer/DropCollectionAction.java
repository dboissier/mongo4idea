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

package org.codinjutsu.tools.mongo.view.action.explorer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DropCollectionAction extends AnAction implements DumbAware {

    private final MongoExplorerPanel mongoExplorerPanel;

    public DropCollectionAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Drop collection", "Drop the selected collection", AllIcons.Actions.GC);
        this.mongoExplorerPanel = mongoExplorerPanel;

        registerCustomShortcutSet(KeyEvent.VK_DELETE, 0, mongoExplorerPanel);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        if (mongoExplorerPanel.getSelectedCollection() == null) { //fix in case of deleting a database
            return;
        }

        int result = JOptionPane.showConfirmDialog(null,
                String.format("Do you REALLY want to drop the '%s' collection?",
                        mongoExplorerPanel.getSelectedCollection().getName()), "Warning", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            mongoExplorerPanel.dropCollection();
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoExplorerPanel.getSelectedCollection() != null);
    }
}
