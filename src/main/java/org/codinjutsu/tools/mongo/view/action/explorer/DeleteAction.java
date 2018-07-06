/*
 * Copyright (c) 2017 David Boissier.
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
import com.intellij.openapi.util.SystemInfo;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DeleteAction extends AnAction {
    private final MongoExplorerPanel mongoExplorerPanel;

    public DeleteAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Delete...", "Delete selected item", AllIcons.General.Remove);

        this.mongoExplorerPanel = mongoExplorerPanel;

        if (SystemInfo.isMac) {
            registerCustomShortcutSet(KeyEvent.VK_BACK_SPACE, 0, mongoExplorerPanel);
        } else {
            registerCustomShortcutSet(KeyEvent.VK_DELETE, 0, mongoExplorerPanel);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Object selectedItem = mongoExplorerPanel.getSelectedItem();

        if (selectedItem instanceof MongoServer) {
            MongoServer mongoServer = (MongoServer) selectedItem;
            deleteItem("server", mongoServer.getLabel(),
                    () -> mongoExplorerPanel.removeSelectedServer(mongoServer));
            return;
        }

        if (selectedItem instanceof MongoDatabase) {
            MongoDatabase mongoDatabase = (MongoDatabase) selectedItem;
            deleteItem("database", mongoDatabase.getName(),
                    () -> mongoExplorerPanel.removeSelectedDatabase(mongoDatabase));
            return;
        }

        if (selectedItem instanceof MongoCollection) {
            MongoCollection mongoCollection = (MongoCollection) selectedItem;
            deleteItem("collection", mongoCollection.getName(),
                    () -> mongoExplorerPanel.removeSelectedCollection(mongoCollection));
        }
    }

    private void deleteItem(String itemTypeLabel, String itemLabel, Runnable deleteOperation) {
        int result = JOptionPane.showConfirmDialog(null,
                String.format("Do you REALLY want to remove the '%s' %s?", itemLabel, itemTypeLabel),
                "Warning",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            deleteOperation.run();
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoExplorerPanel.getSelectedItem() != null);
    }
}
