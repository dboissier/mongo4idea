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

package org.codinjutsu.tools.mongo;

import com.mongodb.DBPort;

import java.util.LinkedList;
import java.util.List;

public class ServerConfiguration implements Cloneable {

    private static final String DEFAULT_URL = "localhost";
    public static final int DEFAULT_PORT = DBPort.PORT;


    private String label;

    private String serverName;
    private int serverPort;

    private String username;
    private String password;

    private boolean connectOnIdeStartup = false;

    private String userDatabase;
    private List<String> collectionsToIgnore = new LinkedList<String>();

    private String shellArgumentsLine;
    private String shellWorkingDir;


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

    public void setUserDatabase(String userDatabase) {
        this.userDatabase = userDatabase;
    }

    public String getUserDatabase() {
        return userDatabase;
    }

    public void setCollectionsToIgnore(List<String> collectionsToIgnore) {
        this.collectionsToIgnore = collectionsToIgnore;
    }

    public List<String> getCollectionsToIgnore() {
        return collectionsToIgnore;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getShellArgumentsLine() {
        return shellArgumentsLine;
    }

    public void setShellArgumentsLine(String shellArgumentsLine) {
        this.shellArgumentsLine = shellArgumentsLine;
    }

    public String getShellWorkingDir() {
        return shellWorkingDir;
    }

    public void setShellWorkingDir(String shellWorkingDir) {
        this.shellWorkingDir = shellWorkingDir;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerConfiguration that = (ServerConfiguration) o;

        if (connectOnIdeStartup != that.connectOnIdeStartup) return false;
        if (serverPort != that.serverPort) return false;
        if (collectionsToIgnore != null ? !collectionsToIgnore.equals(that.collectionsToIgnore) : that.collectionsToIgnore != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (serverName != null ? !serverName.equals(that.serverName) : that.serverName != null) return false;
        if (shellArgumentsLine != null ? !shellArgumentsLine.equals(that.shellArgumentsLine) : that.shellArgumentsLine != null)
            return false;
        if (shellWorkingDir != null ? !shellWorkingDir.equals(that.shellWorkingDir) : that.shellWorkingDir != null)
            return false;
        if (userDatabase != null ? !userDatabase.equals(that.userDatabase) : that.userDatabase != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (serverName != null ? serverName.hashCode() : 0);
        result = 31 * result + serverPort;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (connectOnIdeStartup ? 1 : 0);
        result = 31 * result + (userDatabase != null ? userDatabase.hashCode() : 0);
        result = 31 * result + (collectionsToIgnore != null ? collectionsToIgnore.hashCode() : 0);
        result = 31 * result + (shellArgumentsLine != null ? shellArgumentsLine.hashCode() : 0);
        result = 31 * result + (shellWorkingDir != null ? shellWorkingDir.hashCode() : 0);
        return result;
    }
}
