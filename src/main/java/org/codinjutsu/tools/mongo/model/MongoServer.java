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

package org.codinjutsu.tools.mongo.model;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.ServerConfiguration;

import java.util.LinkedList;
import java.util.List;

public class MongoServer {

    private final List<MongoDatabase> databases = new LinkedList<MongoDatabase>();
    private final ServerConfiguration configuration;

    public MongoServer(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getServerName() {
        return configuration.getServerName();
    }

    public int getServerPort() {
        return configuration.getServerPort();
    }

    public String getUsername() {
        return configuration.getUsername();
    }

    public String getPassword() {
        return configuration.getPassword();
    }

    public String getLabel() {
        return configuration.getLabel();
    }

    public void addDatabase(MongoDatabase mongoDatabase) {
        databases.add(mongoDatabase);
    }

    public boolean hasDatabases() {
        return !databases.isEmpty();
    }

    public List<MongoDatabase> getDatabases() {
        return databases;
    }

    public static boolean isCompliantWithPipelineOperations(String serverVersion) {

        if (StringUtils.isBlank(serverVersion)) {
            return false;
        }

        String[] versionNumbers = StringUtils.split(serverVersion, ".");

        return Integer.parseInt(versionNumbers[0]) >= 2 && Integer.parseInt(versionNumbers[1]) >= 2;
    }

    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    public void cleanAllDatabases() {
        databases.clear();
    }
}
