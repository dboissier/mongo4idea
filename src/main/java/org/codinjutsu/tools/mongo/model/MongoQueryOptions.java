/*
 * Copyright (c) 2012 David Boissier
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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MongoQueryOptions {

    public static final int DEFAULT_RESULT_LIMIT = 300;

    private static final BasicDBObject EMPTY_FILTER = new BasicDBObject();
    private final List<DBObject> operations = new LinkedList<DBObject>();

    private DBObject filter = EMPTY_FILTER;

    private int resultLimit = DEFAULT_RESULT_LIMIT;

    public boolean isAggregate() {
        return !operations.isEmpty();
    }


    public void addQuery(MongoAggregateOperator operator, String query) {
        if (!StringUtils.isBlank(query)) {
            DBObject operationObject = (DBObject) JSON.parse(query);

            operations.add(new BasicDBObject(operator.getLabel(), operationObject));
        }
    }

    public DBObject getFirstOperation() {
        if (isAggregate()) {
            return operations.get(0);
        }
        return null;
    }

    public List<DBObject> getOperationsExceptTheFirst() {
        if (operations.size() > 1) {
            return operations.subList(1, operations.size());
        }

        return Collections.emptyList();
    }

    public void setFilter(String query) {
        if (!StringUtils.isBlank(query)) {
            filter = (DBObject) JSON.parse(query);
        }
    }

    public DBObject getFilter() {
        return filter;
    }

    public List<DBObject> getAllOperations() {
        return operations;
    }

    public int getResultLimit() {
        return resultLimit;
    }

    public void setResultLimit(int resultLimit) {
        this.resultLimit = resultLimit;
    }
}
