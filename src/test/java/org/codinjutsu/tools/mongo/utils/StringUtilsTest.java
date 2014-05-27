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

package org.codinjutsu.tools.mongo.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void abbreviateInCenter() throws Exception {
        String value = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        assertEquals("abcdefghijklmnopq...ghijklmnopqrstuvwxyz", StringUtils.abbreviateInCenter(value, 40));

    }

    @Test
    public void parseNumber() throws Exception {
        assertEquals(1, StringUtils.parseNumber("1"));
        assertEquals(1.000000000001d, StringUtils.parseNumber("1.000000000001"));
        assertEquals(1000000000000000L, StringUtils.parseNumber("1000000000000000"));
    }
}
