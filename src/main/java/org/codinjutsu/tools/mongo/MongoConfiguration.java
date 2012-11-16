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

public class MongoConfiguration {

    private static final String DEFAULT_SERVER_NAME = "localhost";
    private static final int DEFAULT_PORT = DBPort.PORT;

    private String serverName = DEFAULT_SERVER_NAME;
    private int serverPort = DEFAULT_PORT;

    private String username;
    private String password;

    public List<String> collectionsToIgnore = new LinkedList<String>();

    private String serverVersion;


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
}
