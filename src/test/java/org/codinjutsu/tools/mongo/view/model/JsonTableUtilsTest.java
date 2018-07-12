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
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTableUtilsTest {

    @Test
    public void buildJsonTable() throws Exception {
        Document document1 = Document.parse(IOUtils.toString(getClass().getResourceAsStream("/testData/JsonTableUtilsTest_document1.json")));
        Document document2 = Document.parse(IOUtils.toString(getClass().getResourceAsStream("/testData/JsonTableUtilsTest_document2.json")));

        MongoCollectionResult result = new MongoCollectionResult("collectionForTest");
        result.add(document1);
        result.add(document2);

        ListTableModel tableModel = JsonTableUtils.buildJsonTable(result);

        assertThat(tableModel).isNotNull();

        ColumnInfo[] columnInfos = tableModel.getColumnInfos();
        assertThat(columnInfos.length).isEqualTo(4);
        assertThat(columnInfos[0].getName()).isEqualTo("_id");
        assertThat(columnInfos[1].getName()).isEqualTo("label");
        assertThat(columnInfos[2].getName()).isEqualTo("visible");
        assertThat(columnInfos[3].getName()).isEqualTo("doc");

        assertThat(tableModel.getRowCount()).isEqualTo(2);

        Document item = (Document) tableModel.getItem(0);
        assertThat(item.get("_id")).isEqualTo("50b8d63414f85401b9268b99");
        assertThat(item.get("label")).isEqualTo("toto");
        assertThat(item.get("visible")).isEqualTo(false);

        assertThat(item.get("doc")).isEqualTo(
                new Document("title", "hello")
                        .append("nbPages", 10)
                        .append("keyWord", Arrays.asList("toto", true, 10))
        );
    }

}