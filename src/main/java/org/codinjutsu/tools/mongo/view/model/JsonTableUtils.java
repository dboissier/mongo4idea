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

package org.codinjutsu.tools.mongo.view.model;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.bson.Document;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.renderer.MongoTableCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellRenderer;
import java.util.List;
import java.util.Set;

public class JsonTableUtils {


    public static ListTableModel buildJsonTable(MongoCollectionResult mongoCollectionResult) {
        List<Document> resultObjects = mongoCollectionResult.getDocuments();
        if (resultObjects.isEmpty()) {
            return null;
        }

        ColumnInfo[] columnInfos = extractColumnNames(resultObjects.get(0));

        return new ListTableModel<>(columnInfos, resultObjects);
    }

    private static ColumnInfo[] extractColumnNames(final Document document) {
        Set<String> keys = document.keySet();
        ColumnInfo[] columnInfos = new ColumnInfo[keys.size()];
        int index = 0;
        for (final String key : keys) {
            columnInfos[index++] = new TableColumnInfo(key);
        }
        return columnInfos;
    }

    private static class TableColumnInfo extends ColumnInfo {
        private final String key;

        private static final TableCellRenderer MONGO_TABLE_CELL_RENDERER = new MongoTableCellRenderer();

        public TableColumnInfo(String key) {
            super(key);
            this.key = key;
        }

        @Nullable
        @Override
        public Object valueOf(Object o) {
            Document document = (Document) o;
            return document.get(key);
        }

        @Nullable
        @Override
        public TableCellRenderer getRenderer(Object o) {
            return MONGO_TABLE_CELL_RENDERER;
        }
    }
}
