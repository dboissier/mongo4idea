package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoRunnerPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class OpenFindAction extends AnAction implements DumbAware {
    private static final Icon FIND_ICON = StyleAttributesUtils.getInstance().getFindIcon();
    private final MongoRunnerPanel mongoRunnerPanel;

    public OpenFindAction(MongoRunnerPanel mongoRunnerPanel) {
        super("Find", "Open Find editor", FIND_ICON);
        this.mongoRunnerPanel = mongoRunnerPanel;
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        mongoRunnerPanel.openFindEditor();
    }
}
