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

import com.mongodb.AuthenticationMechanism;
import com.mongodb.ReadPreference;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.singletonList;

public class ServerConfiguration implements Cloneable {

    private static final String DEFAULT_URL = "localhost";
    public static final int DEFAULT_PORT = 27017;


    private String label;

    private List<String> serverUrls = new LinkedList<String>();

    private boolean sslConnection;
    private ReadPreference readPreference = ReadPreference.primary();

    private String username;
    private String password;
    private String authenticationDatabase;
    private AuthenticationMechanism authenticationMechanism = null;

    private String userDatabase;
    private boolean connectOnIdeStartup = false;

    private List<String> collectionsToIgnore = new LinkedList<>();
    private String shellArgumentsLine;
    private String shellWorkingDir;


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getServerUrls() {
        return serverUrls;
    }

    public void setServerUrls(List<String> serverUrls) {
        this.serverUrls = serverUrls;
    }

    public boolean isSslConnection() {
        return sslConnection;
    }

    public void setSslConnection(boolean sslConnection) {
        this.sslConnection = sslConnection;
    }

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    public void setReadPreference(ReadPreference readPreference) {
        this.readPreference = readPreference;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthenticationDatabase() {
        return authenticationDatabase;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

    public void setUserDatabase(String userDatabase) {
        this.userDatabase = userDatabase;
    }

    public String getUserDatabase() {
        return userDatabase;
    }

    public boolean isConnectOnIdeStartup() {
        return connectOnIdeStartup;
    }

    public void setConnectOnIdeStartup(boolean connectOnIdeStartup) {
        this.connectOnIdeStartup = connectOnIdeStartup;
    }

    public void setCollectionsToIgnore(List<String> collectionsToIgnore) {
        this.collectionsToIgnore = collectionsToIgnore;
    }

    public List<String> getCollectionsToIgnore() {
        return collectionsToIgnore;
    }

    public void setAuthenticationMechanism(AuthenticationMechanism authenticationMechanism) {
        this.authenticationMechanism = authenticationMechanism;
    }

    public AuthenticationMechanism getAuthenticationMechanism() {
        return authenticationMechanism;
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

    public static ServerConfiguration byDefault() {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setServerUrls(singletonList(String.format("%s:%s", DEFAULT_URL, DEFAULT_PORT)));
        return serverConfiguration;
    }

    public String getUrlsInSingleString() {
        return StringUtils.join(serverUrls, ",");
    }

    public boolean isSingleServer() {
        return serverUrls.size() == 1;
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

        if (sslConnection != that.sslConnection) return false;
        if (connectOnIdeStartup != that.connectOnIdeStartup) return false;
        if (!label.equals(that.label)) return false;
        if (!serverUrls.equals(that.serverUrls)) return false;
        if (!readPreference.equals(that.readPreference)) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (authenticationDatabase != null ? !authenticationDatabase.equals(that.authenticationDatabase) : that.authenticationDatabase != null)
            return false;
        if (authenticationMechanism != that.authenticationMechanism) return false;
        if (userDatabase != null ? !userDatabase.equals(that.userDatabase) : that.userDatabase != null) return false;
        if (!collectionsToIgnore.equals(that.collectionsToIgnore)) return false;
        if (shellArgumentsLine != null ? !shellArgumentsLine.equals(that.shellArgumentsLine) : that.shellArgumentsLine != null)
            return false;
        return !(shellWorkingDir != null ? !shellWorkingDir.equals(that.shellWorkingDir) : that.shellWorkingDir != null);

    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + serverUrls.hashCode();
        result = 31 * result + (sslConnection ? 1 : 0);
        result = 31 * result + readPreference.hashCode();
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (authenticationDatabase != null ? authenticationDatabase.hashCode() : 0);
        result = 31 * result + (authenticationMechanism != null ? authenticationMechanism.hashCode() : 0);
        result = 31 * result + (userDatabase != null ? userDatabase.hashCode() : 0);
        result = 31 * result + (connectOnIdeStartup ? 1 : 0);
        result = 31 * result + collectionsToIgnore.hashCode();
        result = 31 * result + (shellArgumentsLine != null ? shellArgumentsLine.hashCode() : 0);
        result = 31 * result + (shellWorkingDir != null ? shellWorkingDir.hashCode() : 0);
        return result;
    }
}
