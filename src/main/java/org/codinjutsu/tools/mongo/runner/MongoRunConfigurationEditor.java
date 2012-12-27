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

package org.codinjutsu.tools.mongo.runner;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.ui.NumberDocument;
import com.intellij.ui.RawCommandLineEditor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MongoRunConfigurationEditor extends SettingsEditor<MongoRunConfiguration> {

    private JTextField scriptPathField;
    private JCheckBox overwriteDefaultParameterCheckBox;
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private RawCommandLineEditor shellParametersField;

    private JPanel mainPanel;


    public MongoRunConfigurationEditor() {

        portField.setDocument(new NumberDocument());
        setConnectionParameterFieldsEnabled(false);

        overwriteDefaultParameterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean enabled = isOverwriteDefaultParameters();
                setConnectionParameterFieldsEnabled(enabled);
            }
        });
    }

    private void setConnectionParameterFieldsEnabled(boolean enabled) {
        hostField.setEnabled(enabled);
        portField.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }

    @Override
    protected void resetEditorFrom(MongoRunConfiguration configuration) {
//        scriptPathField.setText(configuration.getScriptPath());
        shellParametersField.setText(configuration.getShellParameters());
        overwriteDefaultParameterCheckBox.setSelected(configuration.isOverwriteDefaultParameters());
        if (configuration.isOverwriteDefaultParameters()) {
            hostField.setText(configuration.getHost());
            portField.setText(String.valueOf(configuration.getPort()));
            usernameField.setText(configuration.getUsername());
            passwordField.setText(configuration.getPassword());
        }
    }

    @Override
    protected void applyEditorTo(MongoRunConfiguration configuration) throws ConfigurationException {
        configuration.setScriptPath(getScriptPath());
        configuration.setShellParameters(getShellParameters());
        configuration.setOverwriteDefaultParameters(isOverwriteDefaultParameters());
        if (configuration.isOverwriteDefaultParameters()) {
            configuration.setHost(getHost());
            configuration.setPort(getPort());
            configuration.setUsername(getUsername());
            configuration.setPassword(getPassword());
        }
    }

    private String getScriptPath() {
        return scriptPathField.getText();
    }

    private String getShellParameters() {
        return shellParametersField.getText();
    }

    private boolean isOverwriteDefaultParameters() {
        return overwriteDefaultParameterCheckBox.isSelected();
    }

    private String getHost() {
        return hostField.getText();
    }

    private String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }

    private String getUsername() {
        return usernameField.getText();
    }

    private Integer getPort() {
        String portStrValue = portField.getText();
        if (StringUtils.isEmpty(portStrValue)) return 0;
        return Integer.valueOf(portStrValue);
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
}
