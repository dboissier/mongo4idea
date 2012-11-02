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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.mongo.utils.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class QueryPanel extends JPanel {
    private static final Icon FAIL_ICON = GuiUtil.loadIcon("fail.png");

    private JTextArea filterTextArea;
    private JPanel mainPanel;
    private JLabel feedbackLabel;

    public QueryPanel() {
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    public String getFilterText() {
        return StringUtils.trim(filterTextArea.getText());
    }

    public MongoQueryOptions getQueryOptions() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();


        String filterText = getFilterText();
        if (!StringUtils.isBlank(filterText)) {
            try {
                DBObject filter = (DBObject) JSON.parse(filterText);
                mongoQueryOptions.setFilter(filter);
                if (feedbackLabel.getIcon() != null) {
                    feedbackLabel.setIcon(null);
                }
            } catch (Exception ex) {
                setErrorMsg(ex);
            }
        }

        return mongoQueryOptions;
    }

    public void setErrorMsg(Exception ex) {
        feedbackLabel.setIcon(FAIL_ICON);
        feedbackLabel.setText(String.format("[%s] %s", ex.getClass().getSimpleName(), ex.getMessage()));
    }
}
