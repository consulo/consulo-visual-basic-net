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
import consulo.language.parser.PsiBuilder;
import consulo.visualBasic.dotnet.impl.localize.VisualBasicParserLocalize;

/**
 * Query expressions: {@code From x In xs Where ... Select ...}.
 * Query operators may start on the next line (implicit line continuation).
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class QueryParsing extends ExpressionParsing {
    public static PsiBuilder.Marker parseQuery(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        if (builder.isContextual(AGGREGATE_KEYWORD)) {
            parseAggregate(builder);
        }
        else {
            parseFrom(builder);
        }
        while (true) {
            if (builder.getTokenType() == NEW_LINE && isClauseStart(builder, true)) {
                builder.skipLineBreaks();
            }
            if (!isClauseStart(builder, false) || !parseClause(builder)) {
                break;
            }
        }
        marker.done(QUERY_EXPRESSION);
        return marker;
    }

    private static boolean isClauseStart(VisualBasicBuilder builder, boolean afterLineBreaks) {
        int steps = 0;
        IElementType type = builder.lookAhead(steps, afterLineBreaks);
        if (type == SELECT_KEYWORD) {
            // Select Case on the next line is a statement
            return builder.lookAhead(steps + 1, afterLineBreaks) != CASE_KEYWORD;
        }
        if (type == LET_KEYWORD) {
            return true;
        }
        return builder.isContextualAt(steps, FROM_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, AGGREGATE_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, WHERE_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, ORDER_KEYWORD, afterLineBreaks) && builder.isContextualAt(steps + 1, BY_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, DISTINCT_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, SKIP_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, TAKE_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, JOIN_KEYWORD, afterLineBreaks)
            || builder.isContextualAt(steps, GROUP_KEYWORD, afterLineBreaks);
    }

    private static boolean parseClause(VisualBasicBuilder builder) {
        IElementType type = builder.getTokenType();
        if (type == SELECT_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            builder.skipLineBreaks();
            parseExpressionRangeVariables(builder);
            marker.done(SELECT_CLAUSE);
            return true;
        }
        if (type == LET_KEYWORD) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer();
            builder.skipLineBreaks();
            parseExpressionRangeVariables(builder);
            marker.done(LET_CLAUSE);
            return true;
        }
        if (builder.isContextual(FROM_KEYWORD)) {
            parseFrom(builder);
            return true;
        }
        if (builder.isContextual(AGGREGATE_KEYWORD)) {
            parseAggregate(builder);
            return true;
        }
        if (builder.isContextual(WHERE_KEYWORD)) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceAs(WHERE_KEYWORD);
            builder.skipLineBreaks();
            expectExpression(builder);
            marker.done(WHERE_CLAUSE);
            return true;
        }
        if (builder.isContextual(ORDER_KEYWORD)) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceAs(ORDER_KEYWORD);
            if (!builder.expectContextual(BY_KEYWORD)) {
                builder.error(VisualBasicParserLocalize.expected0("By"));
            }
            builder.skipLineBreaks();
            while (true) {
                PsiBuilder.Marker ordering = builder.mark();
                expectExpression(builder);
                if (!builder.expectContextual(ASCENDING_KEYWORD)) {
                    builder.expectContextual(DESCENDING_KEYWORD);
                }
                ordering.done(ORDERING);
                if (builder.getTokenType() == COMMA) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    continue;
                }
                break;
            }
            marker.done(ORDER_BY_CLAUSE);
            return true;
        }
        if (builder.isContextual(DISTINCT_KEYWORD)) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceAs(DISTINCT_KEYWORD);
            marker.done(DISTINCT_CLAUSE);
            return true;
        }
        if (builder.isContextual(SKIP_KEYWORD) || builder.isContextual(TAKE_KEYWORD)) {
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceAs(builder.isContextual(SKIP_KEYWORD) ? SKIP_KEYWORD : TAKE_KEYWORD);
            boolean whileClause = false;
            if (builder.getTokenType() == WHILE_KEYWORD) {
                builder.advanceLexer();
                whileClause = true;
            }
            builder.skipLineBreaks();
            expectExpression(builder);
            marker.done(whileClause ? PARTITION_WHILE_CLAUSE : PARTITION_CLAUSE);
            return true;
        }
        if (builder.isContextual(JOIN_KEYWORD)) {
            parseJoin(builder, JOIN_CLAUSE);
            return true;
        }
        if (builder.isContextual(GROUP_KEYWORD)) {
            if (builder.isContextualAt(1, JOIN_KEYWORD, false)) {
                PsiBuilder.Marker marker = builder.mark();
                builder.advanceAs(GROUP_KEYWORD);
                parseJoinBody(builder);
                parseInto(builder);
                marker.done(GROUP_JOIN_CLAUSE);
                return true;
            }
            PsiBuilder.Marker marker = builder.mark();
            builder.advanceAs(GROUP_KEYWORD);
            builder.skipLineBreaks();
            if (!builder.isContextual(BY_KEYWORD)) {
                parseExpressionRangeVariables(builder);
            }
            if (builder.expectContextual(BY_KEYWORD)) {
                builder.skipLineBreaks();
                parseExpressionRangeVariables(builder);
            }
            else {
                builder.error(VisualBasicParserLocalize.expected0("By"));
            }
            parseInto(builder);
            marker.done(GROUP_BY_CLAUSE);
            return true;
        }
        return false;
    }

    private static void parseFrom(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceAs(FROM_KEYWORD);
        builder.skipLineBreaks();
        parseCollectionRangeVariables(builder);
        marker.done(FROM_CLAUSE);
    }

    private static void parseAggregate(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceAs(AGGREGATE_KEYWORD);
        builder.skipLineBreaks();
        parseCollectionRangeVariables(builder);
        while (true) {
            if (builder.getTokenType() == NEW_LINE && isClauseStart(builder, true)) {
                builder.skipLineBreaks();
            }
            if (!isClauseStart(builder, false) || !parseClause(builder)) {
                break;
            }
        }
        parseInto(builder);
        marker.done(AGGREGATE_CLAUSE);
    }

    private static void parseJoin(VisualBasicBuilder builder, IElementType elementType) {
        PsiBuilder.Marker marker = builder.mark();
        parseJoinBody(builder);
        marker.done(elementType);
    }

    /**
     * {@code Join x In xs [nested joins] On a Equals b [And c Equals d]}
     */
    private static void parseJoinBody(VisualBasicBuilder builder) {
        builder.advanceAs(JOIN_KEYWORD);
        builder.skipLineBreaks();
        parseCollectionRangeVariable(builder);
        while (builder.isContextual(JOIN_KEYWORD) || builder.isContextual(GROUP_KEYWORD) && builder.isContextualAt(1, JOIN_KEYWORD, false)) {
            parseClause(builder);
        }
        if (builder.getTokenType() == NEW_LINE && builder.lookAhead(0, true) == ON_KEYWORD) {
            builder.skipLineBreaks();
        }
        if (expect(builder, ON_KEYWORD)) {
            builder.skipLineBreaks();
            while (true) {
                PsiBuilder.Marker condition = builder.mark();
                expectExpression(builder);
                if (builder.expectContextual(EQUALS_KEYWORD)) {
                    builder.skipLineBreaks();
                    expectExpression(builder);
                }
                else {
                    builder.error(VisualBasicParserLocalize.expected0("Equals"));
                }
                condition.done(JOIN_CONDITION);
                if (builder.getTokenType() == AND_KEYWORD) {
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                    continue;
                }
                break;
            }
        }
    }

    /**
     * {@code Into name = Count(), Sum(x)}
     */
    private static void parseInto(VisualBasicBuilder builder) {
        if (builder.getTokenType() == NEW_LINE && builder.isContextualAt(0, INTO_KEYWORD, true)) {
            builder.skipLineBreaks();
        }
        if (!builder.expectContextual(INTO_KEYWORD)) {
            builder.error(VisualBasicParserLocalize.expected0("Into"));
            return;
        }
        builder.skipLineBreaks();
        while (true) {
            PsiBuilder.Marker variable = builder.mark();
            if (builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == EQ) {
                builder.advanceLexer();
                builder.advanceLexer();
                builder.skipLineBreaks();
            }
            expectExpression(builder);
            variable.done(AGGREGATION_RANGE_VARIABLE);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
    }

    private static void parseCollectionRangeVariables(VisualBasicBuilder builder) {
        while (true) {
            parseCollectionRangeVariable(builder);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
    }

    /**
     * {@code x [As T] In xs}
     */
    private static void parseCollectionRangeVariable(VisualBasicBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        expectIdentifier(builder);
        if (builder.getTokenType() == QUESTION) {
            builder.advanceLexer();
        }
        if (builder.getTokenType() == AS_KEYWORD) {
            builder.advanceLexer();
            if (parseType(builder) == null) {
                builder.error(VisualBasicParserLocalize.expectedType());
            }
        }
        if (expect(builder, IN_KEYWORD)) {
            builder.skipLineBreaks();
            expectExpression(builder);
        }
        marker.done(COLLECTION_RANGE_VARIABLE);
    }

    /**
     * {@code [name [As T] =] expression, ...}
     */
    private static void parseExpressionRangeVariables(VisualBasicBuilder builder) {
        while (true) {
            PsiBuilder.Marker variable = builder.mark();
            if (builder.getTokenType() == IDENTIFIER) {
                IElementType next = builder.lookAhead(1);
                if (next == EQ) {
                    builder.advanceLexer();
                    builder.advanceLexer();
                    builder.skipLineBreaks();
                }
                else if (next == AS_KEYWORD) {
                    builder.advanceLexer();
                    builder.advanceLexer();
                    if (parseType(builder) == null) {
                        builder.error(VisualBasicParserLocalize.expectedType());
                    }
                    expect(builder, EQ);
                    builder.skipLineBreaks();
                }
            }
            expectExpression(builder);
            variable.done(EXPRESSION_RANGE_VARIABLE);
            if (builder.getTokenType() == COMMA) {
                builder.advanceLexer();
                builder.skipLineBreaks();
                continue;
            }
            break;
        }
    }

    private static void expectExpression(VisualBasicBuilder builder) {
        if (parseExpression(builder) == null) {
            builder.error(VisualBasicParserLocalize.expectedExpression());
        }
    }
}
