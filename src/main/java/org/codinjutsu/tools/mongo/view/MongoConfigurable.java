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

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.*;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.codinjutsu.tools.mongo.utils.MongoUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;


public class MongoConfigurable extends BaseConfigurable {


    private final Project project;

    private final MongoConfiguration configuration;
    private final MongoManager mongoManager;

    private final List<ServerConfiguration> configurations;

    private JPanel mainPanel;
    private JBTable table;
    private final MongoServerTableModel tableModel;
    private LabeledComponent<TextFieldWithBrowseButton> shellPathField;
    private JLabel testMongoPathFeedbackLabel;


    public MongoConfigurable(Project project) {
        this.project = project;
        this.configuration = MongoConfiguration.getInstance(project);
        this.mongoManager = MongoManager.getInstance(project);
        configurations = new LinkedList<ServerConfiguration>(this.configuration.getServerConfigurations());
        tableModel = new MongoServerTableModel(configurations);
        mainPanel = new JPanel(new BorderLayout());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Mongo servers";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preferences.mongoOptions";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel mongoShellOptionsPanel = new JPanel();
        mongoShellOptionsPanel.setLayout(new BoxLayout(mongoShellOptionsPanel, BoxLayout.X_AXIS));
        shellPathField = createShellPathField();
        mongoShellOptionsPanel.add(new JLabel("Path to Mongo executable:"));
        mongoShellOptionsPanel.add(shellPathField);
        mongoShellOptionsPanel.add(createTestButton());
        mongoShellOptionsPanel.add(createFeedbackLabel());

        mainPanel.add(mongoShellOptionsPanel, BorderLayout.NORTH);


        PanelWithButtons panelWithButtons = new PanelWithButtons() {

            {
                initPanel();
            }

            @Nullable
            @Override
            protected String getLabelText() {
                return "Servers";
            }

            @Override
            protected JButton[] createButtons() {
                return new JButton[]{};
            }

            @Override
            protected JComponent createMainComponent() {
                table = new JBTable(tableModel);
                table.getEmptyText().setText("No server configuration set");
                table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                TableColumn autoConnectColumn = table.getColumnModel().getColumn(2);
                int width = table.getFontMetrics(table.getFont()).stringWidth(table.getColumnName(2)) + 10;
                autoConnectColumn.setPreferredWidth(width);
                autoConnectColumn.setMaxWidth(width);
                autoConnectColumn.setMinWidth(width);

                return ToolbarDecorator.createDecorator(table)
                        .setAddAction(new AnActionButtonRunnable() {
                            @Override
                            public void run(AnActionButton button) {
                                stopEditing();

                                ServerConfiguration serverConfiguration = ServerConfiguration.byDefault();

                                ConfigurationDialog dialog = new ConfigurationDialog(mainPanel, mongoManager, serverConfiguration);
                                dialog.setTitle("Add a Mongo Server");
                                dialog.show();
                                if (!dialog.isOK()) {
                                    return;
                                }

                                configurations.add(serverConfiguration);
                                int index = configurations.size() - 1;
                                tableModel.fireTableRowsInserted(index, index);
                                table.getSelectionModel().setSelectionInterval(index, index);
                                table.scrollRectToVisible(table.getCellRect(index, 0, true));
                            }
                        })
                        .setAddActionName("addServer")
                        .setEditAction(new AnActionButtonRunnable() {
                            @Override
                            public void run(AnActionButton button) {
                                stopEditing();

                                int selectedIndex = table.getSelectedRow();
                                if (selectedIndex < 0 || selectedIndex >= tableModel.getRowCount()) {
                                    return;
                                }
                                ServerConfiguration sourceConfiguration = configurations.get(selectedIndex);
                                ServerConfiguration copiedConfiguration = sourceConfiguration.clone();


                                ConfigurationDialog dialog = new ConfigurationDialog(mainPanel, mongoManager, copiedConfiguration);
                                dialog.setTitle("Edit a Mongo Server");
                                dialog.show();
                                if (!dialog.isOK()) {
                                    return;
                                }

                                configurations.set(selectedIndex, copiedConfiguration);
                                tableModel.fireTableRowsUpdated(selectedIndex, selectedIndex);
                                table.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
                            }
                        })
                        .setEditActionName("editServer")
                        .setRemoveAction(new AnActionButtonRunnable() {
                            @Override
                            public void run(AnActionButton button) {
                                stopEditing();

                                int selectedIndex = table.getSelectedRow();
                                if (selectedIndex < 0 || selectedIndex >= tableModel.getRowCount()) {
                                    return;
                                }
                                TableUtil.removeSelectedItems(table);
                            }
                        })
                        .setRemoveActionName("removeServer")
                        .disableUpDownActions().createPanel();
            }
        };

        mainPanel.add(panelWithButtons, BorderLayout.CENTER);

        return mainPanel;
    }

    private JLabel createFeedbackLabel() {
        testMongoPathFeedbackLabel = new JLabel();
        return testMongoPathFeedbackLabel;
    }

    private JButton createTestButton() {
        JButton testButton = new JButton("Test");
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                testPath();
            }
        });
        return testButton;
    }

    private void testPath() {
        try {
            testMongoPathFeedbackLabel.setIcon(null);
            if (MongoUtils.checkMongoShellPath(getShellPath())) {
                testMongoPathFeedbackLabel.setIcon(ServerConfigurationPanel.SUCCESS);
            } else {
                testMongoPathFeedbackLabel.setIcon(ServerConfigurationPanel.FAIL);
            }
        } catch (ExecutionException e) {
            Messages.showErrorDialog(mainPanel, e.getMessage(), "Error During Mongo Shell Path Checking...");
        }
    }

    private LabeledComponent<TextFieldWithBrowseButton> createShellPathField() {
        LabeledComponent<TextFieldWithBrowseButton> shellPathField = new LabeledComponent<TextFieldWithBrowseButton>();
        TextFieldWithBrowseButton component = new TextFieldWithBrowseButton();
        component.getChildComponent().setName("shellPathField");
        shellPathField.setComponent(component);
        shellPathField.getComponent().addBrowseFolderListener("Mongo shell configuration", "", null,
                new FileChooserDescriptor(true, false, false, false, false, false));

        shellPathField.getComponent().setText(configuration.getShellPath());

        return shellPathField;
    }

    public boolean isModified() {
        return areConfigurationsModified() || isShellPathModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        stopEditing();
        if (areConfigurationsModified()) {
            configuration.setServerConfigurations(configurations);
        }

        if (isShellPathModified()) {
            configuration.setShellPath(getShellPath());
        }

        MongoWindowManager.getInstance(project).apply();
    }

    private boolean isShellPathModified() {
        String existingShellPath = MongoConfiguration.getInstance(project).getShellPath();

        return !StringUtils.equals(existingShellPath, getShellPath());
    }

    private boolean areConfigurationsModified() {
        List<ServerConfiguration> existingConfigurations = MongoConfiguration.getInstance(project).getServerConfigurations();

        if (configurations.size() != existingConfigurations.size()) {
            return true;
        }

        for (ServerConfiguration existingConfiguration : existingConfigurations) {
            if (!configurations.contains(existingConfiguration)) {
                return true;
            }
        }

        return false;
    }

    private String getShellPath() {
        String shellPath = shellPathField.getComponent().getText();
        if (StringUtils.isNotBlank(shellPath)) {
            return shellPath;
        }

        return null;
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        tableModel.removeTableModelListener(table);
        shellPathField = null;
        table = null;
    }


    private void stopEditing() {
        if (table.isEditing()) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }

}
