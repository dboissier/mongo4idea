/*
 * Copyright (c) 2012 David Boissier
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

package org.codinjutsu.tools.mongo;

import com.mongodb.DBPort;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ServerConfiguration implements Cloneable {

    private static final String DEFAULT_URL = "localhost";
    public static final int DEFAULT_PORT = DBPort.PORT;


    private String label;

    private String serverName;
    private int serverPort;

    private String username;
    private String password;

    private boolean connectOnIdeStartup = false;

    private final List<String> collectionsToIgnore = new LinkedList<String>();

    private String serverVersion;
    private String shellArgumentsLine;


    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnectOnIdeStartup() {
        return connectOnIdeStartup;
    }

    public void setConnectOnIdeStartup(boolean connectOnIdeStartup) {
        this.connectOnIdeStartup = connectOnIdeStartup;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCollectionsToIgnore(Set<String> collectionsToIgnore) {
        this.collectionsToIgnore.clear();
        this.collectionsToIgnore.addAll(collectionsToIgnore);
    }

    public List<String> getCollectionsToIgnore() {
        return collectionsToIgnore;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getShellArgumentsLine() {
        return shellArgumentsLine;
    }

    public void setShellArgumentsLine(String shellArgumentsLine) {
        this.shellArgumentsLine = shellArgumentsLine;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return String.format("%s/%s", serverName, serverPort);
    }

    public static ServerConfiguration byDefault() {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setServerName(DEFAULT_URL);
        serverConfiguration.setServerPort(DEFAULT_PORT);
        return serverConfiguration;
    }


    public ServerConfiguration clone() {
        try {
            return (ServerConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
