package org.codinjutsu.tools.mongo.view.edition;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.bson.Document;
import org.codinjutsu.tools.mongo.view.MongoPanel;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MongoEditionDialog extends DialogWrapper {

    private final MongoEditionPanel editionPanel;

    public static MongoEditionDialog create(@Nullable Project project, MongoPanel.MongoDocumentOperations operations,
                                            MongoResultPanel.ActionCallback actionCallback) {
        return new MongoEditionDialog(project, operations, actionCallback);
    }

    private MongoEditionDialog(@Nullable Project project, MongoPanel.MongoDocumentOperations operations,
                                 MongoResultPanel.ActionCallback actionCallback) {
        super(project, true);
        editionPanel = new MongoEditionPanel(operations, actionCallback);
        init();
    }



    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return editionPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (!editionPanel.save()) {
            return new ValidationInfo("Unable to save the document", editionPanel);
        }
        return null;
    }



    public MongoEditionDialog initDocument(Document mongoDocument) {
        String dialogTitle = "New document";
        if (mongoDocument != null) {
            dialogTitle = "Edition";
        }
        setTitle(dialogTitle);
        editionPanel.updateEditionTree(mongoDocument);
        return this;
    }
}
