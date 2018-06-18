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

package org.codinjutsu.tools.mongo.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

public class MongoQueryOptions {

    public static final int NO_LIMIT = 0;
    public static final int DEFAULT_RESULT_LIMIT = 300;

    public static final Document EMPTY_DOCUMENT = new Document();
    private final List<BasicDBObject> operations = new LinkedList<>();

    private Document filter = EMPTY_DOCUMENT;
    private Document projection = EMPTY_DOCUMENT;
    private Document sort = EMPTY_DOCUMENT;

    private int resultLimit = DEFAULT_RESULT_LIMIT;

    public boolean isAggregate() {
        return !operations.isEmpty();
    }

    public List<BasicDBObject> getOperations() {
        return operations;
    }

    public void setOperations(String aggregateQuery) {
        operations.clear();
        BasicDBList operations = (BasicDBList) JSON.parse(aggregateQuery);
        for (Object operation1 : operations) {
            BasicDBObject operation = (BasicDBObject) operation1;
            this.operations.add(operation);
        }
    }

    public void setFilter(String query) {
        if (!StringUtils.isBlank(query)) {
            filter = Document.parse(query);
        }
    }

    public MongoQueryOptions setFilter(Document filter) {
        this.filter = filter;
        return this;
    }

    public Document getFilter() {
        return filter;
    }


    public void setProjection(String query) {
        if (!StringUtils.isBlank(query)) {
            projection = Document.parse(query);
        }
    }

    public Document getProjection() {
        return projection;
    }

    public void setSort(String query) {
        if (!StringUtils.isBlank(query)) {
            sort = Document.parse(query);
        }
    }

    public Document getSort() {
        return sort;
    }

    public int getResultLimit() {
        return resultLimit;
    }

    public void setResultLimit(int resultLimit) {
        this.resultLimit = resultLimit;
    }
}
