package org.codinjutsu.tools.mongo.view.action.explorer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

import java.awt.*;
import java.awt.event.KeyEvent;

public class DuplicateServerAction extends AnAction implements DumbAware {

    private final MongoExplorerPanel mongoExplorerPanel;

    public DuplicateServerAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Duplicate...", "Duplicate Server Configuration", AllIcons.Actions.Copy);
        this.mongoExplorerPanel = mongoExplorerPanel;

        registerCustomShortcutSet(KeyEvent.VK_D,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                mongoExplorerPanel);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ServerConfiguration configuration = mongoExplorerPanel.getSelectedServer().getConfiguration();
        ServerConfiguration clonedConfiguration = configuration.clone();
        clonedConfiguration.setLabel("Copy of " + clonedConfiguration.getLabel());
        mongoExplorerPanel.addConfiguration(clonedConfiguration);
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoExplorerPanel.getSelectedServer() != null);
    }
}
