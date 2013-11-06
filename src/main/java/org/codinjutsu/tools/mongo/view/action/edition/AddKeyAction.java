package org.codinjutsu.tools.mongo.view.action.edition;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.mongo.view.MongoEditionPanel;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;

import javax.swing.*;

public class AddKeyAction extends AnAction {

    private static final Icon ADD_ICON = StyleAttributesUtils.getInstance().getAddIcon();

    private final MongoEditionPanel mongoEditionPanel;

    public AddKeyAction(MongoEditionPanel mongoEditionPanel) {
        super("Add a key", "Add a key after the selected node", ADD_ICON);
        this.mongoEditionPanel = mongoEditionPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //dialog menu with label and data type (string, boolean, numeric, dbobject, dblist)
    }
}
