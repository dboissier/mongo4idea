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
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RefreshServerAction extends AnAction implements DumbAware {

    private static final Icon CONNECT_ICON = GuiUtils.loadIcon("connector.png", "connector_dark.png");
    private static final Icon REFRESH_ICON = AllIcons.Actions.Refresh;
    private static final String REFRESH_TEXT = "Refresh this server";
    private static final String CONNECT_TEXT = "Connect to this server";

    private final MongoExplorerPanel mongoExplorerPanel;

    public RefreshServerAction(MongoExplorerPanel mongoExplorerPanel) {
        super(REFRESH_TEXT);
        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        MongoServer selectedMongoServer = mongoExplorerPanel.getSelectedServer();
        mongoExplorerPanel.openServer(selectedMongoServer);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        MongoServer mongoServer = mongoExplorerPanel.getSelectedServer();
        event.getPresentation().setVisible(mongoServer != null);
        if (mongoServer == null) {
            return;
        }

        boolean isLoading = MongoServer.Status.LOADING.equals(mongoServer.getStatus());
        event.getPresentation().setEnabled(!isLoading);

        boolean isConnected = mongoServer.isConnected();
        event.getPresentation().setIcon(isConnected ? REFRESH_ICON : CONNECT_ICON);
        event.getPresentation().setText(isConnected ? REFRESH_TEXT : CONNECT_TEXT);
    }
}
