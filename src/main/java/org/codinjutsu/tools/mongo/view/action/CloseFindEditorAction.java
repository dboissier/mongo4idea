package org.codinjutsu.tools.mongo.view.action;

import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.MongoRunnerPanel;

public class CloseFindEditorAction extends CloseTabToolbarAction {
    private final MongoRunnerPanel mongoRunnerPanel;

    public CloseFindEditorAction(MongoRunnerPanel mongoRunnerPanel) {
        this.mongoRunnerPanel = mongoRunnerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        mongoRunnerPanel.closeFindEditor();
    }
}