package org.codinjutsu.tools.mongo.view.action.edition;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.MongoEditionPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class DeleteKeyAction extends AnAction {

    private static final Icon DELETE_ICON = StyleAttributesUtils.getInstance().getDeleteIcon();

    private final MongoEditionPanel mongoEditionPanel;

    public DeleteKeyAction(MongoEditionPanel mongoEditionPanel) {
        super("Delete this", "Delete the selected node", DELETE_ICON);
        this.mongoEditionPanel = mongoEditionPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        mongoEditionPanel.removeSelectedKey();
    }
}
