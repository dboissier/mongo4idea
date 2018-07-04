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

package org.codinjutsu.tools.mongo.utils;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.mongodb.AuthenticationMechanism;
import org.bson.Document;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoUtilsTest {

    @Test
    public void buildCommandeLine() {
        ServerConfiguration serverConfiguration = ServerConfiguration.byDefault();
        serverConfiguration.setUsername("john.doe");
        serverConfiguration.setPassword("secretPassword");
        serverConfiguration.setAuthenticationDatabase("users");
        serverConfiguration.setAuthenticationMechanism(AuthenticationMechanism.SCRAM_SHA_1);

        serverConfiguration.setShellWorkingDir("/tmp");
        serverConfiguration.setShellArgumentsLine("--quiet --ipv6");

        GeneralCommandLine commandLine = MongoUtils.buildCommandLine("/usr/bin/mongo", serverConfiguration,
                new MongoDatabase("mydatabase", new MongoServer(serverConfiguration)));

        assertThat(commandLine.getCommandLineString())
                .isEqualTo("/usr/bin/mongo localhost:27017/mydatabase " +
                        "--username john.doe --password secretPassword " +
                        "--authenticationDatabase users --authenticationMechanism SCRAM-SHA-1 " +
                        "--quiet --ipv6");
    }

    @Test
    public void buildMongoUrl() {
        ServerConfiguration serverConfiguration = ServerConfiguration.byDefault();
        assertThat(MongoUtils.buildMongoUrl(serverConfiguration, new MongoDatabase("mydatabase", new MongoServer(serverConfiguration))))
                .isEqualTo("localhost:27017/mydatabase");
    }

    @Test
    public void buildMongoUrlWithoutDatabase() {
        assertThat(MongoUtils.buildMongoUrl(ServerConfiguration.byDefault(), null))
                .isEqualTo("localhost:27017/test");

    }

    @Test
    public void stringifyListOfSimpleObjects() {
        List<Object> list = new LinkedList<>();
        list.add("foo");
        list.add(123);
        list.add(null);
        list.add(new Document("key", "value"));

        assertThat(MongoUtils.stringifyList(list)).isEqualTo("[\"foo\", 123, null, { \"key\" : \"value\" }]");
    }

    @Test
    public void stringifyListOfInnerList() {
        List<Object> innerList = new LinkedList<>();
        innerList.add("foo");
        innerList.add(new Document("key", "value1"));
        List<Object> list = new LinkedList<>();
        list.add(innerList);
        list.add(new Document("bar", 12));

        assertThat(MongoUtils.stringifyList(list)).isEqualTo("[[\"foo\", { \"key\" : \"value1\" }], { \"bar\" : 12 }]");
    }
}