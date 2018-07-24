/*
 * Copyright (c) 2018 David Boissier.
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
