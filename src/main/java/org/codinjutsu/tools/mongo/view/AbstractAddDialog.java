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

package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBCheckBox;
import org.codinjutsu.tools.mongo.view.model.JsonDataType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAddDialog extends DialogWrapper {
    protected static final Map<JsonDataType, TextFieldWrapper> UI_COMPONENT_BY_JSON_DATATYPE = new HashMap<JsonDataType, TextFieldWrapper>();


    static {

        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.STRING, new StringFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.BOOLEAN, new BooleanFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.NUMBER, new StringFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.NULL, new NullFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.OBJECT, new StringFieldWrapper());
        UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.ARRAY, new StringFieldWrapper());
    }

    protected final MongoEditionPanel mongoEditionPanel;
    protected JsonDataType currentDataType = null;
    protected TextFieldWrapper currentEditor = null;


    public AbstractAddDialog(MongoEditionPanel mongoEditionPanel) {
        super(mongoEditionPanel, true);
        this.mongoEditionPanel = mongoEditionPanel;
    }

    protected void initCombo(final ComboBox combobox, final JPanel parentPanel) {
        combobox.setModel(new DefaultComboBoxModel(JsonDataType.values()));
        combobox.setRenderer(new ColoredListCellRenderer() {

            @Override
            protected void customizeCellRenderer(JList jList, Object o, int i, boolean b, boolean b2) {
                append(((JsonDataType) o).type);
            }
        });

        combobox.setSelectedItem(null);
        combobox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                JsonDataType selectedType = (JsonDataType) combobox.getSelectedItem();
                currentDataType = selectedType;
                currentEditor = UI_COMPONENT_BY_JSON_DATATYPE.get(selectedType);
                currentEditor.reset();

                parentPanel.invalidate();
                parentPanel.removeAll();
                parentPanel.add(currentEditor.getComponent(), BorderLayout.CENTER);
                parentPanel.validate();
            }
        });

        combobox.setSelectedItem(JsonDataType.STRING);
    }

    public JsonDataType getJsonDataType() {
        return currentDataType;
    }

    public abstract String getValue();

    public interface TextFieldWrapper<T extends JComponent> {

        String getValue();

        T getComponent();

        void reset();
    }

    protected static class StringFieldWrapper implements TextFieldWrapper {

        private final JTextField component;

        protected StringFieldWrapper() {
            component = new JTextField();
        }

        @Override
        public String getValue() {
            return component.getText();
        }

        public JTextField getComponent() {
            return component;
        }

        @Override
        public void reset() {
            component.setText("");
        }
    }

    protected static class BooleanFieldWrapper implements TextFieldWrapper {

        private final JBCheckBox component;

        protected BooleanFieldWrapper() {
            component = new JBCheckBox();
        }

        @Override
        public String getValue() {
            return String.valueOf(component.isSelected());
        }

        public JBCheckBox getComponent() {
            return component;
        }

        @Override
        public void reset() {
            component.setSelected(false);
        }
    }

    protected static class NullFieldWrapper implements TextFieldWrapper {

        private final JLabel component;

        protected NullFieldWrapper() {
            component = new JLabel("null");
        }

        @Override
        public String getValue() {
            return component.getText();
        }

        @Override
        public void reset() {

        }

        public JLabel getComponent() {
            return component;
        }
    }
}
