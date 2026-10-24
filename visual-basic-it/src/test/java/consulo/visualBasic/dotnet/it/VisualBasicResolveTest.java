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
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Every reference of {@code resolve/<name>.vb} with its targets, compared with {@code <name>.txt}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicResolveTest {
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"TypeNames", "Expressions"})
    public void resolve(String testName, HeadlessProjects projects) throws Exception {
        Path directory = VisualBasicTesting.resource("resolve");
        String text = Files.readString(directory.resolve(testName + ".vb"), StandardCharsets.UTF_8);
        PsiFile file = VisualBasicTesting.openSource(projects, testName + ".vb", text);

        String actual = ReadAction.compute(() -> dump(file));
        VisualBasicTesting.assertGold("resolve/" + testName + ".txt", actual);
    }

    private static String dump(PsiFile file) {
        StringBuilder builder = new StringBuilder();
        file.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                // references of the element itself, not the ones its container reports for it
                PsiReference self = element.getReference();
                for (PsiReference reference : self != null && self.getElement() == element ? new PsiReference[]{self} : new PsiReference[0]) {
                    ResolveResult[] results = reference instanceof PsiPolyVariantReference poly
                        ? poly.multiResolve(false)
                        : new ResolveResult[0];
                    builder.append(element.getText()).append(element.getTextRange()).append(": ");
                    builder.append(results.length == 0
                        ? "empty"
                        : Arrays.stream(results).map(it -> describe(it.getElement())).collect(Collectors.joining(", ")));
                    builder.append('\n');
                }
            }
        });
        return builder.toString().strip();
    }

    static String describe(PsiElement element) {
        if (element == null) {
            return "null";
        }
        String language = element.getLanguage().getID();
        if (element instanceof DotNetTypeDeclaration type) {
            return "type " + type.getVmQName() + " (" + language + ")";
        }
        if (element instanceof DotNetNamespaceAsElement namespace) {
            return "namespace " + namespace.getPresentableQName();
        }
        if (element instanceof DotNetGenericParameter parameter) {
            return "generic-parameter " + parameter.getName();
        }
        if (element instanceof DotNetQualifiedElement qualified) {
            return element.getClass().getSimpleName() + " " + qualified.getPresentableQName();
        }
        return element.toString();
    }
}
