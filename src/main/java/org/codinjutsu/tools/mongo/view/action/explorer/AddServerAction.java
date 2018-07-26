/*
 * Copyright (c) 2018 David Boissier.
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
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.view.ConfigurationDialog;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

public class AddServerAction extends AnAction implements DumbAware {
    private final MongoExplorerPanel mongoExplorerPanel;

    public AddServerAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Add Server", "Add a Mongo server configuration", AllIcons.General.Add);

        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ServerConfiguration serverConfiguration = ServerConfiguration.byDefault();

        ConfigurationDialog dialog = new ConfigurationDialog(event.getProject(), mongoExplorerPanel, serverConfiguration);
        dialog.setTitle("Add a Mongo Server");
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }

        MongoConfiguration mongoConfiguration = MongoConfiguration.getInstance(event.getProject());
        mongoConfiguration.addServerConfiguration(serverConfiguration);
        mongoExplorerPanel.addConfiguration(serverConfiguration);
    }
}
