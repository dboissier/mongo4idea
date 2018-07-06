package org.codinjutsu.tools.mongo.model;

import static org.codinjutsu.tools.mongo.model.StatInfoEntry.*;

public enum CollectionStatInfoEnum {

    size(INTEGER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    count,
    avgObjSize,
    storageSize(INTEGER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    capped(BOOLEAN_EXTRACTOR, BOOLEAN_DATA_BUILDER),
    nindexes,
    totalIndexSize(INTEGER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    indexSizes(DOCUMENT_EXTRACTOR, EMPTY_DATA_BUILDER);

    private final DataExtractor dataExtractor;
    private final StatInfoEntry.DataBuilder dataBuilder;

    CollectionStatInfoEnum(DataExtractor dataExtractor, StatInfoEntry.DataBuilder dataBuilder) {
        this.dataExtractor = dataExtractor;
        this.dataBuilder = dataBuilder;
    }

    CollectionStatInfoEnum() {
        dataExtractor = INTEGER_EXTRACTOR;
        dataBuilder = INTEGER_DATA_BUILDER;
    }

    public DataExtractor getDataExtractor() {
        return dataExtractor;
    }

    public DataBuilder getDataBuilder() {
        return dataBuilder;
    }
}
