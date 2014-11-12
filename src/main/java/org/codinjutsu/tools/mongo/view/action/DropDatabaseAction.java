package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

/**
 * Created by piddubnyi on 06.11.14 .
 */
public class DropDatabaseAction extends AnAction implements DumbAware {

    private static final Icon REMOVE_ICON = StyleAttributesUtils.getInstance().getClearAllIcon();

    private final MongoExplorerPanel mongoExplorerPanel;

    public DropDatabaseAction(MongoExplorerPanel mongoExplorerPanel) {
        super("Drop Database", "Drop the selected database", REMOVE_ICON);
        this.mongoExplorerPanel = mongoExplorerPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        mongoExplorerPanel.dropDatabase();
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(mongoExplorerPanel.getSelectedDatabase() != null);
    }
}