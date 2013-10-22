package org.codinjutsu.tools.mongo.view;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.fest.swing.driver.BasicJTableCellReader;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

public class MongoEditionPanelTest {

    private MongoEditionPanel mongoEditionPanel;

    private FrameFixture frameFixture;

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {
        mongoEditionPanel = GuiActionRunner.execute(new GuiQuery<MongoEditionPanel>() {
            protected MongoEditionPanel executeInEDT() {
                MongoEditionPanel panel = new MongoEditionPanel();
                panel.init(new MongoRunnerPanel.MongoDocumentOperations() {
                               @Override
                               public void updateMongoDocument(DBObject mongoDocument) {

                               }

                               @Override
                               public DBObject getMongoDocument(ObjectId objectId) {
                                   return new BasicDBObject();
                               }

                               @Override
                               public void deleteMongoDocument(ObjectId objectId) {

                               }
                           }, new MongoResultPanel.ActionCallback() {
                               @Override
                               public void afterOperation() {

                               }
                           }
                );

                return panel;
            }
        });

        frameFixture = Containers.showInFrame(mongoEditionPanel);
    }

    @Test
    public void updateEditionTree() throws Exception {
        DBObject mongoDocument = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("model/simpleDocument.json")));

        mongoDocument.put("_id", new ObjectId(String.valueOf(mongoDocument.get("_id"))));

        mongoEditionPanel.updateEditionTree(mongoDocument);

        frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"\"_id\"", "50b8d63414f85401b9268b99"},
                        {"\"label\"", "toto"},
                        {"\"visible\"", "false"},
                        {"\"image\"", "null"}
                });
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
}
