/*
 * Copyright (c) 2015 David Boissier
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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.QueryPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;


public class EnableQuerySortAction extends AnAction {

    private static final Icon SORT_ICON = StyleAttributesUtils.getInstance().getSortedIcon();

    private final QueryPanel queryPanel;

    public EnableQuerySortAction(final QueryPanel queryPanel) {
        super("Show Sorting", "{ 'foo':  -1}", SORT_ICON);
        this.queryPanel = queryPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        queryPanel.showHideSort();
    }
}
