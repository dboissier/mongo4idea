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

public class AddKeyDialog extends AbstractAddDialog {


    private JTextField nameTextfield;
    private ComboBox typeCombobox;
    private JPanel valuePanel;
    private JPanel mainPanel;

    public AddKeyDialog(MongoEditionPanel mongoEditionPanel) {
        super(mongoEditionPanel);
        valuePanel.setLayout(new BorderLayout());
        nameTextfield.setName("keyName");
        typeCombobox.setName("valueType");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    public static AddKeyDialog createDialog(MongoEditionPanel parentPanel) {
        AddKeyDialog dialog = new AddKeyDialog(parentPanel);
        dialog.init();
        dialog.setTitle("Add A Key");

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
        String keyName = getKey();
        if (StringUtils.isBlank(keyName)) {
            return new ValidationInfo("Key name is not set");
        }

        if (mongoEditionPanel.containsKey(keyName)) {
            return new ValidationInfo(String.format("Key '%s' is already used", keyName));
        }

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

    public String getKey() {
        return nameTextfield.getText();
    }

    @Override
    public String getValue() {
        return currentEditor.getValue();
    }
}