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

import com.intellij.execution.Executor;
import com.intellij.execution.configuration.AbstractRunConfiguration;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

class MongoRunConfiguration extends AbstractRunConfiguration {

    private static final String SCRIPT_PATH = "SCRIPT_PATH";
    private static final String SHELL_PARAMETERS = "SHELL_PARAMETERS";

    private final String mongoShell;
    private String scriptPath;
    private String shellParameters;
    private ServerConfiguration serverConfiguration;
    private MongoDatabase database;
    private String shellWorkingDir;


    MongoRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);

        mongoShell = MongoConfiguration.getInstance(getProject()).getShellPath();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new MongoRunConfigurationEditor(getProject());
    }


    @Override
    public Collection<Module> getValidModules() {
        Module[] allModules = ModuleManager.getInstance(getProject()).getModules();
        return Arrays.asList(allModules);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        scriptPath = JDOMExternalizerUtil.readField(element, SCRIPT_PATH);
        shellParameters = JDOMExternalizerUtil.readField(element, SHELL_PARAMETERS);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, SCRIPT_PATH, scriptPath);
        JDOMExternalizerUtil.writeField(element, SHELL_PARAMETERS, shellParameters);

    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        final MongoCommandLineState state = new MongoCommandLineState(this, env);
        state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
        return state;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (mongoShell == null) {
            throw new RuntimeConfigurationError("Mongo shell path is not set.");
        }

        if (scriptPath == null) {
            throw new RuntimeConfigurationError("Script path is not set.");
        }

        if (serverConfiguration == null) {
            throw new RuntimeConfigurationError("Server is not set.");
        }

        if (database == null) {
            throw new RuntimeConfigurationError("Database is not set.");
        }
    }

    @Nullable
    @Override
    public String suggestedName() {
        if (scriptPath == null) {
            return null;
        }
        return new File(scriptPath).getName();
    }

    String getScriptPath() {
        return scriptPath;
    }

    void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    String getShellParameters() {
        return shellParameters;
    }

    void setShellParameters(String shellParameters) {
        this.shellParameters = shellParameters;
    }

    String getMongoShell() {
        return mongoShell;
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    String getShellWorkingDir() {
        return shellWorkingDir;
    }

    void setShellWorkingDir(String shellWorkingDir) {
        this.shellWorkingDir = shellWorkingDir;
    }
}
