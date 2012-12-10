/*
 * Copyright (c) 2012 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.mongo.view;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.mongodb.util.JSONParseException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class ErrorPanel extends JPanel {

    private static final Color ERROR_COLOR = new Color(255, 220, 220);

    ErrorPanel(final Exception ex) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(ERROR_COLOR);
        JLabel label = new JLabel();
        final String title;
        final String message;
        if (ex instanceof JSONParseException) {
            title = "Bad query fragment";
            message = ex.getMessage();
        } else {
            title = "Error during query execution";
            message = ex.toString();
        }
        label.setText(title);
        add(label);
        final HoverHyperlinkLabel hoverHyperlinkLabel = new HoverHyperlinkLabel("more detail...");
        hoverHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Messages.showErrorDialog(message, title);
                }
            }
        });
        add(Box.createRigidArea(new Dimension(10, 10)));
        add(hoverHyperlinkLabel);

    }
}
