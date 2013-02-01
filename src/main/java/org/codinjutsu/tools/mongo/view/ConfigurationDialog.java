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

import com.intellij.openapi.ui.DialogWrapper;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConfigurationDialog extends DialogWrapper {

    private final MongoManager mongoManager;
    private final ServerConfiguration configuration;
    private ServerConfigurationPanel serverConfigurationPanel;

    public ConfigurationDialog(Component parent, MongoManager mongoManager, ServerConfiguration configuration) {
        super(parent, true);
        this.mongoManager = mongoManager;
        this.configuration = configuration;

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        serverConfigurationPanel = new ServerConfigurationPanel(mongoManager);
        serverConfigurationPanel.loadConfigurationData(configuration);
        return serverConfigurationPanel.getRootPanel();
    }


    @Override
    protected void doOKAction() {
        serverConfigurationPanel.applyConfigurationData(configuration);
        super.doOKAction();
    }
}
