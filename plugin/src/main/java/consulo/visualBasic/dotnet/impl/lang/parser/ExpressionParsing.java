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
import consulo.visualBasic.dotnet.impl.localize.VisualBasicParserLocalize;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Expressions. Operator precedence follows Roslyn {@code OperatorPrecedence}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class ExpressionParsing extends SharedParsing {
    private static final int PRECEDENCE_XOR = 1;
    private static final int PRECEDENCE_OR = 2;
    private static final int PRECEDENCE_AND = 3;
    private static final int PRECEDENCE_NOT = 4;
    private static final int PRECEDENCE_RELATIONAL = 5;
    private static final int PRECEDENCE_SHIFT = 6;
    private static final int PRECEDENCE_CONCATENATE = 7;
    private static final int PRECEDENCE_ADD = 8;
    private static final int PRECEDENCE_MODULUS = 9;
    private static final int PRECEDENCE_INTEGER_DIVIDE = 10;
    private static final int PRECEDENCE_MULTIPLY = 11;
    private static final int PRECEDENCE_NEGATE = 12;
    private static final int PRECEDENCE_EXPONENTIATE = 13;
    private static final int PRECEDENCE_AWAIT = 14;

    protected static final TokenSet LITERALS = TokenSet.create(
        INTEGER_LITERAL,
        FLOATING_LITERAL,
        DECIMAL_LITERAL,
        DATE_LITERAL,
        STRING_LITERAL,
        CHARACTER_LITERAL,
        TRUE_KEYWORD,
        FALSE_KEYWORD,
        NOTHING_KEYWORD
    );

    protected static final TokenSet PREDEFINED_CAST_KEYWORDS = TokenSet.create(
        CBOOL_KEYWORD,
        CBYTE_KEYWORD,
        CCHAR_KEYWORD,
        CDATE_KEYWORD,
        CDEC_KEYWORD,
        CDBL_KEYWORD,
        CINT_KEYWORD,
        CLNG_KEYWORD,
        COBJ_KEYWORD,
        CSBYTE_KEYWORD,
        CSHORT_KEYWORD,
        CSNG_KEYWORD,
        CSTR_KEYWORD,
        CUINT_KEYWORD,
        CULNG_KEYWORD,
        CUSHORT_KEYWORD
    );

    protected static final TokenSet RELATIONAL_OPERATORS = TokenSet.create(EQ, LTGT, LT, GT, LTEQ, GTEQ);

    private static int binaryPrecedence(@Nullable IElementType type) {
        if (type == XOR_KEYWORD) {
            return PRECEDENCE_XOR;
        }
        if (type == OR_KEYWORD || type == ORELSE_KEYWORD) {
            return PRECEDENCE_OR;
        }
        if (type == AND_KEYWORD || type == ANDALSO_KEYWORD) {
            return PRECEDENCE_AND;
        }
        if (RELATIONAL_OPERATORS.contains(type) || type == IS_KEYWORD || type == ISNOT_KEYWORD || type == LIKE_KEYWORD) {
            return PRECEDENCE_RELATIONAL;
        }
        if (type == LTLT || type == GTGT) {
            return PRECEDENCE_SHIFT;
        }
        if (type == AMPERSAND) {
            return PRECEDENCE_CONCATENATE;
        }
        if (type == PLUS || type == MINUS) {
            return PRECEDENCE_ADD;
        }
        if (type == MOD_KEYWORD) {
            return PRECEDENCE_MODULUS;
        }
        if (type == BACKSLASH) {
            return PRECEDENCE_INTEGER_DIVIDE;
        }
        if (type == ASTERISK || type == SLASH) {
            return PRECEDENCE_MULTIPLY;
        }
        if (type == CARET) {
            return PRECEDENCE_EXPONENTIATE;
        }
        return -1;
    }

    public static PsiBuilder.@Nullable Marker parseExpression(VisualBasicBuilder builder) {
        return parseExpression(builder, 0);
    }

    private static PsiBuilder.@Nullable Marker parseExpression(VisualBasicBuilder builder, int minPrecedence) {
        PsiBuilder.Marker left = parseUnary(builder);
        if (left == null) {
            return null;
        }
        while (true) {
            int precedence = binaryPrecedence(builder.getCompositeTokenType());
            if (precedence < 0 || precedence < minPrecedence) {
                return left;
            }
            PsiBuilder.Marker binary = left.precede();
            builder.advanceComposite();
            builder.skipLineBreaks();
            if (parseExpression(builder, precedence + 1) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            binary.done(BINARY_EXPRESSION);
            left = binary;
        }
    }

    private static PsiBuilder.@Nullable Marker parseUnary(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == NOT_KEYWORD) {
            return parsePrefix(builder, PREFIX_EXPRESSION, PRECEDENCE_NOT);
        }
        if (tokenType == PLUS || tokenType == MINUS) {
            return parsePrefix(builder, PREFIX_EXPRESSION, PRECEDENCE_NEGATE);
        }
        if (tokenType == ADDRESSOF_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            if (parseTerm(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            marker.done(ADDRESS_OF_EXPRESSION);
            return marker;
        }
        if (isAwait(builder)) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceAs(AWAIT_KEYWORD);
            if (parseExpression(builder, PRECEDENCE_AWAIT) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            marker.done(AWAIT_EXPRESSION);
            return marker;
        }
        return parseTerm(builder);
    }

    private static PsiBuilder.Marker parsePrefix(VisualBasicBuilder builder, IElementType elementType, int precedence) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (parseExpression(builder, precedence) == null) {
            builder.error(VisualBasicParserLocalize.expectedExpression());
        }
        marker.done(elementType);
        return marker;
    }

    /**
     * {@code Await} is a keyword in async methods and lambdas; elsewhere only when an operand follows on the same line.
     */
    protected static boolean isAwait(VisualBasicBuilder builder) {
        if (!builder.isContextual(AWAIT_KEYWORD)) {
            return false;
        }
        if (builder.isAsync()) {
            return true;
        }
        IElementType next = builder.lookAhead(1);
        return next == IDENTIFIER || next == ME_KEYWORD || next == MYBASE_KEYWORD || next == MYCLASS_KEYWORD || next == NEW_KEYWORD;
    }

    // terms

    public static PsiBuilder.@Nullable Marker parseTerm(VisualBasicBuilder builder) {
        PsiBuilder.Marker expression = parsePrimary(builder);
        if (expression == null) {
            return null;
        }
        return parsePostfix(builder, expression);
    }

    private static PsiBuilder.Marker parsePostfix(VisualBasicBuilder builder, PsiBuilder.Marker start) {
        PsiBuilder.Marker expression = start;
        while (true) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == DOT && isXmlAxis(builder, 1)) {
                expression = expression.precede();
                parseXmlAxis(builder);
                expression.done(XML_MEMBER_ACCESS_EXPRESSION);
            }
            else if (tokenType == DOT) {
                expression = expression.precede();
                builder.advanceLexer();
                builder.skipLineBreaks();
                expectMemberName(builder);
                parseTypeArgumentListIfAny(builder);
                expression.done(REFERENCE_EXPRESSION);
            }
            else if (tokenType == EXCLAMATION) {
                expression = expression.precede();
                builder.advanceLexer();
                expectMemberName(builder);
                expression.done(DICTIONARY_ACCESS_EXPRESSION);
            }
            else if (tokenType == QUESTION && isConditionalAccess(builder.lookAhead(1))) {
                expression = expression.precede();
                builder.advanceLexer();
                IElementType next = builder.getTokenType();
                if (next == DOT) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    expectMemberName(builder);
                    parseTypeArgumentListIfAny(builder);
                    expression.done(REFERENCE_EXPRESSION);
                }
                else if (next == EXCLAMATION) {
                    builder.advanceLexer();
                    expectMemberName(builder);
                    expression.done(DICTIONARY_ACCESS_EXPRESSION);
                }
                else {
                    parseArgumentList(builder, ARGUMENT_LIST);
                    expression.done(INVOCATION_EXPRESSION);
                }
            }
            else if (tokenType == LPAR) {
                expression = expression.precede();
                parseArgumentList(builder, ARGUMENT_LIST);
                expression.done(INVOCATION_EXPRESSION);
            }
            else {
                return expression;
            }
        }
    }

    private static boolean isConditionalAccess(@Nullable IElementType next) {
        return next == DOT || next == LPAR || next == EXCLAMATION;
    }

    private static PsiBuilder.@Nullable Marker parsePrimary(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == null) {
            return null;
        }
        if (LITERALS.contains(tokenType)) {
            return single(builder, LITERAL_EXPRESSION);
        }
        if (tokenType == INTERPOLATED_STRING_START) {
            return parseInterpolatedString(builder);
        }
        if (XML_LITERAL_STARTS.contains(tokenType)) {
            return parseXmlLiteral(builder);
        }
        if (tokenType == ME_KEYWORD || tokenType == MYBASE_KEYWORD || tokenType == MYCLASS_KEYWORD) {
            return single(builder, INSTANCE_EXPRESSION);
        }
        if (tokenType == IDENTIFIER) {
            if (isQueryStart(builder)) {
                return QueryParsing.parseQuery(builder);
            }
            if (isLambdaStart(builder)) {
                return parseLambda(builder);
            }
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            parseTypeArgumentListIfAny(builder);
            marker.done(REFERENCE_EXPRESSION);
            return marker;
        }
        if (tokenType == GLOBAL_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            marker.done(REFERENCE_EXPRESSION);
            if (builder.getTokenType() != DOT) {
                builder.error(VisualBasicParserLocalize.expected0("."));
            }
            return marker;
        }
        if (PREDEFINED_TYPE_KEYWORDS.contains(tokenType)) {
            PsiBuilder.Marker marker = builder.mark();
            parseNonArrayType(builder);
            marker.done(TYPE_EXPRESSION);
            return marker;
        }
        if (tokenType == LPAR) {
            return parseParenthesized(builder);
        }
        if (tokenType == LBRACE) {
            return parseCollectionInitializer(builder);
        }
        if (tokenType == NEW_KEYWORD) {
            return parseNew(builder);
        }
        if (tokenType == GETTYPE_KEYWORD) {
            return parseGetType(builder);
        }
        if (tokenType == GETXMLNAMESPACE_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            if (expect(builder, LPAR)) {
                if (builder.getTokenType() == IDENTIFIER) {
                    builder.advanceLexer();
                }
                expect(builder, RPAR);
            }
            marker.done(GET_XML_NAMESPACE_EXPRESSION);
            return marker;
        }
        if (tokenType == NAMEOF_KEYWORD) {
            return parseParenthesizedOperand(builder, NAME_OF_EXPRESSION);
        }
        if (tokenType == CTYPE_KEYWORD || tokenType == DIRECTCAST_KEYWORD || tokenType == TRYCAST_KEYWORD) {
            return parseCast(builder);
        }
        if (PREDEFINED_CAST_KEYWORDS.contains(tokenType)) {
            return parseParenthesizedOperand(builder, PREDEFINED_CAST_EXPRESSION);
        }
        if (tokenType == IF_KEYWORD && builder.lookAhead(1) == LPAR) {
            return parseIfExpression(builder);
        }
        if (tokenType == TYPEOF_KEYWORD) {
            return parseTypeOf(builder);
        }
        if (tokenType == FUNCTION_KEYWORD || tokenType == SUB_KEYWORD) {
            return parseLambda(builder);
        }
        if (tokenType == DOT && isXmlAxis(builder, 1)) {
            PsiBuilder.Marker marker = builder.mark();
            parseXmlAxis(builder);
            marker.done(XML_MEMBER_ACCESS_EXPRESSION);
            return marker;
        }
        if (tokenType == DOT) {
            // member access in a With block
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            expectMemberName(builder);
            parseTypeArgumentListIfAny(builder);
            marker.done(REFERENCE_EXPRESSION);
            return marker;
        }
        if (tokenType == EXCLAMATION) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            expectMemberName(builder);
            marker.done(DICTIONARY_ACCESS_EXPRESSION);
            return marker;
        }
        return null;
    }

    // XML

    /**
     * {@code <a><%= x %></a>}: the lexer decides where XML starts and ends, the literal has no structure yet but its
     * embedded expressions.
     */
    private static PsiBuilder.Marker parseXmlLiteral(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        while (XML_TOKENS.contains(builder.getTokenType())) {
            if (builder.getTokenType() == XML_EMBED_START) {
                PsiBuilder.Marker embedded = builder.mark();
                builder.advanceLexer();
                if (parseExpression(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedExpression());
                }
                if (builder.getTokenType() != XML_EMBED_END && !XML_TOKENS.contains(builder.getTokenType())) {
                    PsiBuilder.Marker error = builder.mark();
                    while (!builder.eof() && builder.getTokenType() != XML_EMBED_END && !XML_TOKENS.contains(builder.getTokenType())) {
                        builder.advanceLexer();
                    }
                    error.error(VisualBasicParserLocalize.expected0("%>"));
                }
                expect(builder, XML_EMBED_END);
                embedded.done(XML_EMBEDDED_EXPRESSION);
            }
            else {
                builder.advanceLexer();
            }
        }
        marker.done(XML_LITERAL_EXPRESSION);
        return marker;
    }

    /**
     * After a dot: {@code .<child>}, {@code .@attribute}, {@code .@<attribute>}, {@code ...<descendant>}.
     */
    private static boolean isXmlAxis(VisualBasicBuilder builder, int steps) {
        IElementType next = builder.lookAhead(steps);
        if (next == LT || next == AT) {
            return true;
        }
        return next == DOT && builder.lookAhead(steps + 1) == DOT && builder.lookAhead(steps + 2) == LT;
    }

    private static void parseXmlAxis(VisualBasicBuilder builder) {
        builder.advanceLexer();
        if (builder.getTokenType() == DOT) {
            builder.advanceLexer();
            builder.advanceLexer();
        }
        boolean attribute = builder.getTokenType() == AT;
        if (attribute) {
            builder.advanceLexer();
        }
        if (builder.getTokenType() == LT) {
            builder.advanceLexer();
            parseXmlName(builder);
            expect(builder, GT);
        }
        else if (attribute) {
            parseXmlName(builder);
        }
        else {
            builder.error(VisualBasicParserLocalize.expected0("<"));
        }
    }

    /**
     * {@code name} or {@code prefix:name}
     */
    private static void parseXmlName(VisualBasicBuilder builder) {
        PsiBuilder.Marker name = builder.mark();
        if (!expectMemberName(builder)) {
            name.drop();
            return;
        }
        if (builder.getTokenType() == COLON && (builder.lookAhead(1) == IDENTIFIER || RESERVED_KEYWORDS.contains(builder.lookAhead(1)))) {
            builder.advanceLexer();
            expectMemberName(builder);
        }
        name.done(XML_NAME);
    }

    private static PsiBuilder.Marker single(VisualBasicBuilder builder, IElementType elementType) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(elementType);
        return marker;
    }

    private static PsiBuilder.Marker parseInterpolatedString(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        while (true) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == INTERPOLATED_STRING_TEXT) {
                builder.advanceLexer();
            }
            else if (tokenType == INTERPOLATION_START) {
                PsiBuilder.Marker interpolation = builder.mark();
                builder.advanceLexer();
                if (parseExpression(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedExpression());
                }
                if (builder.getTokenType() == COMMA) {
                    PsiBuilder.Marker alignment = builder.mark();
                    builder.advanceLexer();
                    if (parseExpression(builder) == null) {
                        builder.error(VisualBasicParserLocalize.expectedExpression());
                    }
                    alignment.done(INTERPOLATION_ALIGNMENT);
                }
                if (builder.getTokenType() == COLON) {
                    PsiBuilder.Marker format = builder.mark();
                    builder.advanceLexer();
                    if (builder.getTokenType() == INTERPOLATION_FORMAT) {
                        builder.advanceLexer();
                    }
                    format.done(INTERPOLATION_FORMAT_CLAUSE);
                }
                // skip garbage up to the end of the interpolation
                if (builder.getTokenType() != INTERPOLATION_END && !isInterpolationStop(builder.getTokenType())) {
                    PsiBuilder.Marker error = builder.mark();
                    while (!isInterpolationStop(builder.getTokenType()) && builder.getTokenType() != INTERPOLATION_END) {
                        builder.advanceLexer();
                    }
                    error.error(VisualBasicParserLocalize.expected0("}"));
                }
                expect(builder, INTERPOLATION_END);
                interpolation.done(INTERPOLATION);
            }
            else if (tokenType == INTERPOLATED_STRING_END) {
                builder.advanceLexer();
                break;
            }
            else {
                builder.error(VisualBasicParserLocalize.expected0("\""));
                break;
            }
        }
        marker.done(INTERPOLATED_STRING_EXPRESSION);
        return marker;
    }

    private static boolean isInterpolationStop(@Nullable IElementType type) {
        return type == null || type == INTERPOLATED_STRING_END || type == INTERPOLATED_STRING_TEXT || type == INTERPOLATION_START;
    }

    private static PsiBuilder.Marker parseParenthesized(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        boolean tuple = false;
        while (true) {
            if (builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == COLONEQ) {
                PsiBuilder.Marker named = builder.mark();
                builder.advanceLexer();
                builder.advanceLexer();
                builder.skipLineBreaks();
                if (parseExpression(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedExpression());
                }
                named.done(NAMED_ARGUMENT);
                tuple = true;
            }
            else if (parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            if (builder.getTokenType() == COMMA) {
                tuple = true;
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        builder.skipLineBreaksBefore(RPAR_SET);
        expect(builder, RPAR);
        marker.done(tuple ? TUPLE_EXPRESSION : PARENTHESIZED_EXPRESSION);
        return marker;
    }

    /**
     * {@code {a, b}}
     */
    public static PsiBuilder.Marker parseCollectionInitializer(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        if (builder.getTokenType() != RBRACE) {
            while (true) {
                if (parseExpression(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedExpression());
                }
                if (builder.getTokenType() == COMMA) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    continue;
                }
                break;
            }
        }
        builder.skipLineBreaksBefore(RBRACE_SET);
        expect(builder, RBRACE);
        marker.done(COLLECTION_INITIALIZER);
        return marker;
    }

    /**
     * {@code New T(args)}, {@code New T With {...}}, {@code New T From {...}}, {@code New T(bounds) {...}},
     * {@code New With {...}}
     */
    public static PsiBuilder.Marker parseNew(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (builder.getTokenType() == WITH_KEYWORD) {
            parseObjectInitializer(builder);
            marker.done(ANONYMOUS_OBJECT_CREATION_EXPRESSION);
            return marker;
        }
        if (parseNonArrayType(builder) == null) {
            builder.error(VisualBasicParserLocalize.expectedType());
        }
        if (builder.getTokenType() == LPAR) {
            parseArgumentList(builder, ARGUMENT_LIST);
        }
        boolean array = false;
        while (builder.getTokenType() == LPAR && isRankSpecifier(builder)) {
            parseArrayRankSpecifier(builder);
            array = true;
        }
        if (builder.getTokenType() == LBRACE) {
            parseCollectionInitializer(builder);
            marker.done(ARRAY_CREATION_EXPRESSION);
            return marker;
        }
        if (builder.getTokenType() == WITH_KEYWORD) {
            parseObjectInitializer(builder);
        }
        else if (builder.isContextual(FROM_KEYWORD) && builder.lookAhead(1, true) == LBRACE) {
            builder.advanceAs(FROM_KEYWORD);
            builder.skipLineBreaks();
            parseCollectionInitializer(builder);
        }
        else if (array) {
            builder.error(VisualBasicParserLocalize.expected0("{"));
        }
        marker.done(NEW_EXPRESSION);
        return marker;
    }

    /**
     * {@code With {.A = 1, Key .B = 2, c}}
     */
    private static void parseObjectInitializer(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (builder.getTokenType() != LBRACE) {
            builder.error(VisualBasicParserLocalize.expected0("{"));
            marker.done(OBJECT_INITIALIZER);
            return;
        }
        builder.advanceLexer();
        builder.skipLineBreaks();
        while (builder.getTokenType() != RBRACE) {
            PsiBuilder.Marker field = builder.mark();
            if (builder.isContextual(KEY_KEYWORD) && builder.lookAhead(1) == DOT) {
                builder.advanceAs(KEY_KEYWORD);
            }
            if (builder.getTokenType() == DOT) {
                builder.advanceLexer();
                expectMemberName(builder);
                expect(builder, EQ);
                builder.skipLineBreaks();
                if (parseExpression(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedExpression());
                }
            }
            else if (parseExpression(builder) == null) {
                field.drop();
                builder.error(VisualBasicParserLocalize.expectedExpression());
                break;
            }
            field.done(FIELD_INITIALIZER);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        builder.skipLineBreaksBefore(RBRACE_SET);
        expect(builder, RBRACE);
        marker.done(OBJECT_INITIALIZER);
    }

    private static PsiBuilder.Marker parseGetType(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (expect(builder, LPAR)) {
            builder.skipLineBreaks();
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
            builder.skipLineBreaksBefore(RPAR_SET);
            expect(builder, RPAR);
        }
        marker.done(GET_TYPE_EXPRESSION);
        return marker;
    }

    private static PsiBuilder.Marker parseParenthesizedOperand(VisualBasicBuilder builder, IElementType elementType) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (expect(builder, LPAR)) {
            builder.skipLineBreaks();
            if (parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            builder.skipLineBreaksBefore(RPAR_SET);
            expect(builder, RPAR);
        }
        marker.done(elementType);
        return marker;
    }

    private static PsiBuilder.Marker parseCast(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (expect(builder, LPAR)) {
            builder.skipLineBreaks();
            if (parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            if (expect(builder, COMMA)) {
                builder.skipLineBreaks();
                if (parseType(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedType());
                }
            }
            builder.skipLineBreaksBefore(RPAR_SET);
            expect(builder, RPAR);
        }
        marker.done(CAST_EXPRESSION);
        return marker;
    }

    /**
     * {@code If(condition, whenTrue, whenFalse)} or {@code If(value, whenNothing)}
     */
    private static PsiBuilder.Marker parseIfExpression(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.advanceLexer();
        builder.skipLineBreaks();
        int count = 0;
        while (true) {
            if (parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            count++;
            if (builder.getTokenType() == COMMA && count < 3) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        builder.skipLineBreaksBefore(RPAR_SET);
        expect(builder, RPAR);
        marker.done(IF_EXPRESSION);
        return marker;
    }

    /**
     * {@code TypeOf x Is T}, {@code TypeOf x IsNot T}
     */
    private static PsiBuilder.Marker parseTypeOf(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (parseExpression(builder, PRECEDENCE_SHIFT) == null) {
            builder.error(VisualBasicParserLocalize.expectedExpression());
        }
        if (builder.getTokenType() == IS_KEYWORD || builder.getTokenType() == ISNOT_KEYWORD) {
            builder.advanceLexer();
            builder.skipLineBreaks();
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
        }
        else {
            builder.error(VisualBasicParserLocalize.expected0("Is"));
        }
        marker.done(TYPE_OF_EXPRESSION);
        return marker;
    }

    // lambdas

    private static boolean isLambdaStart(VisualBasicBuilder builder) {
        if (!builder.isContextual(ASYNC_KEYWORD) && !builder.isContextual(ITERATOR_KEYWORD)) {
            return false;
        }
        for (int i = 1; ; i++) {
            IElementType next = builder.lookAhead(i);
            if (next == SUB_KEYWORD || next == FUNCTION_KEYWORD) {
                return true;
            }
            if (next != IDENTIFIER) {
                return false;
            }
        }
    }

    private static PsiBuilder.Marker parseLambda(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        Set<IElementType> modifiers = parseModifierList(builder, MODIFIER_KEYWORDS);
        IElementType keyword = builder.getTokenType();
        if (keyword != SUB_KEYWORD && keyword != FUNCTION_KEYWORD) {
            builder.error(VisualBasicParserLocalize.expected0("Function"));
            marker.done(LAMBDA_EXPRESSION);
            return marker;
        }
        builder.advanceLexer();
        if (builder.getTokenType() == LPAR) {
            parseParameterList(builder);
        }
        if (builder.getTokenType() == AS_KEYWORD) {
            builder.advanceLexer();
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
        }
        builder.enterMethod(modifiers.contains(ASYNC_KEYWORD), modifiers.contains(ITERATOR_KEYWORD));
        int singleLineIf = builder.resetSingleLineIf();
        try {
            if (builder.getTokenType() == NEW_LINE || builder.eof()) {
                builder.pushBlock(VisualBasicBuilder.BlockKind.LAMBDA, keyword);
                StatementParsing.parseBlock(builder);
                builder.popBlock();
                expectEnd(builder, keyword);
            }
            else if (keyword == FUNCTION_KEYWORD) {
                if (parseExpression(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedExpression());
                }
            }
            else {
                PsiBuilder.Marker body = builder.mark();
                if (!StatementParsing.parseStatement(builder)) {
                    builder.error(VisualBasicParserLocalize.expectedStatement());
                }
                body.done(CODE_BLOCK);
            }
        }
        finally {
            builder.restoreSingleLineIf(singleLineIf);
            builder.exitMethod();
        }
        marker.done(LAMBDA_EXPRESSION);
        return marker;
    }

    // queries

    private static boolean isQueryStart(VisualBasicBuilder builder) {
        if (builder.isContextual(FROM_KEYWORD) || builder.isContextual(AGGREGATE_KEYWORD)) {
            IElementType next = builder.lookAhead(1);
            if (next != IDENTIFIER) {
                return false;
            }
            IElementType afterName = builder.lookAhead(2);
            if (afterName == QUESTION) {
                afterName = builder.lookAhead(3);
            }
            return afterName == IN_KEYWORD || afterName == AS_KEYWORD;
        }
        return false;
    }

    // arguments

    public static void parseArgumentList(VisualBasicBuilder builder, IElementType elementType) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.skipLineBreaks();
        if (builder.getTokenType() == RPAR) {
            builder.advanceLexer();
            marker.done(elementType);
            return;
        }
        while (true) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == COMMA || tokenType == RPAR) {
                builder.mark().done(OMITTED_ARGUMENT);
            }
            else if (!parseArgument(builder)) {
                builder.error(VisualBasicParserLocalize.expectedArgument());
                break;
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
        marker.done(elementType);
    }

    private static boolean parseArgument(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        if ((builder.getTokenType() == IDENTIFIER || RESERVED_KEYWORDS.contains(builder.getTokenType())) && builder.lookAhead(1) == COLONEQ) {
            expectMemberName(builder);
            builder.advanceLexer();
            builder.skipLineBreaks();
            if (parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            marker.done(NAMED_ARGUMENT);
            return true;
        }
        if (parseExpression(builder) == null) {
            marker.drop();
            return false;
        }
        if (builder.getTokenType() == TO_KEYWORD) {
            builder.advanceLexer();
            builder.skipLineBreaks();
            if (parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
            marker.done(RANGE_ARGUMENT);
            return true;
        }
        marker.done(SIMPLE_ARGUMENT);
        return true;
    }
}
