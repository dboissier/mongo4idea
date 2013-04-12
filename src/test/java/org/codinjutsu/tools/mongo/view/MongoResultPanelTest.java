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

import com.intellij.mock.MockProject;
import com.intellij.openapi.command.impl.DummyProject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.uispec4j.DefaultTreeCellValueConverter;
import org.uispec4j.Panel;
import org.uispec4j.Tree;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MongoResultPanelTest extends UISpecTestCase {


    private MongoResultPanel mongoResultPanel;
    private Panel uiSpecPanel;


    public void testDisplayTreeWithASimpleArray() throws Exception {
        mongoResultPanel.updateResultTree(createCollectionResults("simpleArray.json", "mycollec"));

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results of 'mycollec' #(bold)\n" +
                        "  [0] \"toto\" #(bold)\n" +
                        "  [1] true #(bold)\n" +
                        "  [2] 10 #(bold)\n" +
                        "  [3] null #(bold)\n"
        ).check();
    }


    public void testDisplayTreeWithASimpleDocument() throws Exception {
        mongoResultPanel.updateResultTree(createCollectionResults("simpleDocument.json", "mycollec"));

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results of 'mycollec' #(bold)\n" +
                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"image\" :  null } #(bold)\n" +
                        "    \"id\": 0 #(bold)\n" +
                        "    \"label\": \"toto\" #(bold)\n" +
                        "    \"visible\": false #(bold)\n" +
                        "    \"image\": null #(bold)\n"
        ).check();
    }


    public void testDisplayTreeWithAStructuredDocument() throws Exception {
        mongoResultPanel.updateResultTree(createCollectionResults("structuredDocument.json", "mycollec"));

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results of 'mycollec' #(bold)\n" +
                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
                        "    \"id\": 0 #(bold)\n" +
                        "    \"label\": \"toto\" #(bold)\n" +
                        "    \"visible\": false #(bold)\n" +
                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
                        "      \"title\": \"hello\" #(bold)\n" +
                        "      \"nbPages\": 10 #(bold)\n" +
                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
                        "        [0] \"toto\" #(bold)\n" +
                        "        [1] true #(bold)\n" +
                        "        [2] 10 #(bold)\n"
        ).check();
    }


    public void testDisplayTreeWithAnArrayOfStructuredDocument() throws Exception {
        mongoResultPanel.updateResultTree(createCollectionResults("arrayOfDocuments.json", "mycollec"));

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results of 'mycollec' #(bold)\n" +
                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
                        "    \"id\": 0 #(bold)\n" +
                        "    \"label\": \"toto\" #(bold)\n" +
                        "    \"visible\": false #(bold)\n" +
                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
                        "      \"title\": \"hello\" #(bold)\n" +
                        "      \"nbPages\": 10 #(bold)\n" +
                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
                        "        [0] \"toto\" #(bold)\n" +
                        "        [1] true #(bold)\n" +
                        "        [2] 10 #(bold)\n" +
                        "  [1] { \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}} #(bold)\n" +
                        "    \"id\": 1 #(bold)\n" +
                        "    \"label\": \"tata\" #(bold)\n" +
                        "    \"visible\": true #(bold)\n" +
                        "    \"doc\": { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]} #(bold)\n" +
                        "      \"title\": \"ola\" #(bold)\n" +
                        "      \"nbPages\": 1 #(bold)\n" +
                        "      \"keyWord\": [ \"tutu\" , false , 10] #(bold)\n" +
                        "        [0] \"tutu\" #(bold)\n" +
                        "        [1] false #(bold)\n" +
                        "        [2] 10 #(bold)\n"
        ).check();
    }

    public void testDisplayTreeSortedbyKey() throws Exception {
        mongoResultPanel.updateResultTree(createCollectionResults("structuredDocument.json", "mycollec"));

        mongoResultPanel.setSortedByKey(true);

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results of 'mycollec' #(bold)\n" +
                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
                        "        [0] \"toto\" #(bold)\n" +
                        "        [1] true #(bold)\n" +
                        "        [2] 10 #(bold)\n" +
                        "      \"nbPages\": 10 #(bold)\n" +
                        "      \"title\": \"hello\" #(bold)\n" +
                        "    \"id\": 0 #(bold)\n" +
                        "    \"label\": \"toto\" #(bold)\n" +
                        "    \"visible\": false #(bold)\n"
        ).check();


        mongoResultPanel.setSortedByKey(false);

        tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results of 'mycollec' #(bold)\n" +
                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
                        "    \"id\": 0 #(bold)\n" +
                        "    \"label\": \"toto\" #(bold)\n" +
                        "    \"visible\": false #(bold)\n" +
                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
                        "      \"title\": \"hello\" #(bold)\n" +
                        "      \"nbPages\": 10 #(bold)\n" +
                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
                        "        [0] \"toto\" #(bold)\n" +
                        "        [1] true #(bold)\n" +
                        "        [2] 10 #(bold)\n"
        ).check();
    }

    public void testCopyMongoObjectNodeValue() throws Exception {
        mongoResultPanel.updateResultTree(createCollectionResults("structuredDocument.json", "mycollec"));
        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.select("[0]");

        assertEquals("{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}", mongoResultPanel.getSelectedNodeStringifiedValue());

        tree.select("[0]/label");
        assertEquals("{ \"label\" : \"toto\"}", mongoResultPanel.getSelectedNodeStringifiedValue());

        tree.select("[0]/doc");
        assertEquals("{ \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}}", mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    public void testCopyMongoResults() throws Exception {
        mongoResultPanel.updateResultTree(createCollectionResults("arrayOfDocuments.json", "mycollec"));

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results of 'mycollec' #(bold)\n" +
                        "  [0] { \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} #(bold)\n" +
                        "    \"id\": 0 #(bold)\n" +
                        "    \"label\": \"toto\" #(bold)\n" +
                        "    \"visible\": false #(bold)\n" +
                        "    \"doc\": { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]} #(bold)\n" +
                        "      \"title\": \"hello\" #(bold)\n" +
                        "      \"nbPages\": 10 #(bold)\n" +
                        "      \"keyWord\": [ \"toto\" , true , 10] #(bold)\n" +
                        "        [0] \"toto\" #(bold)\n" +
                        "        [1] true #(bold)\n" +
                        "        [2] 10 #(bold)\n" +
                        "  [1] { \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}} #(bold)\n" +
                        "    \"id\": 1 #(bold)\n" +
                        "    \"label\": \"tata\" #(bold)\n" +
                        "    \"visible\": true #(bold)\n" +
                        "    \"doc\": { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]} #(bold)\n" +
                        "      \"title\": \"ola\" #(bold)\n" +
                        "      \"nbPages\": 1 #(bold)\n" +
                        "      \"keyWord\": [ \"tutu\" , false , 10] #(bold)\n" +
                        "        [0] \"tutu\" #(bold)\n" +
                        "        [1] false #(bold)\n" +
                        "        [2] 10 #(bold)\n"
        ).check();

        tree.selectRoot();

        assertEquals("[ " +
                "{ \"id\" : 0 , \"label\" : \"toto\" , \"visible\" : false , \"doc\" : { \"title\" : \"hello\" , \"nbPages\" : 10 , \"keyWord\" : [ \"toto\" , true , 10]}} , " +
                "{ \"id\" : 1 , \"label\" : \"tata\" , \"visible\" : true , \"doc\" : { \"title\" : \"ola\" , \"nbPages\" : 1 , \"keyWord\" : [ \"tutu\" , false , 10]}}" +
                " ]", mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mongoResultPanel = new MongoResultPanel(DummyProject.getInstance());
        uiSpecPanel = new Panel(mongoResultPanel);
    }

    private MongoCollectionResult createCollectionResults(String data, String collectionName) throws IOException {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream(data)));

        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult(collectionName);
        mongoCollectionResult.add(jsonObject);

        return mongoCollectionResult;
    }

    private static class TreeCellConverter extends DefaultTreeCellValueConverter {

        @Override
        protected JLabel getLabel(Component renderedComponent) {
            MongoResultCellRenderer mongoResultCellRenderer = (MongoResultCellRenderer) renderedComponent;
            return new JLabel(mongoResultCellRenderer.toString());
        }
    }
}
