package org.codinjutsu.tools.mongo.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.mongo.view.MongoResultPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class EditMongoDocumentAction extends AnAction implements DumbAware {

    private static final Icon EDIT_ICON = StyleAttributesUtils.getInstance().getCopyIcon();
    private final MongoResultPanel resultPanel;

    public EditMongoDocumentAction(MongoResultPanel resultPanel) {
        super("Edit", "Edit mongo document", EDIT_ICON);
        this.resultPanel = resultPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        resultPanel.editSelectedMongoDocument();
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setEnabled(resultPanel.isSelectedNodeObjectId());
    }
}
