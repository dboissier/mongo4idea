package org.codinjutsu.tools.mongo.view;

import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;

public class ConfigurationPanelTest extends UISpecTestCase {

    private Panel uiSpecPanel;
    private ConfigurationPanel configurationPanel;

    public void testForm() throws Exception {
        uiSpecPanel.getTextBox("serverName").setText("localhost");
        uiSpecPanel.getTextBox("serverPort").setText("25");
        uiSpecPanel.getTextBox("username").setText("john");
        uiSpecPanel.getTextBox("password").setText("johnpassword");

        MongoConfiguration configuration = new MongoConfiguration();

        assertTrue(configurationPanel.isModified(configuration));
        configurationPanel.applyConfigurationData(configuration);

        assertEquals("localhost", configuration.getHost());
        assertEquals(25, configuration.getPort());
        assertEquals("john", configuration.getUsername());
        assertEquals("johnpassword", configuration.getPassword());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configurationPanel = new ConfigurationPanel();
        uiSpecPanel = new Panel(configurationPanel.getRootPanel());
    }
}
