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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.utils.CollectionUtils;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationPanel {

    private static final Icon SUCCESS = GuiUtils.loadIcon("success.png");
    private static final Icon FAIL = GuiUtils.loadIcon("fail.png");

    private JPanel rootPanel;

    private JTextField serverNameField;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton testConnectionButton;
    private JLabel feedbackLabel;
    private JTextField collectionsToIgnoreField;

    private LabeledComponent<TextFieldWithBrowseButton> shellPathField;
    private RawCommandLineEditor shellArgumentsLineField;
    private JPanel mongoShellOptionsPanel;

    private final MongoManager mongoManager;

    private String serverVersion = "";


    public ConfigurationPanel(MongoManager mongoManager) {
        this.mongoManager = mongoManager;
        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));

        shellArgumentsLineField.setDialogCaption("Mongo arguments");
        serverNameField.setName("serverNameField");
        serverPortField.setName("serverPortField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
        feedbackLabel.setName("feedbackLabel");
        shellPathField.getComponent().setName("shellPathField");

        initListeners();
    }

    private void initListeners() {
        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    serverVersion = mongoManager.connect(getServerName(), getServerPort(), getUsername(), getPassword());

                    feedbackLabel.setIcon(SUCCESS);
                    feedbackLabel.setText("");
                } catch (ConfigurationException ex) {
                    feedbackLabel.setIcon(FAIL);
                    feedbackLabel.setText(ex.getMessage());
                }

            }
        });

        installBrowserListener();
    }

    protected void installBrowserListener() {
        shellPathField.getComponent().addBrowseFolderListener("Mongo shell path", "", null,
                new FileChooserDescriptor(true, false, false, false, false, false));
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isModified(MongoConfiguration configuration) {

        return !(
                StringUtils.equals(configuration.getServerName(), getServerName())
                        && (configuration.getServerPort() == getServerPort())
                        && StringUtils.equals(configuration.getUsername(), getUsername())
                        && StringUtils.equals(configuration.getPassword(), getPassword())
                        && CollectionUtils.isEqualCollection(configuration.getCollectionsToIgnore(), getCollectionsToIgnore())
                        && StringUtils.equals(configuration.getShellPath(), getShellPath())
                        && StringUtils.equals(configuration.getShellArgumentsLine(), getShellArgumentsLine())
        );
    }

    private Set<String> getCollectionsToIgnore() {
        String collectionsToIgnoreText = collectionsToIgnoreField.getText();
        if (StringUtils.isNotBlank(collectionsToIgnoreText)) {
            String[] collectionsToIgnore = collectionsToIgnoreText.split(",");

            Set<String> collections = new HashSet<String>();
            for (String collectionToIgnore : collectionsToIgnore) {
                collections.add(StringUtils.trim(collectionToIgnore));
            }
            return collections;
        }
        return Collections.emptySet();
    }

    public void applyConfigurationData(MongoConfiguration configuration) {
        configuration.setServerName(getServerName());
        configuration.setServerPort(getServerPort());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setCollectionsToIgnore(getCollectionsToIgnore());
        configuration.setShellPath(getShellPath());
        configuration.setShellArgumentsLine(getShellArgumentsLine());
        configuration.setServerVersion(serverVersion);
    }

    private String getServerName() {
        String serverName = serverNameField.getText();
        if (StringUtils.isNotBlank(serverName)) {
            return serverName;
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

    private String getUsername() {
        String username = usernameField.getText();
        if (StringUtils.isNotBlank(username)) {
            return username;
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

    private String getShellPath() {
        String shellPath = shellPathField.getComponent().getText();
        if (StringUtils.isNotBlank(shellPath)) {
            return shellPath;
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

    public void loadConfigurationData(MongoConfiguration configuration) {
        serverNameField.setText(configuration.getServerName());
        serverPortField.setText(Integer.toString(configuration.getServerPort()));
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
        collectionsToIgnoreField.setText(StringUtils.join(configuration.getCollectionsToIgnore(), ","));
        shellPathField.getComponent().setText(configuration.getShellPath());

        serverVersion = configuration.getServerVersion();
    }
}
