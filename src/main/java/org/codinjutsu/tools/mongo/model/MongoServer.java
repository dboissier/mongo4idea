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

package org.codinjutsu.tools.mongo.model;

import org.codinjutsu.tools.mongo.ServerConfiguration;

import java.util.LinkedList;
import java.util.List;

public class MongoServer {

    public enum Status {
        OK, LOADING, ERROR
    }

    private List<MongoDatabase> databases = new LinkedList<>();

    private final ServerConfiguration configuration;

    private Status status = Status.OK;

    public MongoServer(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<String> getServerUrls() {
        return configuration.getServerUrls();
    }

    public String getLabel() {
        return configuration.getLabel();
    }

    public void setDatabases(List<MongoDatabase> databases) {
        this.databases = databases;
    }

    public boolean hasDatabases() {
        return !databases.isEmpty();
    }

    public List<MongoDatabase> getDatabases() {
        return databases;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ServerConfiguration getConfiguration() {
        return configuration;
    }
}
