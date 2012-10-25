/*
 * Copyright (c) 2012 David Boissier
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
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.uispec4j.DefaultTreeCellValueConverter;
import org.uispec4j.Panel;
import org.uispec4j.Tree;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import java.awt.*;

public class MongoRunnerPanelTest extends UISpecTestCase {


    private MongoRunnerPanel mongoRunnerPanel;
    private Panel uiSpecPanel;


    public void testDisplaySingleJsonObject() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("MongoRunnerPanelTest_testDisplaySingleJsonObject.json")));

        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult();
        mongoCollectionResult.add(jsonObject);
        mongoRunnerPanel.showResults(mongoCollectionResult);

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results #(bold)\n" +
                        "  [0] Object:{ \"clone_url\" : \"https://gi... #(bold)\n" +
                        "    clone_url:\"https://github.com/dboissier/mongo4idea.git\" #(bold)\n" +
                        "    forks_count:0 #(bold)\n" +
                        "    mirror_url:null #(bold)\n" +
                        "    has_wiki:false #(bold)\n" +
                        "    _links:{ \"self\" : { \"href\" : \"http... #(bold)\n" +
                        "      self:{ \"href\" : \"https://api.git... #(bold)\n" +
                        "        href:\"https://api.github.com/repos/dboissier/mongo4idea\" #(bold)\n"
        ).check();
    }

    public void testDisplayJsonObjects() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("MongoRunnerPanelTest_testDisplayJsonObjects.json")));

        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult();
        mongoCollectionResult.add(jsonObject);
        mongoRunnerPanel.showResults(mongoCollectionResult);

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results #(bold)\n" +
                        "  [0] Object:[ { \"clone_url\" : \"https://... #(bold)\n" +
                        "    clone_url:\"https://github.com/dboissier/mongo4idea.git\" #(bold)\n" +
                        "    _links:{ \"self\" : { \"href\" : \"http... #(bold)\n" +
                        "      self:{ \"href\" : \"https://api.git... #(bold)\n" +
                        "        href:\"https://api.github.com/repos/dboissier/mongo4idea\" #(bold)\n" +
                        "  [1] Object:[ { \"clone_url\" : \"https://... #(bold)\n" +
                        "    clone_url:\"https://github.com/dboissier/github-utils.git\" #(bold)"
        ).check();
    }

    public void testDisplayJsonArrays() throws Exception {
        DBObject jsonObject = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream("MongoRunnerPanelTest_testDisplayJsonWithArrays.json")));

        MongoCollectionResult mongoCollectionResult = new MongoCollectionResult();
        mongoCollectionResult.add(jsonObject);
        mongoRunnerPanel.showResults(mongoCollectionResult);

        Tree tree = uiSpecPanel.getTree();
        tree.setCellValueConverter(new TreeCellConverter());
        tree.contentEquals(
                "results #(bold)\n" +
                        "  [0] Object:[ { \"clone_url\" : \"https://... #(bold)\n" +
                        "    clone_url:\"https://github.com/dboissier/mongo4idea.git\" #(bold)\n" +
                        "    chaine:[ \"toto\" , \"pipo\" , \"zozo\"] #(bold)\n" +
                        "      :\"toto\" #(bold)\n" +
                        "      :\"pipo\" #(bold)\n" +
                        "      :\"zozo\" #(bold)\n" +
                        "  [1] Object:[ { \"clone_url\" : \"https://... #(bold)\n" +
                        "    clone_url:\"https://github.com/dboissier/github-utils.git\" #(bold)\n" +
                        "    booleen:[ true , false , true] #(bold)\n" +
                        "      :true #(bold)\n" +
                        "      :false #(bold)\n" +
                        "      :true #(bold)" +
                        "  [2] Object:[ { \"clone_url\" : \"https://... #(bold)\n" +
                        "    clone_url:\"https://github.com/dboissier/github-utils.git\" #(bold)\n" +
                        "    document:[ { \"titre\" : \"titre1\" , \"m... #(bold)\n" +
                        "      [0] Object " +
                        "        titre:\"titre1\" #(bold)\n" +
                        "        motscles:[ \"java\" , \"info\" , \"ejb\"] #(bold)\n" +
                        "          :\"java\" #(bold)\n" +
                        "          :\"info\" #(bold)\n" +
                        "          :\"ejb\" #(bold)\n" +
                        "      [1] Object { \"clone_url\" : \"https://... #(bold)" +
                        "        titre:\"titre2\" #(bold)\n" +
                        "        motscles:[ \"cinema\" , \"horreur\" , \"s... #(bold)\n" +
                        "          :\"cinema\" #(bold)\n" +
                        "          :\"horreur\" #(bold)\n" +
                        "          :\"slasher\" #(bold)\n" +
                        "      [2] Object { \"clone_url\" : \"https://... #(bold)" +
                        "        titre:\"titre3\" #(bold)\n" +
                        "        motscles:[ \"musique\" , \"rock\" , \"ann... #(bold)\n" +
                        "          :\"musique\" #(bold)\n" +
                        "          :\"rock\" #(bold)\n" +
                        "          :\"annees 70\" #(bold)"
        ).check();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mongoRunnerPanel = new MongoRunnerPanel();
        uiSpecPanel = new Panel(mongoRunnerPanel);
    }

    private static class TreeCellConverter extends DefaultTreeCellValueConverter {

        @Override
        protected JLabel getLabel(Component renderedComponent) {
            JsonTreeCellRenderer jsonTreeCellRenderer = (JsonTreeCellRenderer) renderedComponent;
            return new JLabel(jsonTreeCellRenderer.toString());
        }
    }
}
