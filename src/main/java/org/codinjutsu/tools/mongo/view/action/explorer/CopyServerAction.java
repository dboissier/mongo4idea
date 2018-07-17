package org.codinjutsu.tools.mongo.view.action.explorer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

public class CopyServerAction extends AnAction implements DumbAware {

    private final MongoExplorerPanel mongoExplorerPanel;

    public CopyServerAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Copy server", "Copy Server Configuration", AllIcons.Actions.Copy);
        this.mongoExplorerPanel = mongoExplorerPanel;
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
