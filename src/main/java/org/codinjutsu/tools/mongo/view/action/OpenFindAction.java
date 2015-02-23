package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class OpenFindAction extends AnAction implements DumbAware {
    private static final Icon FIND_ICON = StyleAttributesProvider.getFindIcon();
    private final MongoPanel mongoPanel;

    public OpenFindAction(MongoPanel mongoPanel) {
        super("Find", "Open Find editor", FIND_ICON);
        this.mongoPanel = mongoPanel;
        registerCustomShortcutSet(KeyEvent.VK_F, KeyEvent.CTRL_MASK, mongoPanel);
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        if (!mongoPanel.isFindEditorOpened()) {
            mongoPanel.openFindEditor();
        } else {
            mongoPanel.focusOnEditor();
        }
    }
}
