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
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * C# sees Visual Basic types and members (through the DotNetPsiSearcher extension of VB): references of a C# file resolve to elements
 * navigating to the Visual Basic file.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicCSharpInteropTest {
    private static final String VISUAL_BASIC = """
        Namespace Demo
            Public Class Animal
                Public Property Name As String

                Public Shared Function Create(name As String) As Animal
                    Return Nothing
                End Function
            End Class

            Public Class Dog
                Inherits Animal

                Public Age As Integer

                Public Sub New(age As Integer)
                End Sub

                Public Function Bark(times As Integer) As String
                    Return ""
                End Function

                Public Class Collar
                End Class
            End Class

            Public Module Helpers
                Public Function Twice(x As Integer) As Long
                    Return x * 2
                End Function
            End Module
        End Namespace
        """;

    private static final String CSHARP = """
        using Demo;

        class Use
        {
            void Run()
            {
                Dog dog = new Dog(3);
                string s = dog.Bark(2);
                int age = dog.Age;
                string name = dog.Name;
                Animal created = Animal.Create("x");
                Dog.Collar collar = null;
                long twice = Helpers.Twice(1);
            }
        }
        """;

    @Test
    public void csharpToVisualBasic(HeadlessProjects projects) throws Exception {
        Map<String, PsiFile> files = VisualBasicTesting.openSources(projects, Map.of("Shapes.vb", VISUAL_BASIC, "Use.cs", CSHARP));
        PsiFile csharp = files.get("Use.cs");

        ReadAction.run(() -> {
            String text = csharp.getText();
            List<String> unresolved = new ArrayList<>();
            for (String usage : List.of("Dog dog", "Dog(3)", "Bark(", "Age;", "Name;", "Animal created", "Create(", "Collar collar", "Helpers.", "Twice(")) {
                String identifier = usage.replaceAll("[^A-Za-z].*", "");
                int offset = text.indexOf(usage);
                assertThat(offset).as(usage).isGreaterThanOrEqualTo(0);
                PsiElement target = resolve(csharp.findReferenceAt(offset + usage.indexOf(identifier)));
                PsiElement navigation = target == null ? null : target.getNavigationElement();
                if (navigation == null || navigation.getContainingFile() == null || !navigation.getContainingFile().getName().equals("Shapes.vb")) {
                    unresolved.add(usage + " -> " + target);
                }
            }
            assertThat(unresolved).isEmpty();
        });
    }

    /**
     * Types of a project without {@code Namespace} statements live in its root namespace, which C# imports.
     */
    @Test
    public void csharpToRootNamespace(HeadlessProjects projects) throws Exception {
        String visualBasic = """
            Public Class Hexagon
            End Class
            """;
        String csharp = """
            using Vb.Formatting;

            class Use
            {
                Hexagon hexagon;
            }
            """;
        Map<String, PsiFile> files = VisualBasicTesting.openSources(projects,
            Map.of("Hexagon.vb", visualBasic, "Use.cs", csharp),
            extension -> extension.setRootNamespace("Vb.Formatting"));
        PsiFile csharpFile = files.get("Use.cs");

        ReadAction.run(() -> {
            String text = csharpFile.getText();
            assertThat(resolve(csharpFile.findReferenceAt(text.indexOf("Formatting;")))).as("using Vb.Formatting").isNotNull();
            PsiElement hexagon = resolve(csharpFile.findReferenceAt(text.indexOf("Hexagon hexagon")));
            assertThat(hexagon).as("Hexagon").isNotNull();
            assertThat(hexagon.getNavigationElement().getContainingFile().getName()).isEqualTo("Hexagon.vb");
        });
    }

    /**
     * {@code <Extension>} functions of modules, by the namespace C# imports, for C# extension method calls.
     */
    @Test
    public void extensionMethods(HeadlessProjects projects) throws Exception {
        String visualBasic = """
            Imports System.Runtime.CompilerServices

            Public Module ShapeReports
                <Extension>
                Public Function ToReport(values As Integer()) As String
                    Return ""
                End Function

                Public Function NotExtension(values As Integer()) As String
                    Return ""
                End Function
            End Module
            """;
        String csharp = """
            using Vb.Formatting;

            class Use
            {
                string Run(int[] values)
                {
                    return values.ToReport();
                }
            }
            """;
        Map<String, PsiFile> files = VisualBasicTesting.openSources(projects, Map.of("ShapeReports.vb", visualBasic, "Use.cs", csharp),
            extension -> extension.setRootNamespace("Vb.Formatting"));
        PsiFile csharpFile = files.get("Use.cs");
        Project project = csharpFile.getProject();

        ReadAction.run(() -> {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(project);

            Collection<? extends DotNetLikeMethodDeclaration> methods = searcher.findExtensionMethods("Vb.Formatting", "ToReport", scope);
            assertThat(methods).hasSize(1);
            DotNetLikeMethodDeclaration method = methods.iterator().next();
            assertThat(DotNetAttributeUtil.hasAttribute(method, DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute)).isTrue();
            assertThat(PsiTreeUtil.getParentOfType(method, DotNetTypeDeclaration.class)).isNotNull();

            assertThat(searcher.findExtensionMethods("Vb.Formatting", "NotExtension", scope)).isEmpty();
            assertThat(searcher.findExtensionMethods("", "ToReport", scope)).isEmpty();

            List<String> all = new ArrayList<>();
            searcher.processExtensionMethods("Vb.Formatting", scope, it -> all.add(it.getName()));
            assertThat(all).containsExactly("ToReport");

            // the C# call
            String text = csharpFile.getText();
            PsiElement target = resolve(csharpFile.findReferenceAt(text.indexOf("ToReport()")));
            assertThat(target).as("values.ToReport()").isNotNull();
            assertThat(describeNavigation(target)).endsWith("ShapeReports.vb");
        });
    }

    /**
     * The chain of navigation elements, down to the file of the last one.
     */
    private static String describeNavigation(PsiElement element) {
        StringBuilder chain = new StringBuilder(element.getClass().getSimpleName());
        PsiElement current = element;
        for (int i = 0; i < 5 && current.getNavigationElement() != current; i++) {
            current = current.getNavigationElement();
            chain.append(" -> ").append(current.getClass().getSimpleName());
        }
        PsiFile file = current.getContainingFile();
        return chain.append(" in ").append(file == null ? null : file.getName()).toString();
    }

    private static PsiElement resolve(PsiReference reference) {
        if (reference == null) {
            return null;
        }
        if (reference instanceof PsiPolyVariantReference poly) {
            for (ResolveResult result : poly.multiResolve(false)) {
                if (result.isValidResult() && result.getElement() != null) {
                    return result.getElement();
                }
            }
        }
        return reference.resolve();
    }
}
