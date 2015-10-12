/*
 * Copyright (c) 2015 David Boissier
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

package org.codinjutsu.tools.mongo.logic;

import com.mongodb.AuthenticationMechanism;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MongoClientURIBuilderTest {

    @Test
    public void withoutAuthentication() throws Exception {
        assertEquals(
                "mongodb://localhost:27017,localhost:27018/",
                MongoClientURIBuilder.builder()
                        .setServerAddresses("localhost:27017,localhost:27018")
                        .build());
    }

    @Test
    public void withSimpleAuthentication() throws Exception {
        assertEquals(
                "mongodb://toto:pass@localhost:27018/?authSource=userdb",
                MongoClientURIBuilder.builder()
                        .setServerAddresses("localhost:27018")
                        .setCredential("toto", "pass", "userdb")
                        .build());
    }

    @Test
    public void withSpecificAuthentication() throws Exception {
        assertEquals(
                "mongodb://toto:pass@localhost:27018/?authMechanism=MONGODB-CR&authSource=userdb",
                MongoClientURIBuilder.builder()
                        .setServerAddresses("localhost:27018")
                        .setCredential("toto", "pass", "userdb")
                        .setAuthenticationMecanism(AuthenticationMechanism.MONGODB_CR)
                        .build());
    }

    @Test
    public void addSslOption() throws Exception {
        assertEquals(
                "mongodb://localhost:27018/?ssl=true",
                MongoClientURIBuilder.builder()
                        .setServerAddresses("localhost:27018")
                        .sslEnabled()
                        .build());
    }
}
