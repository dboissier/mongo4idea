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

import com.intellij.openapi.ui.Splitter;
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.model.JsonTreeModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MongoRunnerPanel extends JPanel {
    public static final String MONGO_EXE_PATH = "/usr/bin/mongo";
    public static final String WORK_DIRECTORY = "/tmp";
    private JPanel rootPanel;
    private Splitter splitter;
    private JEditorPane scriptEditorPane = new JEditorPane();
    private final JsonTreeView jsonResultTree = new JsonTreeView();
    private final JPanel resultToolBarPanel = new JPanel();

    public MongoRunnerPanel() {
        splitter.setFirstComponent(createMongoEditorPanel());
        splitter.setSecondComponent(createMongoResultPanel());

        setLayout(new BorderLayout());
        add(rootPanel);

        jsonResultTree.setVisible(false);
    }

    private JPanel createMongoResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(jsonResultTree), BorderLayout.CENTER);
        panel.add(resultToolBarPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createMongoEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(scriptEditorPane), BorderLayout.CENTER);
        JPanel toolBarPanel = new JPanel();
        panel.add(toolBarPanel, BorderLayout.NORTH);
        return panel;
    }

    public void showResults(MongoCollectionResult mongoCollectionResult) {
        List<DBObject> mongoObjects = mongoCollectionResult.getMongoObjects();
        jsonResultTree.invalidate();
        jsonResultTree.setVisible(true);
        jsonResultTree.setModel(new JsonTreeModel(mongoObjects));
        jsonResultTree.validate();
    }
}
