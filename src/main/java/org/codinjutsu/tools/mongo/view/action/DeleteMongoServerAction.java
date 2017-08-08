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

package org.codinjutsu.tools.mongo.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

import javax.swing.*;

public class DeleteMongoServerAction extends AnAction {
    private final MongoExplorerPanel mongoExplorerPanel;

    public DeleteMongoServerAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Remove Server", "Remove the Mongo server configuration", AllIcons.General.Remove);

        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        int result = JOptionPane.showConfirmDialog(null,
                String.format("Do you REALLY want to remove the '%s' server?",
                mongoExplorerPanel.getConfiguration().getLabel()),
                "Warning",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            MongoConfiguration mongoConfiguration = MongoConfiguration.getInstance(event.getProject());
            mongoConfiguration.removeServerConfiguration(mongoExplorerPanel.getConfiguration());
            mongoExplorerPanel.removeSelectedServerNode();
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoExplorerPanel.getConfiguration() != null);
    }
}
