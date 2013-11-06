package org.codinjutsu.tools.mongo.view.action.edition;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.MongoEditionPanel;

public class EditKeyAction extends AnAction {

    private final MongoEditionPanel mongoEditionPanel;

    public EditKeyAction(MongoEditionPanel mongoEditionPanel) {
        this.mongoEditionPanel = mongoEditionPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

    }
}
