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

import org.jetbrains.annotations.NotNull;

public class MongoCollection implements Comparable<MongoCollection> {

    private final String name;
    private final String databaseName;

    public MongoCollection(String name, String databaseName) {
        this.name = name;
        this.databaseName = databaseName;
    }

    public String getName() {
        return name;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public int compareTo(@NotNull MongoCollection otherCollection) {
        return this.name.compareTo(otherCollection.getName());
    }
    public String toString() {
        return getName();
    }
}
