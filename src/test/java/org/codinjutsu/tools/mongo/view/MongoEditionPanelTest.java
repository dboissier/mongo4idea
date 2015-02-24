/*
 * Copyright (c) 2013 David Boissier
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
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.fest.swing.data.TableCell;
import org.fest.swing.driver.BasicJTableCellReader;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTableFixture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MongoEditionPanelTest {

    private MongoEditionPanel mongoEditionPanel;

    private FrameFixture frameFixture;
    private MongoPanel.MongoDocumentOperations mockMongoOperations = mock(MongoPanel.MongoDocumentOperations.class);
    private MongoResultPanel.ActionCallback mockActionCallback = mock(MongoResultPanel.ActionCallback.class);

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {

        mongoEditionPanel = GuiActionRunner.execute(new GuiQuery<MongoEditionPanel>() {
            protected MongoEditionPanel executeInEDT() {
                MongoEditionPanel panel = new MongoEditionPanel() {
                    @Override
                    void buildPopupMenu() {
                    }
                };
                return panel.init(mockMongoOperations, mockActionCallback);
            }
        });

        mongoEditionPanel.updateEditionTree(buildDocument("simpleDocument.json"));

        frameFixture = Containers.showInFrame(mongoEditionPanel);
    }

    @Test
    public void displayMongoDocumentInTheTreeTable() throws Exception {
        frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"\"_id\"", "50b8d63414f85401b9268b99"},
                        {"\"label\"", "toto"},
                        {"\"visible\"", "false"},
                        {"\"image\"", "null"}
                });
    }

    @Test
    public void editKeyWithStringValue() throws Exception {
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());

//        edit 'label' key
        editionTreeTable.enterValue(TableCell.row(1).column(1), "Hello");

        frameFixture.button("saveButton").click();

        ArgumentCaptor<DBObject> argument = ArgumentCaptor.forClass(DBObject.class);
        verify(mockMongoOperations).updateMongoDocument(argument.capture());

        Assert.assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"label\" : \"Hello\" , \"visible\" : false , \"image\" :  null }",
                argument.getValue().toString());

        verify(mockActionCallback, times(1)).onOperationSuccess(any(String.class));
    }

    @Test
    public void cancelEdition() throws Exception {
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());

//        edit 'label' key
        editionTreeTable.enterValue(TableCell.row(1).column(1), "Hello");

        frameFixture.button("cancelButton").click();
        verify(mockMongoOperations, times(0)).updateMongoDocument(any(DBObject.class));

        verify(mockActionCallback, times(1)).onOperationCancelled(any(String.class));
    }

    @Test
    public void addKeyWithSomeValue() throws Exception {
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());


        editionTreeTable.selectCell(TableCell.row(1).column(1));
        mongoEditionPanel.addKey("stringKey", "pouet");

        editionTreeTable.selectCell(TableCell.row(1).column(1));
        mongoEditionPanel.addKey("numberKey", "1.1");

        editionTreeTable.requireContents(new String[][]{
                {"\"_id\"", "50b8d63414f85401b9268b99"},
                {"\"label\"", "toto"},
                {"\"visible\"", "false"},
                {"\"image\"", "null"},
                {"\"stringKey\"", "pouet"},
                {"\"numberKey\"", "1.1"},
        });
    }

    @Test
    public void addValueInAList() throws Exception {

        mongoEditionPanel.updateEditionTree(buildDocument("simpleDocumentWithSubList.json"));
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());

        editionTreeTable.requireContents(new String[][]{
                {"\"_id\"", "50b8d63414f85401b9268b99"},
                {"\"title\"", "XP by example"},
                {"\"tags\"", "[ \"pair programming\" , \"tdd\" , \"agile\"]"},
                {"[0]", "pair programming"},
                {"[1]", "tdd"},
                {"[2]", "agile"},
                {"\"innerList\"", "[ [ 1 , 2 , 3 , 4] , [ false , true] , [ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]]"},
                {"[0]", "[ 1 , 2 , 3 , 4]"},
                {"[1]", "[ false , true]"},
                {"[2]", "[ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]"}});

        editionTreeTable.selectCell(TableCell.row(3).column(1));
        mongoEditionPanel.addValue("refactor");

        editionTreeTable.requireContents(new String[][]{
                {"\"_id\"", "50b8d63414f85401b9268b99"},
                {"\"title\"", "XP by example"},
                {"\"tags\"", "[ \"pair programming\" , \"tdd\" , \"agile\"]"},
                {"[0]", "pair programming"},
                {"[1]", "tdd"},
                {"[2]", "agile"},
                {"[3]", "refactor"},
                {"\"innerList\"", "[ [ 1 , 2 , 3 , 4] , [ false , true] , [ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]]"},
                {"[0]", "[ 1 , 2 , 3 , 4]"},
                {"[1]", "[ false , true]"},
                {"[2]", "[ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]"}});

    }

    private static class JsonTableCellReader extends BasicJTableCellReader {

        @Override
        public String valueAt(JTable table, int row, int column) {
            Object value = table.getValueAt(row, column);
            if (value instanceof MongoNodeDescriptor) {
                MongoNodeDescriptor nodeDescriptor = (MongoNodeDescriptor) value;
                return nodeDescriptor.getFormattedKey();
            } else {
                return value == null ? "null" : value.toString();
            }
        }

    }

    private DBObject buildDocument(String jsonFile) throws IOException {
        DBObject mongoDocument = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("model/" + jsonFile)));
        mongoDocument.put("_id", new ObjectId(String.valueOf(mongoDocument.get("_id"))));
        return mongoDocument;
    }
}
