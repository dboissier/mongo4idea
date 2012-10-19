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
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.view.ConfigurationPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@State(
        name = MongoComponent.MONGO_COMPONENT_NAME,
        storages = {@Storage(id = "MongoSettings", file = "$PROJECT_FILE$")}
)
public class MongoComponent implements ProjectComponent, Configurable, PersistentStateComponent<MongoConfiguration> {

    public static final String MONGO_COMPONENT_NAME = "Mongo";

    private static final String MONGO_PLUGIN_NAME = "Mongo Plugin";

    private MongoConfiguration configuration;

    private ConfigurationPanel configurationPanel;


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

    }

    public void projectClosed() {

    }

    public JComponent createComponent() {
        if (configurationPanel == null) {
            MongoManager mongoManager = new MongoManager();
            configurationPanel = new ConfigurationPanel(mongoManager);
        }
        return configurationPanel.getRootPanel();
    }

    public boolean isModified() {
        return configurationPanel.isModified(configuration);
    }

    public void apply() throws ConfigurationException {

    }

    public void reset() {

    }

    public void disposeUIResources() {

    }
}
