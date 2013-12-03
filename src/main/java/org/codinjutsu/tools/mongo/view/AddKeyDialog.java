package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBCheckBox;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.view.model.JsonDataType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddKeyDialog extends DialogWrapper {


    private static final Map<JsonDataType, TextFieldWrapper> UI_COMPONENT_BY_JSON_DATATYPE = new HashMap<JsonDataType, TextFieldWrapper>();

    static {

        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.STRING, new StringFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.BOOLEAN, new BooleanFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.NUMBER, new NumberFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.NULL, new NullFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.OBJECT, new StringFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.ARRAY, new StringFieldWrapper());
    }

    private final MongoEditionPanel mongoEditionPanel;

    private JTextField nameTextfield;
    private ComboBox typeCombobox;
    private JPanel valuePanel;
    private JPanel mainPanel;
    private JsonDataType currentDataType = null;
    private TextFieldWrapper currentEditor = null;

    public AddKeyDialog(MongoEditionPanel mongoEditionPanel) {
        super(mongoEditionPanel, true);
        this.mongoEditionPanel = mongoEditionPanel;
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

        typeCombobox.setModel(new DefaultComboBoxModel(JsonDataType.values()));
        typeCombobox.setRenderer(new ColoredListCellRenderer() {

            @Override
            protected void customizeCellRenderer(JList jList, Object o, int i, boolean b, boolean b2) {
                append(((JsonDataType) o).type);
            }
        });

        typeCombobox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                JsonDataType selectedType = (JsonDataType) typeCombobox.getSelectedItem();
                valuePanel.invalidate();
                valuePanel.removeAll();

                currentDataType = selectedType;
                currentEditor = UI_COMPONENT_BY_JSON_DATATYPE.get(selectedType);

                valuePanel.add(currentEditor.getComponent(), BorderLayout.CENTER);
                valuePanel.validate();
            }
        });

        typeCombobox.setSelectedItem(JsonDataType.STRING);
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

        String value = getKeyValue();
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

    public JsonDataType getJsonDataType() {
        return currentDataType;
    }

    public String getKeyValue() {
        return currentEditor.getValue();
    }

    public interface TextFieldWrapper<T extends JComponent> {

        String getValue();

        T getComponent();
    }

    private static class StringFieldWrapper implements TextFieldWrapper<JTextField> {

        private final JTextField component;

        private StringFieldWrapper() {
            component = new JTextField();
        }

        @Override
        public String getValue() {
            return component.getText();
        }

        public JTextField getComponent() {
            return component;
        }
    }

    private static class BooleanFieldWrapper implements TextFieldWrapper<JBCheckBox> {

        private final JBCheckBox component;

        private BooleanFieldWrapper() {
            component = new JBCheckBox();
        }

        @Override
        public String getValue() {
            return String.valueOf(component.isSelected());
        }

        public JBCheckBox getComponent() {
            return component;
        }
    }

    private static class NumberFieldWrapper implements TextFieldWrapper<JTextField> {

        private final JFormattedTextField component;

        private NumberFieldWrapper() {
            component = new JFormattedTextField(DecimalFormat.getNumberInstance(Locale.ENGLISH));
        }

        @Override
        public String getValue() {
            return component.getText();
        }


        public JTextField getComponent() {
            return component;
        }
    }

    private static class NullFieldWrapper implements TextFieldWrapper<JLabel> {

        private final JLabel component;

        private NullFieldWrapper() {
            component = new JLabel("null");
        }

        @Override
        public String getValue() {
            return component.getText();
        }


        public JLabel getComponent() {
            return component;
        }
    }
}