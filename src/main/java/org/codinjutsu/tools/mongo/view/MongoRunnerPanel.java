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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.mongodb.util.JSONParseException;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoServer;

import javax.swing.*;
import java.awt.*;

public class MongoRunnerPanel extends JPanel {

    private JPanel rootPanel;
    private Splitter splitter;
    private JPanel errorPanel;
    private final MongoResultPanel resultPanel;
    private QueryPanel queryPanel;

    private final MongoConfiguration configuration;
    private final MongoManager mongoManager;
    private MongoCollection currentMongoCollection;

    public MongoRunnerPanel(Project project, MongoConfiguration configuration, MongoManager mongoManager) {
        this.configuration = configuration;
        this.mongoManager = mongoManager;

        errorPanel.setLayout(new BorderLayout());

        queryPanel = createQueryPanel(project, configuration.getServerVersion());
        queryPanel.setCallback(new ErrorQueryCallback());

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
        executeQuery();
    }

    public void executeQuery() {
        try {
            errorPanel.setVisible(false);

            MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(configuration, currentMongoCollection, queryPanel.getQueryOptions());
            resultPanel.updateResultTree(mongoCollectionResult);
        } catch (Exception ex) {
            errorPanel.invalidate();
            errorPanel.removeAll();
            errorPanel.add(new ErrorPanel(ex), BorderLayout.CENTER);
            errorPanel.validate();
            errorPanel.setVisible(true);
        }
    }

    private static class ErrorQueryCallback implements QueryPanel.QueryCallback {

        @Override
        public void notifyOnErrorForOperator(JComponent editorComponent, JSONParseException ex) {
            BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder("<b>Bad JSON syntax:</b>" + ex.getMessage(), MessageType.ERROR, null);
            Balloon balloon = balloonBuilder.createBalloon();
            balloon.show(new RelativePoint(editorComponent, new Point(0, 0)), Balloon.Position.above);
        }
    }
}
