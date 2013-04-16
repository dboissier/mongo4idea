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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.mongodb.util.JSONParseException;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoServer;

import javax.swing.*;
import java.awt.*;

public class MongoRunnerPanel extends JPanel implements Disposable {

    private JPanel rootPanel;
    private Splitter splitter;
    private JPanel errorPanel;
    private final MongoResultPanel resultPanel;
    private final QueryPanel queryPanel;

    private final MongoManager mongoManager;
    private final ServerConfiguration configuration;
    private final MongoCollection mongoCollection;

    public MongoRunnerPanel(Project project, MongoManager mongoManager, ServerConfiguration configuration, MongoCollection mongoCollection) {
        this.mongoManager = mongoManager;
        this.mongoCollection = mongoCollection;
        this.configuration = configuration;

        errorPanel.setLayout(new BorderLayout());

        queryPanel = createQueryPanel(project, configuration.getServerVersion());
        queryPanel.setCallback(new ErrorQueryCallback());

        splitter.setFirstComponent(queryPanel);

        resultPanel = createResultPanel(project);
        splitter.setSecondComponent(resultPanel);

        splitter.setProportion(0.30f);

        setLayout(new BorderLayout());
        add(rootPanel);
    }

    private MongoResultPanel createResultPanel(Project project) {
        return new MongoResultPanel(project);
    }

    QueryPanel createQueryPanel(Project project, String serverVersion) {
        QueryPanel aQueryPanel = new QueryPanel(project);
        if (MongoServer.isCompliantWithPipelineOperations(serverVersion)) {
            aQueryPanel.withAggregation();
        } else {
            aQueryPanel.withSimpleFilter();
        }
        return aQueryPanel;
    }

    public void installActions(MongoWindowManager.CloseAction closeAction) {
        queryPanel.installActions(this, closeAction);
        resultPanel.installActions();
    }


    public MongoCollection getMongoCollection() {
        return mongoCollection;
    }


    public void showResults() {
        executeQuery();
    }

    public void executeQuery() {
        try {
            errorPanel.setVisible(false);

            MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(configuration, mongoCollection, queryPanel.getQueryOptions());
            resultPanel.updateResultTableTree(mongoCollectionResult);
        } catch (Exception ex) {
            errorPanel.invalidate();
            errorPanel.removeAll();
            errorPanel.add(new ErrorPanel(ex), BorderLayout.CENTER);
            errorPanel.validate();
            errorPanel.setVisible(true);
        }
    }

    @Override
    public void dispose() {
        queryPanel.dispose();
        resultPanel.dispose();
    }

    private static class ErrorQueryCallback implements QueryPanel.QueryCallback {

        @Override
        public void notifyOnErrorForOperator(JComponent editorComponent, Exception ex) {

            String message;
            if (ex instanceof JSONParseException) {
                message = StringUtils.removeStart(ex.getMessage(), "\n");
            } else {
                message = String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage());
            }
            NonOpaquePanel nonOpaquePanel = new NonOpaquePanel();
            JTextPane textPane = Messages.configureMessagePaneUi(new JTextPane(), message);
            textPane.setBackground(MessageType.ERROR.getPopupBackground());
            nonOpaquePanel.add(textPane, BorderLayout.CENTER);
            nonOpaquePanel.add(new JLabel(MessageType.ERROR.getDefaultIcon()), BorderLayout.WEST);

            Balloon balloon = JBPopupFactory.getInstance().createBalloonBuilder(nonOpaquePanel)
                    .setFillColor(MessageType.ERROR.getPopupBackground())
                    .createBalloon();
            balloon.show(new RelativePoint(editorComponent, new Point(0, 0)), Balloon.Position.above);
        }
    }

}
