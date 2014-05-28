package org.codinjutsu.tools.mongo.view.action;

import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.MongoPanel;

public class CloseFindEditorAction extends CloseTabToolbarAction {
    private final MongoPanel mongoPanel;

    public CloseFindEditorAction(MongoPanel mongoPanel) {
        this.mongoPanel = mongoPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        mongoPanel.closeFindEditor();
    }
}