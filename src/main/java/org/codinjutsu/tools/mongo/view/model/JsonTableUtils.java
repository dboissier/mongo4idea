package org.codinjutsu.tools.mongo.view.model;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.renderer.MongoTableCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.util.List;
import java.util.Set;

public class JsonTableUtils {


    public static ListTableModel buildJsonTable(MongoCollectionResult mongoCollectionResult) {
        List<DBObject> resultObjects = mongoCollectionResult.getMongoObjects();
        if (resultObjects.isEmpty()) {
            return null;
        }

        ColumnInfo[] columnInfos = extractColumnNames(resultObjects.get(0));

        return new ListTableModel<>(columnInfos, resultObjects);
    }

    private static ColumnInfo[] extractColumnNames(final DBObject dbObject) {
        Set<String> keys = dbObject.keySet();
        ColumnInfo[] columnInfos = new ColumnInfo[keys.size()];
        int index = 0;
        for (final String key: keys) {
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
            BasicDBObject dbObject1 = (BasicDBObject) o;
            return dbObject1.get(key);
        }

        @Nullable
        @Override
        public TableCellRenderer getRenderer(Object o) {
            return MONGO_TABLE_CELL_RENDERER;
        }
    }
}
