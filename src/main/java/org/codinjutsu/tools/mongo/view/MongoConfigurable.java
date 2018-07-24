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

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.MongoConfiguration;
import org.codinjutsu.tools.mongo.utils.MongoUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;


public class MongoConfigurable extends BaseConfigurable implements SearchableConfigurable {


    public static final String PLUGIN_SETTINGS_NAME = "Mongo Plugin";
    private final Project project;

    private final MongoConfiguration configuration;

    private JPanel mainPanel;
    private LabeledComponent<TextFieldWithBrowseButton> shellPathField;
    private JLabel testMongoPathFeedbackLabel;


    public MongoConfigurable(Project project) {
        this.project = project;
        this.configuration = MongoConfiguration.getInstance(project);
        mainPanel = new JPanel(new BorderLayout());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return PLUGIN_SETTINGS_NAME;
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
        mongoShellOptionsPanel.add(new JLabel("Path to Mongo Shell:"));
        mongoShellOptionsPanel.add(shellPathField);
        mongoShellOptionsPanel.add(createTestButton());
        mongoShellOptionsPanel.add(createFeedbackLabel());

        mainPanel.add(mongoShellOptionsPanel, BorderLayout.NORTH);

        return mainPanel;
    }

    private JLabel createFeedbackLabel() {
        testMongoPathFeedbackLabel = new JLabel();
        return testMongoPathFeedbackLabel;
    }

    private JButton createTestButton() {
        JButton testButton = new JButton("Test");
        testButton.addActionListener(actionEvent -> testPath());
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
        LabeledComponent<TextFieldWithBrowseButton> shellPathField = new LabeledComponent<>();
        TextFieldWithBrowseButton component = new TextFieldWithBrowseButton();
        component.getChildComponent().setName("shellPathField");
        shellPathField.setComponent(component);
        shellPathField.getComponent().addBrowseFolderListener("Mongo Shell Configuration", "", null,
                new FileChooserDescriptor(true, false, false, false, false, false));

        shellPathField.getComponent().setText(configuration.getShellPath());

        return shellPathField;
    }

    public boolean isModified() {
        return isShellPathModified();
    }

    @Override
    public void apply() {
        if (isShellPathModified()) {
            configuration.setShellPath(getShellPath());
        }
    }

    private boolean isShellPathModified() {
        String existingShellPath = MongoConfiguration.getInstance(project).getShellPath();

        return !StringUtils.equals(existingShellPath, getShellPath());
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
        shellPathField = null;
    }

    @NotNull
    @Override
    public String getId() {
        return "preferences.mongoOptions";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }
}
