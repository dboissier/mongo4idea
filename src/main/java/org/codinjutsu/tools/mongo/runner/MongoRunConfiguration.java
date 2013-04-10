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

package org.codinjutsu.tools.mongo.runner;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfiguration;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MongoRunConfiguration extends RuntimeConfiguration {

    private String scriptPath;
    private String shellParameters;
    private boolean overwriteDefaultParameters;
    private String host;
    private int port;
    private String username;
    private String password;


    MongoRunConfiguration(Project project, ConfigurationFactory factory) {
        super("Mongo Script", project, factory);
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new MongoRunConfigurationEditor();
    }


    @Override
    public void readExternal(Element element) throws InvalidDataException {
        PathMacroManager.getInstance(getProject()).expandPaths(element);
        super.readExternal(element);
        scriptPath = JDOMExternalizer.readString(element, "path");
        shellParameters = JDOMExternalizer.readString(element, "shellParams");
        overwriteDefaultParameters = JDOMExternalizer.readBoolean(element, "overwriteDefault");
        if (overwriteDefaultParameters) {
            host = JDOMExternalizer.readString(element, "host");
            port = JDOMExternalizer.readInteger(element, "port", ServerConfiguration.DEFAULT_PORT);
            username = JDOMExternalizer.readString(element, "username");
            password = JDOMExternalizer.readString(element, "password");
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizer.write(element, "path", scriptPath);
        JDOMExternalizer.write(element, "shellParams", shellParameters);
        JDOMExternalizer.write(element, "overwriteDefault", overwriteDefaultParameters);
        if (overwriteDefaultParameters) {
            JDOMExternalizer.write(element, "host", host);
            JDOMExternalizer.write(element, "port", port);
            JDOMExternalizer.write(element, "username", username);
            JDOMExternalizer.write(element, "password", password);
        }
        PathMacroManager.getInstance(getProject()).collapsePathsRecursively(element);
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        final VirtualFile script = getScriptPath();
        if (script == null) {
            throw new CantRunException("Cannot find script " + scriptPath);
        }

        final MongoCommandLineState state = new MongoCommandLineState(this, env);
        state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
        return state;
    }

    public VirtualFile getScriptPath() {
        if (scriptPath == null) return null;
        return LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(scriptPath));
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public boolean isOverwriteDefaultParameters() {
        return overwriteDefaultParameters;
    }

    public void setOverwriteDefaultParameters(boolean overwriteDefaultParameters) {
        this.overwriteDefaultParameters = overwriteDefaultParameters;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getShellParameters() {
        return shellParameters;
    }

    public void setShellParameters(String shellParameters) {
        this.shellParameters = shellParameters;
    }
}
