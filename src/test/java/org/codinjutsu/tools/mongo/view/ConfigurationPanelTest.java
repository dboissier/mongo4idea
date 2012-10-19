package org.codinjutsu.tools.mongo.view;

import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.uispec4j.ComponentAmbiguityException;
import org.uispec4j.ItemNotFoundException;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;

public class ConfigurationPanelTest extends UISpecTestCase {

    private Panel uiSpecPanel;
    private ConfigurationPanel configurationPanel;
    private MongoManager mongoManager;

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

    public void testConnectionWithSuccess() {
        MongoConfiguration configuration = new MongoConfiguration();
        configuration.setServerName("localhost");
        configuration.setServerPort(27017);

        configurationPanel.loadConfigurationData(configuration);

        try {
            uiSpecPanel.getButton().click();
        } catch (ConfigurationException e) {
            fail("Connection should succeed");
        }

        Mockito.verify(mongoManager, Mockito.times(1)).connect("localhost", 27017, "", "");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mongoManager = Mockito.spy(new MongoManager());
        configurationPanel = new ConfigurationPanel(mongoManager);
        uiSpecPanel = new Panel(configurationPanel.getRootPanel());
    }
}
