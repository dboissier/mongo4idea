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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.*;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.logic.MongoManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Iterator;
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
    private JPanel mongoShellOptionsPanel;

    public MongoConfigurable(Project project, MongoConfiguration configuration, MongoManager mongoManager) {
        this.project = project;
        this.configuration = configuration;
        this.mongoManager = mongoManager;
        configurations = configuration.getServerConfigurations();
        tableModel = new MongoServerTableModel(configurations);
        mainPanel = new JPanel(new BorderLayout());
    }


    @Nls
    @Override
    public String getDisplayName() {
        return "Mongo Configuration";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preferences.mongoOptions";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
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


                return ToolbarDecorator.createDecorator(table)
                        .setAddAction(new AnActionButtonRunnable() {
                            @Override
                            public void run(AnActionButton button) {
                                stopEditing();
                                ServerConfiguration serverConfiguration = new ServerConfiguration();
                                ConfigurationDialog dialog = new ConfigurationDialog(mainPanel, mongoManager, serverConfiguration);
                                dialog.setTitle("Edit Mongo Server");
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
                        }).setEditAction(new AnActionButtonRunnable() {
                            @Override
                            public void run(AnActionButton button) {
                                editSelectedConfiguration();
                            }
                        }).setRemoveAction(new AnActionButtonRunnable() {
                            @Override
                            public void run(AnActionButton button) {
                                stopEditing();
                                int selectedIndex = table.getSelectedRow();
                                if (selectedIndex < 0 || selectedIndex >= tableModel.getRowCount()) {
                                    return;
                                }
                                ServerConfiguration configurationToBeRemoved = configurations.get(selectedIndex);
                                TableUtil.removeSelectedItems(table);

                                Iterator<ServerConfiguration> iterator = configurations.iterator();
                                while (iterator.hasNext()) {
                                    ServerConfiguration next = iterator.next();
                                    if (next.equals(configurationToBeRemoved)) {
                                        iterator.remove();
                                    }
                                }
                            }
                        }).disableUpDownActions().createPanel();
            }
        };

        mainPanel.add(panelWithButtons, BorderLayout.CENTER);

        mongoShellOptionsPanel = new JPanel();
        mongoShellOptionsPanel.setLayout(new BoxLayout(mongoShellOptionsPanel, BoxLayout.X_AXIS));
        mongoShellOptionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Misc options", true));
        shellPathField = createShellPathField();
        mongoShellOptionsPanel.add(shellPathField);

        mainPanel.add(mongoShellOptionsPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private LabeledComponent<TextFieldWithBrowseButton> createShellPathField() {
        LabeledComponent<TextFieldWithBrowseButton> shellPathField = new LabeledComponent<TextFieldWithBrowseButton>();
        shellPathField.setText("Mongo shell path");
        shellPathField.setComponent(new TextFieldWithBrowseButton());
        shellPathField.getComponent().setName("shellPathField");
        shellPathField.getComponent().addBrowseFolderListener("Mongo shell path", "", null,
                new FileChooserDescriptor(true, false, false, false, false, false));

        return shellPathField;
    }

    public boolean isModified() {
        return areConfigurationsModified() || isShellModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        stopEditing();
        if (areConfigurationsModified()) {
            configuration.setServerConfigurations(configurations);
        }

        if (isShellModified()) {
            configuration.setShellPath(getShellPath());
        }
    }

    private boolean isShellModified() {
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
        //TODO
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

    private void editSelectedConfiguration() {
        stopEditing();
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex < 0 || selectedIndex >= tableModel.getRowCount()) {
            return;
        }
        ServerConfiguration sourceConfiguration = configurations.get(selectedIndex);
        ServerConfiguration configuration = sourceConfiguration.clone();
        ConfigurationDialog dialog = new ConfigurationDialog(mainPanel, mongoManager, configuration);
        dialog.setTitle("");
        dialog.show();
        if (!dialog.isOK()) {
            return;
        }
        configurations.set(selectedIndex, configuration);
        tableModel.fireTableRowsUpdated(selectedIndex, selectedIndex);
        table.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
}
