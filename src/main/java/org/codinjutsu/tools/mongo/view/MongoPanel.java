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

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.action.*;

import javax.swing.*;
import java.awt.*;

public class MongoPanel extends JPanel implements Disposable {

    private JPanel rootPanel;
    private Splitter splitter;
    private JPanel toolBar;
    private JPanel errorPanel;
    private final MongoResultPanel resultPanel;
    private final QueryPanel queryPanel;

    private final MongoManager mongoManager;
    private final ServerConfiguration configuration;
    private final MongoCollection mongoCollection;

    public MongoPanel(Project project, final MongoManager mongoManager, final ServerConfiguration configuration, final MongoCollection mongoCollection) {
        this.mongoManager = mongoManager;
        this.mongoCollection = mongoCollection;
        this.configuration = configuration;

        errorPanel.setLayout(new BorderLayout());

        queryPanel = new QueryPanel(project);

        splitter.setOrientation(true);
        splitter.setProportion(0.1f);
        toolBar.setLayout(new BorderLayout());

        resultPanel = createResultPanel(project, new MongoDocumentOperations() {

            public DBObject getMongoDocument(Object _id) {
                return mongoManager.findMongoDocument(configuration, mongoCollection, _id);
            }

            public void updateMongoDocument(DBObject mongoDocument) {
                mongoManager.update(configuration, mongoCollection, mongoDocument);
                executeQuery();
            }

            public void deleteMongoDocument(Object objectId) {
                mongoManager.delete(configuration, mongoCollection, objectId);
                executeQuery();
            }
        });
        splitter.setSecondComponent(resultPanel);

        setLayout(new BorderLayout());
        add(rootPanel);

        installResultPanelActions();
        installQueryPanelActions();
    }

    private MongoResultPanel createResultPanel(Project project, MongoDocumentOperations mongoDocumentOperations) {
        return new MongoResultPanel(project, mongoDocumentOperations);
    }


    public void installResultPanelActions() {
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("MongoResultGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionResultGroup.add(new ExecuteQuery(this));
            actionResultGroup.add(new OpenFindAction(this));
            actionResultGroup.add(new CopyResultAction(resultPanel));
        }
        final TreeExpander treeExpander = new TreeExpander() {
            @Override
            public void expandAll() {
                resultPanel.expandAll();
            }

            @Override
            public boolean canExpand() {
                return true;
            }

            @Override
            public void collapseAll() {
                resultPanel.collapseAll();
            }

            @Override
            public boolean canCollapse() {
                return true;
            }
        };

        CommonActionsManager actionsManager = CommonActionsManager.getInstance();

        final AnAction expandAllAction = actionsManager.createExpandAllAction(treeExpander, resultPanel);
        final AnAction collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, resultPanel);

        Disposer.register(this, new Disposable() {
            @Override
            public void dispose() {
                collapseAllAction.unregisterCustomShortcutSet(resultPanel);
                expandAllAction.unregisterCustomShortcutSet(resultPanel);
            }
        });

        actionResultGroup.add(expandAllAction);
        actionResultGroup.add(collapseAllAction);

        GuiUtils.installActionGroupInToolBar(actionResultGroup, resultPanel.getToolbar(), ActionManager.getInstance(), "MongoGroupActions", false);
    }

    public void installQueryPanelActions() {
        JPanel queryPanelToolbar = queryPanel.getToolbar();

        DefaultActionGroup actionQueryGroup = new DefaultActionGroup("MongoResultGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionQueryGroup.add(new EnableAggregateAction(this));
            actionQueryGroup.add(new CloseFindEditorAction(this));
        }

        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("MongoQueryGroupActions", actionQueryGroup, true);
        actionToolBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent actionToolBarComponent = actionToolBar.getComponent();
        actionToolBarComponent.setBorder(null);
        actionToolBarComponent.setOpaque(false);

        queryPanelToolbar.add(actionToolBarComponent, BorderLayout.CENTER);
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
            validateQuery();
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
        GuiUtils.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                focusOnEditor();
            }
        });
    }

    public void closeFindEditor() {
        splitter.setFirstComponent(null);
    }

    public void focusOnEditor() {
        queryPanel.requestFocusOnEditor();
    }

    public boolean isFindEditorOpened() {
        return splitter.getFirstComponent() == queryPanel;
    }

    public QueryPanel getQueryPanel() {
        return queryPanel;
    }

    interface MongoDocumentOperations {
        DBObject getMongoDocument(Object _id);

        void deleteMongoDocument(Object mongoDocument);

        void updateMongoDocument(DBObject mongoDocument);
    }
}
