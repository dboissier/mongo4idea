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
