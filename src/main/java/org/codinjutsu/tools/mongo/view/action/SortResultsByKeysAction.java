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

package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.codinjutsu.tools.mongo.utils.GuiUtil;
import org.codinjutsu.tools.mongo.view.MongoRunnerPanel;

import javax.swing.*;

public class SortResultsByKeysAction extends ToggleAction {

    private static final Icon SORT = GuiUtil.loadIcon("sortByKey.png");

    private boolean sortedByKey = false;
    private final MongoRunnerPanel mongoRunnerPanel;

    public SortResultsByKeysAction(MongoRunnerPanel mongoRunnerPanel) {
        super("Sort by key", "Sort by key", SORT);
        this.mongoRunnerPanel = mongoRunnerPanel;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return sortedByKey;
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean sorted) {
        sortedByKey = sorted;
        mongoRunnerPanel.setSortedByKey(sorted);
    }
}
