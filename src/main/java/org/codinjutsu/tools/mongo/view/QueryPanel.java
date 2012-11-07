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
    private JPanel editorPanel;

    private Editor matchEditor;
    private Editor projectEditor;
    private Editor groupEditor;
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
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));

        if (withEditor) {
            matchEditor = createEditor();
            JPanel matchEditorComponent = (JPanel) matchEditor.getComponent();
            matchEditorComponent.setBorder(BorderFactory.createTitledBorder("$match"));
            editorPanel.add(matchEditorComponent);
            myUpdateAlarm.setActivationComponent(matchEditorComponent);

            projectEditor = createEditor();
            JPanel projectEditorComponent = (JPanel) projectEditor.getComponent();
            projectEditorComponent.setBorder(BorderFactory.createTitledBorder("$project"));
            editorPanel.add(projectEditorComponent);
            myUpdateAlarm.setActivationComponent(projectEditorComponent);

            groupEditor = createEditor();
            JPanel groupEditorComponent = (JPanel) groupEditor.getComponent();
            groupEditorComponent.setBorder(BorderFactory.createTitledBorder("$group"));
            editorPanel.add(groupEditorComponent);
            myUpdateAlarm.setActivationComponent(groupEditorComponent);

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

    public String getMatchText() {
        return StringUtils.trim(matchEditor.getDocument().getText());
    }

    public String getProjectText() {
        return StringUtils.trim(projectEditor.getDocument().getText());
    }

    public String getGroupText() {
        return StringUtils.trim(groupEditor.getDocument().getText());
    }

    public MongoQueryOptions getQueryOptions() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();

        String matchText = getMatchText();
        if (!StringUtils.isBlank(matchText)) {
            DBObject filter = (DBObject) JSON.parse(matchText);
            mongoQueryOptions.setMatch(filter);
        }

        String projectText = getProjectText();
        if (!StringUtils.isBlank(projectText)) {
            DBObject filter = (DBObject) JSON.parse(projectText);
            mongoQueryOptions.setProject(filter);
        }

        String groupText = getGroupText();
        if (!StringUtils.isBlank(groupText)) {
            DBObject filter = (DBObject) JSON.parse(groupText);
            mongoQueryOptions.setGroup(filter);
        }

        return mongoQueryOptions;
    }

    @Override
    public void dispose() {
        myUpdateAlarm.cancelAllRequests();
        if (matchEditor != null) {
            EditorFactory.getInstance().releaseEditor(matchEditor);
        }
        if (projectEditor != null) {
            EditorFactory.getInstance().releaseEditor(projectEditor);
        }
        if (groupEditor != null) {
            EditorFactory.getInstance().releaseEditor(groupEditor);
        }
    }
}
