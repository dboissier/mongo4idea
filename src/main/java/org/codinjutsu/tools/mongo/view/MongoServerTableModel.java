/*
 * Copyright (c) 2016 David Boissier.
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

import com.intellij.util.ui.ItemRemovable;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.SshTunnelingConfiguration;

import javax.swing.table.AbstractTableModel;
import java.util.List;

class MongoServerTableModel extends AbstractTableModel implements ItemRemovable {
    private final String[] columnNames = new String[]{
            "Label",
            "URL",
            "SSH Tunneling",
            "Autoconnect"
    };
    private final Class[] columnClasses = new Class[]{String.class, String.class, Boolean.class, Boolean.class};

    private final List<ServerConfiguration> mongoServerConfigurations;

    public MongoServerTableModel(List<ServerConfiguration> mongoServerConfigurations) {
        this.mongoServerConfigurations = mongoServerConfigurations;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Class getColumnClass(int column) {
        return columnClasses[column];
    }

    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return mongoServerConfigurations.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int row, int column) {
        ServerConfiguration configuration = mongoServerConfigurations.get(row);
        switch (column) {
            case 0: { // "Label" column
                return configuration.getLabel();
            }
            case 1: { // "URL" column
                return configuration.getUrlsInSingleString();
            }
            case 2: { // "SSH Tunneling" column
                return !SshTunnelingConfiguration.isEmpty(configuration.getSshTunnelingConfiguration());
            }
            case 3: { // "Autoconnect" column
                return configuration.isConnectOnIdeStartup();
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    public void setValueAt(Object value, int row, int column) {
        ServerConfiguration configuration = mongoServerConfigurations.get(row);
        switch (column) {
            case 0: {
                configuration.setLabel((String) value);
                break;
            }
            case 1: {
                //do nothing url = serverHosts
                break;
            }
            case 2: {
                //do nothing url = serverHosts
                break;
            }
            case 3: {
                configuration.setConnectOnIdeStartup((Boolean) value);
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    public void removeRow(int index) {
        mongoServerConfigurations.remove(index);
        fireTableRowsDeleted(index, index);
    }
}