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
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class RefreshServerAction extends AnAction implements DumbAware {

    private static final Icon REFRESH = StyleAttributesUtils.getInstance().getRefreshIcon();

    private final MongoExplorerPanel mongoExplorerPanel;

    public RefreshServerAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Refresh server", "Refresh server data", REFRESH);
        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        mongoExplorerPanel.reloadConfiguration(false);
    }
}
