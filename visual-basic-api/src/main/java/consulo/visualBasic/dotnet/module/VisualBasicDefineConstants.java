/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.visualBasic.dotnet.module;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses {@code DefineConstants} of a Visual Basic project: {@code CONFIG="Debug",DEBUG=-1,TRACE=-1,_MyType="Console"}.
 * A constant without a value is {@code True}. Commas inside quoted values do not separate constants.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public final class VisualBasicDefineConstants {
    private VisualBasicDefineConstants() {
    }

    /**
     * @return values as Visual Basic expressions, by name
     */
    public static Map<String, String> parse(String text) {
        Map<String, String> constants = new LinkedHashMap<>();
        int start = 0;
        boolean inString = false;
        for (int i = 0; i <= text.length(); i++) {
            if (i < text.length()) {
                char c = text.charAt(i);
                if (c == '"') {
                    inString = !inString;
                }
                if (inString || c != ',' && c != ';') {
                    continue;
                }
            }
            add(constants, text.substring(start, i));
            start = i + 1;
        }
        return constants;
    }

    private static void add(Map<String, String> constants, String definition) {
        String trimmed = definition.strip();
        if (trimmed.isEmpty()) {
            return;
        }
        int equals = trimmed.indexOf('=');
        if (equals < 0) {
            constants.put(trimmed, "True");
        }
        else {
            String name = trimmed.substring(0, equals).strip();
            if (!name.isEmpty()) {
                constants.put(name, trimmed.substring(equals + 1).strip());
            }
        }
    }
}
