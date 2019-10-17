/*
 * Copyright (c) 2018 David Boissier.
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
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.NumberDocument;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.UIUtil;
import com.mongodb.DBRef;
import org.bson.Document;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.logic.Notifier;
import org.codinjutsu.tools.mongo.model.*;
import org.codinjutsu.tools.mongo.view.action.pagination.PaginationAction;
import org.codinjutsu.tools.mongo.view.action.result.*;
import org.codinjutsu.tools.mongo.view.model.NbPerPage;
import org.codinjutsu.tools.mongo.view.model.Pagination;
import org.codinjutsu.tools.mongo.view.model.navigation.Navigation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MongoPanel extends JPanel implements Disposable {

    private final Project project;
    private final LoadingDecorator loadingDecorator;
    private JPanel rootPanel;
    private Splitter splitter;
    private JPanel toolBar;
    private JPanel errorPanel;
    private JPanel paginationPanel;

    private final JTextField rowLimitField = new JTextField();
    private final JBLabel rowCountLabel = new JBLabel();
    private final JBLabel pageNumberLabel = new JBLabel();

    private final MongoResultPanel resultPanel;
    private final QueryPanel queryPanel;

    private final MongoManager mongoManager;
    private final ServerConfiguration configuration;
    private final Navigation navigation;
    private MongoCollectionResult currentResults;

    private final Pagination pagination;

    public MongoPanel(Project project, final MongoManager mongoManager, final ServerConfiguration configuration, final Navigation navigation) {
        this.project = project;
        this.mongoManager = mongoManager;
        this.navigation = navigation;
        this.configuration = configuration;
        this.pagination = new Pagination();

        this.currentResults = new MongoCollectionResult(navigation.getCurrentWayPoint().getLabel());

        errorPanel.setLayout(new BorderLayout());

        queryPanel = new QueryPanel(project);
        queryPanel.setVisible(false);

        resultPanel = createResultPanel(project, Notifier.getInstance(project));

        loadingDecorator = new LoadingDecorator(resultPanel, this, 0);
        splitter.setOrientation(true);
        splitter.setProportion(0.2f);
        splitter.setSecondComponent(loadingDecorator.getComponent());

        setLayout(new BorderLayout());
        add(rootPanel);

        initToolBar();
        initPaginationPanel();

        pagination.addSetPageListener(() -> showResults(true));
        pagination.addSetPageListener(() -> {
                    pagination.setTotalDocuments(currentResults.getTotalDocumentNumber());
                    if (NbPerPage.ALL.equals(pagination.getNbPerPage())) {
                        pageNumberLabel.setVisible(false);
                    } else {
                        pageNumberLabel.setText(
                                String.format("Page %d/%d",
                                        pagination.getPageNumber(),
                                        pagination.getTotalPageNumber())
                        );
                        pageNumberLabel.setVisible(true);
                    }
                }
        );
    }

    private MongoResultPanel createResultPanel(Project project, Notifier notifier) {
        return new MongoResultPanel(project, new MongoDocumentOperations() {

            public Document getMongoDocument(Object _id) {
                return mongoManager.findMongoDocument(configuration, navigation.getCurrentWayPoint().getCollection(), _id);
            }

            public void updateMongoDocument(Document mongoDocument) {
                mongoManager.update(configuration, navigation.getCurrentWayPoint().getCollection(), mongoDocument);
                executeQuery();
            }

            @Override
            public Document getReferenceDocument(String collection, Object _id, String database) {
                return mongoManager.findMongoDocument(
                        configuration,
                        new MongoCollection(collection, database != null ? new MongoDatabase(database, new MongoServer(configuration)) : navigation.getCurrentWayPoint().getCollection().getParentDatabase()),
                        _id);
            }

            public void deleteMongoDocument(Object objectId) {
                mongoManager.delete(configuration, navigation.getCurrentWayPoint().getCollection(), objectId);
                executeQuery();
            }
        }, notifier);
    }

    private void initToolBar() {
        toolBar.setLayout(new BorderLayout());

        JPanel rowLimitPanel = createRowLimitPanel();
        toolBar.add(rowLimitPanel, BorderLayout.WEST);

        JComponent actionToolBarComponent = createResultActionsComponent();
        toolBar.add(actionToolBarComponent, BorderLayout.CENTER);

        JComponent viewToolbarComponent = createSelectViewActionsComponent();
        toolBar.add(viewToolbarComponent, BorderLayout.EAST);
    }

    private void initPaginationPanel() {
        paginationPanel.setLayout(new BorderLayout());

        JComponent actionToolbarComponent = createPaginationActionsComponent();
        paginationPanel.add(actionToolbarComponent, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.add(pageNumberLabel);
        panel.add(com.intellij.ui.GuiUtils.createVerticalStrut());
        panel.add(rowCountLabel);

        paginationPanel.add(panel, BorderLayout.EAST);
    }

    @NotNull
    private JPanel createRowLimitPanel() {
        rowLimitField.setText(Integer.toString(configuration.getDefaultRowLimit()));
        rowLimitField.setColumns(5);
        rowLimitField.setDocument(new NumberDocument());
        rowLimitField.setText(Integer.toString(configuration.getDefaultRowLimit()));

        JPanel rowLimitPanel = new NonOpaquePanel();
        rowLimitPanel.add(new JLabel("Row limit:"), BorderLayout.WEST);
        rowLimitPanel.add(rowLimitField, BorderLayout.CENTER);
        rowLimitPanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
        return rowLimitPanel;
    }

    @NotNull
    private JComponent createResultActionsComponent() {
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("MongoResultGroup", true);
        actionResultGroup.add(new ExecuteQuery(this));
        actionResultGroup.add(new OpenFindAction(this));
        actionResultGroup.add(new EnableAggregateAction(queryPanel));
        actionResultGroup.addSeparator();
        actionResultGroup.add(new AddMongoDocumentAction(resultPanel));
        actionResultGroup.add(new EditMongoDocumentAction(resultPanel));
        actionResultGroup.add(new DeleteMongoDocumentAction(resultPanel));
        actionResultGroup.add(new CopyAllAction(resultPanel));
        actionResultGroup.addSeparator();
        actionResultGroup.add(new NavigateBackwardAction(this));

        addBasicTreeActions(actionResultGroup);
        actionResultGroup.add(new CloseFindEditorAction(this));
//TODO Duplicate
        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("MongoResultGroupActions", actionResultGroup, true);
        actionToolBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent actionToolBarComponent = actionToolBar.getComponent();
        actionToolBarComponent.setBorder(null);
        actionToolBarComponent.setOpaque(false);
        return actionToolBarComponent;
    }

    @NotNull
    private JComponent createPaginationActionsComponent() {
        DefaultActionGroup actionResultGroup = new DefaultActionGroup("MongoPaginationGroup", false);
        actionResultGroup.add(new ChangeNbPerPageActionComponent(() -> new PaginationPopupComponent(pagination).initUi()));
        actionResultGroup.add(new PaginationAction.Previous(pagination));
        actionResultGroup.add(new PaginationAction.Next(pagination));

//TODO Duplicate
        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("MongoPaginationGroupActions", actionResultGroup, true);
        actionToolBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent actionToolBarComponent = actionToolBar.getComponent();
        actionToolBarComponent.setBorder(null);
        actionToolBarComponent.setOpaque(false);
        return actionToolBarComponent;
    }

    @NotNull
    private JComponent createSelectViewActionsComponent() {
        DefaultActionGroup viewSelectGroup = new DefaultActionGroup("MongoViewSelectGroup", false);
        viewSelectGroup.add(new ViewAsTreeAction(this));
        viewSelectGroup.add(new ViewAsTableAction(this));

//TODO Duplicate
        ActionToolbar viewToolbar = ActionManager.getInstance().createActionToolbar("MongoViewSelectedActions", viewSelectGroup, true);

        viewToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent viewToolbarComponent = viewToolbar.getComponent();
        viewToolbarComponent.setBorder(null);
        viewToolbarComponent.setOpaque(false);
        return viewToolbarComponent;
    }

    private void addBasicTreeActions(DefaultActionGroup actionResultGroup) {
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

        Disposer.register(this, () -> {
            collapseAllAction.unregisterCustomShortcutSet(resultPanel);
            expandAllAction.unregisterCustomShortcutSet(resultPanel);
        });


        actionResultGroup.addSeparator();
        actionResultGroup.add(expandAllAction);
        actionResultGroup.add(collapseAllAction);
    }

    public Navigation.WayPoint getCurrentWayPoint() {
        return navigation.getCurrentWayPoint();
    }

    public void showResults() {
        showResults(false);
    }

    private void showResults(boolean cached) {
        executeQuery(cached, navigation.getCurrentWayPoint());
    }

    //TODO refactor
    public void executeQuery() {
        Navigation.WayPoint currentWayPoint = navigation.getCurrentWayPoint();
        currentWayPoint.setQueryOptions(queryPanel.getQueryOptions(rowLimitField.getText()));
        MongoQueryOptions queryOptions = queryPanel.getQueryOptions(rowLimitField.getText());
        currentWayPoint.setQueryOptions(queryOptions);
        executeQuery(false, currentWayPoint);
    }

    //TODO refactor
    private void executeQuery(final boolean useCachedResults, final Navigation.WayPoint wayPoint) {
        errorPanel.setVisible(false);
        validateQuery();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Get documents from " + wayPoint.getLabel()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    UIUtil.invokeLaterIfNeeded(() -> loadingDecorator.startLoading(false));

                    final MongoQueryOptions queryOptions = wayPoint.getQueryOptions();
                    if (!useCachedResults) {
                        currentResults = mongoManager.findMongoDocuments(
                                configuration,
                                wayPoint.getCollection(),
                                queryOptions);
                    }
                    UIUtil.invokeLaterIfNeeded(() -> {
                        resultPanel.updateResultView(currentResults, pagination);
                        rowCountLabel.setText(String.format("%s documents", currentResults.getDocuments().size()));
                        initActions(resultPanel.resultTreeTableView);

                    });
                } catch (final Exception ex) {
                    UIUtil.invokeLaterIfNeeded(() -> {
                        errorPanel.invalidate();
                        errorPanel.removeAll();
                        errorPanel.add(new ErrorPanel(ex), BorderLayout.CENTER);
                        errorPanel.validate();
                        errorPanel.setVisible(true);
                    });
                } finally {
                    UIUtil.invokeLaterIfNeeded(loadingDecorator::stopLoading);
                }

            }
        });
    }

    private void initActions(JsonTreeTableView resultTreeTableView) {
        resultTreeTableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && resultPanel.isSelectedNodeId()) {
                    resultPanel.editSelectedMongoDocument();
                }
            }
        });

        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoResultPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(new EditMongoDocumentAction(resultPanel));
            actionPopupGroup.add(new DeleteMongoDocumentAction(resultPanel));
            actionPopupGroup.add(new CopyNodeAction(resultPanel));
            actionPopupGroup.add(new GoToMongoDocumentAction(this));
        }

        PopupHandler.installPopupHandler(resultTreeTableView, actionPopupGroup, "POPUP", ActionManager.getInstance());
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
        queryPanel.setVisible(true);
        splitter.setFirstComponent(queryPanel);
        UIUtil.invokeLaterIfNeeded(this::focusOnEditor);
    }

    public void closeFindEditor() {
        splitter.setFirstComponent(null);
        queryPanel.setVisible(false);
    }

    public void focusOnEditor() {
        queryPanel.requestFocusOnEditor();
    }

    public boolean isFindEditorOpened() {
        return splitter.getFirstComponent() == queryPanel;
    }

    public void setViewMode(MongoResultPanel.ViewMode viewMode) {
        if (resultPanel.getCurrentViewMode().equals(viewMode)) {
            return;
        }
        this.resultPanel.setCurrentViewMode(viewMode);
        executeQuery(true, navigation.getCurrentWayPoint());
    }

    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    public void navigateBackward() {
        navigation.moveBackward();
        executeQuery(false, navigation.getCurrentWayPoint());
    }

    public boolean hasNavigationHistory() {
        return navigation.getWayPoints().size() > 1;
    }

    public void goToReferencedDocument() {
        DBRef selectedDBRef = resultPanel.getSelectedDBRef();

        Document referencedDocument = resultPanel.getReferencedDocument(selectedDBRef);
        if (referencedDocument == null) {
            Messages.showErrorDialog(this, "Referenced document was not found");
            return;
        }

        navigation.addNewWayPoint(
                new MongoCollection(
                        selectedDBRef.getCollectionName(),
                        selectedDBRef.getDatabaseName() != null
                                ? new MongoDatabase(selectedDBRef.getDatabaseName(), new MongoServer(configuration))
                                : navigation.getCurrentWayPoint().getCollection().getParentDatabase()
                ),
                new MongoQueryOptions().setFilter(
                        new Document("_id", selectedDBRef.getId())
                ));
        executeQuery(false, navigation.getCurrentWayPoint());
    }

    private static class ChangeNbPerPageActionComponent extends DumbAwareAction implements CustomComponentAction {

        @NotNull
        private final Computable<JComponent> myComponentCreator;

        ChangeNbPerPageActionComponent(@NotNull Computable<JComponent> componentCreator) {
            myComponentCreator = componentCreator;
        }

        @Override
        public JComponent createCustomComponent(Presentation presentation, @NotNull String place) {
            return myComponentCreator.compute();
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
        }
    }


    public interface MongoDocumentOperations {
        Document getMongoDocument(Object _id);

        void deleteMongoDocument(Object mongoDocument);

        void updateMongoDocument(Document mongoDocument);

        Document getReferenceDocument(String collection, Object _id, String database);
    }
}
