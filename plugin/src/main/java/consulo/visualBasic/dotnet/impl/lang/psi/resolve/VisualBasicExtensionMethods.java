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

package consulo.visualBasic.dotnet.impl.lang.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicAttributeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicMethodDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicUserTypeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.index.VisualBasicIndexKeys;

import java.util.function.Predicate;

/**
 * {@code <Extension>} methods of modules, for other languages: C# calls them as extension methods.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicExtensionMethods {
    private VisualBasicExtensionMethods() {
    }

    /**
     * Whether the method has an attribute named {@code Extension}, as written: for stubs, which cannot resolve.
     */
    @RequiredReadAction
    public static boolean hasExtensionAttribute(VisualBasicMethodDeclarationImpl method) {
        DotNetModifierList modifierList = method.getModifierList();
        if (modifierList == null) {
            return false;
        }
        for (DotNetAttribute attribute : modifierList.getAttributes()) {
            VisualBasicUserTypeImpl type = attribute instanceof VisualBasicAttributeImpl visualBasic ? visualBasic.getType() : null;
            String name = type == null ? null : type.getReferenceExpression().getReferenceName();
            if ("Extension".equalsIgnoreCase(name) || "ExtensionAttribute".equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param namespace presentable qualified name, empty for the global namespace
     * @return false when {@code processor} stopped
     */
    @RequiredReadAction
    public static boolean process(Project project, String namespace, GlobalSearchScope scope, Predicate<DotNetLikeMethodDeclaration> processor) {
        if (DumbService.isDumb(project)) {
            return true;
        }
        return StubIndex.getInstance().processElements(VisualBasicIndexKeys.EXTENSION_METHOD_BY_NAMESPACE_INDEX,
            DotNetNamespaceStubUtil.getIndexableNamespace(namespace),
            project,
            scope,
            DotNetLikeMethodDeclaration.class,
            method -> processor.test(method));
    }
}
