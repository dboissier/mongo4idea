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

package org.codinjutsu.tools.mongo.view.nodedescriptor;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesUtils;
import org.codinjutsu.tools.mongo.view.style.StyleAttributesProvider;

public interface MongoNodeDescriptor {

    int MAX_LENGTH = 150;

    StyleAttributesProvider TEXT_ATTRIBUTES_PROVIDER = StyleAttributesUtils.getInstance();

    void appendText(ColoredTreeCellRenderer cellRenderer, boolean isNodeExpanded);

    void renderTextValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded);

    void renderTextKey(ColoredTreeCellRenderer cellRenderer);
}
