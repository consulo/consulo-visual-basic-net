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

package consulo.visualBasic.dotnet.impl.lang.psi.resolve;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Operators and their metadata names: {@code +} is {@code op_Addition}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicExpressionOperators {
    private static final TokenSet COMPARISONS = TokenSet.create(
        VisualBasicTokens.EQ,
        VisualBasicTokens.LTGT,
        VisualBasicTokens.LT,
        VisualBasicTokens.GT,
        VisualBasicTokens.LTEQ,
        VisualBasicTokens.GTEQ,
        VisualBasicTokens.IS_KEYWORD,
        VisualBasicTokens.ISNOT_KEYWORD,
        VisualBasicTokens.LIKE_KEYWORD
    );

    private static final Map<IElementType, String> METADATA_NAMES = Map.ofEntries(
        Map.entry(VisualBasicTokens.PLUS, "op_Addition"),
        Map.entry(VisualBasicTokens.MINUS, "op_Subtraction"),
        Map.entry(VisualBasicTokens.ASTERISK, "op_Multiply"),
        Map.entry(VisualBasicTokens.SLASH, "op_Division"),
        Map.entry(VisualBasicTokens.BACKSLASH, "op_IntegerDivision"),
        Map.entry(VisualBasicTokens.CARET, "op_Exponent"),
        Map.entry(VisualBasicTokens.AMPERSAND, "op_Concatenate"),
        Map.entry(VisualBasicTokens.LIKE_KEYWORD, "op_Like"),
        Map.entry(VisualBasicTokens.MOD_KEYWORD, "op_Modulus"),
        Map.entry(VisualBasicTokens.AND_KEYWORD, "op_BitwiseAnd"),
        Map.entry(VisualBasicTokens.OR_KEYWORD, "op_BitwiseOr"),
        Map.entry(VisualBasicTokens.XOR_KEYWORD, "op_ExclusiveOr"),
        Map.entry(VisualBasicTokens.NOT_KEYWORD, "op_OnesComplement"),
        Map.entry(VisualBasicTokens.LTLT, "op_LeftShift"),
        Map.entry(VisualBasicTokens.GTGT, "op_RightShift"),
        Map.entry(VisualBasicTokens.EQ, "op_Equality"),
        Map.entry(VisualBasicTokens.LTGT, "op_Inequality"),
        Map.entry(VisualBasicTokens.LT, "op_LessThan"),
        Map.entry(VisualBasicTokens.GT, "op_GreaterThan"),
        Map.entry(VisualBasicTokens.LTEQ, "op_LessThanOrEqual"),
        Map.entry(VisualBasicTokens.GTEQ, "op_GreaterThanOrEqual"),
        Map.entry(VisualBasicTokens.ISTRUE_KEYWORD, "op_True"),
        Map.entry(VisualBasicTokens.ISFALSE_KEYWORD, "op_False")
    );

    private VisualBasicExpressionOperators() {
    }

    public static boolean isComparison(IElementType operator) {
        return COMPARISONS.contains(operator);
    }

    /**
     * Name of a binary operator method, {@code null} for {@code Is}, {@code AndAlso} and the like.
     */
    public static @Nullable String getMetadataName(IElementType operator) {
        return METADATA_NAMES.get(operator);
    }
}
