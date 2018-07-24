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

public enum DatabaseStatInfoEnum {

    collections(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    views(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    objects(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    avgObjSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    dataSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    storageSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    numExtents(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    indexes(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    indexSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    fsUsedSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    fsTotalSize(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER);

    private final StatInfoEntry.DataExtractor dataExtractor;
    private final StatInfoEntry.DataBuilder dataBuilder;

    DatabaseStatInfoEnum(StatInfoEntry.DataExtractor dataExtractor, StatInfoEntry.DataBuilder dataBuilder) {
        this.dataExtractor = dataExtractor;
        this.dataBuilder = dataBuilder;
    }

    public StatInfoEntry.DataExtractor getDataExtractor() {
        return dataExtractor;
    }

    public StatInfoEntry.DataBuilder getDataBuilder() {
        return dataBuilder;
    }
}
