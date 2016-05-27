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

import com.mongodb.AuthenticationMechanism;
import com.mongodb.ReadPreference;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerConfigurationPanelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServerConfigurationPanel configurationPanel;
    private MongoManager mongoManager;

    private FrameFixture frameFixture;

    @Before
    public void setUp() throws Exception {
        mongoManager = Mockito.spy(new MongoManager());
        configurationPanel = GuiActionRunner.execute(new GuiQuery<ServerConfigurationPanel>() {
            protected ServerConfigurationPanel executeInEDT() {
                return new ServerConfigurationPanel(mongoManager);
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

        frameFixture.textBox("serverUrlsField").setText("localhost:25");
        frameFixture.checkBox("sslConnectionField").check();
        frameFixture.comboBox("readPreferenceComboBox").requireSelection("primary");
        frameFixture.comboBox("readPreferenceComboBox").selectItem("secondary");

        frameFixture.textBox("usernameField").setText("john");
        frameFixture.textBox("passwordField").setText("johnpassword");
        frameFixture.radioButton("defaultAuthMethod").requireSelected();
        frameFixture.radioButton("mongoCRAuthField").click();

        frameFixture.textBox("userDatabaseField").setText("mydatabase");
        frameFixture.checkBox("autoConnectField").check();


        ServerConfiguration configuration = new ServerConfiguration();
        configurationPanel.applyConfigurationData(configuration);

        assertEquals(singletonList("localhost:25"), configuration.getServerUrls());
        assertTrue(configuration.isSslConnection());
        assertEquals(ReadPreference.secondary(), configuration.getReadPreference());
        assertEquals("john", configuration.getUsername());
        assertEquals("johnpassword", configuration.getPassword());
        assertEquals("mydatabase", configuration.getUserDatabase());
        assertEquals(AuthenticationMechanism.MONGODB_CR, configuration.getAuthenticationMechanism());
        assertTrue(configuration.isConnectOnIdeStartup());
    }

    @Test
    public void loadFormWithOneServerUrl() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerUrls(Collections.singletonList("localhost:25"));
        configuration.setUsername("john");
        configuration.setPassword("johnpassword");
        configuration.setReadPreference(ReadPreference.nearest());

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverUrlsField").requireText("localhost:25");
        frameFixture.textBox("usernameField").requireText("john");
        frameFixture.textBox("passwordField").requireText("johnpassword");

        frameFixture.comboBox("readPreferenceComboBox").requireSelection("nearest");
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
