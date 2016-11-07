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

import com.intellij.openapi.command.impl.DummyProject;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.ReadPreference;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.SshTunnelingConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ServerConfigurationPanelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServerConfigurationPanel configurationPanel;
    private MongoManager mongoManager;

    private FrameFixture frameFixture;

    @Before
    public void setUp() throws Exception {
        mongoManager = Mockito.spy(new MongoManager(DummyProject.getInstance()));
        configurationPanel = GuiActionRunner.execute(new GuiQuery<ServerConfigurationPanel>() {
            protected ServerConfigurationPanel executeInEDT() {
                return new ServerConfigurationPanel(DummyProject.getInstance(), mongoManager);
            }
        });

        frameFixture = Containers.showInFrame(configurationPanel);
    }

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Test
    public void validateFormWithOneServerUrl() throws Exception {

        frameFixture.textBox("labelField").setText("MyServer");
        frameFixture.checkBox("autoConnectField").check();

        frameFixture.textBox("serverUrlsField").setText("localhost:25");
        frameFixture.checkBox("sslConnectionField").check();
        frameFixture.comboBox("readPreferenceComboBox").requireSelection("primary");
        frameFixture.comboBox("readPreferenceComboBox").selectItem("secondary");
        frameFixture.textBox("userDatabaseField").setText("mydatabase");

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("Authentication");

        frameFixture.textBox("usernameField").setText("john");
        frameFixture.textBox("passwordField").setText("johnpassword");
        frameFixture.radioButton("defaultAuthMethod").requireSelected();
        frameFixture.radioButton("mongoCRAuthField").click();

        ServerConfiguration configuration = new ServerConfiguration();
        configurationPanel.applyConfigurationData(configuration);

        assertEquals("MyServer", configuration.getLabel());
        assertEquals(singletonList("localhost:25"), configuration.getServerUrls());
        assertTrue(configuration.isSslConnection());
        assertEquals(ReadPreference.secondary(), configuration.getReadPreference());
        assertEquals("john", configuration.getUsername());
        assertEquals("johnpassword", configuration.getPassword());
        assertEquals("mydatabase", configuration.getUserDatabase());
        assertEquals(AuthenticationMechanism.MONGODB_CR, configuration.getAuthenticationMechanism());
        assertEquals(SshTunnelingConfiguration.EMPTY, configuration.getSshTunnelingConfiguration());
        assertTrue(configuration.isConnectOnIdeStartup());
    }

    @Test
    public void validateFormWithSSHTunneling() throws Exception {
        frameFixture.textBox("labelField").setText("MyServer");
        frameFixture.textBox("serverUrlsField").setText("localhost:25");

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("SSH");
        frameFixture.textBox("sshProxyHostField").setText("remotehost");
        frameFixture.textBox("sshProxyPortField").setText("22");
        frameFixture.textBox("sshProxyUsernameField").setText("john.doe");
        frameFixture.textBox("sshProxyPasswordField").setText("mySecuredPassword");

        ServerConfiguration configuration = new ServerConfiguration();
        configurationPanel.applyConfigurationData(configuration);

        assertEquals(singletonList("localhost:25"), configuration.getServerUrls());
        SshTunnelingConfiguration sshTunnelingConfiguration = configuration.getSshTunnelingConfiguration();
        assertNotNull(sshTunnelingConfiguration);
        assertEquals("remotehost", sshTunnelingConfiguration.getProxyHost());
        assertEquals(new Integer(22), sshTunnelingConfiguration.getProxyPort());
        assertEquals("john.doe", sshTunnelingConfiguration.getProxyUser());
        assertEquals("mySecuredPassword", sshTunnelingConfiguration.getProxyPassword());
    }

    @Test
    public void loadFormWithOneServerUrl() throws Exception {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setUsername("john");
        configuration.setPassword("johnpassword");
        configuration.setReadPreference(ReadPreference.nearest());

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverUrlsField").requireText("localhost:27017");
        frameFixture.comboBox("readPreferenceComboBox").requireSelection("nearest");

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("Authentication");
        frameFixture.textBox("usernameField").requireText("john");
        frameFixture.textBox("passwordField").requireText("johnpassword");

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("SSH");
        frameFixture.textBox("sshProxyHostField").requireEmpty();
        frameFixture.textBox("sshProxyPortField").requireEmpty();
        frameFixture.textBox("sshProxyUsernameField").requireEmpty();
        frameFixture.textBox("sshProxyPasswordField").requireEmpty();

    }

    @Test
    public void loadFormWithSSHTunneling() throws Exception {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setSshTunnelingConfiguration(
                new SshTunnelingConfiguration("remotehost", 22, "john.doe", "mySecuredPassword"));
        configurationPanel.loadConfigurationData(configuration);

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("SSH");
        frameFixture.textBox("sshProxyHostField").requireText("remotehost");
        frameFixture.textBox("sshProxyPortField").requireText("22");
        frameFixture.textBox("sshProxyUsernameField").requireText("john.doe");
        frameFixture.textBox("sshProxyPasswordField").requireText("mySecuredPassword");
    }

    @Test
    public void validateFormWithMissingMongoUrlShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("URL(s) should be set");

        frameFixture.textBox("serverUrlsField").setText(null);

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }

    @Test
    public void validateFormWithEmptyMongoUrlShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("URL(s) should be set");

        frameFixture.textBox("serverUrlsField").setText("");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }

    @Test
    public void validateFormWithBadMongoUrlShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("URL 'host' format is incorrect. It should be 'host:port'");

        frameFixture.textBox("serverUrlsField").setText("host");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }


    @Test
    public void validateFormWithBadMongoPortShouldThrowAConfigurationException() {
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Port in the URL 'host:port' is incorrect. It should be a number");

        frameFixture.textBox("serverUrlsField").setText("host:port");

        configurationPanel.applyConfigurationData(new ServerConfiguration());
    }


    @Test
    public void validateFormWithReplicatSet() throws Exception {

        frameFixture.textBox("serverUrlsField").setText(" localhost:25, localhost:26 ");

        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertEquals(Arrays.asList("localhost:25", "localhost:26"), configuration.getServerUrls());
    }

    @Test
    public void loadFormWithReplicatSet() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerUrls(Arrays.asList("localhost:25", "localhost:26"));

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverUrlsField").requireText("localhost:25,localhost:26");
    }
}
