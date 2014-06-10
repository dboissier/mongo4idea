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
import com.intellij.openapi.ui.Splitter;
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;

import javax.swing.*;
import java.awt.*;

public class MongoPanel extends JPanel implements Disposable {

    private JPanel rootPanel;
    private Splitter splitter;
    private final MongoResultPanel resultPanel;
    private final QueryPanel queryPanel;

    private final MongoManager mongoManager;
    private final ServerConfiguration configuration;
    private final MongoCollection mongoCollection;

    public MongoPanel(Project project, final MongoManager mongoManager, final ServerConfiguration configuration, final MongoCollection mongoCollection) {
        this.mongoManager = mongoManager;
        this.mongoCollection = mongoCollection;
        this.configuration = configuration;

        queryPanel = new QueryPanel(project);

        splitter.setOrientation(true);
        splitter.setProportion(0.2f);

        resultPanel = createResultPanel(project, new MongoDocumentOperations() {

            public DBObject getMongoDocument(Object _id) {
                return mongoManager.findMongoDocument(configuration, mongoCollection, _id);
            }

            public void updateMongoDocument(DBObject mongoDocument) {
                mongoManager.update(configuration, mongoCollection, mongoDocument);
            }

            public void deleteMongoDocument(Object objectId) {
                mongoManager.delete(configuration, mongoCollection, objectId);
            }
        });
        splitter.setSecondComponent(resultPanel);

        setLayout(new BorderLayout());
        add(rootPanel);
    }

    private MongoResultPanel createResultPanel(Project project, MongoDocumentOperations mongoDocumentOperations) {
        return new MongoResultPanel(project, mongoDocumentOperations);
    }

    public void installActions() {
        queryPanel.installActions(this);
        resultPanel.installActions(this);
    }


    public MongoCollection getMongoCollection() {
        return mongoCollection;
    }


    public void showResults() {
        executeQuery();
    }

    public void executeQuery() {
        try {
            queryPanel.getErrorPanel().setVisible(false);
            validateQuery();
            MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(configuration, mongoCollection, queryPanel.getQueryOptions());
            resultPanel.updateResultTableTree(mongoCollectionResult);
        } catch (Exception ex) {
            queryPanel.getErrorPanel().invalidate();
            queryPanel.getErrorPanel().removeAll();
            queryPanel.getErrorPanel().add(new ErrorPanel(ex), BorderLayout.CENTER);
            queryPanel.getErrorPanel().validate();
            queryPanel.getErrorPanel().setVisible(true);
        }
    }

    private void validateQuery() {
        queryPanel.validateQuery();

    }

    @Override
    public void dispose() {
        resultPanel.dispose();
    }

    public MongoResultPanel getResultPanel() {
        return resultPanel;
    }

    public void openFindEditor() {
        splitter.setFirstComponent(queryPanel);
    }

    public void closeFindEditor() {
        splitter.setFirstComponent(null);
    }

    interface MongoDocumentOperations {
        DBObject getMongoDocument(Object _id);

        void deleteMongoDocument(Object mongoDocument);

        void updateMongoDocument(DBObject mongoDocument);
    }
}
