package org.codinjutsu.tools.mongo.view;

import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.codinjutsu.tools.mongo.model.StatInfoEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MongoInfosTable extends TableView<StatInfoEntry> {

    private static final ColumnInfo[] COLUMN_INFOS = {new KeyColumnInfo(), new ValueColumnInfo()};

    MongoInfosTable() {
        super(new ListTableModel<>(COLUMN_INFOS));
    }

    public void updateInfos(List<StatInfoEntry> collectionInfoEntries) {
        ((ListTableModel) getModel()).setItems(collectionInfoEntries);
    }

    private static class KeyColumnInfo extends ColumnInfo<StatInfoEntry, String> {

        public KeyColumnInfo() {
            super("Property");
        }

        @Nullable
        @Override
        public String valueOf(StatInfoEntry statInfoEntry) {
            return statInfoEntry.getKey();
        }
    }

    private static class ValueColumnInfo extends ColumnInfo<StatInfoEntry, String> {

        public ValueColumnInfo() {
            super("Value");
        }

        @Nullable
        @Override
        public String valueOf(StatInfoEntry statInfoEntry) {
            return statInfoEntry.getStringifiedValue();
        }
    }
}
