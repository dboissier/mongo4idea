package org.codinjutsu.tools.mongo.model;

import static org.codinjutsu.tools.mongo.model.StatInfoEntry.*;

public enum CollectionStatInfoEnum {

    size(NUMBER_EXTRACTOR, BYTE_SIZE_DATA_BUILDER),
    count(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
    avgObjSize(NUMBER_EXTRACTOR, NUMBER_DATA_BUILDER),
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
