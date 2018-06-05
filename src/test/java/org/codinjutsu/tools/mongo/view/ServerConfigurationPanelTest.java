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

import com.intellij.openapi.command.impl.DummyProject;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.ReadPreference;
import org.assertj.swing.cell.JComboBoxCellReader;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.SshTunnelingConfiguration;
import org.codinjutsu.tools.mongo.logic.ConfigurationException;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.logic.ssh.AuthenticationMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.swing.*;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerConfigurationPanelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServerConfigurationPanel configurationPanel;
    private MongoManager mongoManager;

    private FrameFixture frameFixture;

    @Before
    public void setUp() {
        mongoManager = Mockito.spy(new MongoManager());
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
    public void validateFormWithOneServerUrl() {

        frameFixture.textBox("labelField").setText("MyServer");
        frameFixture.checkBox("autoConnectField").check();

        frameFixture.textBox("serverUrlsField").setText("localhost:25");
        frameFixture.checkBox("sslConnectionField").check();

        JComboBoxFixture readPreferenceComboBox = frameFixture.comboBox("readPreferenceComboBox");
        readPreferenceComboBox.replaceCellReader(new ReadPreferenceComboBoxCellReader());
        readPreferenceComboBox.requireSelection("primary");
        readPreferenceComboBox.selectItem("secondary");
        frameFixture.textBox("userDatabaseField").setText("mydatabase");

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("Authentication");

        frameFixture.textBox("usernameField").setText("john");
        frameFixture.textBox("passwordField").setText("johnpassword");
        frameFixture.radioButton("defaultAuthMethod").requireSelected();
        frameFixture.radioButton("mongoCRAuthField").click();

        ServerConfiguration configuration = new ServerConfiguration();
        configurationPanel.applyConfigurationData(configuration);

        assertThat(configuration.getLabel()).isEqualTo("MyServer");
        assertThat(configuration.getServerUrls()).containsExactly("localhost:25");
        assertThat(configuration.isSslConnection()).isTrue();
        assertThat(configuration.getReadPreference()).isEqualTo(ReadPreference.secondary());
        assertThat(configuration.getUsername()).isEqualTo("john");
        assertThat(configuration.getPassword()).isEqualTo("johnpassword");
        assertThat(configuration.getUserDatabase()).isEqualTo("mydatabase");
        assertThat(configuration.getAuthenticationMechanism()).isEqualTo(AuthenticationMechanism.MONGODB_CR);
        assertThat(configuration.getSshTunnelingConfiguration()).isEqualTo(SshTunnelingConfiguration.EMPTY);
        assertThat(configuration.isConnectOnIdeStartup()).isTrue();
    }

    @Test
    public void validateFormWithSSHTunneling() {
        frameFixture.textBox("labelField").setText("MyServer");
        frameFixture.textBox("serverUrlsField").setText("localhost:25");

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("SSH");
        frameFixture.textBox("sshProxyHostField").setText("remotehost");
        frameFixture.textBox("sshProxyPortField").setText("22");
        frameFixture.comboBox("sshAuthenticationMethodComboBox").selectItem("Passphrase");
        frameFixture.textBox("sshProxyUsernameField").setText("john.doe");
        frameFixture.textBox("sshProxyPasswordField").setText("mySecuredPassword");

        ServerConfiguration configuration = new ServerConfiguration();
        configurationPanel.applyConfigurationData(configuration);

        assertThat(configuration.getServerUrls()).containsExactly("localhost:25");

        SshTunnelingConfiguration sshTunnelingConfiguration = configuration.getSshTunnelingConfiguration();
        assertThat(sshTunnelingConfiguration).isNotNull();
        assertThat(sshTunnelingConfiguration.getProxyHost()).isEqualTo("remotehost");
        assertThat(sshTunnelingConfiguration.getProxyPort()).isEqualTo(22);
        assertThat(sshTunnelingConfiguration.getAuthenticationMethod()).isEqualTo(AuthenticationMethod.PASSPHRASE);
        assertThat(sshTunnelingConfiguration.getProxyUser()).isEqualTo("john.doe");
        assertThat(sshTunnelingConfiguration.getProxyPassword()).isEqualTo("mySecuredPassword");
    }

    @Test
    public void loadFormWithOneServerUrl() {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setUsername("john");
        configuration.setPassword("johnpassword");
        configuration.setReadPreference(ReadPreference.nearest());

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverUrlsField").requireText("localhost:27017");
        JComboBoxFixture readPreferenceComboBox = frameFixture.comboBox("readPreferenceComboBox");
        readPreferenceComboBox.replaceCellReader(new ReadPreferenceComboBoxCellReader());
        readPreferenceComboBox.requireSelection("nearest");

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
    public void loadFormWithSSHTunneling() {
        ServerConfiguration configuration = ServerConfiguration.byDefault();
        configuration.setSshTunnelingConfiguration(
                new SshTunnelingConfiguration("remotehost", 22, "john.doe", AuthenticationMethod.PASSWORD, "mySecuredPassword"));
        configurationPanel.loadConfigurationData(configuration);

        frameFixture.tabbedPane("tabbedSettings")
                .selectTab("SSH");
        frameFixture.textBox("sshProxyHostField").requireText("remotehost");
        frameFixture.textBox("sshProxyPortField").requireText("22");
        frameFixture.textBox("sshProxyUsernameField").requireText("john.doe");
        frameFixture.comboBox("sshAuthenticationMethodComboBox").requireSelection("Password");
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
    public void validateFormWithReplicatSet() {

        frameFixture.textBox("serverUrlsField").setText(" localhost:25, localhost:26 ");

        ServerConfiguration configuration = new ServerConfiguration();

        configurationPanel.applyConfigurationData(configuration);

        assertThat(configuration.getServerUrls()).containsExactly("localhost:25", "localhost:26");
    }

    @Test
    public void loadFormWithReplicatSet() {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setServerUrls(Arrays.asList("localhost:25", "localhost:26"));

        configurationPanel.loadConfigurationData(configuration);

        frameFixture.textBox("serverUrlsField").requireText("localhost:25,localhost:26");
    }

    private static class ReadPreferenceComboBoxCellReader implements JComboBoxCellReader {
        @Override
        public String valueAt(JComboBox<?> jComboBox, int i) {
            ReadPreference readPreference = (ReadPreference) jComboBox.getModel().getElementAt(i);
            return readPreference.getName();
        }
    }
}
