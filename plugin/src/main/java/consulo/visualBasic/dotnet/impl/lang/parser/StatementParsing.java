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
import org.jspecify.annotations.Nullable;

/**
 * Executable statements. A block ends at its {@code End} statement, or earlier at a statement closing an enclosing
 * block ({@code Next}, {@code Loop}, {@code Else}, {@code Case}, {@code Catch}, an outer {@code End}, a declaration):
 * the block owner then reports the missing {@code End}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class StatementParsing extends QueryParsing {
    private static final TokenSet EXIT_KINDS = TokenSet.create(
        SUB_KEYWORD,
        FUNCTION_KEYWORD,
        PROPERTY_KEYWORD,
        OPERATOR_KEYWORD,
        DO_KEYWORD,
        FOR_KEYWORD,
        WHILE_KEYWORD,
        SELECT_KEYWORD,
        TRY_KEYWORD
    );

    private static final TokenSet CONTINUE_KINDS = TokenSet.create(DO_KEYWORD, FOR_KEYWORD, WHILE_KEYWORD);

    private static final TokenSet LOCAL_MODIFIERS = TokenSet.create(DIM_KEYWORD, CONST_KEYWORD, STATIC_KEYWORD);

    /**
     * Statements up to the end of the enclosing block, as {@link #CODE_BLOCK}.
     */
    public static void parseBlock(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        int singleLineIf = builder.resetSingleLineIf();
        while (!builder.eof()) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == NEW_LINE || tokenType == COLON) {
                builder.consumeStatementTerminator();
                continue;
            }
            if (isBlockEnd(builder)) {
                break;
            }
            parseStatementLine(builder);
        }
        builder.restoreSingleLineIf(singleLineIf);
        marker.done(CODE_BLOCK);
    }

    public static boolean isBlockEnd(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == END_KEYWORD) {
            IElementType next = builder.lookAhead(1);
            return next != null && builder.hasBlockClosedBy(next);
        }
        if (tokenType == ENDIF_KEYWORD || tokenType == ELSE_KEYWORD || tokenType == ELSEIF_KEYWORD) {
            return builder.hasBlock(BlockKind.IF);
        }
        if (tokenType == NEXT_KEYWORD) {
            return builder.hasBlock(BlockKind.FOR);
        }
        if (tokenType == LOOP_KEYWORD) {
            return builder.hasBlock(BlockKind.DO);
        }
        if (tokenType == WEND_KEYWORD) {
            return builder.hasBlock(BlockKind.WHILE);
        }
        if (tokenType == CASE_KEYWORD) {
            return builder.hasBlock(BlockKind.SELECT);
        }
        if (tokenType == CATCH_KEYWORD || tokenType == FINALLY_KEYWORD) {
            return builder.hasBlock(BlockKind.TRY);
        }
        if (tokenType == GET_KEYWORD || tokenType == SET_KEYWORD) {
            return builder.hasBlock(BlockKind.PROPERTY);
        }
        if (tokenType == ADDHANDLER_KEYWORD || tokenType == REMOVEHANDLER_KEYWORD || tokenType == RAISEEVENT_KEYWORD) {
            return builder.hasBlock(BlockKind.EVENT) && builder.lookAhead(1) == LPAR;
        }
        return isDeclarationStart(builder);
    }

    /**
     * Tokens which may start a declaration but not a statement.
     */
    public static boolean isDeclarationStart(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (MODIFIER_KEYWORDS.contains(tokenType)) {
            return !LOCAL_MODIFIERS.contains(tokenType);
        }
        if (DECLARATION_KEYWORDS.contains(tokenType)) {
            return true;
        }
        if (tokenType == INHERITS_KEYWORD || tokenType == IMPLEMENTS_KEYWORD || tokenType == IMPORTS_KEYWORD || tokenType == OPTION_KEYWORD) {
            return true;
        }
        if (tokenType == LT) {
            return true;
        }
        return isContextualModifier(builder);
    }

    private static void parseStatementLine(VisualBasicBuilder builder) {
        int offset = builder.getCurrentOffset();
        if (parseStatement(builder)) {
            // a block statement may end at a statement closing an enclosing block
            if (!builder.isAtStatementStart() || !isBlockEnd(builder)) {
                expectStatementEnd(builder);
            }
        }
        else {
            skipToStatementEnd(builder, VisualBasicParserLocalize.expectedStatement());
        }
        if (offset == builder.getCurrentOffset() && !builder.eof() && !builder.isAtStatementEnd()) {
            PsiBuilder.Marker error = builder.mark();
            builder.advanceLexer();
            error.error(VisualBasicParserLocalize.expectedStatement());
        }
    }

    public static boolean parseStatement(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == null) {
            return false;
        }
        if ((tokenType == IDENTIFIER || tokenType == INTEGER_LITERAL) && builder.lookAhead(1) == COLON) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            builder.advanceLexer();
            marker.done(LABEL_STATEMENT);
            return true;
        }
        if (LOCAL_MODIFIERS.contains(tokenType)) {
            PsiBuilder.Marker marker = builder.mark();
            parseModifierList(builder, MODIFIER_KEYWORDS);
            parseVariableDeclarators(builder, LOCAL_VARIABLE);
            marker.done(LOCAL_DECLARATION_STATEMENT);
            return true;
        }
        if (tokenType == IF_KEYWORD) {
            parseIf(builder);
            return true;
        }
        if (tokenType == SELECT_KEYWORD) {
            parseSelect(builder);
            return true;
        }
        if (tokenType == WHILE_KEYWORD) {
            parseWhile(builder);
            return true;
        }
        if (tokenType == DO_KEYWORD) {
            parseDo(builder);
            return true;
        }
        if (tokenType == FOR_KEYWORD) {
            parseFor(builder);
            return true;
        }
        if (tokenType == TRY_KEYWORD) {
            parseTry(builder);
            return true;
        }
        if (tokenType == USING_KEYWORD) {
            parseUsing(builder);
            return true;
        }
        if (tokenType == SYNCLOCK_KEYWORD) {
            parseExpressionBlock(builder, BlockKind.SYNC_LOCK, SYNCLOCK_KEYWORD, SYNC_LOCK_STATEMENT);
            return true;
        }
        if (tokenType == WITH_KEYWORD) {
            parseExpressionBlock(builder, BlockKind.WITH, WITH_KEYWORD, WITH_STATEMENT);
            return true;
        }
        if (tokenType == RETURN_KEYWORD) {
            parseKeywordWithOptionalExpression(builder, RETURN_STATEMENT);
            return true;
        }
        if (tokenType == THROW_KEYWORD) {
            parseKeywordWithOptionalExpression(builder, THROW_STATEMENT);
            return true;
        }
        if (tokenType == EXIT_KEYWORD) {
            parseKeywordWithKind(builder, EXIT_KINDS, EXIT_STATEMENT);
            return true;
        }
        if (tokenType == CONTINUE_KEYWORD) {
            parseKeywordWithKind(builder, CONTINUE_KINDS, CONTINUE_STATEMENT);
            return true;
        }
        if (tokenType == GOTO_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            parseLabelReference(builder);
            marker.done(GOTO_STATEMENT);
            return true;
        }
        if (tokenType == STOP_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            marker.done(STOP_STATEMENT);
            return true;
        }
        if (tokenType == END_KEYWORD) {
            parseEnd(builder);
            return true;
        }
        if (tokenType == ON_KEYWORD && builder.lookAhead(1) == ERROR_KEYWORD) {
            parseOnError(builder);
            return true;
        }
        if (tokenType == RESUME_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            if (builder.getTokenType() == NEXT_KEYWORD) {
                builder.advanceLexer();
            }
            else if (!builder.isAtStatementEnd()) {
                parseLabelReference(builder);
            }
            marker.done(RESUME_STATEMENT);
            return true;
        }
        if (tokenType == ERROR_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            expectExpressionHere(builder);
            marker.done(ERROR_STATEMENT);
            return true;
        }
        if (tokenType == RAISEEVENT_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            PsiBuilder.Marker reference = builder.mark();
            expectIdentifier(builder);
            reference.done(REFERENCE_EXPRESSION);
            if (builder.getTokenType() == LPAR) {
                parseArgumentList(builder, ARGUMENT_LIST);
            }
            marker.done(RAISE_EVENT_STATEMENT);
            return true;
        }
        if (tokenType == ADDHANDLER_KEYWORD || tokenType == REMOVEHANDLER_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            expectExpressionHere(builder);
            if (expect(builder, COMMA)) {
                builder.skipLineBreaks();
                expectExpressionHere(builder);
            }
            marker.done(tokenType == ADDHANDLER_KEYWORD ? ADD_HANDLER_STATEMENT : REMOVE_HANDLER_STATEMENT);
            return true;
        }
        if (tokenType == ERASE_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            parseExpressionList(builder);
            marker.done(ERASE_STATEMENT);
            return true;
        }
        if (tokenType == REDIM_KEYWORD) {
            parseReDim(builder);
            return true;
        }
        if (tokenType == CALL_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            expectExpressionHere(builder);
            marker.done(CALL_STATEMENT);
            return true;
        }
        if (builder.isContextual(YIELD_KEYWORD) && (builder.isIterator() || isExpressionStartAfterYield(builder))) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceAs(YIELD_KEYWORD);
            expectExpressionHere(builder);
            marker.done(YIELD_STATEMENT);
            return true;
        }
        return parseExpressionStatement(builder);
    }

    private static boolean isExpressionStartAfterYield(VisualBasicBuilder builder) {
        IElementType next = builder.lookAhead(1);
        return next != null && next != EQ && next != DOT && next != LPAR && next != NEW_LINE && next != COLON && !ASSIGNMENT_OPERATORS.contains(next);
    }

    private static boolean parseExpressionStatement(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        PsiBuilder.Marker target = isAwait(builder) ? parseExpression(builder) : parseTerm(builder);
        if (target == null) {
            marker.drop();
            return false;
        }
        if (ASSIGNMENT_OPERATORS.contains(builder.getCompositeTokenType())) {
            builder.advanceComposite();
            builder.skipLineBreaks();
            expectExpressionHere(builder);
            marker.done(ASSIGNMENT_STATEMENT);
        }
        else {
            marker.done(EXPRESSION_STATEMENT);
        }
        return true;
    }

    private static void expectExpressionHere(VisualBasicBuilder builder) {
        if (parseExpression(builder) == null) {
            builder.error(VisualBasicParserLocalize.expectedExpression());
        }
    }

    private static void parseExpressionList(VisualBasicBuilder builder) {
        while (true) {
            expectExpressionHere(builder);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
    }

    private static void parseLabelReference(VisualBasicBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == IDENTIFIER || tokenType == INTEGER_LITERAL) {
            PsiBuilder.Marker reference = builder.mark();
            builder.advanceLexer();
            reference.done(REFERENCE_EXPRESSION);
        }
        else {
            builder.error(VisualBasicParserLocalize.expectedIdentifier());
        }
    }

    private static void parseKeywordWithOptionalExpression(VisualBasicBuilder builder, IElementType elementType) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (!builder.isAtStatementEnd()) {
            expectExpressionHere(builder);
        }
        marker.done(elementType);
    }

    private static void parseKeywordWithKind(VisualBasicBuilder builder, TokenSet kinds, IElementType elementType) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (kinds.contains(builder.getTokenType())) {
            builder.advanceLexer();
        }
        else {
            builder.error(VisualBasicParserLocalize.expectedStatement());
        }
        marker.done(elementType);
    }

    /**
     * {@code End} statement, or {@code End X} without an open {@code X} block.
     */
    private static void parseEnd(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        IElementType next = builder.getTokenType();
        if (next != null && !builder.isAtStatementEnd() && KEYWORDS.contains(next)) {
            builder.advanceLexer();
            marker.error(VisualBasicParserLocalize.endWithoutBegin0(tokenText(next)));
            return;
        }
        marker.done(END_STATEMENT);
    }

    private static void parseOnError(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.advanceLexer();
        if (builder.getTokenType() == GOTO_KEYWORD) {
            builder.advanceLexer();
            if (builder.getTokenType() == MINUS) {
                builder.advanceLexer();
            }
            parseLabelReference(builder);
        }
        else if (builder.getTokenType() == RESUME_KEYWORD) {
            builder.advanceLexer();
            expect(builder, NEXT_KEYWORD);
        }
        else {
            builder.error(VisualBasicParserLocalize.expected0("GoTo"));
        }
        marker.done(ON_ERROR_STATEMENT);
    }

    private static void parseReDim(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        builder.expectContextual(PRESERVE_KEYWORD);
        while (true) {
            PsiBuilder.Marker clause = builder.mark();
            if (parseTerm(builder) == null) {
                clause.drop();
                builder.error(VisualBasicParserLocalize.expectedExpression());
                break;
            }
            clause.done(REDIM_CLAUSE);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
        marker.done(REDIM_STATEMENT);
    }

    // variables

    /**
     * {@code a, b As Integer, c As New Foo(), d = 1}
     */
    public static void parseVariableDeclarators(VisualBasicBuilder builder, IElementType variableType) {
        while (true) {
            PsiBuilder.Marker declarator = builder.mark();
            while (true) {
                PsiBuilder.Marker variable = builder.mark();
                expectDeclarationName(builder);
                parseNameModifiers(builder, true);
                variable.done(variableType);
                if (builder.getTokenType() == COMMA && isNextVariableName(builder)) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    continue;
                }
                break;
            }
            if (builder.getTokenType() == AS_KEYWORD) {
                builder.advanceLexer();
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
                expectExpressionHere(builder);
            }
            declarator.done(VARIABLE_DECLARATOR);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
    }

    /**
     * Inside a declarator {@code a, b As T}: the comma continues the names when the next one is not followed by
     * an initializer of its own declarator.
     */
    private static boolean isNextVariableName(VisualBasicBuilder builder) {
        return builder.lookAhead(1, true) == IDENTIFIER;
    }

    private static void parseControlVariable(VisualBasicBuilder builder, IElementType after) {
        if (builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == after) {
            // Option Infer: a new local, unless the name is declared already
            PsiBuilder.Marker variable = builder.mark();
            builder.advanceLexer();
            variable.done(LOCAL_VARIABLE);
            return;
        }
        if (builder.getTokenType() == IDENTIFIER) {
            IElementType next = builder.lookAhead(1);
            if (next == AS_KEYWORD || next == QUESTION && builder.lookAhead(2) == AS_KEYWORD) {
                PsiBuilder.Marker variable = builder.mark();
                builder.advanceLexer();
                parseNameModifiers(builder, false);
                builder.advanceLexer();
                if (parseType(builder) == null) {
                    builder.error(VisualBasicParserLocalize.expectedType());
                }
                variable.done(LOCAL_VARIABLE);
                return;
            }
        }
        if (parseTerm(builder) == null) {
            builder.error(VisualBasicParserLocalize.expectedIdentifier());
        }
    }

    // block statements

    private static void parseBody(VisualBasicBuilder builder, BlockKind kind, @Nullable IElementType endKeyword) {
        expectStatementEnd(builder);
        builder.pushBlock(kind, endKeyword);
        parseBlock(builder);
        builder.popBlock();
    }

    private static void parseIf(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        expectExpressionHere(builder);
        boolean then = builder.getTokenType() == THEN_KEYWORD;
        if (then) {
            builder.advanceLexer();
        }

        if (then && !builder.eof() && builder.getTokenType() != NEW_LINE) {
            parseSingleLineIfBody(builder);
            marker.done(IF_STATEMENT);
            return;
        }

        parseBody(builder, BlockKind.IF, IF_KEYWORD);
        boolean hasElse = false;
        while (!hasElse) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == ELSEIF_KEYWORD || tokenType == ELSE_KEYWORD && builder.lookAhead(1) == IF_KEYWORD) {
                PsiBuilder.Marker clause = builder.mark();
                builder.advanceLexer();
                if (tokenType == ELSE_KEYWORD) {
                    builder.advanceLexer();
                }
                expectExpressionHere(builder);
                if (builder.getTokenType() == THEN_KEYWORD) {
                    builder.advanceLexer();
                }
                parseBody(builder, BlockKind.IF, IF_KEYWORD);
                clause.done(ELSE_IF_CLAUSE);
            }
            else if (tokenType == ELSE_KEYWORD) {
                PsiBuilder.Marker clause = builder.mark();
                builder.advanceLexer();
                parseBody(builder, BlockKind.IF, IF_KEYWORD);
                clause.done(ELSE_CLAUSE);
                hasElse = true;
            }
            else {
                break;
            }
        }
        expectEnd(builder, IF_KEYWORD);
        marker.done(IF_STATEMENT);
    }

    /**
     * {@code If c Then a : b Else d : e} on one line.
     */
    private static void parseSingleLineIfBody(VisualBasicBuilder builder) {
        builder.enterSingleLineIf();
        try {
            parseSingleLineStatements(builder);
            if (builder.getTokenType() == ELSE_KEYWORD) {
                PsiBuilder.Marker clause = builder.mark();
                builder.advanceLexer();
                parseSingleLineStatements(builder);
                clause.done(ELSE_CLAUSE);
            }
        }
        finally {
            builder.exitSingleLineIf();
        }
    }

    private static void parseSingleLineStatements(VisualBasicBuilder builder) {
        PsiBuilder.Marker block = builder.mark();
        while (true) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == null || tokenType == NEW_LINE || tokenType == ELSE_KEYWORD) {
                break;
            }
            if (tokenType == COLON) {
                builder.advanceLexer();
                continue;
            }
            int offset = builder.getCurrentOffset();
            if (parseStatement(builder)) {
                expectStatementEnd(builder);
            }
            else {
                skipToStatementEnd(builder, VisualBasicParserLocalize.expectedStatement());
            }
            if (offset == builder.getCurrentOffset()) {
                break;
            }
        }
        block.done(CODE_BLOCK);
    }

    private static void parseSelect(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (builder.getTokenType() == CASE_KEYWORD) {
            builder.advanceLexer();
        }
        expectExpressionHere(builder);
        expectStatementEnd(builder);
        builder.pushBlock(BlockKind.SELECT, SELECT_KEYWORD);
        while (!builder.eof()) {
            IElementType tokenType = builder.getTokenType();
            if (tokenType == NEW_LINE || tokenType == COLON) {
                builder.consumeStatementTerminator();
                continue;
            }
            if (tokenType == CASE_KEYWORD) {
                PsiBuilder.Marker caseBlock = builder.mark();
                builder.advanceLexer();
                if (builder.getTokenType() == ELSE_KEYWORD) {
                    builder.advanceLexer();
                }
                else {
                    parseCaseClauses(builder);
                }
                parseBody(builder, BlockKind.SELECT, SELECT_KEYWORD);
                caseBlock.done(CASE_BLOCK);
                continue;
            }
            if (isBlockEnd(builder)) {
                break;
            }
            // statements before the first Case
            parseStatementLine(builder);
        }
        builder.popBlock();
        expectEnd(builder, SELECT_KEYWORD);
        marker.done(SELECT_STATEMENT);
    }

    private static void parseCaseClauses(VisualBasicBuilder builder) {
        while (true) {
            PsiBuilder.Marker clause = builder.mark();
            if (builder.getTokenType() == IS_KEYWORD || RELATIONAL_OPERATORS.contains(builder.getCompositeTokenType())) {
                if (builder.getTokenType() == IS_KEYWORD) {
                    builder.advanceLexer();
                }
                if (RELATIONAL_OPERATORS.contains(builder.getCompositeTokenType())) {
                    builder.advanceComposite();
                }
                else {
                    builder.error(VisualBasicParserLocalize.expected0("="));
                }
                expectExpressionHere(builder);
                clause.done(RELATIONAL_CASE_CLAUSE);
            }
            else {
                expectExpressionHere(builder);
                if (builder.getTokenType() == TO_KEYWORD) {
                    builder.advanceLexer();
                    expectExpressionHere(builder);
                    clause.done(RANGE_CASE_CLAUSE);
                }
                else {
                    clause.done(SIMPLE_CASE_CLAUSE);
                }
            }
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
    }

    private static void parseWhile(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        expectExpressionHere(builder);
        parseBody(builder, BlockKind.WHILE, WHILE_KEYWORD);
        if (builder.getTokenType() == WEND_KEYWORD) {
            builder.advanceLexer();
        }
        else {
            expectEnd(builder, WHILE_KEYWORD);
        }
        marker.done(WHILE_STATEMENT);
    }

    private static void parseDo(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        parseLoopConditionIfAny(builder);
        parseBody(builder, BlockKind.DO, null);
        if (expect(builder, LOOP_KEYWORD)) {
            parseLoopConditionIfAny(builder);
        }
        marker.done(DO_LOOP_STATEMENT);
    }

    private static void parseLoopConditionIfAny(VisualBasicBuilder builder) {
        if (builder.getTokenType() != WHILE_KEYWORD && !builder.isContextual(UNTIL_KEYWORD)) {
            return;
        }
        PsiBuilder.Marker condition = builder.mark();
        if (builder.getTokenType() == WHILE_KEYWORD) {
            builder.advanceLexer();
        }
        else {
            builder.advanceAs(UNTIL_KEYWORD);
        }
        expectExpressionHere(builder);
        condition.done(LOOP_CONDITION);
    }

    private static void parseFor(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        boolean forEach = builder.getTokenType() == EACH_KEYWORD;
        if (forEach) {
            builder.advanceLexer();
            parseControlVariable(builder, IN_KEYWORD);
            if (expect(builder, IN_KEYWORD)) {
                builder.skipLineBreaks();
                expectExpressionHere(builder);
            }
        }
        else {
            parseControlVariable(builder, EQ);
            if (expect(builder, EQ)) {
                builder.skipLineBreaks();
                expectExpressionHere(builder);
                if (expect(builder, TO_KEYWORD)) {
                    expectExpressionHere(builder);
                    if (builder.getTokenType() == STEP_KEYWORD) {
                        builder.advanceLexer();
                        expectExpressionHere(builder);
                    }
                }
            }
        }
        parseBody(builder, BlockKind.FOR, null);
        if (expect(builder, NEXT_KEYWORD)) {
            if (!builder.isAtStatementEnd()) {
                parseExpressionList(builder);
            }
        }
        marker.done(forEach ? FOR_EACH_STATEMENT : FOR_STATEMENT);
    }

    private static void parseTry(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        parseBody(builder, BlockKind.TRY, TRY_KEYWORD);
        while (true) {
            if (builder.getTokenType() == CATCH_KEYWORD) {
                PsiBuilder.Marker clause = builder.mark();
                builder.advanceLexer();
                if (builder.getTokenType() == IDENTIFIER) {
                    if (builder.lookAhead(1) == AS_KEYWORD) {
                        PsiBuilder.Marker variable = builder.mark();
                        builder.advanceLexer();
                        builder.advanceLexer();
                        if (parseType(builder) == null) {
                            builder.error(VisualBasicParserLocalize.expectedType());
                        }
                        variable.done(LOCAL_VARIABLE);
                    }
                    else {
                        PsiBuilder.Marker reference = builder.mark();
                        builder.advanceLexer();
                        reference.done(REFERENCE_EXPRESSION);
                    }
                }
                if (builder.getTokenType() == WHEN_KEYWORD) {
                    builder.advanceLexer();
                    expectExpressionHere(builder);
                }
                parseBody(builder, BlockKind.TRY, TRY_KEYWORD);
                clause.done(CATCH_CLAUSE);
            }
            else if (builder.getTokenType() == FINALLY_KEYWORD) {
                PsiBuilder.Marker clause = builder.mark();
                builder.advanceLexer();
                parseBody(builder, BlockKind.TRY, TRY_KEYWORD);
                clause.done(FINALLY_CLAUSE);
            }
            else {
                break;
            }
        }
        expectEnd(builder, TRY_KEYWORD);
        marker.done(TRY_STATEMENT);
    }

    private static void parseUsing(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        IElementType next = builder.lookAhead(1);
        if (builder.getTokenType() == IDENTIFIER && (next == AS_KEYWORD || next == EQ)) {
            parseVariableDeclarators(builder, LOCAL_VARIABLE);
        }
        else {
            expectExpressionHere(builder);
        }
        parseBody(builder, BlockKind.USING, USING_KEYWORD);
        expectEnd(builder, USING_KEYWORD);
        marker.done(USING_STATEMENT);
    }

    private static void parseExpressionBlock(VisualBasicBuilder builder, BlockKind kind, IElementType keyword, IElementType elementType) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        expectExpressionHere(builder);
        parseBody(builder, kind, keyword);
        expectEnd(builder, keyword);
        marker.done(elementType);
    }
}
