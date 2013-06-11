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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerConfigurationPanel {

    public static final Icon SUCCESS = GuiUtils.loadIcon("success.png");
    public static final Icon FAIL = GuiUtils.loadIcon("fail.png");

    private JPanel rootPanel;

    private JTextField serverNameField;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton testConnectionButton;
    private JLabel feedbackLabel;
    private JTextField collectionsToIgnoreField;

    private RawCommandLineEditor shellArgumentsLineField;
    private JPanel mongoShellOptionsPanel;
    private JTextField labelField;
    private JCheckBox autoConnectCheckBox;
    private JTextField databaseField;
    private TextFieldWithBrowseButton shellWorkingDirField;

    private final MongoManager mongoManager;

    private String serverVersion = "";


    public ServerConfigurationPanel(MongoManager mongoManager) {
        this.mongoManager = mongoManager;
        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));

        shellArgumentsLineField.setDialogCaption("Mongo arguments");
        serverNameField.setName("serverNameField");
        serverPortField.setName("serverPortField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
        feedbackLabel.setName("feedbackLabel");
        labelField.setName("labelField");
        autoConnectCheckBox.setName("autoConnectField");
        databaseField.setName("databaseListField");
        databaseField.setToolTipText("If your access is restricted to a specific database, you can set it right here");

        testConnectionButton.setName("testConnection");

        shellWorkingDirField.setText(null);
        initListeners();
    }

    private void initListeners() {
        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    serverVersion = mongoManager.connectAndReturnServerVersion(getServerName(), getServerPort(), getUsername(), getPassword(), getUserDatabase());

                    feedbackLabel.setIcon(SUCCESS);
                    feedbackLabel.setText("");
                } catch (ConfigurationException ex) {
                    feedbackLabel.setIcon(FAIL);
                    feedbackLabel.setText(ex.getMessage());
                }

            }
        });

    }

    public JPanel getRootPanel() {
        return rootPanel;
    }


    private List<String> getCollectionsToIgnore() {
        String collectionsToIgnoreText = collectionsToIgnoreField.getText();
        if (StringUtils.isNotBlank(collectionsToIgnoreText)) {
            String[] collectionsToIgnore = collectionsToIgnoreText.split(",");

            List<String> collections = new LinkedList<String>();
            for (String collectionToIgnore : collectionsToIgnore) {
                collections.add(StringUtils.trim(collectionToIgnore));
            }
            return collections;
        }
        return Collections.emptyList();
    }


    public void applyConfigurationData(ServerConfiguration configuration) {
        configuration.setLabel(getLabel());
        configuration.setServerName(getServerName());
        configuration.setServerPort(getServerPort());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setCollectionsToIgnore(getCollectionsToIgnore());
        configuration.setUserDatabase(getUserDatabase());
        configuration.setShellArgumentsLine(getShellArgumentsLine());
        configuration.setShellWorkingDir(getShellWorkingDir());
        configuration.setConnectOnIdeStartup(isAutoConnect());
        configuration.setServerVersion(serverVersion);
    }

    private String getLabel() {
        String label = labelField.getText();
        if (StringUtils.isNotBlank(label)) {
            return label;
        }
        return null;
    }

    private String getServerName() {
        String serverName = serverNameField.getText();
        if (StringUtils.isNotBlank(serverName)) {
            return serverName;
        }
        return null;
    }

    private String getUserDatabase() {
        String userDatabase = databaseField.getText();
        if (StringUtils.isNotBlank(userDatabase)) {
            return userDatabase;
        }
        return null;
    }

    private int getServerPort() {
        String serverPort = serverPortField.getText();
        if (StringUtils.isNotBlank(serverPort)) {
            return Integer.valueOf(serverPort);
        }
        return 0;
    }

    private String getUsername() {
        String username = usernameField.getText();
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        return null;
    }

    private String getPassword() {
        char[] password = passwordField.getPassword();
        if (password != null && password.length != 0) {
            return String.valueOf(password);
        }
        return null;
    }

    private String getShellArgumentsLine() {
        String shellArgumentsLine = shellArgumentsLineField.getText();
        if (StringUtils.isNotBlank(shellArgumentsLine)) {
            return shellArgumentsLine;
        }

        return null;
    }

    private String getShellWorkingDir() {
        String shellWorkingDir = shellWorkingDirField.getText();
        if (StringUtils.isNotBlank(shellWorkingDir)) {
            return shellWorkingDir;
        }

        return null;
    }

    private boolean isAutoConnect() {
        return autoConnectCheckBox.isSelected();
    }

    public void loadConfigurationData(ServerConfiguration configuration) {
        labelField.setText(configuration.getLabel());
        serverNameField.setText(configuration.getServerName());
        serverPortField.setText(Integer.toString(configuration.getServerPort()));
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
        collectionsToIgnoreField.setText(StringUtils.join(configuration.getCollectionsToIgnore(), ","));
        databaseField.setText(configuration.getUserDatabase());
        shellArgumentsLineField.setText(configuration.getShellArgumentsLine());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
        autoConnectCheckBox.setSelected(configuration.isConnectOnIdeStartup());
        serverVersion = configuration.getServerVersion();
    }

    private void createUIComponents() {
        shellWorkingDirField = new TextFieldWithBrowseButton();
        shellWorkingDirField.addBrowseFolderListener("Mongo shell working directory", "", null,
                new FileChooserDescriptor(false, true, false, false, false, false));
        shellWorkingDirField.setName("shellWorkingDirField");
    }
}
