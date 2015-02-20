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

package org.codinjutsu.tools.mongo.view.style;

import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;

public interface StyleAttributesProvider {

    SimpleTextAttributes getIndexAttribute();

    SimpleTextAttributes getKeyValueAttribute();

    SimpleTextAttributes getNumberAttribute();

    SimpleTextAttributes getBooleanAttribute();

    SimpleTextAttributes getStringAttribute();

    SimpleTextAttributes getNullAttribute();

    SimpleTextAttributes getDBObjectAttribute();

    SimpleTextAttributes getObjectIdAttribute();

    Icon getAddIcon();

    Icon getCopyIcon();

    Icon getExecuteIcon();

    Icon getSettingsIcon();

    Icon getRefreshIcon();

    Icon getEditIcon();

    Icon getClearAllIcon();

    Icon getDeleteIcon();

    Icon getFindIcon();

    Icon getDataSchemaIcon();

}
