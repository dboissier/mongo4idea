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
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.mongodb.DBRef;
import org.bson.Document;
import org.codinjutsu.tools.mongo.view.JsonTreeTableView;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;
import org.codinjutsu.tools.mongo.view.model.JsonTreeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GoToMongoDocumentAction extends AnAction implements DumbAware {

    private final MongoResultPanel mongoResultPanel;

    public GoToMongoDocumentAction(MongoResultPanel mongoResultPanel) {
        super("View reference");
        this.mongoResultPanel = mongoResultPanel;

        registerCustomShortcutSet(KeyEvent.VK_B,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                mongoResultPanel);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DBRef selectedDBRef = mongoResultPanel.getSelectedDBRef();

        Document referencedDocument = mongoResultPanel.getReferencedDocument(selectedDBRef);
        if (referencedDocument == null) {
            Messages.showErrorDialog(mongoResultPanel, "Referenced document was not found");
            return;
        }
        DialogBuilder dialogBuilder = new DialogBuilder(anActionEvent.getProject())
                .centerPanel(new MongoDocumentPanel(
                        referencedDocument
                ))
                .title(String.format("Detail of /%s/%s/%s",
                        selectedDBRef.getDatabaseName(),
                        selectedDBRef.getCollectionName(),
                        selectedDBRef.getId().toString()));
        dialogBuilder.removeAllActions();
        dialogBuilder.addCloseButton();
        dialogBuilder.show();
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(mongoResultPanel.isSelectedDBRef());
    }

    private static class MongoDocumentPanel extends JPanel {

        MongoDocumentPanel(Document document) {
            super(new BorderLayout());

            JsonTreeTableView resultTreeTableView = new JsonTreeTableView(
                    JsonTreeUtils.buildJsonTree(document), JsonTreeTableView.COLUMNS_FOR_READING);
            resultTreeTableView.setName("resultTreeTable");

            this.add(new JBScrollPane(resultTreeTableView), BorderLayout.CENTER);
        }
    }
}
