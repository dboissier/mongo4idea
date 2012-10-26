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
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.utils.GuiUtil;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;
import org.codinjutsu.tools.mongo.view.MongoRunnerPanel;

public class ViewCollectionValuesAction extends AnAction implements DumbAware {

    private MongoRunnerPanel mongoRunnerPanel;
    private final MongoManager mongoManager;
    private final MongoExplorerPanel mongoExplorerPanel;

    public ViewCollectionValuesAction(MongoRunnerPanel mongoRunnerPanel, MongoManager mongoManager, MongoExplorerPanel mongoExplorerPanel) {
        super("View collection content", "View collection content", GuiUtil.loadIcon("folder_magnify.png"));
        this.mongoRunnerPanel = mongoRunnerPanel;
        this.mongoManager = mongoManager;
        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        mongoExplorerPanel.loadSelectedCollectionValues();
    }
}
