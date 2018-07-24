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

package org.codinjutsu.tools.mongo.view.action.explorer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;
import org.codinjutsu.tools.mongo.view.dataimport.DataImportDialog;

public class DataImportAction extends AnAction implements DumbAware {

    private final MongoExplorerPanel mongoExplorerPanel;

    public DataImportAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Import data", "Import data from file", null);
        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        MongoCollection mongoCollection = mongoExplorerPanel.getSelectedCollection();
        if (mongoCollection == null) {
            return;
        }

        DataImportDialog dataImportDialog = DataImportDialog.create(event.getProject(), mongoCollection);
        dataImportDialog.show();

        if (!dataImportDialog.isOK()) {
            return;
        }

        mongoExplorerPanel.importDataFile(mongoCollection, dataImportDialog.getDocumentToImportFilePath(), dataImportDialog.replaceAllDocuments());
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoExplorerPanel.getSelectedCollection() != null);

    }
}
