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

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTreeUtilsTest {

    @Test
    public void buildDocumentFromSimpleTree() throws Exception {
        Document originalDocument =
                new Document("_id", new ObjectId("50b8d63414f85401b9268b99"))
                        .append("label", "toto")
                        .append("visible", false)
                        .append("image", null);

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(originalDocument);
        JsonTreeNode labelNode = (JsonTreeNode) treeNode.getChildAt(1);
        labelNode.getDescriptor().setValue("tata");

        assertThat(JsonTreeUtils.buildDocumentObject(treeNode)).isEqualTo(
                new Document("_id", new ObjectId("50b8d63414f85401b9268b99"))
                        .append("label", "tata")
                        .append("visible", false)
                        .append("image", null));
    }

    @Test
    public void buildDocumentFromTreeWithSubNodes() throws Exception {
        Document originalDocument =
                new Document("_id", new ObjectId("50b8d63414f85401b9268b99"))
                        .append("label", "toto")
                        .append("visible", false)
                        .append("image", null)
                        .append("innerdoc", new Document("title", "What?")
                                .append("numberOfPages", 52)
                                .append("soldOut", false));

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(originalDocument);

        JsonTreeNode innerDocNode = (JsonTreeNode) treeNode.getChildAt(4);
        JsonTreeNode soldOutNode = (JsonTreeNode) innerDocNode.getChildAt(2);
        soldOutNode.getDescriptor().setValue("false");

        assertThat(JsonTreeUtils.buildDocumentObject(treeNode)).isEqualTo(
                new Document("_id", new ObjectId("50b8d63414f85401b9268b99"))
                        .append("label", "toto")
                        .append("visible", false)
                        .append("image", null)
                        .append("innerdoc", new Document("title", "What?")
                                .append("numberOfPages", 52)
                                .append("soldOut", false))
        );
    }

    @Test
    public void buildDocumentFromTreeWithSubList() throws Exception {
        Document document = parseDocument("simpleDocumentWithSubList.json");

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(document);
        JsonTreeNode tagsNode = (JsonTreeNode) treeNode.getChildAt(2);
        JsonTreeNode agileTagNode = (JsonTreeNode) tagsNode.getChildAt(2);
        agileTagNode.getDescriptor().setValue("a gilles");

        assertThat(JsonTreeUtils.buildDocumentObject(treeNode)).isEqualTo(
                new Document("_id", new ObjectId("50b8d63414f85401b9268b99"))
                        .append("title", "XP by example")
                        .append("tags", Arrays.asList(
                                "pair programming", "tdd", "a gilles"
                        ))
                        .append("innerList",
                                Arrays.asList(
                                        Arrays.asList(1, 2, 3, 4),
                                        Arrays.asList(false, true),
                                        Arrays.asList(
                                                new Document("tagName", "pouet"),
                                                new Document("tagName", "paf"))
                                ))
        );
    }

    @Test
    public void getObjectIdFromANode() throws Exception {
        Document document = parseDocument("simpleDocumentWithInnerNodes.json");

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(document);
        JsonTreeNode objectIdNode = (JsonTreeNode) treeNode.getChildAt(0);
        assertThat(objectIdNode.getDescriptor().getFormattedKey()).isEqualTo("\"_id\"");

        assertThat(JsonTreeUtils.findObjectIdNode(treeNode)).isNull();
        assertThat(JsonTreeUtils.findObjectIdNode((JsonTreeNode) treeNode.getChildAt(0))).isEqualTo(objectIdNode);
    }

    @NotNull
    private Document parseDocument(String fileName) throws IOException {
        Document document = Document.parse(IOUtils.toString(getClass().getResourceAsStream(fileName)));
//        Hack to convert _id from string to ObjectId
        document.put("_id", new ObjectId(String.valueOf(document.get("_id"))));
        return document;
    }
}
