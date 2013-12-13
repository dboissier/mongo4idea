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

package org.codinjutsu.tools.mongo.view.action.edition;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.AddValueDialog;
import org.codinjutsu.tools.mongo.view.MongoEditionPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class AddValueAction extends AnAction {

    private static final Icon ADD_ICON = StyleAttributesUtils.getInstance().getAddIcon();

    private final MongoEditionPanel mongoEditionPanel;

    public AddValueAction(MongoEditionPanel mongoEditionPanel) {
        super("Add a value", "Add a value", ADD_ICON);
        this.mongoEditionPanel = mongoEditionPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        AddValueDialog dialog = AddValueDialog.createDialog(mongoEditionPanel);
        dialog.show();

        if (!dialog.isOK()) {
            return;
        }

        mongoEditionPanel.addValue(dialog.getJsonDataType(), dialog.getValue());
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoEditionPanel.canAddValue());
    }
}