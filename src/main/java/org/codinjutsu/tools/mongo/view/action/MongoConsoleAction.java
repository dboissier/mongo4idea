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

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;
import org.codinjutsu.tools.mongo.view.console.MongoConsoleRunner;

public class MongoConsoleAction extends AnAction implements DumbAware {


    private final MongoExplorerPanel mongoExplorerPanel;

    public MongoConsoleAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Mongo Shell...", "Mongo Shell", GuiUtils.loadIcon("toolConsole.png"));
        this.mongoExplorerPanel = mongoExplorerPanel;
    }


    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);

        boolean enabled = project != null;
        if (!enabled) {
            return;
        }

        MongoConfiguration configuration = MongoConfiguration.getInstance(project);

        e.getPresentation().setVisible(configuration != null && StringUtils.isNotBlank(configuration.getShellPath()));
        e.getPresentation().setEnabled(mongoExplorerPanel.getSelectedDatabase() != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        assert project != null;

        runShell(project);
    }

    private void runShell(Project project) {
        MongoConsoleRunner consoleRunner = new MongoConsoleRunner(project, mongoExplorerPanel.getConfiguration(), mongoExplorerPanel.getSelectedDatabase());
        try {
            consoleRunner.initAndRun();
        } catch (ExecutionException e1) {
            throw new RuntimeException(e1);
        }
    }
}
