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
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.partial.VisualBasicCompositeTypeDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A class in two files, only one of them {@code Partial}: one type for the searcher, namespaces, VB and C#.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicPartialTypesTest {
    private static final String PART1 = """
        Namespace Demo
            Public Class Animal
                Public Function Speak() As String
                    Return ""
                End Function
            End Class

            Partial Public Class Dog
                Inherits Animal

                Private myAge As Integer

                Partial Private Sub OnCreated()
                End Sub
            End Class
        End Namespace
        """;

    private static final String PART2 = """
        Namespace Demo
            Class Dog
                Public Function Describe() As String
                    OnCreated()
                    Dim sound = Speak()
                    Return myAge.ToString()
                End Function

                Private Sub OnCreated()
                End Sub
            End Class
        End Namespace
        """;

    private static final String CSHARP = """
        using Demo;

        class Use
        {
            void Run()
            {
                Dog dog = new Dog();
                string description = dog.Describe();
                string sound = dog.Speak();
            }
        }
        """;

    @Test
    public void partialTypes(HeadlessProjects projects) throws Exception {
        PsiFile part2 = VisualBasicTesting.openSources(projects, Map.of("Dog1.vb", PART1, "Dog2.vb", PART2)).get("Dog2.vb");
        Project project = part2.getProject();

        ReadAction.run(() -> {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(project);

            DotNetTypeDeclaration[] dogs = searcher.findTypes("Demo.Dog", scope);
            assertThat(dogs).hasSize(1);
            assertThat(dogs[0]).isInstanceOf(VisualBasicCompositeTypeDeclaration.class);
            DotNetTypeDeclaration dog = dogs[0];
            assertThat(((VisualBasicCompositeTypeDeclaration) dog).getParts()).hasSize(2);
            // Public of the first part, Inherits of the first part, members of both
            assertThat(dog.hasModifier(DotNetModifier.PUBLIC)).isTrue();
            assertThat(dog.hasModifier(DotNetModifier.INTERNAL)).isFalse();
            assertThat(dog.hasModifier(VisualBasicModifier.PARTIAL)).isFalse();
            assertThat(dog.isInheritor("Demo.Animal", true)).isTrue();
            List<String> members = new ArrayList<>();
            for (PsiElement member : dog.getMembers()) {
                members.add(((PsiNamedElement) member).getName());
            }
            assertThat(members).contains("myAge", "Describe", "OnCreated");

            DotNetNamespaceAsElement namespace = searcher.findNamespace("Demo", scope);
            assertThat(namespace).isNotNull();
            assertThat(namespace.findChildren("Dog", scope, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS)).hasSize(1);

            // VB: a private field of the other part, the base class of the other part, the implementation of a partial method
            assertThat(fileOf(resolveAt(part2, "myAge.ToString"))).isEqualTo("Dog1.vb");
            assertThat(fileOf(resolveAt(part2, "Speak()"))).isEqualTo("Dog1.vb");
            PsiElement onCreated = resolveAt(part2, "OnCreated()");
            assertThat(fileOf(onCreated)).isEqualTo("Dog2.vb");
        });
    }

    @Test
    public void partialTypesFromCSharp(HeadlessProjects projects) throws Exception {
        PsiFile csharp = VisualBasicTesting.openSources(projects, Map.of("Dog1.vb", PART1, "Dog2.vb", PART2, "Use.cs", CSHARP)).get("Use.cs");

        ReadAction.run(() -> {
            assertThat(resolveAt(csharp, "Dog dog")).isNotNull();
            assertThat(fileOf(resolveAt(csharp, "Describe()"))).isEqualTo("Dog2.vb");
            assertThat(fileOf(resolveAt(csharp, "Speak()"))).isEqualTo("Dog1.vb");
        });
    }

    private static PsiElement resolveAt(PsiFile file, String usage) {
        int offset = file.getText().indexOf(usage);
        assertThat(offset).as(usage).isGreaterThanOrEqualTo(0);
        PsiReference reference = file.findReferenceAt(offset);
        assertThat(reference).as(usage).isNotNull();
        PsiElement target = null;
        if (reference instanceof PsiPolyVariantReference poly) {
            ResolveResult[] results = poly.multiResolve(false);
            assertThat(results).as(usage).hasSize(1);
            target = results[0].getElement();
        }
        else {
            target = reference.resolve();
        }
        assertThat(target).as(usage).isNotNull();
        return target;
    }

    /**
     * File of the element, through navigation elements of light wrappers.
     */
    private static String fileOf(PsiElement element) {
        PsiElement current = element;
        for (int i = 0; i < 5 && current.getNavigationElement() != current; i++) {
            current = current.getNavigationElement();
        }
        PsiFile file = current.getContainingFile();
        return file == null ? "<no file: " + current + ">" : file.getName();
    }
}
