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

package consulo.visualBasic.dotnet.it;

import consulo.application.ReadAction;
import consulo.document.util.TextRange;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.editor.highlight.VisualBasicHighlightVisitor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Every range {@link VisualBasicHighlightVisitor} colors in {@code highlight/<name>.vb}, as
 * {@code line: text KEY}, compared with {@code <name>.txt}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicHighlightTest {
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Highlight"})
    public void highlight(String testName, HeadlessProjects projects) throws Exception {
        Path directory = VisualBasicTesting.resource("highlight");
        String text = Files.readString(directory.resolve(testName + ".vb"), StandardCharsets.UTF_8);
        PsiFile file = VisualBasicTesting.openSource(projects, testName + ".vb", text);

        String actual = ReadAction.compute(() -> {
            StringBuilder builder = new StringBuilder();
            PsiTreeUtil.processElements(file, element -> {
                VisualBasicHighlightVisitor.highlight(element, (range, key) -> {
                    TextRange textRange = range.getTextRange();
                    int line = text.substring(0, textRange.getStartOffset()).split("\n", -1).length;
                    builder.append(line).append(": ").append(range.getText()).append(' ').append(key.getExternalName()).append('\n');
                });
                return true;
            });
            return builder.toString().strip();
        });
        VisualBasicTesting.assertGold("highlight/" + testName + ".txt", actual);
    }
}
