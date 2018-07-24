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

package org.codinjutsu.tools.mongo;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.mongo.view.MongoWindowManager;
import org.jetbrains.annotations.NotNull;


public class MongoComponent extends AbstractProjectComponent {

    private static final String MONGO_COMPONENT_NAME = "Mongo";

    public MongoComponent(Project project) {
        super(project);

    }

    @NotNull
    public String getComponentName() {
        return MONGO_COMPONENT_NAME;
    }


    public void projectOpened() {
        MongoWindowManager.getInstance(myProject);
    }

    public void projectClosed() {
        MongoWindowManager.getInstance(myProject).unregisterMyself();
    }
}
