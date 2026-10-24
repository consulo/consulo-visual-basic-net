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

package consulo.visualBasic.dotnet.impl.editor.highlight;

import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import consulo.visualBasic.dotnet.impl.lang.lexer.VisualBasicLexer;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;

import java.util.HashMap;
import java.util.Map;

/**
 * Lexer based highlighting. Contextual keywords are identifiers for the lexer, {@link VisualBasicHighlightVisitor}
 * colors them where the parser used them as keywords.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> KEYS = new HashMap<>();

    static {
        fillMap(KEYS, VisualBasicTokens.RESERVED_KEYWORDS, VisualBasicHighlightKey.KEYWORD);
        fillMap(KEYS, VisualBasicTokens.NUMBERS, VisualBasicHighlightKey.NUMBER);
        fillMap(KEYS, VisualBasicTokens.STRINGS, VisualBasicHighlightKey.STRING);
        fillMap(KEYS, TokenSet.create(
            VisualBasicTokens.INTERPOLATED_STRING_START,
            VisualBasicTokens.INTERPOLATED_STRING_TEXT,
            VisualBasicTokens.INTERPOLATED_STRING_END,
            VisualBasicTokens.INTERPOLATION_FORMAT
        ), VisualBasicHighlightKey.STRING);
        safeMap(KEYS, VisualBasicTokens.DATE_LITERAL, VisualBasicHighlightKey.DATE);
        safeMap(KEYS, VisualBasicTokens.COMMENT, VisualBasicHighlightKey.COMMENT);
        safeMap(KEYS, VisualBasicTokens.DOC_COMMENT, VisualBasicHighlightKey.DOC_COMMENT);
        safeMap(KEYS, VisualBasicTokens.PREPROCESSOR_DIRECTIVE, VisualBasicHighlightKey.PREPROCESSOR_DIRECTIVE);
        safeMap(KEYS, VisualBasicTokens.DISABLED_TEXT, VisualBasicHighlightKey.COMMENT);
        safeMap(KEYS, VisualBasicTokens.LINE_CONTINUATION, VisualBasicHighlightKey.LINE_CONTINUATION);
        fillMap(KEYS, VisualBasicTokens.OPERATORS, VisualBasicHighlightKey.OPERATION_SIGN);
        fillMap(KEYS, TokenSet.create(VisualBasicTokens.COLON, VisualBasicTokens.COLONEQ), VisualBasicHighlightKey.OPERATION_SIGN);
        fillMap(KEYS, TokenSet.create(VisualBasicTokens.LPAR, VisualBasicTokens.RPAR), VisualBasicHighlightKey.PARENTHESES);
        fillMap(KEYS, TokenSet.create(
            VisualBasicTokens.LBRACE,
            VisualBasicTokens.RBRACE,
            VisualBasicTokens.INTERPOLATION_START,
            VisualBasicTokens.INTERPOLATION_END
        ), VisualBasicHighlightKey.BRACES);
        safeMap(KEYS, VisualBasicTokens.DOT, VisualBasicHighlightKey.DOT);
        safeMap(KEYS, VisualBasicTokens.COMMA, VisualBasicHighlightKey.COMMA);
        safeMap(KEYS, VisualBasicTokens.BAD_CHARACTER, VisualBasicHighlightKey.BAD_CHARACTER);

        fillMap(KEYS, TokenSet.create(
            VisualBasicTokens.XML_TAG_START,
            VisualBasicTokens.XML_END_TAG_START,
            VisualBasicTokens.XML_TAG_END,
            VisualBasicTokens.XML_EMPTY_TAG_END,
            VisualBasicTokens.XML_TAG_NAME
        ), VisualBasicHighlightKey.XML_TAG);
        fillMap(KEYS, TokenSet.create(VisualBasicTokens.XML_ATTRIBUTE_NAME, VisualBasicTokens.XML_EQ), VisualBasicHighlightKey.XML_ATTRIBUTE);
        safeMap(KEYS, VisualBasicTokens.XML_ATTRIBUTE_VALUE, VisualBasicHighlightKey.XML_ATTRIBUTE_VALUE);
        fillMap(KEYS, TokenSet.create(VisualBasicTokens.XML_TEXT, VisualBasicTokens.XML_CDATA), VisualBasicHighlightKey.XML_TEXT);
        fillMap(KEYS, TokenSet.create(VisualBasicTokens.XML_COMMENT, VisualBasicTokens.XML_PI), VisualBasicHighlightKey.XML_COMMENT);
        fillMap(KEYS, TokenSet.create(VisualBasicTokens.XML_EMBED_START, VisualBasicTokens.XML_EMBED_END), VisualBasicHighlightKey.XML_EMBEDDED);
    }

    @Override
    public Lexer getHighlightingLexer() {
        return new VisualBasicLexer();
    }

    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType elementType) {
        return pack(KEYS.get(elementType));
    }
}
