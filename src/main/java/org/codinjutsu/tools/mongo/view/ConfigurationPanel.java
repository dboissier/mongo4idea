package org.codinjutsu.tools.mongo.view;

import org.codinjutsu.tools.mongo.MongoConfiguration;

import javax.swing.*;

public class ConfigurationPanel {

    private JPanel rootPanel;

    private JTextField serverName;
    private JTextField serverPort;
    private JTextField username;
    private JTextField password;

    private JButton testConnectionButton;

    private JLabel feeedbackLabel;


    public ConfigurationPanel() {
        serverName.setName("serverName");
        serverPort.setName("serverPort");
        username.setName("username");
        password.setName("password");
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isModified(MongoConfiguration configuration) {
        return false;
    }

    public void applyConfigurationData(MongoConfiguration configuration) {

    }

    public JTextField getServerName() {
        return serverName;
    }

    public JTextField getServerPort() {
        return serverPort;
    }

    public JTextField getUsername() {
        return username;
    }

    public JTextField getPassword() {
        return password;
    }
}
