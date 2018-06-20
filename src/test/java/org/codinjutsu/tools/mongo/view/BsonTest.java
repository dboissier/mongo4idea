package org.codinjutsu.tools.mongo.view;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;

public interface BsonTest {

    default Document buildDocument(String jsonFile) throws IOException {
        Document document = Document.parse(IOUtils.toString(getClass().getResourceAsStream(jsonFile)));
        document.put("_id", new ObjectId(String.valueOf(document.get("_id"))));
        return document;
    }
}
