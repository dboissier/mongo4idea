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

package org.codinjutsu.tools.mongo.view.style;

import org.codinjutsu.tools.mongo.utils.GuiUtils;

public class StyleAttributesUtils {


    private static StyleAttributesProvider styleAttributesProvider = null;


    public static StyleAttributesProvider getInstance() {//TODO see how to put it into the pico container
        if (styleAttributesProvider == null) {
            if (GuiUtils.isUnderDarcula()) {
                styleAttributesProvider = new DarculaStyleAttributesProvider();
            } else {
                styleAttributesProvider = new DefaultStyleAttributesProvider();
            }
        }
        return styleAttributesProvider;
    }

}
