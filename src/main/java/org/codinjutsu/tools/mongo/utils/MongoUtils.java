/*
 * Copyright (c) 2013 David Boissier
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
import com.mongodb.util.JSON;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.codinjutsu.tools.mongo.view.model.JsonDataType;

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
        CapturingProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), CharsetToolkit.getDefaultSystemCharset());
        ProcessOutput result = handler.runProcess(15 * 1000);
        return result.getExitCode() == 0;
    }

    public static String buildMongoUrl(ServerConfiguration serverConfiguration, MongoDatabase database) {
        return String.format("%s/%s", serverConfiguration.getServerUrls().get(0), database == null ? "test" : database.getName());
    }
}
