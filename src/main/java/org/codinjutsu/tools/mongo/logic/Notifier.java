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

package org.codinjutsu.tools.mongo.logic;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public class Notifier {

    private final NotificationGroup MONGO_GROUP = NotificationGroup.logOnlyGroup("Mongo");

    private final Project project;

    public static Notifier getInstance(Project project) {
        return ServiceManager.getService(project, Notifier.class);
    }

    private Notifier(Project project) {
        this.project = project;
    }

    public void notifyInfo(String message) {
        notify(message, NotificationType.INFORMATION);
    }

    public void notifyError(String message) {
        notify(message, NotificationType.ERROR);
    }

    private void notify(String message, NotificationType notificationType) {
        MONGO_GROUP.createNotification("[MongoPlugin] " + message, notificationType)
                .notify(project);
    }
}
