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

package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;

import javax.swing.*;

public class EditMongoDocumentAction extends AnAction implements DumbAware {

    private static final Icon EDIT_ICON = StyleAttributesProvider.getEditIcon();
    private final MongoResultPanel resultPanel;

    public EditMongoDocumentAction(MongoResultPanel resultPanel) {
        super("Edit", "Edit mongo document", EDIT_ICON);
        this.resultPanel = resultPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        resultPanel.editSelectedMongoDocument();
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setVisible(resultPanel.isSelectedNodeId());
    }
}
