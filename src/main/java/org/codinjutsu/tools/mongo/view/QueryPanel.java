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
import com.intellij.util.Alarm;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class QueryPanel extends JPanel implements Disposable {
    private static final Icon FAIL_ICON = GuiUtils.loadIcon("fail.png");

    private JPanel mainPanel;
    private JLabel feedbackLabel;
    private JPanel editorPanel;

    private Editor myEditor;
    private final Alarm myUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);


    public static QueryPanel queryPanel() {
        return new QueryPanel(true);
    }

    public static QueryPanel withoutEditor() {
        return new QueryPanel(false);
    }

    private QueryPanel(boolean withEditor) {
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        editorPanel.setLayout(new BorderLayout());

        if (withEditor) {
            myEditor = createEditor();
            if (myEditor != null) {
                myUpdateAlarm.setActivationComponent(myEditor.getComponent());
            }
            editorPanel.add(myEditor.getComponent(), BorderLayout.CENTER);
        }

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
        editorSettings.setFoldingOutlineShown(true);
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

    public String getFilterText() {
        return StringUtils.trim(myEditor.getDocument().getText());
    }

    public MongoQueryOptions getQueryOptions() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();


        String filterText = getFilterText();
        if (!StringUtils.isBlank(filterText)) {
            try {
                DBObject filter = (DBObject) JSON.parse(filterText);
                mongoQueryOptions.setFilter(filter);
                if (feedbackLabel.getIcon() != null) {
                    feedbackLabel.setIcon(null);
                }
            } catch (Exception ex) {
                setErrorMsg(ex);
            }
        }

        return mongoQueryOptions;
    }

    public void setErrorMsg(Exception ex) {
        feedbackLabel.setIcon(FAIL_ICON);
        feedbackLabel.setText(String.format("[%s] %s", ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @Override
    public void dispose() {
        myUpdateAlarm.cancelAllRequests();
        if (myEditor != null) {
            EditorFactory.getInstance().releaseEditor(myEditor);
        }
    }
}
