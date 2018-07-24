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
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MongoScriptRunConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {

    private PsiFile sourceFile;

    public MongoScriptRunConfigurationProducer() {
        super(MongoRunConfigurationType.getInstance());
    }

    @Override
    public PsiElement getSourceElement() {
        return sourceFile;
    }

    @Nullable
    @Override
    protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext configurationContext) {
        sourceFile = location.getPsiElement().getContainingFile();
        if (sourceFile != null && sourceFile.getFileType().getName().toLowerCase().contains("javascript")) {
            Project project = sourceFile.getProject();

            VirtualFile file = sourceFile.getVirtualFile();

            RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(project, configurationContext);

            MongoRunConfiguration runConfiguration = (MongoRunConfiguration) settings.getConfiguration();
            runConfiguration.setName(file.getName());

            runConfiguration.setScriptPath(file.getPath());

            Module module = ModuleUtil.findModuleForPsiElement(location.getPsiElement());
            if (module != null) {
                runConfiguration.setModule(module);
            }

            return settings;
        }
        return null;
    }


    @Override
    public int compareTo(@NotNull Object o) {
        return 0;
    }
}
