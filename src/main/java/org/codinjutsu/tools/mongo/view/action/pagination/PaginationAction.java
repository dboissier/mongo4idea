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

package org.codinjutsu.tools.mongo.view.action.pagination;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.model.NbPerPage;
import org.codinjutsu.tools.mongo.view.model.Pagination;

import javax.swing.*;

public abstract class PaginationAction extends AnAction implements DumbAware {
    final Pagination pagination;

    PaginationAction(Pagination pagination, String toolTip, String description, Icon icon) {
        super(toolTip, description, icon);
        this.pagination = pagination;
    }


    public static class Next extends PaginationAction {
        public Next(Pagination pagination) {
            super(pagination, "See next results", "Next page", AllIcons.Actions.Forward);
        }


        @Override
        public void actionPerformed(AnActionEvent e) {
            pagination.next();
        }

        @Override
        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(
                    !NbPerPage.ALL.equals(pagination.getNbPerPage())
                            && pagination.getPageNumber() < pagination.getTotalPageNumber()
            );
        }
    }

    public static class Previous extends PaginationAction {
        public Previous(Pagination pagination) {
            super(pagination, "See previous results", "Previous page", AllIcons.Actions.Back);
        }


        @Override
        public void actionPerformed(AnActionEvent e) {
            pagination.previous();
        }

        @Override
        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(
                    !NbPerPage.ALL.equals(pagination.getNbPerPage())
                            && pagination.getStartIndex() > 0
            );
        }
    }
}
