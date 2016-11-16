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

package org.codinjutsu.tools.mongo;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.ReadPreference;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.singletonList;

public class ServerConfiguration implements Cloneable {

    private static final String DEFAULT_URL = "localhost";
    private static final int DEFAULT_PORT = 27017;


    private String label;

    private List<String> serverUrls = new LinkedList<>();

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
    private SshTunnelingConfiguration sshTunnelingConfiguration;

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

    public void setSshTunnelingConfiguration(SshTunnelingConfiguration sshTunnelingConfiguration) {
        this.sshTunnelingConfiguration = sshTunnelingConfiguration;
    }

    public SshTunnelingConfiguration getSshTunnelingConfiguration() {
        return sshTunnelingConfiguration;
    }

    public static ServerConfiguration byDefault() {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setServerUrls(singletonList(String.format("%s:%s", DEFAULT_URL, DEFAULT_PORT)));
        serverConfiguration.setSshTunnelingConfiguration(SshTunnelingConfiguration.EMPTY);
        return serverConfiguration;
    }

    public String getUrlsInSingleString() {
        return StringUtils.join(serverUrls, ",");
    }

    public boolean isSingleServer() {
        return serverUrls.size() == 1;
    }

    public static HostAndPort extractHostAndPort(@NotNull String serverUrl) {
        String[] hostAndPort = StringUtils.split(serverUrl, ":");
        return new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerConfiguration that = (ServerConfiguration) o;

        if (sslConnection != that.sslConnection) return false;
        if (connectOnIdeStartup != that.connectOnIdeStartup) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (serverUrls != null ? !serverUrls.equals(that.serverUrls) : that.serverUrls != null) return false;
        if (readPreference != null ? !readPreference.equals(that.readPreference) : that.readPreference != null)
            return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (authenticationDatabase != null ? !authenticationDatabase.equals(that.authenticationDatabase) : that.authenticationDatabase != null)
            return false;
        if (authenticationMechanism != that.authenticationMechanism) return false;
        if (userDatabase != null ? !userDatabase.equals(that.userDatabase) : that.userDatabase != null) return false;
        if (collectionsToIgnore != null ? !collectionsToIgnore.equals(that.collectionsToIgnore) : that.collectionsToIgnore != null)
            return false;
        if (shellArgumentsLine != null ? !shellArgumentsLine.equals(that.shellArgumentsLine) : that.shellArgumentsLine != null)
            return false;
        if (shellWorkingDir != null ? !shellWorkingDir.equals(that.shellWorkingDir) : that.shellWorkingDir != null)
            return false;
        return sshTunnelingConfiguration != null ? sshTunnelingConfiguration.equals(that.sshTunnelingConfiguration) : that.sshTunnelingConfiguration == null;

    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (serverUrls != null ? serverUrls.hashCode() : 0);
        result = 31 * result + (sslConnection ? 1 : 0);
        result = 31 * result + (readPreference != null ? readPreference.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (authenticationDatabase != null ? authenticationDatabase.hashCode() : 0);
        result = 31 * result + (authenticationMechanism != null ? authenticationMechanism.hashCode() : 0);
        result = 31 * result + (userDatabase != null ? userDatabase.hashCode() : 0);
        result = 31 * result + (connectOnIdeStartup ? 1 : 0);
        result = 31 * result + (collectionsToIgnore != null ? collectionsToIgnore.hashCode() : 0);
        result = 31 * result + (shellArgumentsLine != null ? shellArgumentsLine.hashCode() : 0);
        result = 31 * result + (shellWorkingDir != null ? shellWorkingDir.hashCode() : 0);
        result = 31 * result + (sshTunnelingConfiguration != null ? sshTunnelingConfiguration.hashCode() : 0);
        return result;
    }

    public ServerConfiguration clone() {
        try {
            return (ServerConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public static class HostAndPort {

        public final String host;
        public final int port;

        public HostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
