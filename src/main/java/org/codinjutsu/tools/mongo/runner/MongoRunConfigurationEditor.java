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

package org.codinjutsu.tools.mongo.runner;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class MongoRunConfigurationEditor extends SettingsEditor<MongoRunConfiguration> {

    private JPanel mainPanel;

    private JTextField scriptPathField;
    private ComboBox serverConfigurationCombobox;
    private JTextField databaseField;
    private JPanel mongoShellOptionsPanel;
    private RawCommandLineEditor shellParametersField;
    private TextFieldWithBrowseButton shellWorkingDirField;


    public MongoRunConfigurationEditor(Project project) {
        List<ServerConfiguration> serverConfigurationList = MongoConfiguration.getInstance(project).getServerConfigurations();
        ServerConfiguration[] serverConfigurations = serverConfigurationList.toArray(new ServerConfiguration[serverConfigurationList.size()]);

        serverConfigurationCombobox.setModel(new DefaultComboBoxModel(serverConfigurations));

        serverConfigurationCombobox.setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                ServerConfiguration serverConfiguration = (ServerConfiguration) value;
                append(serverConfiguration.getLabel());
            }
        });

        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));

        shellParametersField.setDialogCaption("Mongo arguments");
    }

    @Override
    protected void resetEditorFrom(MongoRunConfiguration configuration) {
        scriptPathField.setText(configuration.getScriptPath() != null ? configuration.getScriptPath().getPath() : null);
        shellParametersField.setText(configuration.getShellParameters());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
    }

    @Override
    protected void applyEditorTo(MongoRunConfiguration configuration) throws ConfigurationException {
        configuration.setScriptPath(getScriptPath());
        configuration.setServerConfiguration(getSelectedConfiguration());
        configuration.setDatabase(getDatabase());
        configuration.setShellParameters(getShellParameters());
        configuration.setShellWorkingDir(getShellWorkingDir());
    }

    private String getScriptPath() {
        return scriptPathField.getText();
    }

    private String getShellParameters() {
        return shellParametersField.getText();
    }

    private ServerConfiguration getSelectedConfiguration() {
        return (ServerConfiguration) serverConfigurationCombobox.getSelectedItem();
    }

    public MongoDatabase getDatabase() {
        return new MongoDatabase(databaseField.getText());
    }

    private String getShellWorkingDir() {
        String shellWorkingDir = shellWorkingDirField.getText();
        if (StringUtils.isNotBlank(shellWorkingDir)) {
            return shellWorkingDir;
        }

        return null;
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }

    @Override
    protected void disposeEditor() {
        mainPanel = null;
    }

    private void createUIComponents() {
        shellWorkingDirField = new TextFieldWithBrowseButton();
        shellWorkingDirField.addBrowseFolderListener("Mongo shell working directory", "", null,
                new FileChooserDescriptor(false, true, false, false, false, false));
        shellWorkingDirField.setName("shellWorkingDirField");
    }
}
