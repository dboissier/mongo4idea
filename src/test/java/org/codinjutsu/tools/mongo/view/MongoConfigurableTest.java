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
import com.intellij.testFramework.PlatformTestCase;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.uispec4j.Panel;
import org.uispec4j.UISpec4J;
import org.uispec4j.interception.toolkit.UISpecDisplay;

import java.util.Arrays;

/**
 * This test uses some hacks use both UISpec4J framework and TestCase class from IntelliJ plateform.
 * Unfortunately, it can only run in IntelliJ and lonely :(
 */
public class MongoConfigurableTest extends PlatformTestCase {

    private MongoConfigurable mongoConfigurable;

    static {
        UISpec4J.init();
    }

    public void _testEmptyConfiguration() throws Exception {
        mongoConfigurable = new MongoConfigurable(DummyProject.getInstance());
        Panel uiSpecPanel = new Panel(mongoConfigurable.createComponent());
        uiSpecPanel.getTable().isEmpty().check();
        uiSpecPanel.getTextBox("shellPathField").textIsEmpty().check();

//Note: Unable to get button from the toolbar :(
//        WindowInterceptor.init(uiSpecPanel.getButton("addServer").triggerClick())
//                .process(new WindowHandler() {
//                    @Override
//                    public Trigger process(Window window) throws Exception {
//                        assertEquals("Add a server", window.getTitle());
//                        return null;
//                    }
//                })
//        .run();
    }

    public void _testWithAConfiguration() throws Exception {

        MongoConfiguration configuration = new MongoConfiguration();

        ServerConfiguration localConfiguration = new ServerConfiguration();
        localConfiguration.setLabel("Local");
        localConfiguration.setServerName("localhost");
        localConfiguration.setServerPort(27017);

        ServerConfiguration remoteConfiguration = new ServerConfiguration();
        remoteConfiguration.setLabel("Remote");
        remoteConfiguration.setServerName("192.168.168.168");
        remoteConfiguration.setServerPort(27018);

        configuration.setServerConfigurations(Arrays.asList(localConfiguration, remoteConfiguration));
        configuration.setShellPath("/usr/bin/mongo");

        mongoConfigurable = new MongoConfigurable(DummyProject.getInstance());
        Panel uiSpecPanel = new Panel(mongoConfigurable.createComponent());

        uiSpecPanel.getTable().contentEquals(new Object[][] {
                {"Local", "localhost/27017"},
                {"Remote", "192.168.168.168/27018"},
        }).check();

        uiSpecPanel.getTextBox("shellPathField").textEquals("/usr/bin/mongo").check();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        UISpecDisplay.instance().reset();
    }


    @Override
    public void tearDown() throws Exception {
        UISpecDisplay.instance().rethrowIfNeeded();
        UISpecDisplay.instance().reset();
        mongoConfigurable.disposeUIResources();
        super.tearDown();
    }
}
