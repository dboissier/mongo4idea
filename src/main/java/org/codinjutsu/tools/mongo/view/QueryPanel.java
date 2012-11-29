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
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
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
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.Alarm;
import com.mongodb.util.JSONParseException;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoAggregateOperator;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.AddOperatorPanelAction;
import org.codinjutsu.tools.mongo.view.action.CopyQueryAction;
import org.codinjutsu.tools.mongo.view.action.ExecuteQuery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

public class QueryPanel extends JPanel implements Disposable {

    private List<OperatorPanel> operatorPanels = new LinkedList<OperatorPanel>();

    private boolean withAggregation = false;

    private final Alarm myUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

    private FilterPanel filterPanel;
    private JPanel toolBarPanel;
    private JPanel mainPanel;
    private JPanel queryContainerPanel;

    public QueryPanel() {
        toolBarPanel.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(mainPanel);
    }

    public void withAggregation() {
        withAggregation = true;
        queryContainerPanel.setLayout(new BoxLayout(queryContainerPanel, BoxLayout.Y_AXIS));
        addOperatorPanel(MongoAggregateOperator.MATCH);
    }

    public void withSimpleFilter() {
        withAggregation = false;
        Editor editor = createEditor();
        filterPanel = new FilterPanel(editor);
        queryContainerPanel.setLayout(new BorderLayout());
        queryContainerPanel.add(filterPanel, BorderLayout.CENTER);
        myUpdateAlarm.setActivationComponent(editor.getComponent());
    }

    public void addOperatorPanel(MongoAggregateOperator selectedOperator) {
        OperatorPanel matchOperatorPanel = new OperatorPanel(createEditor(), selectedOperator);
        matchOperatorPanel.getCloseLabel().addMouseListener(new RemoveOperatorPanelAction(this, matchOperatorPanel));
        operatorPanels.add(matchOperatorPanel);
        myUpdateAlarm.setActivationComponent(matchOperatorPanel);

        invalidate();
        queryContainerPanel.add(matchOperatorPanel);
        validate();
    }

    private void removeOperatorPanel(OperatorPanel operatorPanel) {
        operatorPanels.remove(operatorPanel);
        invalidate();
        remove(operatorPanel);
        validate();
        updateUI();

        operatorPanel.dispose();
    }


    private Editor createEditor() {
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
        editorSettings.setAllowSingleLogicalLineFolding(true);
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

        if (withAggregation) {
            for (OperatorPanel operatorPanel : operatorPanels) {
                mongoQueryOptions.addQuery(operatorPanel.getOperator(), operatorPanel.getQuery());
            }
        } else {
            mongoQueryOptions.setFilter(filterPanel.getQuery());
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

    public boolean isAggregate() {
        return withAggregation;
    }

    public void installActions(MongoRunnerPanel mongoRunnerPanel) {
        DefaultActionGroup actionQueryGroup = new DefaultActionGroup("MongoQueryGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionQueryGroup.add(new ExecuteQuery(mongoRunnerPanel));
            actionQueryGroup.addSeparator();
            actionQueryGroup.add(new AddOperatorPanelAction(this));
            actionQueryGroup.add(new CopyQueryAction(this));
        }
        GuiUtils.installActionGroupInToolBar(actionQueryGroup, toolBarPanel, ActionManager.getInstance(), "MongoQueryGroupActions", false);
    }



    public String getQueryStringifiedValue() {
        if (getQueryOptions().isAggregate()) {
            return String.format("[ %s ]", StringUtils.join(getQueryOptions().getAllOperations(), ","));
        }
        return getQueryOptions().getFilter().toString();
    }

    public boolean isSomeQuerySet() {
        try {
            return getQueryOptions().isSomethingSet();
        } catch (JSONParseException e) {
            return false;
        }
    }

    private static class FilterPanel extends JPanel implements Disposable {
        private final Editor editor;

        private FilterPanel(Editor editor) {
            this.editor = editor;
            setLayout(new BorderLayout());
            add(editor.getComponent(), BorderLayout.CENTER);
        }

        public String getQuery() {
            return StringUtils.trim(editor.getDocument().getText());
        }

        @Override
        public void dispose() {
            EditorFactory.getInstance().releaseEditor(editor);
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
}
