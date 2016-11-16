/*
 * Copyright (c) 2016 David Boissier.
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

public class StringUtils {

    private static final String ELLIPSIS = "...";

    public static String abbreviateInCenter(String stringToAbbreviate, int length) {
        if (stringToAbbreviate.length() <= length) {
            return stringToAbbreviate;
        }
        int halfLength = length / 2;
        int firstPartLastIndex = halfLength - ELLIPSIS.length();
        int stringLength = stringToAbbreviate.length();
        return String.format("%s%s%s",
                stringToAbbreviate.substring(0, firstPartLastIndex),
                ELLIPSIS,
                stringToAbbreviate.substring(stringLength - halfLength, stringLength));
    }

    public static Number parseNumber(String number) {
        try {
            return Integer.parseInt(number);

        } catch(NumberFormatException ex) {
            //UGLY :(
        }
        try {
            return Long.parseLong(number);

        } catch(NumberFormatException ex) {
            //UGLY :(
        }
        return Double.parseDouble(number);
    }
}
