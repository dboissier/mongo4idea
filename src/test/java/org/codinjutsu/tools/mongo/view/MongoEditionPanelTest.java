package org.codinjutsu.tools.mongo.view;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.fest.swing.data.TableCell;
import org.fest.swing.driver.BasicJTableCellReader;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

public class MongoEditionPanelTest {

    private MongoEditionPanel mongoEditionPanel;

    private FrameFixture frameFixture;
    private MyMongoDocumentOperations mongoDocumentOperations;
    private MyActionCallback actionCallback;

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {

        mongoDocumentOperations = new MyMongoDocumentOperations();
        actionCallback = new MyActionCallback();

        mongoEditionPanel = GuiActionRunner.execute(new GuiQuery<MongoEditionPanel>() {
            protected MongoEditionPanel executeInEDT() {

                MongoEditionPanel panel = new MongoEditionPanel() {
                    @Override
                    void buildPopupMenu() {

                    }
                };
                return panel.init(mongoDocumentOperations, actionCallback);
            }
        });

        DBObject mongoDocument = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("model/simpleDocument.json")));

        mongoDocument.put("_id", new ObjectId(String.valueOf(mongoDocument.get("_id"))));

        mongoEditionPanel.updateEditionTree(mongoDocument);

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

        Assert.assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"label\" : \"Hello\" , \"visible\" : false , \"image\" :  null }",
                mongoDocumentOperations.getUpdatedMongoDocument().toString());

        Assert.assertEquals("call afterOperation", actionCallback.logString);
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

    private static class MyMongoDocumentOperations implements MongoRunnerPanel.MongoDocumentOperations {

        private DBObject mongoDocument = null;

        @Override
        public void updateMongoDocument(DBObject mongoDocument) {
            this.mongoDocument = mongoDocument;
        }

        @Override
        public DBObject getMongoDocument(ObjectId objectId) {
            return new BasicDBObject();
        }

        @Override
        public void deleteMongoDocument(ObjectId objectId) {

        }

        private DBObject getUpdatedMongoDocument() {
            return mongoDocument;
        }
    }

    private static class MyActionCallback implements MongoResultPanel.ActionCallback {

        String logString ="";

        @Override
        public void afterOperation() {
            logString = "call afterOperation";
        }
    }
}
