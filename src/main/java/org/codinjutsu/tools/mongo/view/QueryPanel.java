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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import com.mongodb.util.JSONParseException;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.*;

import javax.swing.*;
import java.awt.*;

public class QueryPanel extends JPanel implements Disposable {

    private static final Font COURIER_FONT = new Font("Courier", Font.PLAIN, UIUtil.getLabelFont().getSize());


    private final Alarm myUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

    private FilterPanel filterPanel;
    private JPanel toolBarPanel;
    private JPanel mainPanel;
    private JPanel queryContainerPanel;
    private JPanel errorPanel;
    private final Project project;
    private boolean agregationEnabled = false;
    private OperatorCompletionAction operatorCompletionAction;

    public QueryPanel(Project project) {
        this.project = project;

        errorPanel.setLayout(new BorderLayout());
        toolBarPanel.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(mainPanel);

        Editor editor = createEditor();
        filterPanel = new FilterPanel(editor);

        queryContainerPanel.setLayout(new BorderLayout());
        queryContainerPanel.add(filterPanel);


        myUpdateAlarm.setActivationComponent(editor.getComponent());

        Disposer.register(project, this);
    }


    private Editor createEditor() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document editorDocument = editorFactory.createDocument("");
        Editor editor = editorFactory.createEditor(editorDocument, project);
        fillEditorSettings(editor.getSettings());
        EditorEx editorEx = (EditorEx) editor;
        attachHighlighter(editorEx);
        operatorCompletionAction = new OperatorCompletionAction(editor);
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

    public MongoQueryOptions getQueryOptions() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();

        if (isAgregationEnabled()) {

            try {
                mongoQueryOptions.setQueries(filterPanel.getQueries());
            } catch (JSONParseException ex) {
                filterPanel.notifyOnErrorForOperator(ex);
                throw ex;
            } catch (NumberFormatException ex) {
                filterPanel.notifyOnErrorForOperator(ex);
                throw ex;
            }
        } else {
            try {
                mongoQueryOptions.setFilter(filterPanel.getQuery());
            } catch (JSONParseException ex) {
                filterPanel.notifyOnErrorForOperator(ex);
            }
        }


        return mongoQueryOptions;
    }

    private boolean isAgregationEnabled() {
        return agregationEnabled;
    }

    @Override
    public void dispose() {
        myUpdateAlarm.cancelAllRequests();

        if (operatorCompletionAction != null) {
            operatorCompletionAction.dispose();
        }

        if (filterPanel != null) {
            filterPanel.dispose();
        }
    }

    public void installActions(MongoPanel mongoPanel) {
        DefaultActionGroup actionQueryGroup = new DefaultActionGroup("MongoQueryGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionQueryGroup.add(new ExecuteQuery(mongoPanel));
            actionQueryGroup.add(new EnableAggregateAction(this));
            actionQueryGroup.add(new CopyQueryAction(this));
            actionQueryGroup.add(new CloseFindEditorAction(mongoPanel));
        }
        GuiUtils.installActionGroupInToolBar(actionQueryGroup, toolBarPanel, ActionManager.getInstance(), "MongoQueryGroupActions", true);
    }


    public String getQueryStringifiedValue() {
        return getQueryOptions().getFilter().toString();
    }


    public void toggleToAggregation() {
        this.agregationEnabled = true;
    }

    public void toggleToFind() {
        this.agregationEnabled = false;
    }

    public JPanel getErrorPanel() {
        return errorPanel;
    }

    public void validateQuery() {
        if (isAgregationEnabled()) {
            try {
                filterPanel.getQueries();
            } catch (JSONParseException ex) {
                filterPanel.notifyOnErrorForOperator(ex);
            } catch (NumberFormatException ex) {
                filterPanel.notifyOnErrorForOperator(ex);
            }
        } else {
            try {
                filterPanel.getQuery();
            } catch (JSONParseException ex) {
                filterPanel.notifyOnErrorForOperator(ex);
            } catch (NumberFormatException ex) {
                filterPanel.notifyOnErrorForOperator(ex);
            }
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

        public String getQueries() {
            return String.format("[%s]", getQuery());
        }

        public void notifyOnErrorForOperator(Exception ex) {
            String message;
            if (ex instanceof JSONParseException) {
                message = StringUtils.removeStart(ex.getMessage(), "\n");
            } else {
                message = String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage());
            }
            NonOpaquePanel nonOpaquePanel = new NonOpaquePanel();
            JTextPane textPane = Messages.configureMessagePaneUi(new JTextPane(), message);
            textPane.setFont(COURIER_FONT);
            textPane.setBackground(MessageType.ERROR.getPopupBackground());
            nonOpaquePanel.add(textPane, BorderLayout.CENTER);
            nonOpaquePanel.add(new JLabel(MessageType.ERROR.getDefaultIcon()), BorderLayout.WEST);

            JBPopupFactory.getInstance().createBalloonBuilder(nonOpaquePanel)
                    .setFillColor(MessageType.ERROR.getPopupBackground())
                    .createBalloon()
                    .show(new RelativePoint(this.editor.getComponent(), new Point(0, 0)), Balloon.Position.above);
        }
    }
}
