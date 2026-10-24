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
import consulo.visualBasic.dotnet.impl.lang.parser.VisualBasicBuilder.BlockKind;
import consulo.visualBasic.dotnet.impl.localize.VisualBasicParserLocalize;

import java.util.Set;

/**
 * File level and member declarations.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class DeclarationParsing extends StatementParsing {
    private static final TokenSet OVERLOADABLE_OPERATORS = TokenSet.create(
        PLUS,
        MINUS,
        ASTERISK,
        SLASH,
        BACKSLASH,
        CARET,
        AMPERSAND,
        LIKE_KEYWORD,
        MOD_KEYWORD,
        AND_KEYWORD,
        OR_KEYWORD,
        XOR_KEYWORD,
        NOT_KEYWORD,
        LTLT,
        GTGT,
        EQ,
        LTGT,
        LT,
        GT,
        LTEQ,
        GTEQ,
        CTYPE_KEYWORD
    );

    private static final TokenSet PROPERTY_ACCESSORS = TokenSet.create(GET_KEYWORD, SET_KEYWORD);

    private static final TokenSet EVENT_ACCESSORS = TokenSet.create(ADDHANDLER_KEYWORD, REMOVEHANDLER_KEYWORD, RAISEEVENT_KEYWORD);

    public static void parseFile(VisualBasicBuilder builder) {
        builder.pushBlock(BlockKind.FILE, null);
        parseMembers(builder);
        builder.popBlock();
    }

    /**
     * Declarations up to the {@code End} of the enclosing block.
     */
    private static void parseMembers(VisualBasicBuilder builder) {
        while (!builder.eof()) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == NEW_LINE || tokenType == COLON) {
                builder.consumeStatementTerminator();
                continue;
            }
            if (isMemberBlockEnd(builder)) {
                break;
            }
            parseDeclarationLine(builder);
        }
    }

    private static boolean isMemberBlockEnd(VisualBasicBuilder builder) {
        if (builder.getTokenType() != END_KEYWORD) {
            return false;
        }
        IElementType next = builder.lookAhead(1);
        return next != null && builder.hasBlockClosedBy(next);
    }

    private static void parseDeclarationLine(VisualBasicBuilder builder) {
        int offset = builder.getCurrentOffset();
        if (parseDeclaration(builder)) {
            // a declaration with a body may end at the start of the next declaration or at an enclosing End
            if (!builder.isAtStatementStart() || !isMemberBlockEnd(builder) && !isDeclarationStart(builder)) {
                expectStatementEnd(builder);
            }
        }
        else {
            skipToStatementEnd(builder, VisualBasicParserLocalize.expectedDeclaration());
        }
        if (offset == builder.getCurrentOffset() && !builder.eof() && !builder.isAtStatementEnd()) {
            PsiBuilder.Marker error = builder.mark();
            builder.advanceLexer();
            error.error(VisualBasicParserLocalize.expectedDeclaration());
        }
    }

    private static boolean parseDeclaration(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == OPTION_KEYWORD) {
            parseOption(builder);
            return true;
        }
        if (tokenType == IMPORTS_KEYWORD) {
            parseImports(builder);
            return true;
        }
        if (tokenType == INHERITS_KEYWORD || tokenType == IMPLEMENTS_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            parseTypeList(builder);
            marker.done(tokenType == INHERITS_KEYWORD ? INHERITS_STATEMENT : IMPLEMENTS_STATEMENT);
            return true;
        }
        if (tokenType == END_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            IElementType next = builder.getTokenType();
            if (next != null && KEYWORDS.contains(next)) {
                builder.advanceLexer();
                marker.error(VisualBasicParserLocalize.endWithoutBegin0(tokenText(next)));
            }
            else {
                marker.error(VisualBasicParserLocalize.expectedDeclaration());
            }
            return true;
        }
        if (tokenType == LT && isAttributesStatement(builder)) {
            parseAttributeList(builder);
            return true;
        }

        PsiBuilder.Marker marker = builder.mark();
        Set<IElementType> modifiers = parseModifierList(builder, MODIFIER_KEYWORDS);
        tokenType = builder.getTokenType();
        if (tokenType == CLASS_KEYWORD || tokenType == STRUCTURE_KEYWORD || tokenType == MODULE_KEYWORD || tokenType == INTERFACE_KEYWORD) {
            parseTypeDeclaration(builder, marker);
        }
        else if (tokenType == ENUM_KEYWORD) {
            parseEnum(builder, marker);
        }
        else if (tokenType == DELEGATE_KEYWORD) {
            parseDelegate(builder, marker);
        }
        else if (tokenType == SUB_KEYWORD && builder.lookAhead(1) == NEW_KEYWORD) {
            parseMethod(builder, marker, modifiers, CONSTRUCTOR_DECLARATION);
        }
        else if (tokenType == SUB_KEYWORD || tokenType == FUNCTION_KEYWORD) {
            parseMethod(builder, marker, modifiers, METHOD_DECLARATION);
        }
        else if (tokenType == OPERATOR_KEYWORD) {
            parseOperator(builder, marker, modifiers);
        }
        else if (tokenType == PROPERTY_KEYWORD) {
            parseProperty(builder, marker, modifiers);
        }
        else if (tokenType == EVENT_KEYWORD) {
            parseEvent(builder, marker, modifiers);
        }
        else if (tokenType == DECLARE_KEYWORD) {
            parseExternalMethod(builder, marker);
        }
        else if (tokenType == NAMESPACE_KEYWORD) {
            parseNamespace(builder, marker);
        }
        else if (tokenType == IDENTIFIER && !modifiers.isEmpty()) {
            parseVariableDeclarators(builder, FIELD_DECLARATION);
            marker.done(FIELD_DECLARATION_STATEMENT);
        }
        else {
            marker.rollbackTo();
            return false;
        }
        return true;
    }

    /**
     * {@code <Assembly: A>} or {@code <Module: A>} as a statement.
     */
    private static boolean isAttributesStatement(VisualBasicBuilder builder) {
        IElementType target = builder.lookAhead(1);
        boolean hasTarget = target == MODULE_KEYWORD || builder.isContextualAt(1, ASSEMBLY_KEYWORD, false);
        return hasTarget && builder.lookAhead(2) == COLON;
    }

    private static void parseOption(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (!builder.expectContextual(EXPLICIT_KEYWORD)
            && !builder.expectContextual(STRICT_KEYWORD)
            && !builder.expectContextual(INFER_KEYWORD)
            && !builder.expectContextual(COMPARE_KEYWORD)) {
            builder.error(VisualBasicParserLocalize.expectedIdentifier());
        }
        if (builder.getTokenType() == ON_KEYWORD) {
            builder.advanceLexer();
        }
        else if (!builder.expectContextual(OFF_KEYWORD) && !builder.expectContextual(BINARY_KEYWORD)) {
            builder.expectContextual(TEXT_KEYWORD);
        }
        marker.done(OPTION_STATEMENT);
    }

    private static void parseImports(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        while (true) {
            PsiBuilder.Marker clause = builder.mark();
            if (builder.getTokenType() == LT) {
                // XML namespace import: <xmlns:prefix="namespace">
                while (!builder.eof() && builder.getTokenType() != NEW_LINE && builder.getTokenType() != GT) {
                    builder.advanceLexer();
                }
                expect(builder, GT);
            }
            else if (builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == EQ) {
                builder.advanceLexer();
                builder.advanceLexer();
                parseQualifiedName(builder, true);
            }
            else if (builder.getTokenType() == IDENTIFIER || builder.getTokenType() == GLOBAL_KEYWORD) {
                parseQualifiedName(builder, true);
            }
            else {
                builder.error(VisualBasicParserLocalize.expectedIdentifier());
            }
            clause.done(IMPORTS_CLAUSE);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        marker.done(IMPORTS_STATEMENT);
    }

    private static void parseTypeList(VisualBasicBuilder builder) {
        while (true) {
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
                return;
            }
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            return;
        }
    }

    private static void parseNamespace(VisualBasicBuilder builder, PsiBuilder.Marker marker) {
        builder.advanceLexer();
        if (builder.getTokenType() == IDENTIFIER || builder.getTokenType() == GLOBAL_KEYWORD) {
            parseQualifiedName(builder, false);
        }
        else {
            builder.error(VisualBasicParserLocalize.expectedIdentifier());
        }
        expectStatementEnd(builder);
        builder.pushBlock(BlockKind.NAMESPACE, NAMESPACE_KEYWORD);
        parseMembers(builder);
        builder.popBlock();
        expectEnd(builder, NAMESPACE_KEYWORD);
        marker.done(NAMESPACE_DECLARATION);
    }

    private static void parseTypeDeclaration(VisualBasicBuilder builder, PsiBuilder.Marker marker) {
        IElementType keyword = currentToken(builder);
        builder.advanceLexer();
        expectDeclarationName(builder);
        parseGenericParameterListIfAny(builder);
        expectStatementEnd(builder);
        builder.pushBlock(keyword == INTERFACE_KEYWORD ? BlockKind.INTERFACE : BlockKind.TYPE, keyword);
        parseMembers(builder);
        builder.popBlock();
        expectEnd(builder, keyword);
        marker.done(TYPE_DECLARATION);
    }

    private static void parseEnum(VisualBasicBuilder builder, PsiBuilder.Marker marker) {
        builder.advanceLexer();
        expectDeclarationName(builder);
        if (builder.getTokenType() == AS_KEYWORD) {
            builder.advanceLexer();
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
        }
        expectStatementEnd(builder);
        builder.pushBlock(BlockKind.ENUM, ENUM_KEYWORD);
        while (!builder.eof()) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == NEW_LINE || tokenType == COLON) {
                builder.consumeStatementTerminator();
                continue;
            }
            if (isMemberBlockEnd(builder) || isDeclarationStart(builder) && tokenType != LT) {
                break;
            }
            PsiBuilder.Marker member = builder.mark();
            parseModifierList(builder, TokenSet.EMPTY);
            if (builder.getTokenType() == IDENTIFIER) {
                builder.advanceLexer();
                if (builder.getTokenType() == EQ) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    if (parseExpression(builder) == null) {
                        builder.error(VisualBasicParserLocalize.expectedExpression());
                    }
                }
                member.done(ENUM_MEMBER_DECLARATION);
                expectStatementEnd(builder);
            }
            else {
                member.rollbackTo();
                skipToStatementEnd(builder, VisualBasicParserLocalize.expectedIdentifier());
                if (builder.getTokenType() == LT) {
                    // attributes without a member: consume to avoid a loop
                    PsiBuilder.Marker error = builder.mark();
                    builder.advanceLexer();
                    error.error(VisualBasicParserLocalize.expectedIdentifier());
                }
            }
        }
        builder.popBlock();
        expectEnd(builder, ENUM_KEYWORD);
        marker.done(ENUM_DECLARATION);
    }

    private static void parseDelegate(VisualBasicBuilder builder, PsiBuilder.Marker marker) {
        builder.advanceLexer();
        if (builder.getTokenType() == SUB_KEYWORD || builder.getTokenType() == FUNCTION_KEYWORD) {
            builder.advanceLexer();
        }
        else {
            builder.error(VisualBasicParserLocalize.expected0("Sub"));
        }
        expectIdentifier(builder);
        parseGenericParameterListIfAny(builder);
        if (builder.getTokenType() == LPAR) {
            parseParameterList(builder);
        }
        parseReturnTypeIfAny(builder);
        marker.done(DELEGATE_DECLARATION);
    }

    private static void parseReturnTypeIfAny(VisualBasicBuilder builder) {
        if (builder.getTokenType() != AS_KEYWORD) {
            return;
        }
        builder.advanceLexer();
        while (builder.getTokenType() == LT) {
            parseAttributeList(builder);
        }
        if (parseType(builder) == null) {
            builder.error(VisualBasicParserLocalize.expectedType());
        }
    }

    private static boolean isBodyless(VisualBasicBuilder builder, Set<IElementType> modifiers) {
        VisualBasicBuilder.Block block = builder.getInnermostBlock();
        return modifiers.contains(MUSTOVERRIDE_KEYWORD) || block != null && block.kind() == BlockKind.INTERFACE;
    }

    private static void parseMethod(VisualBasicBuilder builder, PsiBuilder.Marker marker, Set<IElementType> modifiers, IElementType elementType) {
        IElementType keyword = currentToken(builder);
        builder.advanceLexer();
        if (elementType == CONSTRUCTOR_DECLARATION) {
            builder.advanceLexer();
        }
        else {
            expectDeclarationName(builder);
            parseGenericParameterListIfAny(builder);
        }
        if (builder.getTokenType() == LPAR) {
            parseParameterList(builder);
        }
        parseReturnTypeIfAny(builder);
        if (builder.getTokenType() == HANDLES_KEYWORD) {
            PsiBuilder.Marker clause = builder.mark();
            builder.advanceLexer();
            while (true) {
                if (parseTerm(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedIdentifier());
                    break;
                }
                if (builder.getTokenType() == COMMA) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    continue;
                }
                break;
            }
            clause.done(HANDLES_CLAUSE);
        }
        parseImplementsClauseIfAny(builder);
        if (!isBodyless(builder, modifiers)) {
            parseMemberBody(builder, BlockKind.METHOD, keyword, modifiers);
        }
        marker.done(elementType);
    }

    private static void parseMemberBody(VisualBasicBuilder builder, BlockKind kind, IElementType keyword, Set<IElementType> modifiers) {
        expectStatementEnd(builder);
        builder.enterMethod(modifiers.contains(ASYNC_KEYWORD), modifiers.contains(ITERATOR_KEYWORD));
        builder.pushBlock(kind, keyword);
        parseBlock(builder);
        builder.popBlock();
        builder.exitMethod();
        expectEnd(builder, keyword);
    }

    private static void parseImplementsClauseIfAny(VisualBasicBuilder builder) {
        if (builder.getTokenType() != IMPLEMENTS_KEYWORD) {
            return;
        }
        PsiBuilder.Marker clause = builder.mark();
        builder.advanceLexer();
        while (true) {
            if (builder.getTokenType() != IDENTIFIER && builder.getTokenType() != GLOBAL_KEYWORD) {
                builder.error(VisualBasicParserLocalize.expectedIdentifier());
                break;
            }
            parseQualifiedName(builder, true);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        clause.done(IMPLEMENTS_CLAUSE);
    }

    private static void parseOperator(VisualBasicBuilder builder, PsiBuilder.Marker marker, Set<IElementType> modifiers) {
        builder.advanceLexer();
        if (OVERLOADABLE_OPERATORS.contains(builder.getCompositeTokenType())) {
            builder.advanceComposite();
        }
        else if (!builder.expectContextual(ISTRUE_KEYWORD) && !builder.expectContextual(ISFALSE_KEYWORD)) {
            builder.error(VisualBasicParserLocalize.expected0("+"));
        }
        if (builder.getTokenType() == LPAR) {
            parseParameterList(builder);
        }
        parseReturnTypeIfAny(builder);
        if (!isBodyless(builder, modifiers)) {
            parseMemberBody(builder, BlockKind.METHOD, OPERATOR_KEYWORD, modifiers);
        }
        marker.done(OPERATOR_DECLARATION);
    }

    private static void parseExternalMethod(VisualBasicBuilder builder, PsiBuilder.Marker marker) {
        builder.advanceLexer();
        if (!builder.expectContextual(ANSI_KEYWORD) && !builder.expectContextual(UNICODE_KEYWORD)) {
            builder.expectContextual(AUTO_KEYWORD);
        }
        if (builder.getTokenType() == SUB_KEYWORD || builder.getTokenType() == FUNCTION_KEYWORD) {
            builder.advanceLexer();
        }
        else {
            builder.error(VisualBasicParserLocalize.expected0("Sub"));
        }
        expectIdentifier(builder);
        if (expect(builder, LIB_KEYWORD)) {
            expect(builder, STRING_LITERAL);
        }
        if (builder.getTokenType() == ALIAS_KEYWORD) {
            builder.advanceLexer();
            expect(builder, STRING_LITERAL);
        }
        if (builder.getTokenType() == LPAR) {
            parseParameterList(builder);
        }
        parseReturnTypeIfAny(builder);
        marker.done(EXTERNAL_METHOD_DECLARATION);
    }

    private static void parseProperty(VisualBasicBuilder builder, PsiBuilder.Marker marker, Set<IElementType> modifiers) {
        builder.advanceLexer();
        expectDeclarationName(builder);
        if (builder.getTokenType() == LPAR) {
            parseParameterList(builder);
        }
        if (builder.getTokenType() == AS_KEYWORD) {
            builder.advanceLexer();
            while (builder.getTokenType() == LT) {
                parseAttributeList(builder);
            }
            if (builder.getTokenType() == NEW_KEYWORD) {
                parseNew(builder);
            }
            else if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
        }
        if (builder.getTokenType() == EQ) {
            builder.advanceLexer();
            builder.skipLineBreaks();
            if (parseExpression(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedExpression());
            }
        }
        parseImplementsClauseIfAny(builder);
        if (!isBodyless(builder, modifiers) && isAccessorAhead(builder, PROPERTY_ACCESSORS)) {
            expectStatementEnd(builder);
            builder.pushBlock(BlockKind.PROPERTY, PROPERTY_KEYWORD);
            parseAccessors(builder, PROPERTY_ACCESSORS, modifiers.contains(ITERATOR_KEYWORD));
            builder.popBlock();
            expectEnd(builder, PROPERTY_KEYWORD);
        }
        marker.done(PROPERTY_DECLARATION);
    }

    /**
     * After the header of a property: does an accessor follow, so the property is not an auto-implemented one?
     */
    private static boolean isAccessorAhead(VisualBasicBuilder builder, TokenSet accessors) {
        int i = 0;
        while (builder.lookAhead(i, true) == COLON) {
            i++;
        }
        while (builder.lookAhead(i, true) == LT) {
            IElementType type;
            while ((type = builder.lookAhead(i, true)) != null && type != GT) {
                i++;
            }
            i++;
        }
        while (MODIFIER_KEYWORDS.contains(builder.lookAhead(i, true))) {
            i++;
        }
        return accessors.contains(builder.lookAhead(i, true));
    }

    private static void parseAccessors(VisualBasicBuilder builder, TokenSet accessors, boolean iterator) {
        while (!builder.eof()) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == NEW_LINE || tokenType == COLON) {
                builder.consumeStatementTerminator();
                continue;
            }
            if (isMemberBlockEnd(builder)) {
                break;
            }
            PsiBuilder.Marker accessor = builder.mark();
            parseModifierList(builder, MODIFIER_KEYWORDS);
            IElementType keyword = builder.getTokenType();
            if (keyword == null || !accessors.contains(keyword)) {
                accessor.rollbackTo();
                if (isDeclarationStart(builder)) {
                    // missing End Property / End Event
                    break;
                }
                int offset = builder.getCurrentOffset();
                skipToStatementEnd(builder, VisualBasicParserLocalize.expectedDeclaration());
                if (offset == builder.getCurrentOffset() && !builder.isAtStatementEnd()) {
                    builder.advanceLexer();
                }
                continue;
            }
            builder.advanceLexer();
            if (builder.getTokenType() == LPAR) {
                parseParameterList(builder);
            }
            expectStatementEnd(builder);
            builder.enterMethod(false, iterator && keyword == GET_KEYWORD);
            builder.pushBlock(BlockKind.ACCESSOR, keyword);
            parseBlock(builder);
            builder.popBlock();
            builder.exitMethod();
            expectEnd(builder, keyword);
            accessor.done(ACCESSOR_DECLARATION);
            if (!builder.isAtStatementStart() || !isMemberBlockEnd(builder) && !isBlockEnd(builder)) {
                expectStatementEnd(builder);
            }
        }
    }

    private static void parseEvent(VisualBasicBuilder builder, PsiBuilder.Marker marker, Set<IElementType> modifiers) {
        builder.advanceLexer();
        expectDeclarationName(builder);
        if (builder.getTokenType() == LPAR) {
            parseParameterList(builder);
        }
        if (builder.getTokenType() == AS_KEYWORD) {
            builder.advanceLexer();
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
        }
        parseImplementsClauseIfAny(builder);
        if (modifiers.contains(CUSTOM_KEYWORD)) {
            expectStatementEnd(builder);
            builder.pushBlock(BlockKind.EVENT, EVENT_KEYWORD);
            parseAccessors(builder, EVENT_ACCESSORS, false);
            builder.popBlock();
            expectEnd(builder, EVENT_KEYWORD);
        }
        marker.done(EVENT_DECLARATION);
    }
}
