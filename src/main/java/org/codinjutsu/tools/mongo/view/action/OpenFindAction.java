package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class OpenFindAction extends AnAction implements DumbAware {
    private static final Icon FIND_ICON = StyleAttributesUtils.getInstance().getFindIcon();
    private final MongoPanel mongoPanel;

    public OpenFindAction(MongoPanel mongoPanel) {
        super("Find", "Open Find editor", FIND_ICON);
        this.mongoPanel = mongoPanel;
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        mongoPanel.openFindEditor();
    }
}
