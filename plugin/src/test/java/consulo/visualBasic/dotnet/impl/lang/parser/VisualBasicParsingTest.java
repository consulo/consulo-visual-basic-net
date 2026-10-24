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

import consulo.language.impl.DebugUtil;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * <ul>
 * <li>{@code parser}: PSI of {@code *.vb} compared with the {@code .txt} gold next to it. A missing gold file is written
 * from the current output, and the test fails so it gets reviewed</li>
 * <li>{@code parser-robustness}: Roslyn {@code AllInOne.vb} (MIT) must parse without errors outside of XML literals</li>
 * <li>every prefix of every test file must parse, keeping all of the text in the tree</li>
 * </ul>
 *
 * @author VISTALL
 * @since 2026-10-23
 */
@ExtendWith(ConsuloProjectLoader.class)
public class VisualBasicParsingTest {
    private static final Path GOLD_DATA = Path.of("src/test/resources/parser");
    private static final Path ROBUSTNESS_DATA = Path.of("src/test/resources/parser-robustness");
    private static final Path REPORT = Path.of("target/parser-robustness-report.txt");

    /**
     * Prefix test parses at most this many prefixes per file.
     */
    private static final int MAX_PREFIXES = 2000;

    @TestFactory
    public Stream<DynamicTest> gold(PsiFileFactory factory) throws IOException {
        return sources(GOLD_DATA).map(source -> DynamicTest.dynamicTest(name(source), () -> {
            String text = readText(source);
            PsiFile file = factory.createFileFromText(name(source), VisualBasicLanguage.INSTANCE, text);
            Assertions.assertEquals(text, file.getText());
            String actual = DebugUtil.psiToString(file, true, false).strip();
            Path gold = source.resolveSibling(name(source).replaceAll("\\.vb$", ".txt"));
            if (!Files.exists(gold)) {
                Files.writeString(gold, actual + "\n", StandardCharsets.UTF_8);
                Assertions.fail("gold file created: " + gold);
            }
            Assertions.assertEquals(readText(gold).strip(), actual);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> robustness(PsiFileFactory factory) throws IOException {
        return sources(ROBUSTNESS_DATA).map(source -> DynamicTest.dynamicTest(name(source), () -> {
            String text = readText(source);
            PsiFile file = factory.createFileFromText(name(source), VisualBasicLanguage.INSTANCE, text);
            Assertions.assertEquals(text, file.getText());

            Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
            StringBuilder report = new StringBuilder();
            report.append(errors.size()).append(" errors\n");
            for (PsiErrorElement error : errors) {
                int offset = error.getTextRange().getStartOffset();
                int line = (int) text.substring(0, offset).chars().filter(c -> c == '\n').count() + 1;
                report.append(line).append(": ").append(error.getErrorDescription()).append('\n');
            }
            Files.createDirectories(REPORT.getParent());
            Files.writeString(REPORT, report.toString(), StandardCharsets.UTF_8);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> prefixes(PsiFileFactory factory) throws IOException {
        return Stream.concat(sources(GOLD_DATA), sources(ROBUSTNESS_DATA)).map(source -> DynamicTest.dynamicTest(name(source), () -> {
            String text = readText(source);
            int step = Math.max(1, text.length() / MAX_PREFIXES);
            for (int length = 0; length <= text.length(); length += step) {
                String prefix = text.substring(0, length);
                PsiFile file = factory.createFileFromText("a.vb", VisualBasicLanguage.INSTANCE, prefix);
                Assertions.assertEquals(prefix, file.getNode().getText(), "prefix of length " + length);
            }
        }));
    }

    private static Stream<Path> sources(Path dir) throws IOException {
        List<Path> sources = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(it -> name(it).endsWith(".vb")).sorted().forEach(sources::add);
        }
        return sources.stream();
    }

    private static String name(Path path) {
        return path.getFileName().toString();
    }

    private static String readText(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        return !text.isEmpty() && text.charAt(0) == (char) 0xFEFF ? text.substring(1) : text;
    }
}
