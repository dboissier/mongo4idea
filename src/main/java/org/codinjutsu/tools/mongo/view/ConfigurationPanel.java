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

package org.codinjutsu.tools.mongo.view;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;

import javax.swing.*;

public class ConfigurationPanel {

    private JPanel rootPanel;

    private JTextField serverNameField;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton testConnectionButton;
    private JLabel feeedbackLabel;


    public ConfigurationPanel() {
        serverNameField.setName("serverNameField");
        serverPortField.setName("serverPortField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isModified(MongoConfiguration configuration) {

        return !(StringUtils.equals(configuration.getServerName(), serverNameField.getText())
                && (configuration.getServerPort() == Integer.valueOf(serverNameField.getText()))
                && StringUtils.equals(configuration.getUsername(), usernameField.getText())
                && StringUtils.equals(configuration.getPassword(), String.valueOf(passwordField.getPassword())))
                ;
    }

    public void applyConfigurationData(MongoConfiguration configuration) {
        configuration.setServerName(serverNameField.getText());
        configuration.setServerPort(Integer.valueOf(serverPortField.getText()));
        configuration.setUsername(usernameField.getText());
        configuration.setPassword(String.valueOf(passwordField.getPassword()));
    }

    public void loadConfigurationData(MongoConfiguration configuration) {
        serverNameField.setText(configuration.getServerName());
        serverPortField.setText(Integer.toString(configuration.getServerPort()));
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
    }
}
