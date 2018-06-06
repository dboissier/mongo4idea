/*
 * Copyright (c) 2016 David Boissier.
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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBPasswordField;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.ReadPreference;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.SshTunnelingConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.logic.ssh.AuthenticationMethod;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ServerConfigurationPanel extends JPanel {

    public static final Icon SUCCESS = GuiUtils.loadIcon("success.png");
    public static final Icon FAIL = GuiUtils.loadIcon("fail.png");
    private final Project project;

    private JPanel rootPanel;

    private JLabel feedbackLabel;

    private JCheckBox sslConnectionField;

    private JTextField labelField;

    private JTextField serverUrlsField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField authenticationDatabaseField;
    private JRadioButton mongoCRAuthRadioButton;
    private JRadioButton scramSHA1AuthRadioButton;
    private JRadioButton defaultAuthMethodRadioButton;

    private JTextField userDatabaseField;

    private JCheckBox autoConnectCheckBox;

    private JButton testConnectionButton;

    private JTextField collectionsToIgnoreField;
    private JPanel mongoShellOptionsPanel;
    private TextFieldWithBrowseButton shellWorkingDirField;
    private RawCommandLineEditor shellArgumentsLineField;
    private JComboBox readPreferenceComboBox;

    private JTextField sshProxyHostField;
    private JTextField sshProxyPortField;
    private JTextField sshProxyUserField;
    private JTabbedPane settingTabbedPane;
    private JComboBox sshAuthenticationMethodComboBox;
    private JBPasswordField sshProxyPasswordField;
    private JLabel privateKeyPathLabel;
    private TextFieldWithBrowseButton privateKeyPathField;
    private JLabel passLabel;

    private final MongoManager mongoManager;


    public ServerConfigurationPanel(Project project, MongoManager mongoManager) {
        this.project = project;
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        this.mongoManager = mongoManager;

        labelField.setName("labelField");
        feedbackLabel.setName("feedbackLabel");
        settingTabbedPane.setName("tabbedSettings");

        sslConnectionField.setName("sslConnectionField");
        readPreferenceComboBox.setName("readPreferenceComboBox");
        serverUrlsField.setName("serverUrlsField");
        usernameField.setName("usernameField");
        passwordField.setName("passwordField");
        authenticationDatabaseField.setName("authenticationDatabaseField");
        authenticationDatabaseField.setToolTipText("admin by default, otherwise set the user database here if restriced access");
        mongoCRAuthRadioButton.setName("mongoCRAuthField");
        scramSHA1AuthRadioButton.setName("scramSHA1AuthField");
        defaultAuthMethodRadioButton.setName("defaultAuthMethod");

        sshProxyHostField.setName("sshProxyHostField");
        sshProxyPortField.setName("sshProxyPortField");
        sshProxyUserField.setName("sshProxyUsernameField");
        sshProxyPasswordField.setName("sshProxyPasswordField");

        userDatabaseField.setName("userDatabaseField");
        userDatabaseField.setToolTipText("If your access is restricted to a specific database (e.g.: MongoLab), you can set it right here");

        autoConnectCheckBox.setName("autoConnectField");

        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Mongo shell options", true));
        shellArgumentsLineField.setDialogCaption("Mongo arguments");

        testConnectionButton.setName("testConnection");

        readPreferenceComboBox.setModel(new DefaultComboBoxModel<>(
                new ReadPreference[]{
                        ReadPreference.primary(),
                        ReadPreference.primaryPreferred(),
                        ReadPreference.secondary(),
                        ReadPreference.secondaryPreferred(),
                        ReadPreference.nearest()
                }));

        readPreferenceComboBox.setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                ReadPreference readPreference = (ReadPreference) value;
                append(readPreference.getName());
            }
        });

        readPreferenceComboBox.setSelectedItem(ReadPreference.primary());

        ButtonGroup authMethodGroup = new ButtonGroup();
        authMethodGroup.add(mongoCRAuthRadioButton);
        authMethodGroup.add(scramSHA1AuthRadioButton);
        authMethodGroup.add(defaultAuthMethodRadioButton);

        sshAuthenticationMethodComboBox.setName("sshAuthenticationMethodComboBox");
        sshAuthenticationMethodComboBox.setModel(new DefaultComboBoxModel<>(
                AuthenticationMethod.values()
        ));

        passLabel.setName("passLabel");
        sshAuthenticationMethodComboBox.setSelectedItem(AuthenticationMethod.PASSPHRASE);
        sshAuthenticationMethodComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                AuthenticationMethod selectedAuthMethod = (AuthenticationMethod) sshAuthenticationMethodComboBox.getSelectedItem();
                boolean shouldUsePrivateKey = false;
                if (AuthenticationMethod.PASSPHRASE.equals(selectedAuthMethod)) {
                    shouldUsePrivateKey = true;
                }

                privateKeyPathLabel.setVisible(shouldUsePrivateKey);
                privateKeyPathField.setVisible(shouldUsePrivateKey);
                if (!shouldUsePrivateKey) {
                    passLabel.setLabelFor(sshProxyPasswordField);
                    passLabel.setText("Password:");
                    privateKeyPathField.setText(null);
                } else {
                    passLabel.setText("Passphrase:");
                }
            }
        });

        privateKeyPathField.setName("sshPrivateKeyPathComponent");
        privateKeyPathField.getTextField().setName("sshPrivateKeyPathField");

        defaultAuthMethodRadioButton.setSelected(true);
        defaultAuthMethodRadioButton.setToolTipText("Let the driver resolves the auth. mechanism");
        shellWorkingDirField.setText(null);
        initListeners();
    }

    private void initListeners() {
        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final Ref<Exception> excRef = new Ref<>();
                final ProgressManager progressManager = ProgressManager.getInstance();
                progressManager.runProcessWithProgressSynchronously(new Runnable() {
                    @Override
                    public void run() {
                        ServerConfiguration configuration = createServerConfigurationForTesting();

                        final ProgressIndicator progressIndicator = progressManager.getProgressIndicator();
                        if (progressIndicator != null) {
                            progressIndicator.setText("Connecting to Mongo server...");
                        }
                        try {
                            mongoManager.connect(configuration);
                        } catch (Exception ex) {
                            excRef.set(ex);
                        }
                    }

                }, "Testing Connection", true, ServerConfigurationPanel.this.project);

                if (!excRef.isNull()) {
                    Messages.showErrorDialog(rootPanel, excRef.get().getMessage(), "Connection Test Failed");
                } else {
                    Messages.showInfoMessage(rootPanel, "Connection test successful", "Connection Test Successful");
                }
            }
        });
    }

    @NotNull
    private ServerConfiguration createServerConfigurationForTesting() {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setServerUrls(getServerUrls());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setAuthenticationDatabase(getAuthenticationDatabase());
        configuration.setUserDatabase(getUserDatabase());
        configuration.setAuthenticationMechanism(getAuthenticationMecanism());
        configuration.setSslConnection(isSslConnection());

        configuration.setSshTunnelingConfiguration(isSshTunneling() ? createSshTunnelingSettings() : SshTunnelingConfiguration.EMPTY);

        return configuration;
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


    public void applyConfigurationData(ServerConfiguration configuration) {
        validateUrls();

        configuration.setLabel(getLabel());
        configuration.setServerUrls(getServerUrls());
        configuration.setSslConnection(isSslConnection());
        configuration.setReadPreference(getReadPreference());
        configuration.setUsername(getUsername());
        configuration.setPassword(getPassword());
        configuration.setUserDatabase(getUserDatabase());
        configuration.setAuthenticationDatabase(getAuthenticationDatabase());
        configuration.setCollectionsToIgnore(getCollectionsToIgnore());
        configuration.setShellArgumentsLine(getShellArgumentsLine());
        configuration.setShellWorkingDir(getShellWorkingDir());
        configuration.setConnectOnIdeStartup(isAutoConnect());
        configuration.setAuthenticationMechanism(getAuthenticationMecanism());

        configuration.setSshTunnelingConfiguration(isSshTunneling() ? createSshTunnelingSettings() : SshTunnelingConfiguration.EMPTY);
    }

    private SshTunnelingConfiguration createSshTunnelingSettings() {
        return new SshTunnelingConfiguration(
                getSshProxyHost(), getSshProxyPort(), getSshProxyUser(),
                getSshAuthMethod(), getSshPrivateKeyPath(), getSshProxyPassword());
    }

    public void loadConfigurationData(ServerConfiguration configuration) {
        labelField.setText(configuration.getLabel());
        serverUrlsField.setText(StringUtils.join(configuration.getServerUrls(), ","));
        usernameField.setText(configuration.getUsername());
        passwordField.setText(configuration.getPassword());
        userDatabaseField.setText(configuration.getUserDatabase());
        authenticationDatabaseField.setText(configuration.getAuthenticationDatabase());
        sslConnectionField.setSelected(configuration.isSslConnection());
        readPreferenceComboBox.setSelectedItem(configuration.getReadPreference());
        collectionsToIgnoreField.setText(StringUtils.join(configuration.getCollectionsToIgnore(), ","));
        shellArgumentsLineField.setText(configuration.getShellArgumentsLine());
        shellWorkingDirField.setText(configuration.getShellWorkingDir());
        autoConnectCheckBox.setSelected(configuration.isConnectOnIdeStartup());

        SshTunnelingConfiguration sshTunnelingConfiguration = configuration.getSshTunnelingConfiguration();
        if (!SshTunnelingConfiguration.isEmpty(sshTunnelingConfiguration)) {
            sshProxyHostField.setText(sshTunnelingConfiguration.getProxyHost());
            sshProxyPortField.setText(String.valueOf(sshTunnelingConfiguration.getProxyPort()));
            sshAuthenticationMethodComboBox.setSelectedItem(sshTunnelingConfiguration.getAuthenticationMethod());
            if (AuthenticationMethod.PASSPHRASE.equals(sshTunnelingConfiguration.getAuthenticationMethod())) {
                privateKeyPathField.setText(sshTunnelingConfiguration.getPrivateKeyPath());
            }
            sshProxyUserField.setText(sshTunnelingConfiguration.getProxyUser());
            sshProxyPasswordField.setText(sshTunnelingConfiguration.getProxyPassword());
        }

        AuthenticationMechanism authentificationMethod = configuration.getAuthenticationMechanism();
        if (AuthenticationMechanism.MONGODB_CR.equals(authentificationMethod)) {
            mongoCRAuthRadioButton.setSelected(true);
        } else if (AuthenticationMechanism.SCRAM_SHA_1.equals(authentificationMethod)) {
            scramSHA1AuthRadioButton.setSelected(true);
        } else {
            defaultAuthMethodRadioButton.setSelected(true);
        }
    }


    private List<String> getCollectionsToIgnore() {
        String collectionsToIgnoreText = collectionsToIgnoreField.getText();
        if (StringUtils.isNotBlank(collectionsToIgnoreText)) {
            String[] collectionsToIgnore = collectionsToIgnoreText.split(",");

            List<String> collections = new LinkedList<>();
            for (String collectionToIgnore : collectionsToIgnore) {
                collections.add(StringUtils.trim(collectionToIgnore));
            }
            return collections;
        }
        return Collections.emptyList();
    }

    private ReadPreference getReadPreference() {
        return (ReadPreference) readPreferenceComboBox.getSelectedItem();
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

    private boolean isSshTunneling() {
        return StringUtils.isNotBlank(getSshProxyHost());
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

    private String getAuthenticationDatabase() {
        String authenticationDatabase = authenticationDatabaseField.getText();
        if (StringUtils.isNotBlank(authenticationDatabase)) {
            return authenticationDatabase;
        }
        return null;
    }

    private String getUserDatabase() {
        String userDatabase = userDatabaseField.getText();
        if (StringUtils.isNotBlank(userDatabase)) {
            return userDatabase;
        }
        return null;
    }

    private String getSshProxyHost() {
        String proxyHost = sshProxyHostField.getText();
        if (StringUtils.isNotBlank(proxyHost)) {
            return proxyHost;
        }
        return null;
    }

    private Integer getSshProxyPort() {
        String proxyPort = sshProxyPortField.getText();
        if (StringUtils.isNotBlank(proxyPort)) {
            return Integer.parseInt(proxyPort);
        }
        return null;
    }

    private String getSshProxyUser() {
        String proxyUser = sshProxyUserField.getText();
        if (StringUtils.isNotBlank(proxyUser)) {
            return proxyUser;
        }
        return null;
    }

    private AuthenticationMethod getSshAuthMethod() {
        AuthenticationMethod selectedAuthMethod = (AuthenticationMethod) sshAuthenticationMethodComboBox.getSelectedItem();
        if (selectedAuthMethod != null) {
            return selectedAuthMethod;
        }
        return AuthenticationMethod.PASSWORD;
    }

    private String getSshProxyPassword() {
        String proxyUser = String.valueOf(sshProxyPasswordField.getPassword());
        if (StringUtils.isNotBlank(proxyUser)) {
            return proxyUser;
        }
        return null;
    }

    private AuthenticationMechanism getAuthenticationMecanism() {
        if (mongoCRAuthRadioButton.isSelected()) {
            return AuthenticationMechanism.MONGODB_CR;
        } else if (scramSHA1AuthRadioButton.isSelected()) {
            return AuthenticationMechanism.SCRAM_SHA_1;
        }
        return null;
    }

    private String getSshPrivateKeyPath() {
        String shellPath = privateKeyPathField.getText();
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

    private void createUIComponents() {
        shellWorkingDirField = new TextFieldWithBrowseButton();
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderActionListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<>("Mongo shell working directory",
                        null,
                        shellWorkingDirField,
                        null,
                        fileChooserDescriptor,
                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        shellWorkingDirField.addBrowseFolderListener(null, browseFolderActionListener, false);
        shellWorkingDirField.setName("shellWorkingDirField");

        privateKeyPathField = new TextFieldWithBrowseButton();
        ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> privateKeyBrowseFolderActionListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<>("Mongo shell working directory",
                        null,
                        privateKeyPathField,
                        null,
                        fileChooserDescriptor,
                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        privateKeyPathField.addBrowseFolderListener(null, privateKeyBrowseFolderActionListener, false);

    }

    public void setErrorMessage(String message) {
        feedbackLabel.setIcon(FAIL);
        feedbackLabel.setText(message);
    }
}
