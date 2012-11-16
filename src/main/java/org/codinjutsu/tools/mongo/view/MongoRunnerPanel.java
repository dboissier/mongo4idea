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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Splitter;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoServer;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.AddOperatorPanelAction;
import org.codinjutsu.tools.mongo.view.action.CopyQueryAction;
import org.codinjutsu.tools.mongo.view.action.ExecuteQuery;

import javax.swing.*;
import java.awt.*;

public class MongoRunnerPanel extends JPanel {

    private static final Icon FAIL_ICON = GuiUtils.loadIcon("fail.png");

    private JPanel rootPanel;
    private JPanel toolBarPanel;
    private Splitter splitter;
    private JLabel errorLabel;
    private QueryPanel queryPanel;

    private final MongoConfiguration configuration;
    private final MongoManager mongoManager;
    private MongoCollection currentMongoCollection;
    private final MongoResultPanel resultPanel;

    public MongoRunnerPanel(MongoConfiguration configuration, MongoManager mongoManager) {
        this.configuration = configuration;
        this.mongoManager = mongoManager;

        queryPanel = createQueryPanel(configuration.getServerVersion());
        splitter.setFirstComponent(queryPanel);

        resultPanel = createResultPanel();
        splitter.setSecondComponent(resultPanel);

        splitter.setProportion(0.30f);


        toolBarPanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());
        add(rootPanel);

        resultPanel.shouldShowTreeResult(false);
    }

    private MongoResultPanel createResultPanel() {
        return new MongoResultPanel();
    }

    protected QueryPanel createQueryPanel(String serverVersion) {
        if (MongoServer.isCompliantWithPipelineOperations(serverVersion)) {
            return QueryPanel.withAggregation();
        }
        return QueryPanel.withSimpleFilter();
    }

    public void installActions() {

        DefaultActionGroup actionQueryGroup = new DefaultActionGroup("MongoQueryGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionQueryGroup.add(new ExecuteQuery(this));
            actionQueryGroup.addSeparator();
            actionQueryGroup.add(new AddOperatorPanelAction(queryPanel));
            actionQueryGroup.add(new CopyQueryAction(this));
        }
        GuiUtils.installActionGroupInToolBar(actionQueryGroup, toolBarPanel, ActionManager.getInstance(), "MongoQueryGroupActions", false);

        resultPanel.installActions();

    }


    public MongoCollection getCurrentMongoCollection() {
        return currentMongoCollection;
    }


    public void showResults(MongoCollection mongoCollection) {
        currentMongoCollection = mongoCollection;
        resultPanel.updateResultTree(mongoManager.loadCollectionValues(configuration, currentMongoCollection));
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

    public String getQueryStringifiedValue() {
        if (queryPanel.getQueryOptions().isAggregate()) {
            return String.format("[ %s ]", StringUtils.join(queryPanel.getQueryOptions().getAllOperations(), ","));
        }
        return queryPanel.getQueryOptions().getFilter().toString();
    }

    public boolean isSomeQuerySet() {
        return queryPanel.getQueryOptions().isSomethingSet();
    }
}
