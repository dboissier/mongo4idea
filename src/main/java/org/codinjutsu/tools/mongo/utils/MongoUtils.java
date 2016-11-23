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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.mongodb.AuthenticationMechanism;
import org.bson.Document;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.MongoDatabase;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MongoUtils {

    private MongoUtils() {
    }

    public static boolean checkMongoShellPath(String mongoShellPath) throws ExecutionException {
        if (isBlank(mongoShellPath)) {
            return false;
        }

        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(mongoShellPath);
        commandLine.addParameter("--version");
        CapturingProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(),
                CharsetToolkit.getDefaultSystemCharset(),
                commandLine.getCommandLineString());
        ProcessOutput result = handler.runProcess(15 * 1000);
        return result.getExitCode() == 0;
    }

    public static GeneralCommandLine buildCommandLine(String shellPath, ServerConfiguration serverConfiguration, MongoDatabase database) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(shellPath);
        commandLine.addParameter(buildMongoUrl(serverConfiguration, database));


        String username = serverConfiguration.getUsername();
        if (org.apache.commons.lang.StringUtils.isNotBlank(username)) {
            commandLine.addParameter("--username");
            commandLine.addParameter(username);
        }

        String password = serverConfiguration.getPassword();
        if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
            commandLine.addParameter("--password");
            commandLine.addParameter(password);
        }

        String authenticationDatabase = serverConfiguration.getAuthenticationDatabase();
        if (org.apache.commons.lang.StringUtils.isNotBlank(authenticationDatabase)) {
            commandLine.addParameter("--authenticationDatabase");
            commandLine.addParameter(authenticationDatabase);
        }

        AuthenticationMechanism authenticationMechanism = serverConfiguration.getAuthenticationMechanism();
        if (authenticationMechanism != null) {
            commandLine.addParameter("--authenticationMechanism");
            commandLine.addParameter(authenticationMechanism.getMechanismName());
        }

        String shellWorkingDir = serverConfiguration.getShellWorkingDir();
        if (org.apache.commons.lang.StringUtils.isNotBlank(shellWorkingDir)) {
            commandLine.setWorkDirectory(shellWorkingDir);
        }

        String shellArgumentsLine = serverConfiguration.getShellArgumentsLine();
        if (org.apache.commons.lang.StringUtils.isNotBlank(shellArgumentsLine)) {
            commandLine.addParameters(shellArgumentsLine.split(" "));
        }

        return commandLine;
    }

    static String buildMongoUrl(ServerConfiguration serverConfiguration, MongoDatabase database) {
        return String.format("%s/%s", serverConfiguration.getServerUrls().get(0), database == null ? "test" : database.getName());
    }

    public static String stringifyList(List list) {
        List<String> stringifiedObjects = new LinkedList<>();
        for (Object object : list) {
            if (object == null) {
                stringifiedObjects.add("null");
            } else if (object instanceof String) {
                stringifiedObjects.add("\"" + object.toString() + "\"");
            } else if (object instanceof Document) {
                stringifiedObjects.add(((Document) object).toJson());
            } else if (object instanceof List) {
                stringifiedObjects.add(stringifyList(((List) object)));
            } else {
                stringifiedObjects.add(object.toString());
            }
        }

        return "[" + org.apache.commons.lang.StringUtils.join(stringifiedObjects, ", ") + "]";
    }
}
