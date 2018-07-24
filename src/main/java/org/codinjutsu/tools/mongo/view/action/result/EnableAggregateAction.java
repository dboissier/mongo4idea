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

package org.codinjutsu.tools.mongo.view.action.result;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.codinjutsu.tools.mongo.utils.GuiUtils;
import org.codinjutsu.tools.mongo.view.QueryPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class EnableAggregateAction extends ToggleAction {

    private static final String ENABLE_FIND_MODE = "Toggle to Find Mode";
    private static final String ENABLE_AGGREGATION_MODE = "Toggle to Aggregation Mode";
    private static final Icon AGGREGATION_ICON = GuiUtils.loadIcon("sqlGroupByType.png");
    private static final String QUERY_FIND_SAMPLE = "ex: {'name': 'foo'}";
    private static final String QUERY_AGGREGATION_SAMPLE = "ex: [{'$match': {'name': 'foo'}, {'$project': {'address': 1}}]";

    private final QueryPanel queryPanel;

    private boolean enableAggregation = false;

    public EnableAggregateAction(final QueryPanel queryPanel) {
        super(ENABLE_AGGREGATION_MODE, QUERY_FIND_SAMPLE, AGGREGATION_ICON);
        this.queryPanel = queryPanel;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return enableAggregation;
    }

    @Override
    public void setSelected(AnActionEvent event, boolean enableAggregation) {
        this.enableAggregation = enableAggregation;
        if (enableAggregation) {
            queryPanel.toggleToAggregation();
        } else {
            queryPanel.toggleToFind();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setText(isSelected(event) ? ENABLE_FIND_MODE : ENABLE_AGGREGATION_MODE);
        event.getPresentation().setDescription(isSelected(event) ? QUERY_AGGREGATION_SAMPLE : QUERY_FIND_SAMPLE);
        event.getPresentation().setVisible(queryPanel.isVisible());
    }
}
