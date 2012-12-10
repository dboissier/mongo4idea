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

package org.codinjutsu.tools.mongo;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.ConfigurationPanel;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;
import org.codinjutsu.tools.mongo.view.MongoRunnerPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@State(
        name = MongoComponent.MONGO_COMPONENT_NAME,
        storages = {@Storage(id = "MongoSettings", file = "$PROJECT_FILE$")}
)
public class MongoComponent implements ProjectComponent, Configurable, PersistentStateComponent<MongoConfiguration> {

    public static final String MONGO_COMPONENT_NAME = "Mongo";

    public static final String MONGO_EXPLORER = "Mongo Explorer";
    public static final String MONGO_RUNNER = "Mongo Runner";

    private static final String MONGO_PLUGIN_NAME = "Mongo Plugin";

    private MongoConfiguration configuration;

    private ConfigurationPanel configurationPanel;
    private Project project;
    private MongoManager mongoManager;
    private MongoExplorerPanel mongoExplorerPanel;
    private MongoRunnerPanel mongoRunnerPanel;


    public MongoComponent(Project project) {
        this.project = project;
        this.configuration = new MongoConfiguration();
    }


    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @Nls
    public String getDisplayName() {
        return MONGO_PLUGIN_NAME;
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    @NotNull
    public String getComponentName() {
        return MONGO_COMPONENT_NAME;
    }

    public MongoConfiguration getState() {
        return configuration;
    }

    public void loadState(MongoConfiguration mongoConfiguration) {
        XmlSerializerUtil.copyBean(mongoConfiguration, configuration);
    }

    public void projectOpened() {
        mongoManager = new MongoManager();
        //TODO refactor
        try {
            configuration.setServerVersion(mongoManager.connect(configuration.getServerName(), configuration.getServerPort(), configuration.getUsername(), configuration.getPassword()));
        } catch (org.codinjutsu.tools.mongo.logic.ConfigurationException e) {
            //TODO do something
        }
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        mongoRunnerPanel = new MongoRunnerPanel(project, configuration, mongoManager);
        Content mongoRunner = ContentFactory.SERVICE.getInstance().createContent(mongoRunnerPanel, null, false);
        final ToolWindow toolMongoRunnerWindow = toolWindowManager.registerToolWindow(MONGO_RUNNER, false, ToolWindowAnchor.BOTTOM);
        toolMongoRunnerWindow.getContentManager().addContent(mongoRunner);
        toolMongoRunnerWindow.setIcon(GuiUtils.loadIcon("mongo_logo.png"));

        mongoExplorerPanel = new MongoExplorerPanel(mongoManager, configuration, new RunnerCallback() {

            public void execute(final MongoCollection mongoCollection) {
                toolMongoRunnerWindow.activate(new Runnable() {
                    @Override
                    public void run() {
                        mongoRunnerPanel.showResults(mongoCollection);
                    }
                });
            }
        });
        Content mongoExplorer = ContentFactory.SERVICE.getInstance().createContent(mongoExplorerPanel, null, false);
        ToolWindow toolMongoExplorerWindow = toolWindowManager.registerToolWindow(MONGO_EXPLORER, false, ToolWindowAnchor.RIGHT);
        toolMongoExplorerWindow.getContentManager().addContent(mongoExplorer);
        toolMongoExplorerWindow.setIcon(GuiUtils.loadIcon("mongo_logo.png"));

        mongoRunnerPanel.installActions();
        mongoExplorerPanel.installActions();
    }

    public void projectClosed() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(MONGO_EXPLORER);
    }

    public JComponent createComponent() {
        if (configurationPanel == null) {
            configurationPanel = new ConfigurationPanel(mongoManager);
        }
        return configurationPanel.getRootPanel();
    }

    public boolean isModified() {
        return configurationPanel.isModified(configuration);
    }

    public void apply() throws ConfigurationException {
        configurationPanel.applyConfigurationData(configuration);
        mongoExplorerPanel.reloadConfiguration();
    }

    public void reset() {
        configurationPanel.loadConfigurationData(configuration);
    }

    public void disposeUIResources() {

    }

    public interface RunnerCallback {

        void execute(MongoCollection runnable);
    }
}
