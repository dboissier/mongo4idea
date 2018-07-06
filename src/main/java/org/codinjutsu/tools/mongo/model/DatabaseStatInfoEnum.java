package org.codinjutsu.tools.mongo.model;

import static org.codinjutsu.tools.mongo.model.StatInfoEntry.*;

public enum DatabaseStatInfoEnum {

    collections(INTEGER_EXTRACTOR, INTEGER_DATA_BUILDER),
    views,
    objects,
    avgObjSize(DOUBLE_EXTRACTOR, BYTE_SIZE_DOUBLE_DATA_BUILDER),
    dataSize(DOUBLE_EXTRACTOR, BYTE_SIZE_DOUBLE_DATA_BUILDER),
    storageSize(DOUBLE_EXTRACTOR, BYTE_SIZE_DOUBLE_DATA_BUILDER),
    numExtents,
    indexes,
    indexSize(DOUBLE_EXTRACTOR, BYTE_SIZE_DOUBLE_DATA_BUILDER),
    fsUsedSize(DOUBLE_EXTRACTOR, BYTE_SIZE_DOUBLE_DATA_BUILDER),
    fsTotalSize(DOUBLE_EXTRACTOR, BYTE_SIZE_DOUBLE_DATA_BUILDER);

    private final StatInfoEntry.DataExtractor dataExtractor;
    private final StatInfoEntry.DataBuilder dataBuilder;

    DatabaseStatInfoEnum() {
        dataExtractor = INTEGER_EXTRACTOR;
        dataBuilder = INTEGER_DATA_BUILDER;
    }

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
