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

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.io.IOUtils;
import org.assertj.swing.driver.BasicJTableCellReader;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.bson.Document;
import org.codinjutsu.tools.mongo.logic.Notifier;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MongoResultPanelTest {

    private MongoResultPanel mongoResultPanel;

    private FrameFixture frameFixture;

    private Notifier notifierMock = Mockito.mock(Notifier.class);

    @Mock
    private MongoPanel.MongoDocumentOperations mongoDocumentOperations;

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(MongoResultPanelTest.class);

        mongoResultPanel = GuiActionRunner.execute(new GuiQuery<MongoResultPanel>() {
            protected MongoResultPanel executeInEDT() {
                return new MongoResultPanel(DummyProject.getInstance(), mongoDocumentOperations, notifierMock) {
                    @Override
                    void buildPopupMenu() {
                    }
                };
            }
        });

        frameFixture = Containers.showInFrame(mongoResultPanel);
    }

    @Test
    public void displayTreeWithASimpleArray() throws Exception {
        mongoResultPanel.updateResultView(createCollectionResults("simpleArray.json", "mycollec"));

        getResultTable().requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "\"toto\""},
                        {"[1]", "true"},
                        {"[2]", "10"},
                        {"[3]", "null"},
                });
    }

    @Test
    public void testDisplayTreeWithASimpleDocument() throws Exception {
        mongoResultPanel.updateResultView(createCollectionResults("simpleDocument.json", "mycollec"));

        getResultTable().requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "{ \"id\" : 0, \"label\" : \"toto\", \"visible\" : false, \"image\" : null }"},
                        {"\"id\"", "0"},
                        {"\"label\"", "\"toto\""},
                        {"\"visible\"", "false"},
                        {"\"image\"", "null"}
                });
    }


    @Test
    public void testDisplayTreeWithAStructuredDocument() throws Exception {
        mongoResultPanel.updateResultView(createCollectionResults("structuredDocument.json", "mycollec"));
        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());
        getResultTable().requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}"},
                        {"\"id\"", "0"},
                        {"\"label\"", "\"toto\""},
                        {"\"visible\"", "false"},
                        {"\"doc\"", "{ \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}"},
                        {"\"title\"", "\"hello\""},
                        {"\"nbPages\"", "10"},
                        {"\"keyWord\"", "[ \"toto\" , true , 10]"},
                        {"[0]", "\"toto\""},
                        {"[1]", "true"},
                        {"[2]", "10"},
                });
    }


    @Test
    public void testDisplayTreeWithAnArrayOfStructuredDocument() throws Exception {
        mongoResultPanel.updateResultView(createCollectionResults("arrayOfDocuments.json", "mycollec"));

        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());
        getResultTable().requireContents(new String[][]{

                {"[0]", "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}"},
                {"\"id\"", "0"},
                {"\"label\"", "\"toto\""},
                {"\"visible\"", "false"},
                {"\"doc\"", "{ \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}"},
                {"\"title\"", "\"hello\""},
                {"\"nbPages\"", "10"},
                {"\"keyWord\"", "[ \"toto\" , true , 10]"},
                {"[0]", "\"toto\""},
                {"[1]", "true"},
                {"[2]", "10"},
                {"[1]", "{ \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}}"},
                {"\"id\"", "1"},
                {"\"label\"", "\"tata\""},
                {"\"visible\"", "true"},
                {"\"doc\"", "{ \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}"},
                {"\"title\"", "\"ola\""},
                {"\"nbPages\"", "1"},
                {"\"keyWord\"", "[ \"tutu\" , false , 10]"},
                {"[0]", "\"tutu\""},
                {"[1]", "false"},
                {"[2]", "10"},
        });
    }

    @Test
    public void testCopyMongoObjectNodeValue() throws Exception {
        mongoResultPanel.updateResultView(createCollectionResults("structuredDocument.json", "mycollec"));
        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());

        mongoResultPanel.resultTreeTableView.setRowSelectionInterval(0, 0);
        assertEquals("{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}", mongoResultPanel.getSelectedNodeStringifiedValue());

        mongoResultPanel.resultTreeTableView.setRowSelectionInterval(2, 2);
        assertEquals("\"label\" : \"toto\"", mongoResultPanel.getSelectedNodeStringifiedValue());

        mongoResultPanel.resultTreeTableView.setRowSelectionInterval(4, 4);
        assertEquals("\"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}", mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    @Test
    public void copyMongoResults() throws Exception {
        mongoResultPanel.updateResultView(createCollectionResults("arrayOfDocuments.json", "mycollec"));

        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());

        getResultTable().requireContents(new String[][]{
                {"[0]", "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}"},
                {"\"id\"", "0"},
                {"\"label\"", "\"toto\""},
                {"\"visible\"", "false"},
                {"\"doc\"", "{ \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}"},
                {"\"title\"", "\"hello\""},
                {"\"nbPages\"", "10"},
                {"\"keyWord\"", "[ \"toto\" , true , 10]"},
                {"[0]", "\"toto\""},
                {"[1]", "true"},
                {"[2]", "10"},
                {"[1]", "{ \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}}"},
                {"\"id\"", "1"},
                {"\"label\"", "\"tata\""},
                {"\"visible\"", "true"},
                {"\"doc\"", "{ \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}"},
                {"\"title\"", "\"ola\""},
                {"\"nbPages\"", "1"},
                {"\"keyWord\"", "[ \"tutu\" , false , 10]"},
                {"[0]", "\"tutu\""},
                {"[1]", "false"},
                {"[2]", "10"},
        });

        assertEquals("[ " +
                        "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} , " +
                        "{ \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}}" +
                        " ]",
                mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    private MongoCollectionResult createCollectionResults(String data, String collectionName) throws IOException {
        Document jsonObject = Document.parse(IOUtils.toString(getClass().getResourceAsStream(data)));

        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(collectionName);
        mongoCollectionResult.add(jsonObject);

        return mongoCollectionResult;
    }

    @NotNull
    private JTableFixture getResultTable() {
        JTableFixture tableFixture = frameFixture.table("resultTreeTable");
        tableFixture.replaceCellReader(new JsonTableCellReader());
        return tableFixture;
    }

    private static class JsonTableCellReader extends BasicJTableCellReader {

        @Override
        public String valueAt(JTable table, int row, int column) {
            MongoNodeDescriptor nodeDescriptor = (MongoNodeDescriptor) table.getValueAt(row, column);
            if (column == 0) {
                return nodeDescriptor.getFormattedKey();
            }
            return nodeDescriptor.getFormattedValue();
        }
    }
}
