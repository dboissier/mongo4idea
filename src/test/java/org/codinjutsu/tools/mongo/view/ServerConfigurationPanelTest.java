package org.codinjutsu.tools.mongo.view;

import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.mockito.Mockito;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;

public class ServerConfigurationPanelTest extends UISpecTestCase {

    private Panel uiSpecPanel;
    private ServerConfigurationPanel configurationPanel;
    private MongoManager mongoManager;

    public void testForm() throws Exception {
        uiSpecPanel.getTextBox("serverNameField").setText("localhost");
        uiSpecPanel.getTextBox("serverPortField").setText("25");
        uiSpecPanel.getTextBox("usernameField").setText("john");
        uiSpecPanel.getPasswordField("passwordField").setPassword("johnpassword");

        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertEquals("localhost", configuration.getServerName());
        assertEquals(25, configuration.getServerPort());
        assertEquals("john", configuration.getUsername());
        assertEquals("johnpassword", configuration.getPassword());
    }

    public void testLoadConfiguration() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
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
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerName("localhost");
        configuration.setServerPort(27017);

        configurationPanel.loadConfigurationData(configuration);

        uiSpecPanel.getButton("Test Connection").click();
//        uiSpecPanel.getTextBox("feedbackLabel").iconEquals(GuiUtils.loadIcon("success.png")).check();

        Mockito.verify(mongoManager, Mockito.times(1)).connect("localhost", 27017, null, null);
    }

    public void testConnectionWithFailure() {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerName("myserver");
        configuration.setServerPort(25);

        configurationPanel.loadConfigurationData(configuration);

        uiSpecPanel.getButton("Test Connection").click();
        TextBox feedbackLabel = uiSpecPanel.getTextBox("feedbackLabel");
//        feedbackLabel.iconEquals(GuiUtils.loadIcon("fail.png")).check();
        feedbackLabel.textEquals("java.net.UnknownHostException: myserver").check();

        Mockito.verify(mongoManager, Mockito.times(1)).connect("myserver", 25, null, null);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mongoManager = Mockito.spy(new MongoManager());
        configurationPanel = new ServerConfigurationPanel(mongoManager);
        uiSpecPanel = new Panel(configurationPanel.getRootPanel());
    }
}
