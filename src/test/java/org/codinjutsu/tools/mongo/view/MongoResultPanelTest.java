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
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.bson.Document;
import org.codinjutsu.tools.mongo.logic.Notifier;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

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
        MongoCollectionResult collectionResult = new MongoCollectionResult("mycollec");
        collectionResult.add(new Document("label", "a1"));
        collectionResult.add(new Document("label", "a2"));
        mongoResultPanel.updateResultView(collectionResult);

        getResultTable().requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "{ \"label\" : \"a1\" }"},
                        {"\"label\"", "a1"},
                        {"[1]", "{ \"label\" : \"a2\" }"},
                        {"\"label\"", "a2"},
                });
    }

    @Test
    public void testDisplayTreeWithASimpleDocument() throws Exception {
        mongoResultPanel.updateResultView(createCollectionResults("simpleDocument.json", "mycollec"));

        getResultTable().requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "{ \"id\" : 0, \"label\" : \"toto\", \"visible\" : false, \"image\" : null }"},
                        {"\"id\"", "0"},
                        {"\"label\"", "toto"},
                        {"\"visible\"", "false"},
                        {"\"image\"", "null"}
                });
    }


    @Test
    public void testDisplayTreeWithAStructuredDocument() throws Exception {
        MongoCollectionResult collectionResult = new MongoCollectionResult("mycollect");
        collectionResult.add(new Document("id", 0)
                .append("label", "toto")
                .append("visible", false)
                .append("doc", new Document("title", "hello")
                        .append("nbPages", 10)
                        .append("keyWords", Arrays.asList(
                                "toto", true, 10
                        )))
        );
        mongoResultPanel.updateResultView(collectionResult);
        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());
        getResultTable().requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "{ \"id\" : 0, \"label\" : \"toto\", \"visible\" : false, \"doc\" : { \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] } }"},
                        {"\"id\"", "0"},
                        {"\"label\"", "toto"},
                        {"\"visible\"", "false"},
                        {"\"doc\"", "{ \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] }"},
                        {"\"title\"", "hello"},
                        {"\"nbPages\"", "10"},
                        {"\"keyWords\"", "[\"toto\", true, 10]"},
                        {"[0]", "toto"},
                        {"[1]", "true"},
                        {"[2]", "10"},
                });
    }


    @Test
    public void testDisplayTreeWithAnArrayOfStructuredDocument() throws Exception {
        MongoCollectionResult collectionResult = new MongoCollectionResult("mycollect");
        collectionResult.add(new Document("id", 0)
                .append("label", "toto")
                .append("visible", false)
                .append("doc", new Document("title", "hello")
                        .append("nbPages", 10)
                        .append("keyWords", Arrays.asList(
                                "toto", true, 10
                        )))
        );
        collectionResult.add(new Document("id", 1)
                .append("label", "tata")
                .append("visible", false)
                .append("doc", new Document("title", "ola")
                        .append("nbPages", 1)
                        .append("keyWords", Arrays.asList(
                                "tutu", false, 10
                        )))
        );

        mongoResultPanel.updateResultView(collectionResult);

        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());
        getResultTable().requireContents(new String[][]{

                {"[0]", "{ \"id\" : 0, \"label\" : \"toto\", \"visible\" : false, \"doc\" : { \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] } }"},
                {"\"id\"", "0"},
                {"\"label\"", "toto"},
                {"\"visible\"", "false"},
                {"\"doc\"", "{ \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] }"},
                {"\"title\"", "hello"},
                {"\"nbPages\"", "10"},
                {"\"keyWords\"", "[\"toto\", true, 10]"},
                {"[0]", "toto"},
                {"[1]", "true"},
                {"[2]", "10"},
                {"[1]", "{ \"id\" : 1, \"label\" : \"tata\", \"visible\" : false, \"doc\" : { \"title\" : \"ola\", \"nbPages\" : 1, \"keyWords\" : [\"tutu\", false, 10] } }"},
                {"\"id\"", "1"},
                {"\"label\"", "tata"},
                {"\"visible\"", "false"},
                {"\"doc\"", "{ \"title\" : \"ola\", \"nbPages\" : 1, \"keyWords\" : [\"tutu\", false, 10] }"},
                {"\"title\"", "ola"},
                {"\"nbPages\"", "1"},
                {"\"keyWords\"", "[\"tutu\", false, 10]"},
                {"[0]", "tutu"},
                {"[1]", "false"},
                {"[2]", "10"},
        });
    }

    @Test
    public void copyMongoObjectNodeValue() throws Exception {
        MongoCollectionResult collectionResult = new MongoCollectionResult("mycollect");
        collectionResult.add(new Document("id", 0)
                .append("label", "toto")
                .append("visible", false)
                .append("doc", new Document("title", "hello")
                        .append("nbPages", 10)
                        .append("keyWords", Arrays.asList(
                                "toto", true, 10
                        )))
        );
        collectionResult.add(new Document("id", 1)
                .append("label", "tata")
                .append("visible", false)
                .append("doc", new Document("title", "ola")
                        .append("nbPages", 1)
                        .append("keyWords", Arrays.asList(
                                "tutu", false, 10
                        )))
        );
        mongoResultPanel.updateResultView(collectionResult);
        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());

        mongoResultPanel.resultTreeTableView.setRowSelectionInterval(0, 0);
        assertThat(mongoResultPanel.getSelectedNodeStringifiedValue())
                .isEqualTo("{ \"id\" : 0, \"label\" : \"toto\", \"visible\" : false, \"doc\" : { \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] } }");

        mongoResultPanel.resultTreeTableView.setRowSelectionInterval(2, 2);
        assertThat(mongoResultPanel.getSelectedNodeStringifiedValue())
                .isEqualTo("\"label\" : \"toto\"");

        mongoResultPanel.resultTreeTableView.setRowSelectionInterval(4, 4);
        assertThat(mongoResultPanel.getSelectedNodeStringifiedValue())
                .isEqualTo("\"doc\" : { \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] }");
    }

    @Test
    public void copyMongoResults() throws Exception {
        MongoCollectionResult collectionResult = new MongoCollectionResult("mycollect");
        collectionResult.add(new Document("id", 0)
                .append("label", "toto")
                .append("visible", false)
                .append("doc", new Document("title", "hello")
                        .append("nbPages", 10)
                        .append("keyWords", Arrays.asList(
                                "toto", true, 10
                        )))
        );
        collectionResult.add(new Document("id", 1)
                .append("label", "tata")
                .append("visible", false)
                .append("doc", new Document("title", "ola")
                        .append("nbPages", 1)
                        .append("keyWords", Arrays.asList(
                                "tutu", false, 10
                        )))
        );
        mongoResultPanel.updateResultView(collectionResult);

        TreeUtil.expandAll(mongoResultPanel.resultTreeTableView.getTree());

        getResultTable().requireContents(new String[][]{
                {"[0]", "{ \"id\" : 0, \"label\" : \"toto\", \"visible\" : false, \"doc\" : { \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] } }"},
                {"\"id\"", "0"},
                {"\"label\"", "toto"},
                {"\"visible\"", "false"},
                {"\"doc\"", "{ \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] }"},
                {"\"title\"", "hello"},
                {"\"nbPages\"", "10"},
                {"\"keyWords\"", "[\"toto\", true, 10]"},
                {"[0]", "toto"},
                {"[1]", "true"},
                {"[2]", "10"},
                {"[1]", "{ \"id\" : 1, \"label\" : \"tata\", \"visible\" : false, \"doc\" : { \"title\" : \"ola\", \"nbPages\" : 1, \"keyWords\" : [\"tutu\", false, 10] } }"},
                {"\"id\"", "1"},
                {"\"label\"", "tata"},
                {"\"visible\"", "false"},
                {"\"doc\"", "{ \"title\" : \"ola\", \"nbPages\" : 1, \"keyWords\" : [\"tutu\", false, 10] }"},
                {"\"title\"", "ola"},
                {"\"nbPages\"", "1"},
                {"\"keyWords\"", "[\"tutu\", false, 10]"},
                {"[0]", "tutu"},
                {"[1]", "false"},
                {"[2]", "10"},
        });
        assertThat(mongoResultPanel.getSelectedNodeStringifiedValue())
                .isEqualTo("[ " +
                        "{ \"id\" : 0, \"label\" : \"toto\", \"visible\" : false, \"doc\" : { \"title\" : \"hello\", \"nbPages\" : 10, \"keyWords\" : [\"toto\", true, 10] } }, " +
                        "{ \"id\" : 1, \"label\" : \"tata\", \"visible\" : false, \"doc\" : { \"title\" : \"ola\", \"nbPages\" : 1, \"keyWords\" : [\"tutu\", false, 10] } }" +
                        " ]");
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
}
