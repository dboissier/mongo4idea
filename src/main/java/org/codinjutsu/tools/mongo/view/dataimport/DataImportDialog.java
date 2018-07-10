package org.codinjutsu.tools.mongo.view.dataimport;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoDatabase;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class DataImportDialog extends DialogWrapper {

    private final MongoCollection mongoCollection;
    private TextFieldWithBrowseButton documentToImportTextField;
    private JLabel targetServerLabel;
    private JLabel targetDatabaseLabel;
    private JLabel targetCollectionLabel;
    private JPanel rootPanel;
    private JCheckBox replaceAllContentCheckBox;

    public static DataImportDialog create(Project project, MongoCollection mongoCollection) {
        DataImportDialog dataImportDialog = new DataImportDialog(project, mongoCollection);
        dataImportDialog.init();
        dataImportDialog.setTitle("Import Data from File");
        return dataImportDialog;
    }

    private DataImportDialog(Project project, MongoCollection mongoCollection) {
        super(project, false);
        this.mongoCollection = mongoCollection;
    }

    @Override
    protected void init() {
        initLabels();
        super.init();
    }

    private void initLabels() {
        MongoDatabase parentDatabase = mongoCollection.getParentDatabase();
        targetCollectionLabel.setText(mongoCollection.getName());
        targetDatabaseLabel.setText(parentDatabase.getName());
        targetServerLabel.setText(parentDatabase.getParentServer().getLabel());
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String filePath = documentToImportTextField.getTextField().getText();
        if (StringUtils.isBlank(filePath)) {
            return new ValidationInfo("Data file to import should be set", rootPanel);
        }
        File file;
        file = new File(filePath);
        if (!file.exists()) {
            return new ValidationInfo("Data file does not exist", rootPanel);
        }

        return super.doValidate();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    public String getDocumentToImportFilePath() {
        return documentToImportTextField.getText();
    }

    public boolean replaceAllDocuments() {
        return replaceAllContentCheckBox.isSelected();
    }

    private void createUIComponents() {
        documentToImportTextField = new TextFieldWithBrowseButton();
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> privateKeyBrowseFolderActionListener =
                new ComponentWithBrowseButton.BrowseFolderActionListener<>("Mongo Shell Working Directory",
                        null,
                        documentToImportTextField,
                        null,
                        fileChooserDescriptor,
                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        documentToImportTextField.addBrowseFolderListener(null, privateKeyBrowseFolderActionListener, false);
    }
}