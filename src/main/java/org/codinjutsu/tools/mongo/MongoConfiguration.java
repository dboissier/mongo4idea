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

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.util.LinkedList;
import java.util.List;

@State(
        name = "MongoConfiguration",
        storages = {
                @Storage(file = "$PROJECT_FILE$"),
                @Storage(file = "$PROJECT_CONFIG_DIR$/mongoSettings.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class MongoConfiguration implements PersistentStateComponent<MongoConfiguration> {

    private List<ServerConfiguration> serverConfigurations = new LinkedList<>();
    private String shellPath;

    public static MongoConfiguration getInstance(Project project) {
        return ServiceManager.getService(project, MongoConfiguration.class);
    }


    public MongoConfiguration getState() {
        return this;
    }

    public void loadState(MongoConfiguration mongoConfiguration) {
        XmlSerializerUtil.copyBean(mongoConfiguration, this);
    }

    public void setServerConfigurations(List<ServerConfiguration> serverConfigurations) {
        this.serverConfigurations = serverConfigurations;
    }

    public List<ServerConfiguration> getServerConfigurations() {
        return serverConfigurations;
    }

    public String getShellPath() {
        return shellPath;
    }

    public void setShellPath(String shellPath) {
        this.shellPath = shellPath;
    }
}
