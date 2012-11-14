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

package org.codinjutsu.tools.mongo.view;

import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.PlainTextSyntaxHighlighterFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.Alarm;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoAggregateOperator;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

public class QueryPanel extends JPanel implements Disposable {

    private static final Icon ADD_ICON = GuiUtils.loadIcon("add.png");

    private JPanel mainPanel;
    private JPanel editorPanel;
    private NonOpaquePanel headQueryPanel;

    private Editor projectEditor;
    private Editor groupEditor;

    private List<OperatorPanel> operatorPanels = new LinkedList<OperatorPanel>();

    private final Alarm myUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);


    public static QueryPanel queryPanel() {
        return new QueryPanel(true);
    }

    public static QueryPanel withoutEditor() {
        return new QueryPanel(false);
    }

    private QueryPanel(boolean withEditor) {

        setLayout(new BorderLayout());

        JLabel addLabel = new JLabel();
        addLabel.setIcon(ADD_ICON);
        addLabel.setToolTipText("Add operation");
        addLabel.addMouseListener(new AddOperatorPanelAction(addLabel, this));
        headQueryPanel.add(addLabel, BorderLayout.EAST);
        headQueryPanel.add(new JLabel("Query options (CTRL+F5 to execute)"), BorderLayout.WEST);

        add(mainPanel, BorderLayout.CENTER);
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));

        if (withEditor) {
            addOperatorPanel(MongoAggregateOperator.MATCH);
        }

    }

    private void addOperatorPanel(MongoAggregateOperator selectedOperator) {
        OperatorPanel matchOperatorPanel = new OperatorPanel(createEditor(), selectedOperator);
        matchOperatorPanel.getCloseLabel().addMouseListener(new RemoveOperatorPanelAction(this, matchOperatorPanel));
        operatorPanels.add(matchOperatorPanel);
        myUpdateAlarm.setActivationComponent(matchOperatorPanel);

        editorPanel.invalidate();
        editorPanel.add(matchOperatorPanel);
        editorPanel.validate();
    }


    private static Editor createEditor() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document editorDocument = editorFactory.createDocument("");
        EditorEx editor = (EditorEx) editorFactory.createEditor(editorDocument);
        fillEditorSettings(editor.getSettings());
        attachHighlighter(editor);

        return editor;
    }

    private static void fillEditorSettings(final EditorSettings editorSettings) {
        editorSettings.setWhitespacesShown(true);
        editorSettings.setLineMarkerAreaShown(false);
        editorSettings.setIndentGuidesShown(false);
        editorSettings.setLineNumbersShown(false);
        editorSettings.setAdditionalColumnsCount(0);
        editorSettings.setAdditionalLinesCount(1);
        editorSettings.setUseSoftWraps(true);
        editorSettings.setUseTabCharacter(false);
        editorSettings.setCaretInsideTabs(false);
        editorSettings.setVirtualSpace(false);

    }

    private static void attachHighlighter(final EditorEx editor) {
        EditorColorsScheme scheme = editor.getColorsScheme();
        scheme.setColor(EditorColors.CARET_ROW_COLOR, null);
        editor.setHighlighter(createHighlighter(scheme));
    }

    private static EditorHighlighter createHighlighter(EditorColorsScheme settings) {
        Language language = Language.findLanguageByID("JSON");
        if (language == null) {
            language = Language.ANY;
        }
        return new LexerEditorHighlighter(PlainTextSyntaxHighlighterFactory.getSyntaxHighlighter(language, null, null), settings);
    }

    public MongoQueryOptions getQueryOptions() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();

        for (OperatorPanel operatorPanel : operatorPanels) {
            mongoQueryOptions.addQuery(operatorPanel.getOperator(), operatorPanel.getQuery());
        }

        return mongoQueryOptions;
    }

    @Override
    public void dispose() {
        myUpdateAlarm.cancelAllRequests();
        for (OperatorPanel operatorPanel : operatorPanels) {
            operatorPanel.dispose();
        }
    }

    private static class OperatorPanel extends JPanel implements Disposable {

        private static final Icon CLOSE_ICON = GuiUtils.loadIcon("close.png");

        private final Editor editor;
        private final MongoAggregateOperator operator;
        private final JLabel closeLabel;

        private OperatorPanel(Editor editor, MongoAggregateOperator operator) {
            this.editor = editor;
            this.operator = operator;
            setLayout(new BorderLayout());
            NonOpaquePanel headPanel = new NonOpaquePanel();
            JLabel operatorLabel = new JLabel(operator.getLabel());
            headPanel.add(operatorLabel, BorderLayout.WEST);
            closeLabel = new JLabel();
            closeLabel.setIcon(CLOSE_ICON);
            closeLabel.setToolTipText("Close operation");
            headPanel.add(closeLabel, BorderLayout.EAST);

            add(headPanel, BorderLayout.NORTH);
            add(editor.getComponent(), BorderLayout.CENTER);
        }

        public JLabel getCloseLabel() {
            return closeLabel;
        }

        public String getQuery() {
            return StringUtils.trim(editor.getDocument().getText());
        }

        public MongoAggregateOperator getOperator() {
            return operator;
        }

        @Override
        public void dispose() {
            EditorFactory.getInstance().releaseEditor(editor);
        }
    }

    private static class RemoveOperatorPanelAction extends MouseAdapter {

        private final QueryPanel queryPanel;
        private final OperatorPanel operatorPanel;

        private RemoveOperatorPanelAction(QueryPanel queryPanel, OperatorPanel operatorPanel) {
            this.queryPanel = queryPanel;
            this.operatorPanel = operatorPanel;
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            queryPanel.removeOperatorPanel(operatorPanel);
        }
    }

    private static class AddOperatorPanelAction extends MouseAdapter {

        private final JLabel addLabel;
        private final QueryPanel queryPanel;

        private AddOperatorPanelAction(JLabel addLabel, QueryPanel queryPanel) {
            this.addLabel = addLabel;
            this.queryPanel = queryPanel;
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            final JList list = new JBList(MongoAggregateOperator.values());
            new PopupChooserBuilder(list).
                    setMovable(false).
                    setItemChoosenCallback(new Runnable() {
                        public void run() {
                            MongoAggregateOperator selectedOperator = (MongoAggregateOperator) list.getSelectedValue();
                            if (selectedOperator == null) return;
                            queryPanel.addOperatorPanel(selectedOperator);
                        }
                    })
                    .createPopup()
                    .showInCenterOf(addLabel);


        }
    }

    private void removeOperatorPanel(OperatorPanel operatorPanel) {
        operatorPanels.remove(operatorPanel);
        editorPanel.invalidate();
        editorPanel.remove(operatorPanel);
        editorPanel.validate();
        editorPanel.updateUI();

        operatorPanel.dispose();
    }
}
