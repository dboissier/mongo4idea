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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoConnectionException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerConfigurationPanel extends JPanel implements Disposable {

    public static final Icon SUCCESS = GuiUtils.loadIcon("success.png");
    public static final Icon FAIL = GuiUtils.loadIcon("fail.png");

    private JPanel rootPanel;

    private JTextField serverUrlsField;
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
    private JCheckBox userDatabaseAsMySingleDatabaseField;
    private JCheckBox sslConnectionField;

    private final MongoManager mongoManager;


    public ServerConfigurationPanel(MongoManager mongoManager) {
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        this.mongoManager = mongoManager;
        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));

        shellArgumentsLineField.setDialogCaption("Mongo arguments");
        serverUrlsField.setName("serverUrlsField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
        feedbackLabel.setName("feedbackLabel");
        labelField.setName("labelField");
        userDatabaseAsMySingleDatabaseField.setName("userDatabaseAsMySingleDatabaseField");
        userDatabaseAsMySingleDatabaseField.setToolTipText("This should be checked when using a MongoLab single database for instance");
        sslConnectionField.setName("sslConnectionField");
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
                    validateUrls();
                    testConnectionButton.setEnabled(false);
                    testConnectionButton.setText("Connecting...");
                    testConnectionButton.repaint();
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ServerConfiguration configuration = ServerConfiguration.byDefault();
                                configuration.setServerUrls(getServerUrls());
                                configuration.setUsername(getUsername());
                                configuration.setPassword(getPassword());
                                configuration.setUserDatabase(getUserDatabase());
                                mongoManager.connect(configuration);

                                feedbackLabel.setIcon(SUCCESS);
                                feedbackLabel.setText("Connection successfull");
                            } catch (MongoConnectionException ex) {
                                setErrorMessage(ex.getMessage());
                            } finally {
                                testConnectionButton.setEnabled(true);
                                testConnectionButton.setText("Test connection");
                            }
                        }
                    });
                } catch (ConfigurationException ex) {
                    setErrorMessage(ex.getMessage());
                }
            }
        });

    }

    private void validateUrls() {
        List<String> serverUrls = getServerUrls();
        if (serverUrls == null) {
            throw new ConfigurationException("URL(s) should be set");
        }
        for (String serverUrl : serverUrls) {
            String[] host_port = serverUrl.split(":");
            if (host_port.length < 2) {
                throw new ConfigurationException(String.format("URL '%s' format is incorrect. It should be 'host:port'", serverUrl));
            }

            try {
                Integer.valueOf(host_port[1]);
            } catch (NumberFormatException e) {
                throw new ConfigurationException(String.format("Port in the URL '%s' is incorrect. It should be a number", serverUrl));
            }
        }

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
        validateUrls();

        configuration.setLabel(getLabel());
        configuration.setServerUrls(getServerUrls());
        configuration.setSslConnection(isSslConnection());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setUserDatabase(getUserDatabase());
        configuration.setUserDatabaseAsMySingleDatabase(isUserDatabaseAsMySingleDatabase());
        configuration.setCollectionsToIgnore(getCollectionsToIgnore());
        configuration.setShellArgumentsLine(getShellArgumentsLine());
        configuration.setShellWorkingDir(getShellWorkingDir());
        configuration.setConnectOnIdeStartup(isAutoConnect());
    }

    private String getLabel() {
        String label = labelField.getText();
        if (StringUtils.isNotBlank(label)) {
            return label;
        }
        return null;
    }

    private List<String> getServerUrls() {
        String serverUrls = serverUrlsField.getText();
        if (StringUtils.isNotBlank(serverUrls)) {
            return Arrays.asList(StringUtils.split(StringUtils.deleteWhitespace(serverUrls), ","));
        }
        return null;
    }

    private boolean isSslConnection() {
        return sslConnectionField.isSelected();
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

    private String getUserDatabase() {
        String userDatabase = databaseField.getText();
        if (StringUtils.isNotBlank(userDatabase)) {
            return userDatabase;
        }
        return null;
    }

    private boolean isUserDatabaseAsMySingleDatabase() {
        return userDatabaseAsMySingleDatabaseField.isSelected();
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
        serverUrlsField.setText(StringUtils.join(configuration.getServerUrls(), ","));
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
        databaseField.setText(configuration.getUserDatabase());
        sslConnectionField.setSelected(configuration.isSslConnection());
        userDatabaseAsMySingleDatabaseField.setSelected(configuration.isUserDatabaseAsMySingleDatabase());
        collectionsToIgnoreField.setText(StringUtils.join(configuration.getCollectionsToIgnore(), ","));
        shellArgumentsLineField.setText(configuration.getShellArgumentsLine());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
        autoConnectCheckBox.setSelected(configuration.isConnectOnIdeStartup());
    }

    private void createUIComponents() {
        shellWorkingDirField = new TextFieldWithBrowseButton();
        shellWorkingDirField.addBrowseFolderListener("Mongo shell working directory", "", null,
                new FileChooserDescriptor(false, true, false, false, false, false));
        shellWorkingDirField.setName("shellWorkingDirField");
    }

    @Override
    public void dispose() {
    }

    public void setErrorMessage(String message) {
        feedbackLabel.setIcon(FAIL);
        feedbackLabel.setText(message);
    }
}
