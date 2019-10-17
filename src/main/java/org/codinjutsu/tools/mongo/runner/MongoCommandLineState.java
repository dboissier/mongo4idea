/*
 * Copyright (c) 2018 David Boissier.
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

package org.codinjutsu.tools.mongo.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.utils.MongoUtils;
import org.jetbrains.annotations.NotNull;

class MongoCommandLineState extends CommandLineState {

    private final MongoRunConfiguration mongoRunConfiguration;

    MongoCommandLineState(MongoRunConfiguration mongoRunConfiguration, ExecutionEnvironment environment) {
        super(environment);
        this.mongoRunConfiguration = mongoRunConfiguration;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
//TODO need to refactor
        GeneralCommandLine commandLine =
                MongoUtils.buildCommandLine(mongoRunConfiguration.getMongoShell(),
                        mongoRunConfiguration.getServerConfiguration(),
                        mongoRunConfiguration.getDatabase());

        commandLine = overwriteSomeParametersIfNeeded(commandLine);
        commandLine.addParameter(mongoRunConfiguration.getScriptPath());

        final OSProcessHandler processHandler = new ColoredProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler);
        return processHandler;
    }

    private GeneralCommandLine overwriteSomeParametersIfNeeded(GeneralCommandLine commandLine) {
        String shellWorkingDir = mongoRunConfiguration.getShellWorkingDir();
        if (StringUtils.isNotEmpty(shellWorkingDir)) {
            commandLine.setWorkDirectory(shellWorkingDir);
        }

        String shellParameters = mongoRunConfiguration.getShellParameters();
        if (StringUtils.isNotEmpty(shellWorkingDir)) {
            commandLine.addParameters(shellParameters.split(" "));
        }

        return commandLine;
    }
}