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
import org.codinjutsu.tools.mongo.utils.GuiUtil;
import org.codinjutsu.tools.mongo.view.ConfigurationPanel;
import org.codinjutsu.tools.mongo.view.MongoExplorer;
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

    private static final String MONGO_PLUGIN_NAME = "Mongo Plugin";

    private MongoConfiguration configuration;

    private ConfigurationPanel configurationPanel;
    private Project project;
    private MongoManager mongoManager;


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

        Content content = ContentFactory.SERVICE.getInstance()
                .createContent(new MongoExplorer(mongoManager, configuration), null, false);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(MONGO_EXPLORER, false, ToolWindowAnchor.RIGHT);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setIcon(GuiUtil.loadIcon("mongo_16x16.png"));
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
    }

    public void reset() {
        configurationPanel.loadConfigurationData(configuration);
    }

    public void disposeUIResources() {

    }
}
