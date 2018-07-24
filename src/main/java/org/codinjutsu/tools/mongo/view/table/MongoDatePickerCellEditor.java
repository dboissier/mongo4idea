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

package org.codinjutsu.tools.mongo.view.table;

import org.codinjutsu.tools.mongo.utils.DateUtils;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.jdesktop.swingx.table.DatePickerCellEditor;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Locale;

public class MongoDatePickerCellEditor extends DatePickerCellEditor {

    public MongoDatePickerCellEditor() {
        this.dateFormat = DateUtils.utcDateTime(Locale.getDefault());
        datePicker = DateTimePicker.create();
        datePicker.getEditor().setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        datePicker.getEditor().setEditable(false);
    }

    @Override
    protected Date getValueAsDate(Object value) {
        MongoNodeDescriptor descriptor = (MongoNodeDescriptor) value;

        return super.getValueAsDate(descriptor.getValue());
    }

    public void addActionListener(ActionListener actionListener) {
        datePicker.addActionListener(actionListener);
    }
}
