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
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicLocalVariableImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Every local of {@code expressions/<name>.vb} with its type, inferred from the initializer (Option Infer), compared
 * with {@code <name>.txt}. Types not found are marked with {@code ?}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicExpressionTypesTest {
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Expressions"})
    public void types(String testName, HeadlessProjects projects) throws Exception {
        Path directory = VisualBasicTesting.resource("expressions");
        String text = Files.readString(directory.resolve(testName + ".vb"), StandardCharsets.UTF_8);
        PsiFile file = VisualBasicTesting.openSource(projects, testName + ".vb", text);

        String actual = ReadAction.compute(() -> {
            StringBuilder builder = new StringBuilder();
            for (VisualBasicLocalVariableImpl local : PsiTreeUtil.findChildrenOfType(file, VisualBasicLocalVariableImpl.class)) {
                DotNetTypeRef typeRef = ((DotNetVariable) local).toTypeRef(true);
                builder.append(local.getName()).append(": ");
                if (typeRef.resolve().getElement() == null) {
                    builder.append('?');
                }
                builder.append(typeRef.getVmQName()).append('\n');
            }
            return builder.toString().strip();
        });
        VisualBasicTesting.assertGold("expressions/" + testName + ".txt", actual);
    }
}
