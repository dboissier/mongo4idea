package org.codinjutsu.tools.mongo.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.MongoPanel;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;

public class ViewAsTreeAction extends AnAction {
    private final MongoPanel mongoPanel;

    public ViewAsTreeAction(MongoPanel mongoPanel) {
        super("View as tree", "See results as tree", AllIcons.Actions.ShowAsTree);
        this.mongoPanel = mongoPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        mongoPanel.setViewMode(MongoResultPanel.ViewMode.TREE);
    }
}
