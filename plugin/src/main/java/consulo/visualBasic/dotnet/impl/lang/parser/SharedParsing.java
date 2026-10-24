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

package consulo.visualBasic.dotnet.impl.lang.parser;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;
import consulo.localize.LocalizeValue;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicKeywordTokenType;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.localize.VisualBasicParserLocalize;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parsing helpers shared by declarations, statements and expressions.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class SharedParsing implements VisualBasicTokens, VisualBasicElements {
    protected static final TokenSet PREDEFINED_TYPE_KEYWORDS = TokenSet.create(
        BOOLEAN_KEYWORD,
        BYTE_KEYWORD,
        CHAR_KEYWORD,
        DATE_KEYWORD,
        DECIMAL_KEYWORD,
        DOUBLE_KEYWORD,
        INTEGER_KEYWORD,
        LONG_KEYWORD,
        OBJECT_KEYWORD,
        SBYTE_KEYWORD,
        SHORT_KEYWORD,
        SINGLE_KEYWORD,
        STRING_KEYWORD,
        UINTEGER_KEYWORD,
        ULONG_KEYWORD,
        USHORT_KEYWORD
    );

    protected static final TokenSet MODIFIER_KEYWORDS = TokenSet.create(
        PUBLIC_KEYWORD,
        PRIVATE_KEYWORD,
        PROTECTED_KEYWORD,
        FRIEND_KEYWORD,
        SHADOWS_KEYWORD,
        SHARED_KEYWORD,
        OVERRIDABLE_KEYWORD,
        NOTOVERRIDABLE_KEYWORD,
        MUSTOVERRIDE_KEYWORD,
        OVERRIDES_KEYWORD,
        OVERLOADS_KEYWORD,
        MUSTINHERIT_KEYWORD,
        NOTINHERITABLE_KEYWORD,
        PARTIAL_KEYWORD,
        READONLY_KEYWORD,
        WRITEONLY_KEYWORD,
        WITHEVENTS_KEYWORD,
        DIM_KEYWORD,
        CONST_KEYWORD,
        DEFAULT_KEYWORD,
        STATIC_KEYWORD,
        WIDENING_KEYWORD,
        NARROWING_KEYWORD
    );

    protected static final TokenSet PARAMETER_MODIFIER_KEYWORDS = TokenSet.create(
        OPTIONAL_KEYWORD,
        BYVAL_KEYWORD,
        BYREF_KEYWORD,
        PARAMARRAY_KEYWORD
    );

    /**
     * Keywords starting a declaration after modifiers.
     */
    protected static final TokenSet DECLARATION_KEYWORDS = TokenSet.create(
        CLASS_KEYWORD,
        STRUCTURE_KEYWORD,
        MODULE_KEYWORD,
        INTERFACE_KEYWORD,
        ENUM_KEYWORD,
        DELEGATE_KEYWORD,
        SUB_KEYWORD,
        FUNCTION_KEYWORD,
        OPERATOR_KEYWORD,
        PROPERTY_KEYWORD,
        EVENT_KEYWORD,
        DECLARE_KEYWORD,
        NAMESPACE_KEYWORD
    );

    protected static final TokenSet RPAR_SET = TokenSet.create(RPAR);
    protected static final TokenSet RBRACE_SET = TokenSet.create(RBRACE);
    protected static final TokenSet GT_SET = TokenSet.create(GT);

    private static final Map<IElementType, String> TOKEN_TEXT = Map.ofEntries(
        Map.entry(LPAR, "("),
        Map.entry(RPAR, ")"),
        Map.entry(LBRACE, "{"),
        Map.entry(RBRACE, "}"),
        Map.entry(COMMA, ","),
        Map.entry(DOT, "."),
        Map.entry(COLON, ":"),
        Map.entry(COLONEQ, ":="),
        Map.entry(EQ, "="),
        Map.entry(LT, "<"),
        Map.entry(GT, ">"),
        Map.entry(INTERPOLATION_END, "}"),
        Map.entry(INTERPOLATED_STRING_END, "\"")
    );

    public static String tokenText(IElementType type) {
        if (type instanceof VisualBasicKeywordTokenType keyword) {
            return keyword.getRepresentation();
        }
        String text = TOKEN_TEXT.get(type);
        return text == null ? type.toString() : text;
    }

    public static boolean expect(VisualBasicBuilder builder, IElementType type) {
        return expect(builder, type, VisualBasicParserLocalize.expected0(tokenText(type)));
    }

    public static boolean expect(VisualBasicBuilder builder, IElementType type, LocalizeValue message) {
        if (builder.getTokenType() == type) {
            builder.advanceLexer();
            return true;
        }
        builder.error(message);
        return false;
    }

    public static boolean expectIdentifier(VisualBasicBuilder builder) {
        return expect(builder, IDENTIFIER, VisualBasicParserLocalize.expectedIdentifier());
    }

    /**
     * Name of a declaration. A reserved keyword is an error, but is consumed as the name.
     */
    public static boolean expectDeclarationName(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == IDENTIFIER) {
            builder.advanceLexer();
            return true;
        }
        builder.error(VisualBasicParserLocalize.expectedIdentifier());
        if (RESERVED_KEYWORDS.contains(tokenType) && !builder.isAtStatementEnd()) {
            builder.remapCurrentTokenAndRestoreOnRollback(IDENTIFIER);
            builder.advanceLexer();
        }
        return false;
    }

    /**
     * Name after a member access: keywords are allowed here.
     */
    public static boolean expectMemberName(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == IDENTIFIER) {
            builder.advanceLexer();
            return true;
        }
        if (RESERVED_KEYWORDS.contains(tokenType)) {
            builder.remapCurrentTokenAndRestoreOnRollback(IDENTIFIER);
            builder.advanceLexer();
            return true;
        }
        builder.error(VisualBasicParserLocalize.expectedIdentifier());
        return false;
    }

    /**
     * Error up to the end of the statement.
     */
    public static void skipToStatementEnd(VisualBasicBuilder builder, LocalizeValue message) {
        if (builder.isAtStatementEnd()) {
            builder.error(message);
            return;
        }
        PsiBuilder.Marker error = builder.mark();
        while (!builder.isAtStatementEnd()) {
            builder.advanceLexer();
        }
        error.error(message);
    }

    public static void expectStatementEnd(VisualBasicBuilder builder) {
        if (!builder.isAtStatementEnd()) {
            skipToStatementEnd(builder, VisualBasicParserLocalize.expectedEndOfStatement());
        }
    }

    /**
     * {@code End <keyword>} closing a block.
     */
    public static boolean isEnd(VisualBasicBuilder builder, IElementType keyword) {
        if (builder.getTokenType() == END_KEYWORD) {
            return builder.lookAhead(1) == keyword;
        }
        return keyword == IF_KEYWORD && builder.getTokenType() == ENDIF_KEYWORD;
    }

    public static boolean expectEnd(VisualBasicBuilder builder, IElementType keyword) {
        if (isEnd(builder, keyword)) {
            if (builder.getTokenType() == ENDIF_KEYWORD) {
                builder.advanceLexer();
                return true;
            }
            builder.advanceLexer();
            builder.advanceLexer();
            return true;
        }
        builder.error(VisualBasicParserLocalize.expectedEnd0(tokenText(keyword)));
        return false;
    }

    // modifiers and attributes

    /**
     * Attributes and modifiers, always creates {@link VisualBasicElements#MODIFIER_LIST}.
     *
     * @return modifier keywords found
     */
    public static Set<IElementType> parseModifierList(VisualBasicBuilder builder, TokenSet modifiers) {
        Set<IElementType> result = new HashSet<>();
        PsiBuilder.Marker marker = builder.mark();
        while (true) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == LT) {
                parseAttributeList(builder);
                builder.skipLineBreaks();
            }
            else if (modifiers.contains(tokenType)) {
                result.add(tokenType);
                builder.advanceLexer();
            }
            else if (modifiers == MODIFIER_KEYWORDS && isContextualModifier(builder)) {
                IElementType keyword = currentContextualModifier(builder);
                result.add(keyword);
                builder.advanceAs(keyword);
            }
            else {
                break;
            }
        }
        marker.done(MODIFIER_LIST);
        return result;
    }

    private static IElementType currentContextualModifier(VisualBasicBuilder builder) {
        IElementType keyword = contextualModifier(builder);
        if (keyword == null) {
            throw new IllegalStateException();
        }
        return keyword;
    }

    /**
     * Current token type, when the caller already knows it is not the end of the file.
     */
    protected static IElementType currentToken(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == null) {
            throw new IllegalStateException("unexpected end of file");
        }
        return tokenType;
    }

    private static @Nullable IElementType contextualModifier(VisualBasicBuilder builder) {
        if (builder.isContextual(ASYNC_KEYWORD)) {
            return ASYNC_KEYWORD;
        }
        if (builder.isContextual(ITERATOR_KEYWORD)) {
            return ITERATOR_KEYWORD;
        }
        if (builder.isContextual(CUSTOM_KEYWORD)) {
            return CUSTOM_KEYWORD;
        }
        return null;
    }

    /**
     * {@code Async}, {@code Iterator} and {@code Custom} are modifiers when a declaration or another modifier follows.
     */
    protected static boolean isContextualModifier(VisualBasicBuilder builder) {
        IElementType keyword = contextualModifier(builder);
        if (keyword == null) {
            return false;
        }
        IElementType next = builder.lookAhead(1);
        if (keyword == CUSTOM_KEYWORD) {
            return next == EVENT_KEYWORD;
        }
        if (next == SUB_KEYWORD || next == FUNCTION_KEYWORD || next == PROPERTY_KEYWORD || MODIFIER_KEYWORDS.contains(next)) {
            return true;
        }
        if (next == IDENTIFIER) {
            CharSequence text = builder.lookAheadText(1, false);
            return text != null && (text.toString().equalsIgnoreCase("Async") || text.toString().equalsIgnoreCase("Iterator"));
        }
        return false;
    }

    public static void parseAttributeList(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        while (true) {
            PsiBuilder.Marker attribute = builder.mark();
            if ((builder.isContextual(ASSEMBLY_KEYWORD) || builder.getTokenType() == MODULE_KEYWORD) && builder.lookAhead(1) == COLON) {
                PsiBuilder.Marker target = builder.mark();
                if (builder.getTokenType() == MODULE_KEYWORD) {
                    builder.advanceLexer();
                }
                else {
                    builder.advanceAs(ASSEMBLY_KEYWORD);
                }
                builder.advanceLexer();
                target.done(ATTRIBUTE_TARGET);
            }
            if (parseNonArrayType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
            if (builder.getTokenType() == LPAR) {
                ExpressionParsing.parseArgumentList(builder, ARGUMENT_LIST);
            }
            attribute.done(ATTRIBUTE);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        builder.skipLineBreaksBefore(GT_SET);
        expect(builder, GT);
        marker.done(ATTRIBUTE_LIST);
    }

    // types

    public static PsiBuilder.@Nullable Marker parseType(VisualBasicBuilder builder) {
        PsiBuilder.Marker type = parseNonArrayType(builder);
        if (type == null) {
            return null;
        }
        while (true) {
            if (builder.getTokenType() == QUESTION) {
                type = type.precede();
                builder.advanceLexer();
                type.done(NULLABLE_TYPE);
            }
            else if (builder.getTokenType() == LPAR && isRankSpecifier(builder)) {
                type = type.precede();
                parseArrayRankSpecifier(builder);
                type.done(ARRAY_TYPE);
            }
            else {
                return type;
            }
        }
    }

    /**
     * {@code ()} or {@code (,)}
     */
    protected static boolean isRankSpecifier(VisualBasicBuilder builder) {
        for (int i = 1; ; i++) {
            IElementType type = builder.lookAhead(i);
            if (type == RPAR) {
                return true;
            }
            if (type != COMMA) {
                return false;
            }
        }
    }

    protected static void parseArrayRankSpecifier(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        while (builder.getTokenType() == COMMA) {
            builder.advanceLexer();
        }
        expect(builder, RPAR);
        marker.done(ARRAY_RANK_SPECIFIER);
    }

    public static PsiBuilder.@Nullable Marker parseNonArrayType(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (PREDEFINED_TYPE_KEYWORDS.contains(tokenType)) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            marker.done(PREDEFINED_TYPE);
            return marker;
        }
        if (tokenType == LPAR) {
            return parseTupleType(builder);
        }
        if (tokenType == IDENTIFIER || tokenType == GLOBAL_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            parseQualifiedName(builder, true);
            marker.done(USER_TYPE);
            return marker;
        }
        return null;
    }

    private static PsiBuilder.Marker parseTupleType(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        while (true) {
            PsiBuilder.Marker element = builder.mark();
            if (builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == AS_KEYWORD) {
                builder.advanceLexer();
                builder.advanceLexer();
            }
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
            element.done(TUPLE_TYPE_ELEMENT);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        builder.skipLineBreaksBefore(RPAR_SET);
        expect(builder, RPAR);
        marker.done(TUPLE_TYPE);
        return marker;
    }

    /**
     * Qualified name as nested {@link VisualBasicElements#REFERENCE_EXPRESSION}s, starting at an identifier or {@code Global}.
     */
    public static PsiBuilder.Marker parseQualifiedName(VisualBasicBuilder builder, boolean typeArguments) {
        PsiBuilder.Marker reference = builder.mark();
        if (builder.getTokenType() == GLOBAL_KEYWORD) {
            builder.advanceLexer();
            reference.done(REFERENCE_EXPRESSION);
            reference = reference.precede();
            expect(builder, DOT);
            builder.skipLineBreaks();
            expectMemberName(builder);
        }
        else {
            expectIdentifier(builder);
        }
        if (typeArguments) {
            parseTypeArgumentListIfAny(builder);
        }
        reference.done(REFERENCE_EXPRESSION);

        while (builder.getTokenType() == DOT) {
            reference = reference.precede();
            builder.advanceLexer();
            builder.skipLineBreaks();
            expectMemberName(builder);
            if (typeArguments) {
                parseTypeArgumentListIfAny(builder);
            }
            reference.done(REFERENCE_EXPRESSION);
        }
        return reference;
    }

    /**
     * {@code (Of T, U)}, type arguments may be omitted: {@code (Of ,)}
     */
    public static boolean parseTypeArgumentListIfAny(VisualBasicBuilder builder) {
        if (builder.getTokenType() != LPAR || builder.lookAhead(1, true) != OF_KEYWORD) {
            return false;
        }
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        builder.advanceLexer();
        builder.skipLineBreaks();
        while (true) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType != COMMA && tokenType != RPAR) {
                if (parseType(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedType());
                    break;
                }
            }
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        builder.skipLineBreaksBefore(RPAR_SET);
        expect(builder, RPAR);
        marker.done(TYPE_ARGUMENT_LIST);
        return true;
    }

    // parameters

    public static void parseParameterList(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        if (builder.getTokenType() != RPAR) {
            while (true) {
                if (!parseParameter(builder)) {
                    builder.error(VisualBasicParserLocalize.expectedParameter());
                }
                if (builder.getTokenType() == COMMA) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    continue;
                }
                break;
            }
        }
        builder.skipLineBreaksBefore(RPAR_SET);
        expect(builder, RPAR);
        marker.done(PARAMETER_LIST);
    }

    private static boolean parseParameter(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType != IDENTIFIER && tokenType != LT && !PARAMETER_MODIFIER_KEYWORDS.contains(tokenType)) {
            return false;
        }
        PsiBuilder.Marker marker = builder.mark();
        parseModifierList(builder, PARAMETER_MODIFIER_KEYWORDS);
        expectDeclarationName(builder);
        parseNameModifiers(builder, false);
        if (builder.getTokenType() == AS_KEYWORD) {
            builder.advanceLexer();
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
        }
        if (builder.getTokenType() == EQ) {
            builder.advanceLexer();
            builder.skipLineBreaks();
            if (ExpressionParsing.parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
        }
        marker.done(PARAMETER);
        return true;
    }

    /**
     * {@code ?} and array bounds after a declared name: {@code x?}, {@code x()}, {@code x(10)}.
     */
    protected static void parseNameModifiers(VisualBasicBuilder builder, boolean allowBounds) {
        if (builder.getTokenType() == QUESTION) {
            builder.advanceLexer();
        }
        if (builder.getTokenType() == LPAR) {
            if (isRankSpecifier(builder)) {
                parseArrayRankSpecifier(builder);
            }
            else if (allowBounds) {
                ExpressionParsing.parseArgumentList(builder, ARRAY_BOUNDS);
            }
        }
    }

    // generic parameters

    public static boolean parseGenericParameterListIfAny(VisualBasicBuilder builder) {
        if (builder.getTokenType() != LPAR || builder.lookAhead(1, true) != OF_KEYWORD) {
            return false;
        }
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        builder.advanceLexer();
        builder.skipLineBreaks();
        while (true) {
            PsiBuilder.Marker parameter = builder.mark();
            PsiBuilder.Marker modifiers = builder.mark();
            if (builder.getTokenType() == IN_KEYWORD) {
                builder.advanceLexer();
            }
            else if (builder.isContextual(OUT_KEYWORD) && builder.lookAhead(1) == IDENTIFIER) {
                builder.advanceAs(OUT_KEYWORD);
            }
            modifiers.done(MODIFIER_LIST);
            expectIdentifier(builder);
            if (builder.getTokenType() == AS_KEYWORD) {
                builder.advanceLexer();
                if (builder.getTokenType() == LBRACE) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    while (true) {
                        parseGenericConstraint(builder);
                        if (builder.getTokenType() == COMMA) {
                            builder.advanceLexer();
                            builder.skipLineBreaks();
                            continue;
                        }
                        break;
                    }
                    builder.skipLineBreaksBefore(RBRACE_SET);
                    expect(builder, RBRACE);
                }
                else {
                    parseGenericConstraint(builder);
                }
            }
            parameter.done(GENERIC_PARAMETER);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        builder.skipLineBreaksBefore(RPAR_SET);
        expect(builder, RPAR);
        marker.done(GENERIC_PARAMETER_LIST);
        return true;
    }

    private static void parseGenericConstraint(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == CLASS_KEYWORD || tokenType == STRUCTURE_KEYWORD || tokenType == NEW_KEYWORD) {
            builder.advanceLexer();
            return;
        }
        if (parseType(builder) == null) {
            builder.error(VisualBasicParserLocalize.expectedType());
        }
    }
}
