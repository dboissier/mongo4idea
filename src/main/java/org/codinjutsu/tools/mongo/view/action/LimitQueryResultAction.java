/*
 * Copyright (c) 2012 David Boissier
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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.ui.NumberDocument;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.view.QueryPanel;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

class LimitQueryResultAction extends AnAction implements CustomComponentAction {

    private final JTextField limitTextField;
    private final QueryPanel queryPanel;

    public LimitQueryResultAction(final QueryPanel queryPanel) {
        this.queryPanel = queryPanel;
        limitTextField = new JTextField();
        NumberDocument numberDocument = new NumberDocument();
        limitTextField.setDocument(numberDocument);
        limitTextField.setColumns(2);
        limitTextField.setToolTipText("Result limit");
        limitTextField.setText(String.valueOf(MongoQueryOptions.DEFAULT_RESULT_LIMIT));

        limitTextField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
//                queryPanel.setResultLimit(getLimitValue());
            }
        });
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        queryPanel.requestFocus();
    }

    private int getLimitValue() {
        String limit = limitTextField.getText();
        if (StringUtils.isBlank(limit)) {
            return 0;
        }
        return Integer.parseInt(limit);
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation) {
        return limitTextField;
    }
}
