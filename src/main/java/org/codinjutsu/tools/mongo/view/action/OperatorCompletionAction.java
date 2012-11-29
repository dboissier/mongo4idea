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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.components.JBList;
import com.mongodb.QueryOperators;

import java.awt.event.KeyEvent;
import java.util.Arrays;

public class OperatorCompletionAction extends AnAction implements Disposable {

    private static final JBList QUERY_OPERATOR_LIST = new JBList(Arrays.asList(
            QueryOperators.GT,
            QueryOperators.GTE,
            QueryOperators.LT,
            QueryOperators.LTE,
            QueryOperators.NE,
            QueryOperators.IN,
            QueryOperators.NIN,
            QueryOperators.MOD,
            QueryOperators.ALL,
            QueryOperators.SIZE,
            QueryOperators.EXISTS,
            QueryOperators.WHERE,
            QueryOperators.NEAR
    ));

    private final Editor editor;

    public OperatorCompletionAction(Editor editor) {
        this.editor = editor;
        registerCustomShortcutSet(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK, editor.getContentComponent());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        final int offset = caretModel.getOffset();
        new PopupChooserBuilder(QUERY_OPERATOR_LIST)
                .setMovable(false)
                .setCancelKeyEnabled(true)
                .setItemChoosenCallback(new Runnable() {
                    public void run() {
                        final String selectedQueryOperator = (String) QUERY_OPERATOR_LIST.getSelectedValue();
                        if (selectedQueryOperator == null) return;

                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            public void run() {
                                document.insertString(offset, selectedQueryOperator);
                            }
                        });


                    }
                })
                .createPopup()
                .showInBestPositionFor(editor);
    }

    @Override
    public void dispose() {
        unregisterCustomShortcutSet(editor.getContentComponent());
    }
}
