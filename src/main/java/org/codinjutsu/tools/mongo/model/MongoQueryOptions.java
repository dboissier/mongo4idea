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

import com.mongodb.DBObject;

public class MongoQueryOptions {

    private DBObject match = null;
    private DBObject project;
    private DBObject group;

    public boolean isNotEmpty() {
        return match != null || project != null || group != null;
    }

    public void setMatch(DBObject match) {
        this.match = match;
    }

    public DBObject getMatch() {
        return match;
    }

    public void setProject(DBObject project) {
        this.project = project;
    }

    public DBObject getProject() {
        return project;
    }

    public void setGroup(DBObject group) {
        this.group = group;
    }

    public DBObject getGroup() {
        return group;
    }

    public boolean isSimpleFilter() {
        return match != null && project == null && group == null;
    }
}
