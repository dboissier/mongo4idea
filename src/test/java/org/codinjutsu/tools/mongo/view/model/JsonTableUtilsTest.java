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