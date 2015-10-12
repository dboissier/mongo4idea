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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class MongoClientURIBuilder {

    private static final String DEFAULT_AUTH_DB = "admin";

    private String serverUrls;
    private String username;
    private String password;
    private String authDatabase;
    private AuthenticationMechanism authenticationMecanism;
    private boolean sslEnabled = false;

    private MongoClientURIBuilder() {
    }

    public static MongoClientURIBuilder builder() {
        return new MongoClientURIBuilder();
    }

    public MongoClientURIBuilder setServerAddresses(String serverUrls) {
        this.serverUrls = serverUrls;
        return this;
    }

    public MongoClientURIBuilder setCredential(String username, String password, String authDatabase) {
        this.username = username;
        this.password = password;
        this.authDatabase = StringUtils.isNotEmpty(authDatabase) ? authDatabase : DEFAULT_AUTH_DB;
        return this;
    }

    public String build() {
        StringBuilder strBuilder = new StringBuilder();
        Map<String,String> options = new HashMap<String, String>();
        if (StringUtils.isEmpty(username)) {
            strBuilder.append(String.format("mongodb://%s/", this.serverUrls));
        } else {
            strBuilder.append(String.format("mongodb://%s:%s@%s/", username, password, serverUrls));
        }
        if (authDatabase != null) {
            options.put("authSource", authDatabase);
        }

        if (authenticationMecanism != null) {
            options.put("authMechanism", authenticationMecanism.getMechanismName());
        }

        if (sslEnabled) {
            options.put("ssl", Boolean.TRUE.toString());
        }
        if (options.size() == 0) {
            return strBuilder.toString();
        }
        return strBuilder.append(buildOptions(options)).toString();
    }

    public MongoClientURIBuilder setAuthenticationMecanism(@NotNull AuthenticationMechanism authenticationMecanism) {
        this.authenticationMecanism = authenticationMecanism;
        return this;
    }

    public MongoClientURIBuilder sslEnabled() {
        sslEnabled = true;
        return this;
    }

    private static String buildOptions(Map<String, String> options) {
        List<String> optionList = new LinkedList<String>();
        for (Map.Entry<String, String> keyValue : options.entrySet()) {
            optionList.add(String.format("%s=%s", keyValue.getKey(), keyValue.getValue()));
        }
        return "?" + StringUtils.join(optionList, "&");
    }
}
