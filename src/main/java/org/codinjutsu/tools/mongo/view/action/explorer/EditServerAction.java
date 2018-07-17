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
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.view.ConfigurationDialog;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

public class EditServerAction extends AnAction {
    private final MongoExplorerPanel mongoExplorerPanel;

    public EditServerAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Edit Server", "Edit the Mongo server configuration", AllIcons.Actions.Edit);

        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        MongoServer mongoServer = mongoExplorerPanel.getSelectedServer();
        ServerConfiguration configuration = mongoServer.getConfiguration();

        ConfigurationDialog dialog = new ConfigurationDialog(event.getProject(), mongoExplorerPanel, configuration);
        dialog.setTitle("Edit a Mongo Server");
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        if (mongoServer.isConnected()) {
            mongoExplorerPanel.loadServerConfiguration(mongoServer);
        }
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoExplorerPanel.getSelectedServer() != null);
    }
}
