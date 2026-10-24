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

package consulo.visualBasic.dotnet.impl.lang.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.Lexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Compares the tokens of {@code src/test/resources/lexer/*.vb} with the {@code .txt} gold files next to them.
 * A missing gold file is written from the current output, and the test fails so it gets reviewed.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicLexerTest {
    private static final Path TEST_DATA = Path.of("src/test/resources/lexer");

    private record TokenInfo(IElementType type, int start, int end, int state) {
    }

    @TestFactory
    public Stream<DynamicTest> gold() throws IOException {
        return sources().map(source -> DynamicTest.dynamicTest(name(source), () -> {
            String actual = dumpTokens(new VisualBasicLexer(), readText(source));
            Path gold = source.resolveSibling(name(source).replaceAll("\\.vb$", ".txt"));
            if (!Files.exists(gold)) {
                Files.writeString(gold, actual + "\n", StandardCharsets.UTF_8);
                Assertions.fail("gold file created: " + gold);
            }
            Assertions.assertEquals(readText(gold).strip(), actual);
        }));
    }

    /**
     * {@code \r\n} is one line terminator. Not in a test file: git may store those with {@code \n}.
     */
    @Test
    public void windowsLineEnds() {
        String actual = dumpTokens(new VisualBasicLexer(), "x = 1\r\n' comment\r\ny = 2\r\n");
        Assertions.assertEquals("""
            0000: IdentifierToken 'x'
            0001: WHITE_SPACE ' '
            0002: EqualsToken '='
            0003: WHITE_SPACE ' '
            0004: IntegerLiteralToken '1'
            0005: StatementTerminatorToken '\\r\\n'
            0007: CommentTrivia '' comment'
            0016: StatementTerminatorToken '\\r\\n'
            0018: IdentifierToken 'y'
            0019: WHITE_SPACE ' '
            0020: EqualsToken '='
            0021: WHITE_SPACE ' '
            0022: IntegerLiteralToken '2'
            0023: StatementTerminatorToken '\\r\\n'""", actual);
    }

    /**
     * The editor highlighter restarts lexing from tokens with a restartable state: the result must match a full pass.
     */
    @TestFactory
    public Stream<DynamicTest> restart() throws IOException {
        return sources().map(source -> DynamicTest.dynamicTest(name(source), () -> {
            String text = readText(source);
            VisualBasicLexer lexer = new VisualBasicLexer();
            List<TokenInfo> tokens = lex(lexer, text, 0, lexer.getStartState());
            for (int i = 0; i < tokens.size(); i++) {
                TokenInfo token = tokens.get(i);
                if (!lexer.isRestartableState(token.state())) {
                    continue;
                }
                List<TokenInfo> restarted = lex(lexer, text, token.start(), token.state());
                Assertions.assertEquals(tokens.subList(i, tokens.size()), restarted, "restart at offset " + token.start());
            }
        }));
    }

    /**
     * Every prefix of the test files must be lexed without gaps.
     */
    @TestFactory
    public Stream<DynamicTest> prefixes() throws IOException {
        return sources().map(source -> DynamicTest.dynamicTest(name(source), () -> {
            String text = readText(source);
            for (int length = 0; length <= text.length(); length++) {
                List<TokenInfo> tokens = lex(new VisualBasicLexer(), text.substring(0, length), 0, 0);
                int offset = 0;
                for (TokenInfo token : tokens) {
                    Assertions.assertEquals(offset, token.start(), "gap in prefix of length " + length);
                    Assertions.assertTrue(token.end() > token.start(), "empty token in prefix of length " + length);
                    offset = token.end();
                }
                Assertions.assertEquals(length, offset, "prefix of length " + length);
            }
        }));
    }

    private static List<TokenInfo> lex(Lexer lexer, String text, int start, int state) {
        List<TokenInfo> tokens = new ArrayList<>();
        lexer.start(text, start, text.length(), state);
        IElementType tokenType;
        while ((tokenType = lexer.getTokenType()) != null) {
            tokens.add(new TokenInfo(tokenType, lexer.getTokenStart(), lexer.getTokenEnd(), lexer.getState()));
            lexer.advance();
        }
        return tokens;
    }

    private static Stream<Path> sources() throws IOException {
        List<Path> sources;
        try (Stream<Path> files = Files.list(TEST_DATA)) {
            sources = files.filter(it -> name(it).endsWith(".vb")).sorted().toList();
        }
        return sources.stream();
    }

    private static String name(Path path) {
        return path.getFileName().toString();
    }

    public static String dumpTokens(Lexer lexer, String text) {
        StringBuilder builder = new StringBuilder();
        lexer.start(text);
        IElementType tokenType;
        while ((tokenType = lexer.getTokenType()) != null) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(String.format("%04d: %s '", lexer.getTokenStart(), tokenType));
            escape(builder, text.substring(lexer.getTokenStart(), lexer.getTokenEnd()));
            builder.append('\'');
            lexer.advance();
        }
        return builder.toString();
    }

    private static void escape(StringBuilder builder, String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(c);
            }
        }
    }

    private static String readText(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        return !text.isEmpty() && text.charAt(0) == (char) 0xFEFF ? text.substring(1) : text;
    }
}
