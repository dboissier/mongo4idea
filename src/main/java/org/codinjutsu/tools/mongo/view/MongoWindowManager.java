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

package org.codinjutsu.tools.mongo.view;

import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.ide.impl.ContentManagerWatcher;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.codinjutsu.tools.mongo.MongoComponent;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;


public class MongoWindowManager  {

    public static final Icon MONGO_ICON = GuiUtils.loadIcon("mongo_logo.png");

    public static final String MONGO_RUNNER = "Mongo Runner";

    private static final String MONGO_EXPLORER = "Mongo Explorer";

    private final Key<Boolean> MONGO_CONTENT_KEY = Key.create("MongoResultManager.MONGO_CONTENT_KEY");


    private ToolWindow mongoResultWindow;
    private Project project;
    private MongoManager mongoManager;
    private MongoExplorerPanel mongoExplorerPanel;

    public static final MongoWindowManager getInstance(Project project) {
        return ServiceManager.getService(project, MongoWindowManager.class);
    }

    public MongoWindowManager(Project project) {
        this.project = project;
        this.mongoManager = MongoManager.getInstance(project);

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        mongoResultWindow = toolWindowManager.registerToolWindow(MONGO_RUNNER, true, ToolWindowAnchor.BOTTOM);
        mongoResultWindow.setToHideOnEmptyContent(true);
        mongoResultWindow.setIcon(MONGO_ICON);

        new ContentManagerWatcher(mongoResultWindow, mongoResultWindow.getContentManager());

        mongoExplorerPanel = new MongoExplorerPanel(project, mongoManager);
        mongoExplorerPanel.installActions();
        Content mongoExplorer = ContentFactory.SERVICE.getInstance().createContent(mongoExplorerPanel, null, false);

        ToolWindow toolMongoExplorerWindow = toolWindowManager.registerToolWindow(MONGO_EXPLORER, false, ToolWindowAnchor.RIGHT);
        toolMongoExplorerWindow.getContentManager().addContent(mongoExplorer);
        toolMongoExplorerWindow.setIcon(MONGO_ICON);
    }

    public void showToolWindow(boolean activateWindow) {
        mongoResultWindow.show(null);
        if (activateWindow && !mongoResultWindow.isActive()) {
            mongoResultWindow.activate(null);
        }
    }

    public void addResultContent(final ServerConfiguration configuration, final MongoCollection mongoCollection) {

        final MongoRunnerPanel mongoRunnerPanel = getOrCreate(configuration, mongoCollection);

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                mongoRunnerPanel.showResults();
            }
        });
    }

    private MongoRunnerPanel getOrCreate(final ServerConfiguration configuration, final MongoCollection mongoCollection) {

        String tabName = String.format("%s/%s/%s", configuration.getLabel(), mongoCollection.getDatabaseName(), mongoCollection.getName());
        Content content = mongoResultWindow.getContentManager().findContent(tabName);
        if (content == null) {
            final MongoRunnerPanel mongoRunnerPanel = new MongoRunnerPanel(project, mongoManager, configuration, mongoCollection);
            Content mongoResultContent = ContentFactory.SERVICE.getInstance().createContent(mongoRunnerPanel, tabName, false);

            mongoRunnerPanel.installActions(new CloseAction(project, mongoResultContent));
            mongoResultContent.putUserData(MONGO_CONTENT_KEY, Boolean.TRUE);  //TODO necessary ??

            mongoResultWindow.getContentManager().addContent(mongoResultContent);
            mongoResultWindow.getContentManager().setSelectedContent(mongoResultContent);

            return mongoRunnerPanel;

        } else {
            mongoResultWindow.getContentManager().setSelectedContent(content);
            return (MongoRunnerPanel) content.getComponent();
        }
    }

    public void unregisterMyself() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(MONGO_RUNNER);
        ToolWindowManager.getInstance(project).unregisterToolWindow(MONGO_EXPLORER);
    }

    public void closeContent(Content content) {
        mongoResultWindow.getContentManager().removeContent(content, true);
    }

    public void apply() {
        mongoExplorerPanel.reloadConfiguration();
    }

    static class CloseAction extends CloseTabToolbarAction {
        private final Project project;
        private final Content content;

        private CloseAction(Project project, Content content) {
            this.project = project;
            this.content = content;
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            MongoWindowManager.getInstance(project).closeContent(content);
        }
    }
}
