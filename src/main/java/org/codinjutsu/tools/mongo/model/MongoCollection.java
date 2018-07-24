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

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MongoCollection implements Comparable<MongoCollection> {

    private final String name;
    private final MongoDatabase parentDatabase;

    public MongoCollection(String name, MongoDatabase mongoDatabase) {
        this.name = name;
        this.parentDatabase = mongoDatabase;
    }

    public String getName() {
        return name;
    }

    public MongoDatabase getParentDatabase() {
        return parentDatabase;
    }

    @Override
    public int compareTo(@NotNull MongoCollection otherCollection) {
        return this.name.compareTo(otherCollection.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoCollection)) return false;
        MongoCollection that = (MongoCollection) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(parentDatabase, that.parentDatabase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parentDatabase);
    }

    @Override
    public String toString() {
        return "MongoCollection{" +
                "name='" + name + '\'' +
                '}';
    }
}
