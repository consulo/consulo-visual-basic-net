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
import consulo.application.WriteAction;
import consulo.application.dumb.IndexNotReadyException;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicLocalVariableImpl;
import consulo.visualBasic.dotnet.it.extension.TestVisualBasicModuleExtensionProvider;
import consulo.visualBasic.dotnet.it.extension.TestVisualBasicMutableModuleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Project level settings of the Visual Basic module extension: root namespace, imports, conditional compilation
 * constants with values, Option Infer.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicProjectSettingsTest {
    private static final String SOURCE = """
        Class AtRoot
        End Class

        Namespace Inner
            Class Nested
            End Class
        End Namespace

        Namespace Global.Outside
            Class Free
            End Class
        End Namespace

        Module Use
            Sub Run()
                Dim builder = New StringBuilder()
        #If CONFIG = "Debug" AndAlso DEBUG Then
                Dim debug = 1
        #Else
                Dim release = 1
        #End If
            End Sub
        End Module
        """;

    @Test
    public void settings(HeadlessProjects projects) throws Exception {
        PsiFile file = VisualBasicTesting.openSources(projects, Map.of("Settings.vb", SOURCE), extension -> {
            extension.setRootNamespace("Company.Product");
            extension.getImports().add("System.Text");
            extension.getConstants().put("CONFIG", "\"Debug\"");
            extension.getConstants().put("DEBUG", "-1");
        }).get("Settings.vb");
        Project project = file.getProject();

        ReadAction.run(() -> {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(project);
            assertThat(searcher.findType("Company.Product.AtRoot", scope)).isNotNull();
            assertThat(searcher.findType("Company.Product.Inner.Nested", scope)).isNotNull();
            assertThat(searcher.findType("Outside.Free", scope)).isNotNull();
            assertThat(searcher.findType("AtRoot", scope)).isNull();
            assertThat(searcher.findNamespace("Company", scope)).isNotNull();
            assertThat(searcher.findNamespace("Company.Product.Inner", scope)).isNotNull();

            Map<String, String> locals = new HashMap<>();
            for (VisualBasicLocalVariableImpl local : PsiTreeUtil.findChildrenOfType(file, VisualBasicLocalVariableImpl.class)) {
                locals.put(local.getName(), ((DotNetVariable) local).toTypeRef(true).getVmQName());
            }
            // project imports, and DEBUG/CONFIG of the project: the #Else branch is inactive
            assertThat(locals).containsEntry("builder", "System.Text.StringBuilder").containsKey("debug").doesNotContainKey("release");

            DotNetTypeDeclaration atRoot = searcher.findType("Company.Product.AtRoot", scope);
            assertThat(atRoot.getPresentableQName()).isEqualTo("Company.Product.AtRoot");
        });
    }

    /**
     * Settings set after the first indexing, as a {@code .vbproj} import does: the files are reindexed and reparsed.
     */
    @Test
    public void settingsChangedAfterIndexing(HeadlessProjects projects) throws Exception {
        PsiFile file = VisualBasicTesting.openSource(projects, "Settings.vb", SOURCE);
        Project project = file.getProject();
        GlobalSearchScope scope = ReadAction.compute(() -> GlobalSearchScope.allScope(project));
        DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(project);
        assertThat(ReadAction.compute(() -> searcher.findType("AtRoot", scope))).isNotNull();

        Module module = ReadAction.compute(() -> ModuleManager.getInstance(project).findModuleByName("main"));
        assertThat(module).isNotNull();
        WriteAction.run(() -> {
            ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
            TestVisualBasicMutableModuleExtension extension = rootModel.getExtensionWithoutCheck(TestVisualBasicModuleExtensionProvider.ID);
            assertThat(extension).isNotNull();
            extension.setRootNamespace("Company.Product");
            extension.getConstants().put("CONFIG", "\"Debug\"");
            extension.getConstants().put("DEBUG", "-1");
            rootModel.commit();
        });

        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60);
        while (!ReadAction.compute(() -> isReindexed(searcher, scope))) {
            assertThat(System.currentTimeMillis()).as("reindexed with the new settings").isLessThan(deadline);
            Thread.sleep(100);
        }

        ReadAction.run(() -> {
            PsiFile current = PsiManager.getInstance(project).findFile(file.getVirtualFile());
            assertThat(current).isNotNull();
            List<String> locals = new ArrayList<>();
            for (VisualBasicLocalVariableImpl local : PsiTreeUtil.findChildrenOfType(current, VisualBasicLocalVariableImpl.class)) {
                locals.add(local.getName());
            }
            assertThat(locals).contains("debug").doesNotContain("release");
        });
    }

    private static boolean isReindexed(DotNetPsiSearcher searcher, GlobalSearchScope scope) {
        try {
            return searcher.findType("Company.Product.AtRoot", scope) != null
                && searcher.findType("AtRoot", scope) == null
                && searcher.findNamespace("Company.Product", scope) != null;
        }
        catch (IndexNotReadyException e) {
            return false;
        }
    }
}
