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

public enum MongoAggregateOperator {

    MATCH("$match"),
    PROJECT("$project"),
    GROUP("$group"),
    SORT("$sort"),
    LIMIT("$limit"),
    SKIP("$skip"),
    UNWIND("$unwind");

    private final String operator;

    MongoAggregateOperator(String operator) {
        this.operator = operator;
    }

    public String getLabel() {
        return operator;
    }


    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
