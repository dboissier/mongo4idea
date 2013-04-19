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

import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class ServerConfigurationPanelTest {

    private ServerConfigurationPanel configurationPanel;
    private MongoManager mongoManager;

    private FrameFixture frameFixture;

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {
        mongoManager = Mockito.spy(new MongoManager());
        configurationPanel = GuiActionRunner.execute(new GuiQuery<ServerConfigurationPanel>() {
            protected ServerConfigurationPanel executeInEDT() {
                return new ServerConfigurationPanel(mongoManager);
            }
        });

        frameFixture = Containers.showInFrame(configurationPanel.getRootPanel());
    }

    @Test
    public void validateForm() throws Exception {

        frameFixture.textBox("serverNameField").setText("localhost");
        frameFixture.textBox("serverPortField").setText("25");
        frameFixture.textBox("usernameField").setText("john");
        frameFixture.textBox("passwordField").setText("johnpassword");

        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertEquals("localhost", configuration.getServerName());
        assertEquals(25, configuration.getServerPort());
        assertEquals("john", configuration.getUsername());
        assertEquals("johnpassword", configuration.getPassword());
    }

    @Test
    public void loadForm() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerName("localhost");
        configuration.setServerPort(25);
        configuration.setUsername("john");
        configuration.setPassword("johnpassword");

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverNameField").requireText("localhost");
        frameFixture.textBox("serverPortField").requireText("25");
        frameFixture.textBox("usernameField").requireText("john");
        frameFixture.textBox("passwordField").requireText("johnpassword");
    }


    @Test
    public void connectionWithSuccess() {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerName("localhost");
        configuration.setServerPort(27017);

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.button("testConnection").click();

        Mockito.verify(mongoManager, Mockito.times(1)).connectAndReturnServerVersion("localhost", 27017, null, null, null);
    }

    @Test
    public void connectionWithFailure() {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerName("myserver");
        configuration.setServerPort(25);

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.button("testConnection").click();
        frameFixture.label("feedbackLabel")
                .requireText("java.net.UnknownHostException: myserver");

        Mockito.verify(mongoManager, Mockito.times(1)).connectAndReturnServerVersion("myserver", 25, null, null, null);
    }
}
