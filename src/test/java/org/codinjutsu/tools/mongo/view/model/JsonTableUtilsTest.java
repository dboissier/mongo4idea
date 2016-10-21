package org.codinjutsu.tools.mongo.view.model;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonTableUtilsTest {

    @Test
    public void buildJsonTable() throws Exception {
        BasicDBList dbList = (BasicDBList) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("arrayOfDocuments.json")));

        MongoCollectionResult result = new MongoCollectionResult("dummyCollection");
        for (Object dbObject : dbList) {
            result.add((DBObject) dbObject);
        }

        ListTableModel tableModel = JsonTableUtils.buildJsonTable(result);

        assertNotNull(tableModel);

        ColumnInfo[] columnInfos = tableModel.getColumnInfos();
        assertEquals(4, columnInfos.length);
        assertEquals("_id", columnInfos[0].getName());
        assertEquals("label", columnInfos[1].getName());
        assertEquals("visible", columnInfos[2].getName());
        assertEquals("doc", columnInfos[3].getName());

        assertEquals(2, tableModel.getRowCount());

        DBObject item = (DBObject) tableModel.getItem(0);
        assertEquals("50b8d63414f85401b9268b99", item.get("_id"));
        assertEquals("toto", item.get("label"));
        assertEquals(false, item.get("visible"));
        BasicDBList docElement = new BasicDBList();
        docElement.addAll(Arrays.asList("toto", true, 10));
        assertEquals(new BasicDBObject().append("title", "hello")
                .append("nbPages", 10)
                .append("keyWord", docElement), item.get("doc"));
    }

}