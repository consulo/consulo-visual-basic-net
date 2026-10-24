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
import consulo.language.parser.PsiBuilderAdapter;
import consulo.util.dataholder.Key;
import consulo.visualBasic.dotnet.impl.lang.parser.preprocessor.VisualBasicDirective;
import consulo.visualBasic.dotnet.impl.lang.parser.preprocessor.VisualBasicPreprocessorState;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicKeywordTokenType;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Builder with Visual Basic specifics:
 * <ul>
 * <li>{@link VisualBasicTokens#NEW_LINE} is significant. Consumed line breaks (statement terminators and implicit line
 * continuations) are remapped to {@link VisualBasicTokens#END_OF_LINE}, a whitespace token</li>
 * <li>contextual keywords are identifiers remapped to their keyword token when used as keywords</li>
 * <li>the stack of open blocks, used to recover from missing {@code End} statements</li>
 * <li>conditional compilation: directives are applied while parsing, tokens of inactive code become
 * {@link VisualBasicTokens#DISABLED_TEXT}</li>
 * <li>{@code <} and {@code >} are single tokens, composed here into {@code <=}, {@code <>}, {@code <<}...</li>
 * </ul>
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicBuilder extends PsiBuilderAdapter {
    public enum BlockKind {
        FILE,
        NAMESPACE,
        TYPE,
        INTERFACE,
        ENUM,
        METHOD,
        PROPERTY,
        EVENT,
        ACCESSOR,
        LAMBDA,
        IF,
        SELECT,
        WHILE,
        DO,
        FOR,
        TRY,
        USING,
        SYNC_LOCK,
        WITH
    }

    /**
     * @param endKeyword keyword after {@code End} closing the block, or {@code null} when the block is not closed by {@code End}
     */
    public record Block(BlockKind kind, @Nullable IElementType endKeyword) {
    }

    /**
     * Conditional compilation constants of the project, values as expressions by name; put on the builder by the file
     * element type.
     */
    public static final Key<Map<String, String>> PREPROCESSOR_DEFINES = Key.create("visualBasic.preprocessor.defines");

    private final List<Block> myBlocks = new ArrayList<>();
    private final VisualBasicPreprocessorState myPreprocessor;
    private final Deque<Boolean> myAsync = new ArrayDeque<>();
    private final Deque<Boolean> myIterator = new ArrayDeque<>();
    private int mySingleLineIfDepth;

    public VisualBasicBuilder(PsiBuilder delegate) {
        super(delegate);
        Map<String, String> defines = delegate.getUserData(PREPROCESSOR_DEFINES);
        myPreprocessor = new VisualBasicPreprocessorState(defines == null ? Map.of() : defines);
    }

    // conditional compilation

    @Override
    public @Nullable IElementType getTokenType() {
        while (true) {
            IElementType tokenType = super.getTokenType();
            if (tokenType == null) {
                return null;
            }
            if (tokenType == VisualBasicTokens.PREPROCESSOR_DIRECTIVE) {
                CharSequence text = super.getTokenSequence();
                boolean disabled = myPreprocessor.process(VisualBasicDirective.parse(text == null ? "" : text));
                remapCurrentToken(disabled ? VisualBasicTokens.DISABLED_DIRECTIVE : VisualBasicTokens.ACTIVE_DIRECTIVE);
                super.advanceLexer();
                continue;
            }
            if (!myPreprocessor.isActive()) {
                remapCurrentToken(VisualBasicTokens.DISABLED_TEXT);
                super.advanceLexer();
                continue;
            }
            return tokenType;
        }
    }

    @Override
    public boolean eof() {
        return getTokenType() == null;
    }

    // composite operators

    /**
     * Current token, with {@code <} and {@code >} composed with the following {@code <}, {@code >} and {@code =}:
     * Visual Basic allows whitespace between the characters of these operators.
     */
    public @Nullable IElementType getCompositeTokenType() {
        IElementType tokenType = getTokenType();
        if (tokenType != VisualBasicTokens.LT && tokenType != VisualBasicTokens.GT) {
            return tokenType;
        }
        IElementType second = compositePart(1);
        if (tokenType == VisualBasicTokens.LT) {
            if (second == VisualBasicTokens.EQ) {
                return VisualBasicTokens.LTEQ;
            }
            if (second == VisualBasicTokens.GT) {
                return VisualBasicTokens.LTGT;
            }
            if (second == VisualBasicTokens.LT) {
                return compositePart(2) == VisualBasicTokens.EQ ? VisualBasicTokens.LTLTEQ : VisualBasicTokens.LTLT;
            }
            return tokenType;
        }
        if (second == VisualBasicTokens.EQ) {
            return VisualBasicTokens.GTEQ;
        }
        if (second == VisualBasicTokens.GT) {
            return compositePart(2) == VisualBasicTokens.EQ ? VisualBasicTokens.GTGTEQ : VisualBasicTokens.GTGT;
        }
        return tokenType;
    }

    /**
     * Consumes the current token, a composite one as one leaf.
     */
    public void advanceComposite() {
        IElementType composite = getCompositeTokenType();
        int parts = composite == VisualBasicTokens.LTLTEQ || composite == VisualBasicTokens.GTGTEQ ? 3
            : composite == VisualBasicTokens.LTEQ || composite == VisualBasicTokens.LTGT || composite == VisualBasicTokens.LTLT
            || composite == VisualBasicTokens.GTEQ || composite == VisualBasicTokens.GTGT ? 2 : 1;
        if (parts == 1 || composite == null) {
            advanceLexer();
            return;
        }
        Marker marker = mark();
        for (int i = 0; i < parts; i++) {
            advanceLexer();
        }
        marker.collapse(composite);
    }

    /**
     * Token after the current one, when only spaces are between.
     */
    private @Nullable IElementType compositePart(int index) {
        int found = 0;
        for (int i = 1; ; i++) {
            IElementType type = rawLookup(i);
            if (type == null) {
                return null;
            }
            if (type == VisualBasicTokens.WHITE_SPACE) {
                continue;
            }
            found++;
            if (found == index) {
                return type;
            }
            if (type != VisualBasicTokens.LT && type != VisualBasicTokens.GT) {
                return null;
            }
        }
    }

    // line breaks

    /**
     * Implicit line continuation: line breaks at the current position become whitespace.
     */
    public void skipLineBreaks() {
        while (getTokenType() == VisualBasicTokens.NEW_LINE) {
            remapCurrentTokenAndRestoreOnRollback(VisualBasicTokens.END_OF_LINE);
            advanceLexer();
        }
    }

    /**
     * Implicit line continuation before a closing token: skips line breaks only when one of {@code tokens} follows them.
     */
    public void skipLineBreaksBefore(TokenSet tokens) {
        if (getTokenType() != VisualBasicTokens.NEW_LINE) {
            return;
        }
        int index = significantRawIndex(0, true);
        if (index >= 0 && tokens.contains(rawLookup(index))) {
            skipLineBreaks();
        }
    }

    /**
     * The current token starts a line, or follows a colon.
     */
    public boolean isAtStatementStart() {
        getTokenType();
        for (int i = -1; ; i--) {
            IElementType type = rawLookup(i);
            if (type == null) {
                return true;
            }
            if (type == VisualBasicTokens.WHITE_SPACE || VisualBasicTokens.COMMENTS.contains(type) || type == VisualBasicTokens.PREPROCESSOR_DIRECTIVE) {
                continue;
            }
            return type == VisualBasicTokens.END_OF_LINE || type == VisualBasicTokens.NEW_LINE || type == VisualBasicTokens.COLON;
        }
    }

    public boolean isAtStatementEnd() {
        IElementType tokenType = getTokenType();
        return tokenType == null
            || tokenType == VisualBasicTokens.NEW_LINE
            || tokenType == VisualBasicTokens.COLON
            || mySingleLineIfDepth > 0 && tokenType == VisualBasicTokens.ELSE_KEYWORD;
    }

    /**
     * Consumes a line break or a colon.
     *
     * @return false when there is no statement terminator at the current position
     */
    public boolean consumeStatementTerminator() {
        IElementType tokenType = getTokenType();
        if (tokenType == VisualBasicTokens.NEW_LINE) {
            remapCurrentTokenAndRestoreOnRollback(VisualBasicTokens.END_OF_LINE);
            advanceLexer();
            return true;
        }
        if (tokenType == VisualBasicTokens.COLON) {
            advanceLexer();
            return true;
        }
        return tokenType == null;
    }

    // raw lookahead

    /**
     * @return raw lookup index of the n-th (0-based) significant token from the current one, or -1 at the end of the file
     */
    private int significantRawIndex(int n, boolean skipLineBreaks) {
        // skips whitespace before the current token, so raw lookup starts at it
        getTokenType();
        int count = -1;
        for (int i = 0; ; i++) {
            IElementType type = rawLookup(i);
            if (type == null) {
                return -1;
            }
            if (VisualBasicTokens.WHITESPACES.contains(type) || VisualBasicTokens.COMMENTS.contains(type) || type == VisualBasicTokens.PREPROCESSOR_DIRECTIVE) {
                continue;
            }
            if (skipLineBreaks && type == VisualBasicTokens.NEW_LINE) {
                continue;
            }
            count++;
            if (count == n) {
                return i;
            }
        }
    }

    /**
     * Like {@link #lookAhead(int)}, optionally ignoring line breaks.
     */
    public @Nullable IElementType lookAhead(int steps, boolean skipLineBreaks) {
        int index = significantRawIndex(steps, skipLineBreaks);
        return index < 0 ? null : rawLookup(index);
    }

    public @Nullable CharSequence lookAheadText(int steps, boolean skipLineBreaks) {
        int index = significantRawIndex(steps, skipLineBreaks);
        if (index < 0) {
            return null;
        }
        return getOriginalText().subSequence(rawTokenTypeStart(index), rawTokenTypeStart(index + 1));
    }

    // contextual keywords

    public boolean isContextual(IElementType keyword) {
        return isContextualAt(0, keyword, false);
    }

    public boolean isContextualAt(int steps, IElementType keyword, boolean skipLineBreaks) {
        if (lookAhead(steps, skipLineBreaks) != VisualBasicTokens.IDENTIFIER) {
            return false;
        }
        CharSequence text = lookAheadText(steps, skipLineBreaks);
        return text != null && ((VisualBasicKeywordTokenType) keyword).getRepresentation().equalsIgnoreCase(text.toString());
    }

    /**
     * Consumes the current identifier as the contextual keyword.
     */
    public void advanceAs(IElementType keyword) {
        // remap the current token, not the whitespace before it
        getTokenType();
        remapCurrentTokenAndRestoreOnRollback(keyword);
        advanceLexer();
    }

    public boolean expectContextual(IElementType keyword) {
        if (isContextual(keyword)) {
            advanceAs(keyword);
            return true;
        }
        return false;
    }

    // blocks

    public void pushBlock(BlockKind kind, @Nullable IElementType endKeyword) {
        myBlocks.add(new Block(kind, endKeyword));
    }

    public void popBlock() {
        myBlocks.remove(myBlocks.size() - 1);
    }

    public List<Block> getBlocks() {
        return myBlocks;
    }

    public boolean hasBlock(BlockKind kind) {
        for (Block block : myBlocks) {
            if (block.kind() == kind) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBlockClosedBy(IElementType endKeyword) {
        for (Block block : myBlocks) {
            if (block.endKeyword() == endKeyword) {
                return true;
            }
        }
        return false;
    }

    public @Nullable Block getInnermostBlock() {
        return myBlocks.isEmpty() ? null : myBlocks.get(myBlocks.size() - 1);
    }

    public void enterSingleLineIf() {
        mySingleLineIfDepth++;
    }

    public void exitSingleLineIf() {
        mySingleLineIfDepth--;
    }

    /**
     * Statements in a block reset the single-line {@code If} context: {@code Else} belongs to the block statement.
     */
    public int resetSingleLineIf() {
        int depth = mySingleLineIfDepth;
        mySingleLineIfDepth = 0;
        return depth;
    }

    public void restoreSingleLineIf(int depth) {
        mySingleLineIfDepth = depth;
    }

    // method context

    public void enterMethod(boolean async, boolean iterator) {
        myAsync.push(async);
        myIterator.push(iterator);
    }

    public void exitMethod() {
        myAsync.pop();
        myIterator.pop();
    }

    public boolean isAsync() {
        return !myAsync.isEmpty() && myAsync.peek();
    }

    public boolean isIterator() {
        return !myIterator.isEmpty() && myIterator.peek();
    }
}
