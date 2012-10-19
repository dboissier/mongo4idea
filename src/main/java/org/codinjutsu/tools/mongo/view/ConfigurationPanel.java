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
import org.codinjutsu.tools.mongo.logic.MongoManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigurationPanel {

    private JPanel rootPanel;

    private JTextField serverNameField;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton testConnectionButton;
    private JLabel feeedbackLabel;
    private final MongoManager mongoManager;


    public ConfigurationPanel(MongoManager mongoManager) {
        this.mongoManager = mongoManager;

        serverNameField.setName("serverNameField");
        serverPortField.setName("serverPortField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");

        initListeners();
    }

    private void initListeners() {
        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mongoManager.connect(getServerName(), getServerPort(), getUsername(), getPassword());
            }
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isModified(MongoConfiguration configuration) {

        return !(StringUtils.equals(configuration.getServerName(), getServerName())
                && (configuration.getServerPort() == getServerPort())
                && StringUtils.equals(configuration.getUsername(), getUsername())
                && StringUtils.equals(configuration.getPassword(), getPassword()))
                ;
    }

    public void applyConfigurationData(MongoConfiguration configuration) {
        configuration.setServerName(getServerName());
        configuration.setServerPort(getServerPort());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
    }

    private String getServerName() {
        return StringUtils.trim(serverNameField.getText());
    }

    private String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }

    private String getUsername() {
        return StringUtils.trim(usernameField.getText());
    }

    private int getServerPort() {
        return Integer.valueOf(serverPortField.getText());
    }

    public void loadConfigurationData(MongoConfiguration configuration) {
        serverNameField.setText(configuration.getServerName());
        serverPortField.setText(Integer.toString(configuration.getServerPort()));
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
    }
}
