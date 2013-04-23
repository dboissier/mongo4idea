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

package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import org.codinjutsu.tools.mongo.model.MongoAggregateOperator;
import org.codinjutsu.tools.mongo.view.QueryPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;
import java.awt.*;

public class AddOperatorPanelAction extends AnAction implements DumbAware {

    private static final Icon ADD_ICON = StyleAttributesUtils.getInstance().getAddIcon();

    private final JList OPERATOR_LIST = new JBList(MongoAggregateOperator.values());
    private final QueryPanel queryPanel;

    public AddOperatorPanelAction(QueryPanel queryPanel) {
        super("Add operation", "Add operation", ADD_ICON);
        this.queryPanel = queryPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Component component = anActionEvent.getInputEvent().getComponent();
        new PopupChooserBuilder(OPERATOR_LIST).
                setMovable(false).
                setItemChoosenCallback(new Runnable() {
                    public void run() {
                        MongoAggregateOperator selectedOperator = (MongoAggregateOperator) OPERATOR_LIST.getSelectedValue();
                        if (selectedOperator == null) return;
                        queryPanel.addOperatorPanel(selectedOperator);
                    }
                })
                .createPopup()
                .show(new RelativePoint(component, new Point(component.getWidth(), 0)));


    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(queryPanel.isAggregate());
    }
}
