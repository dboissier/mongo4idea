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

import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.mongo.view.BsonTest;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonTreeUtilsTest implements BsonTest {

    @Test
    public void buildDocumentFromSimpleTree() {
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
    public void buildDocumentFromTreeWithSubNodes() {
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
        Document document = buildDocument("simpleDocumentWithSubList.json");

        JsonTreeNode treeNode = (JsonTreeNode) JsonTreeUtils.buildJsonTree(document);
        JsonTreeNode tagsNode = (JsonTreeNode) treeNode.getChildAt(2);
        JsonTreeNode agileTagNode = (JsonTreeNode) tagsNode.getChildAt(2);
        agileTagNode.getDescriptor().setValue("a gilles");

        assertThat(JsonTreeUtils.buildDocumentObject(treeNode)).isEqualTo(
                new Document("_id", new ObjectId("50b8d63414f85401b9268b99"))
                        .append("title", "XP by example")
                        .append("tags", asList(
                                "pair programming", "tdd", "a gilles"
                        ))
                        .append("innerList",
                                asList(
                                        asList(1, 2, 3, 4),
                                        asList(false, true),
                                        asList(
                                                new Document("tagName", "pouet"),
                                                new Document("tagName", "paf"))
                                ))
        );
    }
}
