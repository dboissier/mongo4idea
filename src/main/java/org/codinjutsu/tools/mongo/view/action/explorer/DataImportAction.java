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
