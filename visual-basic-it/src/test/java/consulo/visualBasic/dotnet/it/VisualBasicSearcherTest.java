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
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.dotnet.psi.resolve.DotNetShortNameSearcher;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Visual Basic declarations as other .NET languages see them: through {@link DotNetPsiSearcher} and
 * {@link DotNetShortNameSearcher}, by VM names.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicSearcherTest {
    private static final String SOURCE = """
        Namespace Company.Product
            Public Class Widget(Of T)
                Public Class Part
                End Class
            End Class

            Module Helpers
            End Module
        End Namespace

        Namespace Global.Other
            Class Thing
            End Class
        End Namespace
        """;

    @Test
    public void searcher(HeadlessProjects projects) throws Exception {
        PsiFile file = VisualBasicTesting.openSource(projects, "Searcher.vb", SOURCE);
        Project project = file.getProject();

        ReadAction.run(() -> {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(project);

            DotNetTypeDeclaration widget = searcher.findType("Company.Product.Widget`1", scope);
            assertThat(widget).isNotNull();
            assertThat(widget.getLanguage()).isEqualTo(VisualBasicLanguage.INSTANCE);
            assertThat(widget.getGenericParametersCount()).isEqualTo(1);

            assertThat(searcher.findType("Company.Product.Widget`1/Part", scope)).isNotNull();
            assertThat(searcher.findType("Company.Product.Helpers", scope)).isNotNull();
            assertThat(searcher.findType("Other.Thing", scope)).isNotNull();
            // System.Object of mscorlib, through the msil searcher
            assertThat(searcher.findType("System.Object", scope)).isNotNull();

            DotNetNamespaceAsElement namespace = searcher.findNamespace("Company.Product", scope);
            assertThat(namespace).isNotNull();
            Collection<PsiElement> children = namespace.getChildren(scope, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS);
            assertThat(children).extracting(it -> ((DotNetTypeDeclaration) it).getVmQName())
                .containsExactlyInAnyOrder("Company.Product.Widget`1", "Company.Product.Helpers");
            assertThat(searcher.findNamespace("Company", scope)).isNotNull();

            List<String> names = List.copyOf(DotNetShortNameSearcher.getInstance(project).getTypeNames(scope, null));
            assertThat(names).contains("Widget", "Part", "Helpers", "Thing");
        });
    }
}
