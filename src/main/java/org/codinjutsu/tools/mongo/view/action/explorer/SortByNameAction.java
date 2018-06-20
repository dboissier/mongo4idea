package org.codinjutsu.tools.mongo.view.action.explorer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoExplorerPanel;

public class SortByNameAction extends ToggleAction implements DumbAware {

    private boolean sortByName = false;

    private final MongoExplorerPanel explorerPanel;

    public SortByNameAction(MongoExplorerPanel explorerPanel) {
        super("Sort by name", "Sort alphabetically", AllIcons.ObjectBrowser.Sorted);
        this.explorerPanel = explorerPanel;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return sortByName;
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean sortByName) {
        this.sortByName = sortByName;
        explorerPanel.sortTreeNodes();
    }
}
