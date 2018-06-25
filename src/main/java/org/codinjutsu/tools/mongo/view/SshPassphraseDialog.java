package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SshPassphraseDialog extends DialogWrapper {
    private JPanel contentPane;
    private JPasswordField passPhraseField;

    public SshPassphraseDialog(JPanel parent) {
        super(parent, true);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    public static SshPassphraseDialog createDialog(JPanel parentPanel) {
        SshPassphraseDialog dialog = new SshPassphraseDialog(parentPanel);
        dialog.init();
        dialog.setTitle("Passphrase Required");

        return dialog;
    }

    public String getPassphrase() {
        return String.valueOf(passPhraseField.getPassword());
    }
}
