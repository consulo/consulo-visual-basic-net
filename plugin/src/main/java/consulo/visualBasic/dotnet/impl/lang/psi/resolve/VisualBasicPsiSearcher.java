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
import consulo.annotation.component.ExtensionImpl;
import consulo.project.DumbService;
import consulo.content.scope.SearchScope;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.impl.resolve.impl.IndexBasedDotNetPsiSearcherExtension;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import consulo.project.Project;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.index.VisualBasicIndexKeys;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Visual Basic types and namespaces for all .NET languages, by their declared case, and {@code <Extension>} methods of
 * modules, which C# calls as extension methods.
 * <p>
 * TODO [VISTALL] merge Partial types
 *
 * @author VISTALL
 * @since 2026-10-23
 */
@ExtensionImpl
public class VisualBasicPsiSearcher extends IndexBasedDotNetPsiSearcherExtension {
    @Inject
    public VisualBasicPsiSearcher(Project project) {
        super(project);
    }

    @Override
    protected DotNetNamespaceAsElement createNamespace(String indexKey, String qName) {
        return new VisualBasicNamespaceAsElementImpl(myProject, indexKey, qName, this);
    }

    @Override
    public StubIndexKey<String, DotNetQualifiedElement> getElementByQNameIndexKey() {
        return VisualBasicIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX;
    }

    @Override
    public StubIndexKey<String, DotNetQualifiedElement> getNamespaceIndexKey() {
        return VisualBasicIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX;
    }

    @RequiredReadAction
    @Override
    public Collection<? extends DotNetTypeDeclaration> findTypesImpl(String vmQName, SearchScope scope) {
        if (DumbService.isDumb(myProject) || !(scope instanceof GlobalSearchScope globalScope)) {
            return List.of();
        }
        Collection<DotNetTypeDeclaration> types = StubIndex.getElements(VisualBasicIndexKeys.TYPE_BY_VMQNAME_INDEX,
            vmQName.hashCode(),
            myProject,
            globalScope,
            DotNetTypeDeclaration.class);
        List<DotNetTypeDeclaration> result = new ArrayList<>(types.size());
        for (DotNetTypeDeclaration type : types) {
            // hash collisions
            if (vmQName.equals(type.getVmQName())) {
                result.add(type);
            }
        }
        return result;
    }

    @RequiredReadAction
    @Override
    public Collection<? extends DotNetLikeMethodDeclaration> findExtensionMethods(String namespace, String name, GlobalSearchScope scope) {
        List<DotNetLikeMethodDeclaration> methods = new ArrayList<>();
        // the compiled name: callers are case-sensitive
        VisualBasicExtensionMethods.process(myProject, namespace, scope, method -> {
            if (name.equals(method.getName())) {
                methods.add(method);
            }
            return true;
        });
        return methods;
    }

    @RequiredReadAction
    @Override
    public boolean processExtensionMethods(String namespace, GlobalSearchScope scope, Predicate<DotNetLikeMethodDeclaration> processor) {
        return VisualBasicExtensionMethods.process(myProject, namespace, scope, processor);
    }
}
