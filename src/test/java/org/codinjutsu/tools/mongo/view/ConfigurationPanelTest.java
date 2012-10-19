package org.codinjutsu.tools.mongo.view;

import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;

public class ConfigurationPanelTest extends UISpecTestCase {

    private Panel uiSpecPanel;
    private ConfigurationPanel configurationPanel;

    public void testForm() throws Exception {
        uiSpecPanel.getTextBox("serverNameField").setText("localhost");
        uiSpecPanel.getTextBox("serverPortField").setText("25");
        uiSpecPanel.getTextBox("usernameField").setText("john");
        uiSpecPanel.getPasswordField("passwordField").setPassword("johnpassword");

        MongoConfiguration configuration = new MongoConfiguration();

        assertTrue(configurationPanel.isModified(configuration));
        configurationPanel.applyConfigurationData(configuration);

        assertEquals("localhost", configuration.getServerName());
        assertEquals(25, configuration.getServerPort());
        assertEquals("john", configuration.getUsername());
        assertEquals("johnpassword", configuration.getPassword());
    }

    public void testLoadConfiguration() throws Exception {
        MongoConfiguration configuration = new MongoConfiguration();
        configuration.setServerName("localhost");
        configuration.setServerPort(25);
        configuration.setUsername("john");
        configuration.setPassword("johnpassword");

        configurationPanel.loadConfigurationData(configuration);

        uiSpecPanel.getTextBox("serverNameField").textEquals("localhost").check();
        uiSpecPanel.getTextBox("serverPortField").textEquals("25").check();
        uiSpecPanel.getTextBox("usernameField").textEquals("john").check();
        uiSpecPanel.getPasswordField("passwordField").passwordEquals("johnpassword").check();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configurationPanel = new ConfigurationPanel();
        uiSpecPanel = new Panel(configurationPanel.getRootPanel());
    }
}
