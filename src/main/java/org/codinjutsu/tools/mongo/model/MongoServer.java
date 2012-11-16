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

import java.util.LinkedList;
import java.util.List;

public class MongoServer {

    private final String serverName;
    private final int serverPort;

    private final List<MongoDatabase> databases = new LinkedList<MongoDatabase>();

    public MongoServer(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
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
        if (Integer.parseInt(versionNumbers[0]) >= 2
                && Integer.parseInt(versionNumbers[1]) >= 2) {
            return true;
        }

        return false;
    }
}
