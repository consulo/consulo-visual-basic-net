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

package consulo.visualBasic.dotnet.impl.lang.parser.preprocessor;

import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A preprocessor directive line: {@code #If}, {@code #ElseIf}, {@code #Else}, {@code #End If}, {@code #Const} or another one.
 *
 * @param expression condition of {@code #If}/{@code #ElseIf}, value of {@code #Const}
 * @param name constant name of {@code #Const}
 * @author VISTALL
 * @since 2026-10-24
 */
public record VisualBasicDirective(Kind kind, String expression, @Nullable String name) {
    public enum Kind {
        IF,
        ELSE_IF,
        ELSE,
        END_IF,
        CONST,
        OTHER
    }

    private static final Pattern IF = Pattern.compile("(?i)#\\s*(if|elseif)\\b(.*?)(\\bthen\\b.*)?");
    private static final Pattern ELSE = Pattern.compile("(?i)#\\s*else\\b.*");
    private static final Pattern END_IF = Pattern.compile("(?i)#\\s*(end\\s*if)\\b.*");
    private static final Pattern CONST = Pattern.compile("(?i)#\\s*const\\s+(\\[?[\\p{L}_][\\p{L}\\p{Nd}_]*]?)\\s*=(.*)");

    public static VisualBasicDirective parse(CharSequence directive) {
        String text = stripComment(directive.toString()).strip();
        Matcher matcher = IF.matcher(text);
        if (matcher.matches()) {
            Kind kind = matcher.group(1).toLowerCase(Locale.ROOT).equals("if") ? Kind.IF : Kind.ELSE_IF;
            return new VisualBasicDirective(kind, matcher.group(2).strip(), null);
        }
        if (END_IF.matcher(text).matches()) {
            return new VisualBasicDirective(Kind.END_IF, "", null);
        }
        if (ELSE.matcher(text).matches()) {
            return new VisualBasicDirective(Kind.ELSE, "", null);
        }
        matcher = CONST.matcher(text);
        if (matcher.matches()) {
            String name = matcher.group(1);
            if (name.startsWith("[") && name.endsWith("]")) {
                name = name.substring(1, name.length() - 1);
            }
            return new VisualBasicDirective(Kind.CONST, matcher.group(2).strip(), name);
        }
        return new VisualBasicDirective(Kind.OTHER, "", null);
    }

    /**
     * Drops a trailing {@code '} comment, which may follow a directive.
     */
    private static String stripComment(String text) {
        boolean inString = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                inString = !inString;
            }
            else if (!inString && (c == '\'' || c == (char) 0x2018 || c == (char) 0x2019)) {
                return text.substring(0, i);
            }
        }
        return text;
    }
}
