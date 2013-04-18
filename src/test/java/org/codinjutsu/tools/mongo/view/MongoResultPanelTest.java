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

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MongoResultPanelTest {

    private MongoResultPanel mongoResultPanel;

    private FrameFixture frameFixture;

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {

        mongoResultPanel = GuiActionRunner.execute(new GuiQuery<MongoResultPanel>() {
            protected MongoResultPanel executeInEDT() {
                return new MongoResultPanel(DummyProject.getInstance());
            }
        });

        frameFixture = Containers.showInFrame(mongoResultPanel);
    }

    @Test
    public void displayTreeWithASimpleArray() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("simpleArray.json", "mycollec"));

        frameFixture.table().cellReader(new MyJTableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "toto"},
                        {"[1]", "true"},
                        {"[2]", "10"},
                        {"[3]", "null"},
                });

    }

    @Test
    public void testDisplayTreeWithASimpleDocument() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("simpleDocument.json", "mycollec"));

        frameFixture.table().cellReader(new MyJTableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"image\" :  null }"},
                        {"\"id\"", "{ \"id\" : 0}"},
                        {"\"label\"", "{ \"label\" : \"toto\"}"},
                        {"\"visible\"", "{ \"visible\" : false}"},
                        {"\"image\"", "{ \"image\" : null}"}
                });
    }


    @Test
    public void testDisplayTreeWithAStructuredDocument() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("structuredDocument.json", "mycollec"));
        TreeUtil.expandAll(mongoResultPanel.jsonTreeTableView.getTree());
        frameFixture.table().cellReader(new MyJTableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}"},
                        {"\"id\"", "{ \"id\" : 0}"},
                        {"\"label\"", "{ \"label\" : \"toto\"}"},
                        {"\"visible\"", "{ \"visible\" : false}"},
                        {"\"doc\"", "{ \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}"},
                        {"\"title\"", "{ \"title\" : \"hello\"}"},
                        {"\"nbPages\"", "{ \"nbPages\" : 10}"},
                        {"\"keyWord\"", "{ \"keyWord\" : [ \"toto\" , true , 10]}"},
                        {"[0]", "toto"},
                        {"[1]", "true"},
                        {"[2]", "10"},
                });
    }

    //
//    @Test
//    public void testDisplayTreeWithAnArrayOfStructuredDocument() throws Exception {
//        mongoResultPanel.updateResultTableTree(createCollectionResults("arrayOfDocuments.json", "mycollec"));
//
//        Tree tree = uiSpecPanel.getTree();
//        tree.setCellValueConverter(new TreeCellConverter());
//        tree.contentEquals(
//                "results of 'mycollec' #(bold)\n" +
//                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
//                        "    \"id\": 0 #(bold)\n" +
//                        "    \"label\": \"toto\" #(bold)\n" +
//                        "    \"visible\": false #(bold)\n" +
//                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
//                        "      \"title\": \"hello\" #(bold)\n" +
//                        "      \"nbPages\": 10 #(bold)\n" +
//                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
//                        "        [0] \"toto\" #(bold)\n" +
//                        "        [1] true #(bold)\n" +
//                        "        [2] 10 #(bold)\n" +
//                        "  [1] { \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}} #(bold)\n" +
//                        "    \"id\": 1 #(bold)\n" +
//                        "    \"label\": \"tata\" #(bold)\n" +
//                        "    \"visible\": true #(bold)\n" +
//                        "    \"doc\": { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]} #(bold)\n" +
//                        "      \"title\": \"ola\" #(bold)\n" +
//                        "      \"nbPages\": 1 #(bold)\n" +
//                        "      \"keyWord\": [ \"tutu\" , false , 10] #(bold)\n" +
//                        "        [0] \"tutu\" #(bold)\n" +
//                        "        [1] false #(bold)\n" +
//                        "        [2] 10 #(bold)\n"
//        ).check();
//    }
//
//    @Test
//    public void testDisplayTreeSortedbyKey() throws Exception {
//        mongoResultPanel.updateResultTableTree(createCollectionResults("structuredDocument.json", "mycollec"));
//
//        mongoResultPanel.setSortedByKey(true);
//
//        Tree tree = uiSpecPanel.getTree();
//        tree.setCellValueConverter(new TreeCellConverter());
//        tree.contentEquals(
//                "results of 'mycollec' #(bold)\n" +
//                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
//                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
//                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
//                        "        [0] \"toto\" #(bold)\n" +
//                        "        [1] true #(bold)\n" +
//                        "        [2] 10 #(bold)\n" +
//                        "      \"nbPages\": 10 #(bold)\n" +
//                        "      \"title\": \"hello\" #(bold)\n" +
//                        "    \"id\": 0 #(bold)\n" +
//                        "    \"label\": \"toto\" #(bold)\n" +
//                        "    \"visible\": false #(bold)\n"
//        ).check();
//
//
//        mongoResultPanel.setSortedByKey(false);
//
//        tree = uiSpecPanel.getTree();
//        tree.setCellValueConverter(new TreeCellConverter());
//        tree.contentEquals(
//                "results of 'mycollec' #(bold)\n" +
//                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
//                        "    \"id\": 0 #(bold)\n" +
//                        "    \"label\": \"toto\" #(bold)\n" +
//                        "    \"visible\": false #(bold)\n" +
//                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
//                        "      \"title\": \"hello\" #(bold)\n" +
//                        "      \"nbPages\": 10 #(bold)\n" +
//                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
//                        "        [0] \"toto\" #(bold)\n" +
//                        "        [1] true #(bold)\n" +
//                        "        [2] 10 #(bold)\n"
//        ).check();
//    }
//
    @Test
    public void testCopyMongoObjectNodeValue() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("structuredDocument.json", "mycollec"));
        TreeUtil.expandAll(mongoResultPanel.jsonTreeTableView.getTree());

        mongoResultPanel.jsonTreeTableView.setRowSelectionInterval(0, 0);
        assertEquals("{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}", mongoResultPanel.getSelectedNodeStringifiedValue());

        mongoResultPanel.jsonTreeTableView.setRowSelectionInterval(2, 2);
        assertEquals("{ \"label\" : \"toto\"}", mongoResultPanel.getSelectedNodeStringifiedValue());

        mongoResultPanel.jsonTreeTableView.setRowSelectionInterval(4, 4);
        assertEquals("{ \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}", mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    @Test
    public void copyMongoResults() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("arrayOfDocuments.json", "mycollec"));

        TreeUtil.expandAll(mongoResultPanel.jsonTreeTableView.getTree());

//        Tree tree = uiSpecPanel.getTree();
//        tree.setCellValueConverter(new TreeCellConverter());
//        tree.contentEquals(
//                "results of 'mycollec' #(bold)\n" +
//                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
//                        "    \"id\": 0 #(bold)\n" +
//                        "    \"label\": \"toto\" #(bold)\n" +
//                        "    \"visible\": false #(bold)\n" +
//                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
//                        "      \"title\": \"hello\" #(bold)\n" +
//                        "      \"nbPages\": 10 #(bold)\n" +
//                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
//                        "        [0] \"toto\" #(bold)\n" +
//                        "        [1] true #(bold)\n" +
//                        "        [2] 10 #(bold)\n" +
//                        "  [1] { \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}} #(bold)\n" +
//                        "    \"id\": 1 #(bold)\n" +
//                        "    \"label\": \"tata\" #(bold)\n" +
//                        "    \"visible\": true #(bold)\n" +
//                        "    \"doc\": { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]} #(bold)\n" +
//                        "      \"title\": \"ola\" #(bold)\n" +
//                        "      \"nbPages\": 1 #(bold)\n" +
//                        "      \"keyWord\": [ \"tutu\" , false , 10] #(bold)\n" +
//                        "        [0] \"tutu\" #(bold)\n" +
//                        "        [1] false #(bold)\n" +
//                        "        [2] 10 #(bold)\n"
//        ).check();

        assertEquals("[ " +
                "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} , " +
                "{ \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}}" +
                " ]",
                mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    private MongoCollectionResult createCollectionResults(String data, String collectionName) throws IOException {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream(data)));

        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(collectionName);
        mongoCollectionResult.add(jsonObject);

        return mongoCollectionResult;
    }

    public class MyJTableCellReader extends BasicJTableCellReader {

        @Override
        public String valueAt(JTable table, int row, int column) {
            MongoNodeDescriptor nodeDescriptor = (MongoNodeDescriptor) table.getValueAt(row, column);
            if (column == 0) {
                return nodeDescriptor.getNodeText();
            }
            return nodeDescriptor.toString();
        }
    }
}
