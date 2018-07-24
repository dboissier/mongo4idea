/*
 * Copyright (c) 2018 David Boissier.
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
import java.util.Objects;

import static java.util.Collections.singletonList;

public class ServerConfiguration implements Cloneable {

    public static final int DEFAULT_ROW_LIMIT = 300;

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

    private List<String> collectionsToIgnore = new LinkedList<>();
    private String shellArgumentsLine;
    private String shellWorkingDir;
    private Integer defaultRowLimit = DEFAULT_ROW_LIMIT;

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

    public Integer getDefaultRowLimit() {
        return defaultRowLimit;
    }

    public void setDefaultRowLimit(Integer defaultRowLimit) {
        this.defaultRowLimit = defaultRowLimit;
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
        if (!(o instanceof ServerConfiguration)) return false;
        ServerConfiguration that = (ServerConfiguration) o;
        return sslConnection == that.sslConnection &&
                Objects.equals(label, that.label) &&
                Objects.equals(serverUrls, that.serverUrls) &&
                Objects.equals(readPreference, that.readPreference) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(authenticationDatabase, that.authenticationDatabase) &&
                authenticationMechanism == that.authenticationMechanism &&
                Objects.equals(userDatabase, that.userDatabase) &&
                Objects.equals(collectionsToIgnore, that.collectionsToIgnore) &&
                Objects.equals(shellArgumentsLine, that.shellArgumentsLine) &&
                Objects.equals(shellWorkingDir, that.shellWorkingDir) &&
                Objects.equals(defaultRowLimit, that.defaultRowLimit) &&
                Objects.equals(sshTunnelingConfiguration, that.sshTunnelingConfiguration);
    }

    @Override
    public int hashCode() {

        return Objects.hash(label, serverUrls, sslConnection, readPreference, username, password, authenticationDatabase, authenticationMechanism, userDatabase, collectionsToIgnore, shellArgumentsLine, shellWorkingDir, defaultRowLimit, sshTunnelingConfiguration);
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
