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

package org.codinjutsu.tools.mongo.logic;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.apache.commons.lang.StringUtils;

import java.net.UnknownHostException;

public class MongoManager {

    public void connect(String serverName, int serverPort, String username, String password) {
        try {
            Mongo mongo = new Mongo(serverName, serverPort);
            DB test = mongo.getDB("test");
            if (StringUtils.isNotBlank(username)) {
                test.authenticate(username, password.toCharArray());
            }
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        }
    }
}
