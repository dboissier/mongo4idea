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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class MongoRunnerPanel extends JPanel {

    private static final Icon FAIL_ICON = GuiUtils.loadIcon("fail.png");

    private JPanel rootPanel;
    private Splitter splitter;
    private JLabel errorLabel;
    private final MongoResultPanel resultPanel;
    private QueryPanel queryPanel;

    private final MongoConfiguration configuration;
    private final MongoManager mongoManager;
    private MongoCollection currentMongoCollection;

    private int resultLimit = 200;

    public MongoRunnerPanel(Project project, MongoConfiguration configuration, MongoManager mongoManager) {
        this.configuration = configuration;
        this.mongoManager = mongoManager;

        queryPanel = createQueryPanel(project, configuration.getServerVersion());
        splitter.setFirstComponent(queryPanel);

        resultPanel = createResultPanel();
        splitter.setSecondComponent(resultPanel);

        splitter.setProportion(0.30f);

        setLayout(new BorderLayout());
        add(rootPanel);

        resultPanel.shouldShowTreeResult(false);
    }

    private MongoResultPanel createResultPanel() {
        return new MongoResultPanel();
    }

    protected QueryPanel createQueryPanel(Project project, String serverVersion) {
        QueryPanel aQueryPanel = new QueryPanel(project);
        if (MongoServer.isCompliantWithPipelineOperations(serverVersion)) {
            aQueryPanel.withAggregation();
        } else {
            aQueryPanel.withSimpleFilter();
        }
        return aQueryPanel;
    }

    public void installActions() {
        queryPanel.installActions(this);
        resultPanel.installActions();

    }


    public MongoCollection getCurrentMongoCollection() {
        return currentMongoCollection;
    }


    public void showResults(MongoCollection mongoCollection) {
        currentMongoCollection = mongoCollection;
        resultPanel.updateResultTree(mongoManager.loadCollectionValues(configuration, currentMongoCollection, queryPanel.getQueryOptions()));
    }

    public void executeQuery() {
        try {
            errorLabel.setVisible(false);

            MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(configuration, currentMongoCollection, queryPanel.getQueryOptions());
            resultPanel.updateResultTree(mongoCollectionResult);
        } catch (Exception ex) {
            errorLabel.setIcon(FAIL_ICON);
            errorLabel.setText(String.format("[%s]: %s", ex.getClass().getSimpleName(), ex.getMessage()));
            errorLabel.setVisible(true);
        }
    }
}
