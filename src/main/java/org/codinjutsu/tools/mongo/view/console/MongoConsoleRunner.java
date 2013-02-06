
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

package org.codinjutsu.tools.mongo.view.console;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.console.ConsoleHistoryController;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory;
import com.intellij.execution.runners.ConsoleExecuteActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;


public class MongoConsoleRunner extends AbstractConsoleRunnerWithHistory<MongoConsoleView> {

    public static final Key<Boolean> MONGO_SHELL_FILE = Key.create("MONGO_SHELL_FILE");
    private final ServerConfiguration serverConfiguration;


    public MongoConsoleRunner(@NotNull Project project, ServerConfiguration serverConfiguration) {
        super(project, "Mongo Shell", "/tmp");

        this.serverConfiguration = serverConfiguration;
    }

    @Override
    protected MongoConsoleView createConsoleView() {
        MongoConsoleView res = new MongoConsoleView(getProject());

        PsiFile file = res.getConsole().getFile();
        assert file.getContext() == null;
        file.putUserData(MONGO_SHELL_FILE, Boolean.TRUE);

        return res;
    }

    @Nullable
    @Override
    protected Process createProcess() throws ExecutionException {

        MongoConfiguration mongoConfiguration = MongoConfiguration.getInstance(getProject());
        String shellPath = mongoConfiguration.getShellPath();
        final GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(shellPath);

        commandLine.addParameter(String.format("%s:%s", serverConfiguration.getServerName(), serverConfiguration.getServerPort()));

        String username = serverConfiguration.getUsername();
        if (StringUtils.isNotBlank(username)) {
            commandLine.addParameter("--username");
            commandLine.addParameter(username);
        }

        String password = serverConfiguration.getPassword();
        if (StringUtils.isNotBlank(password)) {
            commandLine.addParameter("--password");
            commandLine.addParameter(password);
        }

        String shellArgumentsLine = serverConfiguration.getShellArgumentsLine();
        if (StringUtils.isNotBlank(shellArgumentsLine)) {
            commandLine.addParameter(shellArgumentsLine);
        }

        return commandLine.createProcess();
    }

    @Override
    protected OSProcessHandler createProcessHandler(Process process) {
        return new OSProcessHandler(process, null) {
            @Override
            protected Reader createProcessOutReader() {
                return super.createProcessOutReader();
            }
        };
    }

    @NotNull
    @Override
    protected ConsoleExecuteActionHandler createConsoleExecuteActionHandler() {
        ConsoleExecuteActionHandler handler = new ConsoleExecuteActionHandler(getProcessHandler(), false) {
            @Override
            public void processLine(String line) {
                super.processLine(line);
            }

            @Override
            public String getEmptyExecuteAction() {
                return "Mongo.Shell.Execute";
            }
        };
        new ConsoleHistoryController("Mongo Shell", null, getLanguageConsole(), handler.getConsoleHistoryModel()).install();
        return handler;
    }


}
