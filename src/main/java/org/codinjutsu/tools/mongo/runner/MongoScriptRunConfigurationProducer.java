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

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class MongoScriptRunConfigurationProducer extends LazyRunConfigurationProducer<MongoRunConfiguration> implements Cloneable {

    @Override
    protected boolean setupConfigurationFromContext(@NotNull MongoRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> ref) {
        Location location = context.getLocation();
        if (location == null) {
            return false;
        }

        PsiFile script = location.getPsiElement().getContainingFile();
        if (!isJavascriptFile(script)) return false;

        VirtualFile virtualFile = script.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }

        configuration.setName(virtualFile.getName());
        configuration.setScriptPath(virtualFile.getPath());
        final VirtualFile parent = virtualFile.getParent();
        if (parent != null && StringUtil.isEmpty(configuration.getShellWorkingDir())) {
            configuration.setShellWorkingDir(parent.getPath());
        }

        final Module module = ModuleUtilCore.findModuleForPsiElement(script);
        if (module != null) {
            configuration.setModule(module);
        }
        configuration.setName(configuration.suggestedName());
        return true;
    }

    private static boolean isJavascriptFile(@Nullable final PsiFile script) {
        return script != null && script.getFileType().getName().toLowerCase().contains("javascript");
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull MongoRunConfiguration configuration, @NotNull ConfigurationContext context) {
        final Location location = context.getLocation();
        if (location == null) return false;
        final PsiFile script = location.getPsiElement().getContainingFile();
        if (!isJavascriptFile(script)) return false;
        final VirtualFile virtualFile = script.getVirtualFile();
        if (virtualFile == null) return false;
        if (virtualFile instanceof LightVirtualFile) return false;
        final String workingDirectory = configuration.getShellWorkingDir();
        final String scriptName = configuration.getScriptPath();
        final String path = virtualFile.getPath();
        return scriptName.equals(path) || path.equals(new File(workingDirectory, scriptName).getAbsolutePath());
    }

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return MongoRunConfigurationType.getInstance().getFactory();
    }
}
