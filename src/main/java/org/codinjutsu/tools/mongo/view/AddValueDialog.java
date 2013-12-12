package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.view.model.JsonDataType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AddValueDialog extends AbstractAddDialog {


    private ComboBox typeCombobox;
    private JPanel valuePanel;
    private JPanel mainPanel;

    public AddValueDialog(MongoEditionPanel mongoEditionPanel) {
        super(mongoEditionPanel);
        valuePanel.setLayout(new BorderLayout());
        typeCombobox.setName("valueType");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    public static AddValueDialog createDialog(MongoEditionPanel parentPanel) {
        AddValueDialog dialog = new AddValueDialog(parentPanel);
        dialog.init();
        dialog.setTitle("Add A Key");
        dialog.setSize(400, 300);

        return dialog;
    }

    @Override
    protected void init() {
        super.init();

        initCombo(typeCombobox, valuePanel);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {

        JsonDataType dataType = getJsonDataType();
        if (JsonDataType.NULL.equals(dataType)) {
            return null;
        }

        String value = getValue();
        if (JsonDataType.NUMBER.equals(dataType) && StringUtils.isEmpty(value)) {
            return new ValidationInfo("Key value is not set");
        }

        if (JsonDataType.OBJECT.equals(dataType)) {
            try {
                JSON.parse(value);
            } catch (JSONParseException e) {
                return new ValidationInfo("Invalid JSON object");
            }
        }

        return null;
    }

    @Override
    public String getValue() {
        return currentEditor.getValue();
    }

}