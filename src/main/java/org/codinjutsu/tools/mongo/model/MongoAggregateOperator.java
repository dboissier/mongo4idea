/*
 * Copyright (c) 2013 David Boissier
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

    MATCH("$match", OperatorValueConverter.jSONValueConverter),
    PROJECT("$project", OperatorValueConverter.jSONValueConverter),
    GROUP("$group", OperatorValueConverter.jSONValueConverter),
    SORT("$sort", OperatorValueConverter.jSONValueConverter),
    LIMIT("$limit", OperatorValueConverter.integerValueConverter),
    SKIP("$skip", OperatorValueConverter.integerValueConverter),
    UNWIND("$unwind", OperatorValueConverter.stringValueConverter);

    private final String operator;
    private final OperatorValueConverter operatorValueConverter;

    private MongoAggregateOperator(String operator, OperatorValueConverter operatorValueConverter) {
        this.operator = operator;
        this.operatorValueConverter = operatorValueConverter;
    }

    public String getLabel() {
        return operator;
    }

    public OperatorValueConverter getOperatorValueConverter() {
        return operatorValueConverter;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }


}
