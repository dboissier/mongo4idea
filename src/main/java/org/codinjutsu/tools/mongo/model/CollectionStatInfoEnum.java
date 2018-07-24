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

package org.codinjutsu.tools.mongo.model;

import static org.codinjutsu.tools.mongo.model.StatInfoEntry.*;

public enum CollectionStatInfoEnum {

    size(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    count(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    avgObjSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    storageSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    capped(BOOLEAN_EXTRACTOR, BOOLEAN_DATA_BUILDER),
    nindexes(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    totalIndexSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    indexSizes(DOCUMENT_EXTRACTOR, EMPTY_DATA_BUILDER);

    private final DataExtractor dataExtractor;
    private final StatInfoEntry.DataBuilder dataBuilder;

    CollectionStatInfoEnum(DataExtractor dataExtractor, StatInfoEntry.DataBuilder dataBuilder) {
        this.dataExtractor = dataExtractor;
        this.dataBuilder = dataBuilder;
    }

    public DataExtractor getDataExtractor() {
        return dataExtractor;
    }

    public DataBuilder getDataBuilder() {
        return dataBuilder;
    }
}
