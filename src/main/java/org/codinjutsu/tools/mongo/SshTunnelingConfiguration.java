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

public class SshTunnelingConfiguration implements Cloneable {

    public static SshTunnelingConfiguration EMPTY = new SshTunnelingConfiguration();

    private String proxyHost;
    private Integer proxyPort;
    private String proxyUser;
    private String proxyPassword;

    public SshTunnelingConfiguration() {
        proxyHost = null;
        proxyPort = null;
        proxyUser = null;
        proxyPassword = null;
    }

    public SshTunnelingConfiguration(String sshProxyHost, Integer sshProxyPort, String sshProxyUser, String sshProxyPassword) {
        proxyHost = sshProxyHost;
        proxyPort = sshProxyPort;
        proxyUser = sshProxyUser;
        proxyPassword = sshProxyPassword;
    }


    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public static boolean isEmpty(SshTunnelingConfiguration sshTunnelingConfiguration) {
        return sshTunnelingConfiguration == null || EMPTY.equals(sshTunnelingConfiguration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SshTunnelingConfiguration that = (SshTunnelingConfiguration) o;

        if (proxyHost != null ? !proxyHost.equals(that.proxyHost) : that.proxyHost != null) return false;
        if (proxyPort != null ? !proxyPort.equals(that.proxyPort) : that.proxyPort != null) return false;
        if (proxyUser != null ? !proxyUser.equals(that.proxyUser) : that.proxyUser != null) return false;
        return proxyPassword != null ? proxyPassword.equals(that.proxyPassword) : that.proxyPassword == null;

    }

    @Override
    public int hashCode() {
        int result = proxyHost != null ? proxyHost.hashCode() : 0;
        result = 31 * result + (proxyPort != null ? proxyPort.hashCode() : 0);
        result = 31 * result + (proxyUser != null ? proxyUser.hashCode() : 0);
        result = 31 * result + (proxyPassword != null ? proxyPassword.hashCode() : 0);
        return result;
    }

    public ServerConfiguration clone() {
        try {
            return (ServerConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
