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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.view.action.result.OperatorCompletionAction;

import javax.swing.*;
import java.awt.*;

public class QueryPanel extends JPanel implements Disposable {

    private static final Font COURIER_FONT = new Font("Courier", Font.PLAIN, UIUtil.getLabelFont().getSize());

    private static final String FILTER_PANEL = "FilterPanel";
    private static final String AGGREGATION_PANEL = "AggregationPanel";

    private final Alarm myUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

    private final Project project;

    private JPanel mainPanel;
    private final CardLayout queryCardLayout;

    private JPanel queryContainerPanel;

    private final OperatorPanel filterPanel;
    private final OperatorPanel aggregationPanel;

    public QueryPanel(Project project) {
        this.project = project;

        setLayout(new BorderLayout());
        add(mainPanel);

        queryCardLayout = new CardLayout();
        queryContainerPanel.setLayout(queryCardLayout);

        filterPanel = createFilterPanel();
        queryContainerPanel.add(filterPanel, FILTER_PANEL);

        aggregationPanel = createAggregationPanel();
        queryContainerPanel.add(aggregationPanel, AGGREGATION_PANEL);

        toggleToFind();

        Disposer.register(project, this);
    }

    private OperatorPanel createAggregationPanel() {
        return new AggregatorPanel();
    }

    private OperatorPanel createFilterPanel() {
        return new FilterPanel();
    }

    public void requestFocusOnEditor() {// Code from requestFocus of EditorImpl
        final IdeFocusManager focusManager = IdeFocusManager.getInstance(this.project);
        JComponent editorContentComponent = getCurrentOperatorPanel().getRequestFocusComponent();
        if (focusManager.getFocusOwner() != editorContentComponent) {
            focusManager.requestFocus(editorContentComponent, true);
        }
    }

    private OperatorPanel getCurrentOperatorPanel() {
        return filterPanel.isVisible() ? filterPanel : aggregationPanel;
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

    private void attachHighlighter(final EditorEx editor) {
        EditorColorsScheme scheme = editor.getColorsScheme();
        scheme.setColor(EditorColors.CARET_ROW_COLOR, null);
        editor.setHighlighter(createHighlighter(scheme));
    }

    private EditorHighlighter createHighlighter(EditorColorsScheme settings) {
        Language language = Language.findLanguageByID("JSON");
        if (language == null) {
            language = Language.ANY;
        }
        return new LexerEditorHighlighter(PlainTextSyntaxHighlighterFactory.getSyntaxHighlighter(language, null, null), settings);
    }

    public MongoQueryOptions getQueryOptions(String rowLimit) {
        return getCurrentOperatorPanel().buildQueryOptions(rowLimit);
    }

    @Override
    public void dispose() {
        myUpdateAlarm.cancelAllRequests();
        filterPanel.dispose();
        aggregationPanel.dispose();
    }

    public void toggleToAggregation() {
        queryCardLayout.show(queryContainerPanel, AGGREGATION_PANEL);
    }

    public void toggleToFind() {
        queryCardLayout.show(queryContainerPanel, FILTER_PANEL);
    }

    public void validateQuery() {
        getCurrentOperatorPanel().validateQuery();
    }

    private class AggregatorPanel extends OperatorPanel {

        private final Editor editor;
        private final OperatorCompletionAction operatorCompletionAction;

        private AggregatorPanel() {
            this.editor = createEditor();

            setLayout(new BorderLayout());
            NonOpaquePanel headPanel = new NonOpaquePanel();
            JLabel operatorLabel = new JLabel("Aggregation");
            headPanel.add(operatorLabel, BorderLayout.WEST);
            add(headPanel, BorderLayout.NORTH);
            add(this.editor.getComponent(), BorderLayout.CENTER);

            this.operatorCompletionAction = new OperatorCompletionAction(project, editor);


            myUpdateAlarm.setActivationComponent(this.editor.getComponent());
        }

        @Override
        public void validateQuery() {
            try {
                String query = getQuery();
                if (StringUtils.isEmpty(query)) {
                    return;
                }
                JSON.parse(query);
            } catch (JSONParseException | NumberFormatException ex) {
                notifyOnErrorForOperator(editor.getComponent(), ex);
            }
        }

        private String getQuery() {
            return String.format("[%s]", StringUtils.trim(this.editor.getDocument().getText()));
        }

        @Override
        public MongoQueryOptions buildQueryOptions(String rowLimit) {
            MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
            try {
                mongoQueryOptions.setOperations(getQuery());
            } catch (JSONParseException ex) {
                notifyOnErrorForOperator(editor.getComponent(), ex);
            }

            if (StringUtils.isNotBlank(rowLimit)) {
                mongoQueryOptions.setResultLimit(Integer.parseInt(rowLimit));
            }

            return mongoQueryOptions;
        }

        @Override
        public JComponent getRequestFocusComponent() {
            return this.editor.getContentComponent();
        }

        @Override
        public void dispose() {
            operatorCompletionAction.dispose();
            EditorFactory.getInstance().releaseEditor(this.editor);
        }
    }

    private class FilterPanel extends OperatorPanel {

        private final Editor selectEditor;
        private final OperatorCompletionAction operatorCompletionAction;
        private final Editor projectionEditor;
        private final Editor sortEditor;

        private FilterPanel() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.selectEditor = createEditor();
            this.operatorCompletionAction = new OperatorCompletionAction(project, selectEditor);
            add(createSubOperatorPanel("Filter", this.selectEditor));

            this.projectionEditor = createEditor();
            add(createSubOperatorPanel("Projection", this.projectionEditor));

            this.sortEditor = createEditor();
            add(createSubOperatorPanel("Sort", this.sortEditor));
        }

        @Override
        public JComponent getRequestFocusComponent() {
            return this.selectEditor.getContentComponent();
        }

        @Override
        public void validateQuery() {
            validateEditorQuery(selectEditor);
            validateEditorQuery(projectionEditor);
            validateEditorQuery(sortEditor);
        }

        @Override
        public MongoQueryOptions buildQueryOptions(String rowLimit) {
            MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
            try {
                mongoQueryOptions.setFilter(getQueryFrom(selectEditor));
                mongoQueryOptions.setProjection(getQueryFrom(projectionEditor));
                mongoQueryOptions.setSort(getQueryFrom(sortEditor));
            } catch (JSONParseException ex) {
                notifyOnErrorForOperator(selectEditor.getComponent(), ex);
            }

            if (StringUtils.isNotBlank(rowLimit)) {
                mongoQueryOptions.setResultLimit(Integer.parseInt(rowLimit));
            } else {
                mongoQueryOptions.setResultLimit(MongoQueryOptions.NO_LIMIT);
            }

            return mongoQueryOptions;
        }

        @Override
        public void dispose() {
            operatorCompletionAction.dispose();
            EditorFactory.getInstance().releaseEditor(this.selectEditor);
            EditorFactory.getInstance().releaseEditor(this.projectionEditor);
            EditorFactory.getInstance().releaseEditor(this.sortEditor);
        }

        private void validateEditorQuery(Editor editor) {
            try {
                String query = getQueryFrom(editor);
                if (StringUtils.isEmpty(query)) {
                    return;
                }
                JSON.parse(query);
            } catch (JSONParseException | NumberFormatException ex) {
                notifyOnErrorForOperator(editor.getComponent(), ex);
            }
        }

        private String getQueryFrom(Editor editor) {
            return StringUtils.trim(editor.getDocument().getText());
        }

        private JPanel createSubOperatorPanel(String title, Editor subOperatorEditor) {
            JPanel selectPanel = new JPanel();
            selectPanel.setLayout(new BorderLayout());
            NonOpaquePanel headPanel = new NonOpaquePanel();
            JLabel operatorLabel = new JLabel(title);
            headPanel.add(operatorLabel, BorderLayout.WEST);
            selectPanel.add(headPanel, BorderLayout.NORTH);
            selectPanel.add(subOperatorEditor.getComponent(), BorderLayout.CENTER);

            myUpdateAlarm.setActivationComponent(subOperatorEditor.getComponent());

            return selectPanel;
        }
    }

    private abstract class OperatorPanel extends JPanel implements Disposable {

        protected abstract JComponent getRequestFocusComponent();

        protected abstract void validateQuery();

        protected abstract MongoQueryOptions buildQueryOptions(String rowLimit);

        void notifyOnErrorForOperator(final JComponent component, Exception ex) {
            String message;
            if (ex instanceof JSONParseException) {
                message = StringUtils.removeStart(ex.getMessage(), "\n");
            } else {
                message = String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage());
            }

            final NonOpaquePanel nonOpaquePanel = new NonOpaquePanel();
            JTextPane textPane = Messages.configureMessagePaneUi(new JTextPane(), message);
            textPane.setFont(COURIER_FONT);
            textPane.setBackground(MessageType.ERROR.getPopupBackground());
            nonOpaquePanel.add(textPane, BorderLayout.CENTER);
            nonOpaquePanel.add(new JLabel(MessageType.ERROR.getDefaultIcon()), BorderLayout.WEST);

            UIUtil.invokeLaterIfNeeded(() ->
                    JBPopupFactory.getInstance().createBalloonBuilder(nonOpaquePanel)
                            .setFillColor(MessageType.ERROR.getPopupBackground())
                            .createBalloon()
                            .show(new RelativePoint(component, new Point(0, 0)), Balloon.Position.above)
            );
        }

        Editor createEditor() {
            EditorFactory editorFactory = EditorFactory.getInstance();
            Document editorDocument = editorFactory.createDocument("");
            Editor editor = editorFactory.createEditor(editorDocument, project);
            fillEditorSettings(editor.getSettings());
            EditorEx editorEx = (EditorEx) editor;
            attachHighlighter(editorEx);

            return editor;
        }

    }
}
