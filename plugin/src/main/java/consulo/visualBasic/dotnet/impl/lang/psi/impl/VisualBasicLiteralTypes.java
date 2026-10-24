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

package consulo.visualBasic.dotnet.impl.lang.psi.impl;

import consulo.dotnet.DotNetTypes;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Types of literals: {@code 1} is {@code Integer}, {@code 1L} {@code Long}, {@code 1.5} {@code Double}, {@code 1.5F}
 * {@code Single}, {@code 1D} {@code Decimal}, {@code "a"c} {@code Char}, {@code #1/1/2000#} {@code Date}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicLiteralTypes {
    private VisualBasicLiteralTypes() {
    }

    /**
     * @return VM name of the type of a literal expression, null for other expressions
     */
    public static @Nullable String getVmQName(@Nullable PsiElement expression) {
        if (expression == null || expression.getNode().getElementType() != VisualBasicElements.LITERAL_EXPRESSION) {
            return null;
        }
        PsiElement token = expression.getFirstChild();
        if (token == null) {
            return null;
        }
        IElementType type = token.getNode().getElementType();
        String text = token.getText().toUpperCase(Locale.ROOT);
        if (type == VisualBasicTokens.INTEGER_LITERAL) {
            return integerType(text);
        }
        if (type == VisualBasicTokens.FLOATING_LITERAL) {
            return text.endsWith("F") || text.endsWith("!") ? DotNetTypes.System.Single : DotNetTypes.System.Double;
        }
        if (type == VisualBasicTokens.DECIMAL_LITERAL) {
            return DotNetTypes.System.Decimal;
        }
        if (type == VisualBasicTokens.STRING_LITERAL) {
            return DotNetTypes.System.String;
        }
        if (type == VisualBasicTokens.CHARACTER_LITERAL) {
            return DotNetTypes.System.Char;
        }
        if (type == VisualBasicTokens.DATE_LITERAL) {
            return "System.DateTime";
        }
        if (type == VisualBasicTokens.TRUE_KEYWORD || type == VisualBasicTokens.FALSE_KEYWORD) {
            return DotNetTypes.System.Boolean;
        }
        if (type == VisualBasicTokens.NOTHING_KEYWORD) {
            return DotNetTypes.System.Object;
        }
        return null;
    }

    private static String integerType(String text) {
        // hexadecimal digits may look like suffixes: &H1S is Short, &HFF is Integer
        String digits = text.startsWith("&") ? text.substring(2) : text;
        if (digits.endsWith("US")) {
            return DotNetTypes.System.UInt16;
        }
        if (digits.endsWith("UI")) {
            return DotNetTypes.System.UInt32;
        }
        if (digits.endsWith("UL")) {
            return DotNetTypes.System.UInt64;
        }
        if (digits.endsWith("S") && !isHexDigitSuffix(text, 'S')) {
            return DotNetTypes.System.Int16;
        }
        if (digits.endsWith("L") || digits.endsWith("&")) {
            return DotNetTypes.System.Int64;
        }
        if (digits.endsWith("I") || digits.endsWith("%")) {
            return DotNetTypes.System.Int32;
        }
        // TODO [VISTALL] literals out of the Integer range are Long
        return DotNetTypes.System.Int32;
    }

    private static boolean isHexDigitSuffix(String text, char suffix) {
        return text.startsWith("&H") && Character.digit(suffix, 16) >= 0;
    }
}
