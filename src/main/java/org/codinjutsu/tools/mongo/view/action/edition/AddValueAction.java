package org.codinjutsu.tools.mongo.view.action.edition;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.AddValueDialog;
import org.codinjutsu.tools.mongo.view.MongoEditionPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class AddValueAction extends AnAction {

    private static final Icon ADD_ICON = StyleAttributesUtils.getInstance().getAddIcon();

    private final MongoEditionPanel mongoEditionPanel;

    public AddValueAction(MongoEditionPanel mongoEditionPanel) {
        super("Add a value", "Add a value", ADD_ICON);
        this.mongoEditionPanel = mongoEditionPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        AddValueDialog dialog = AddValueDialog.createDialog(mongoEditionPanel);
        dialog.show();

        if (!dialog.isOK()) {
            return;
        }

        mongoEditionPanel.addValue(dialog.getJsonDataType(), dialog.getValue());
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoEditionPanel.canAddValue());
    }
}