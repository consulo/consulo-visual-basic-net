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
import consulo.dotnet.psi.impl.IndexBasedDotNetNamespaceAsElement;
import consulo.dotnet.psi.impl.resolve.impl.IndexBasedDotNetPsiSearcherExtension;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import consulo.visualBasic.dotnet.impl.lang.psi.partial.VisualBasicPartialTypes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicNamespaceAsElementImpl extends IndexBasedDotNetNamespaceAsElement {
    public VisualBasicNamespaceAsElementImpl(Project project, String indexKey, String qName, IndexBasedDotNetPsiSearcherExtension searcher) {
        super(project, VisualBasicLanguage.INSTANCE, indexKey, qName, searcher);
    }

    // the parts of partial types as one type

    @RequiredReadAction
    @Override
    protected Set<? extends PsiElement> getOnlyElements(GlobalSearchScope globalSearchScope) {
        return new LinkedHashSet<>(VisualBasicPartialTypes.merge(super.getOnlyElements(globalSearchScope)));
    }

    @RequiredReadAction
    @Override
    public List<PsiElement> findChildren(String name,
                                         GlobalSearchScope globalSearchScope,
                                         Function<PsiElement, PsiElement> transformer,
                                         ChildrenFilter filter) {
        if (filter != ChildrenFilter.ONLY_ELEMENTS) {
            return super.findChildren(name, globalSearchScope, transformer, filter);
        }
        List<PsiElement> elements = VisualBasicPartialTypes.merge(super.findChildren(name, globalSearchScope, Function.identity(), filter));
        return transformData(elements, elements.size(), transformer);
    }

    @RequiredReadAction
    @Override
    public boolean processChildren(GlobalSearchScope globalSearchScope,
                                   Function<PsiElement, PsiElement> transformer,
                                   ChildrenFilter filter,
                                   Predicate<PsiElement> processor) {
        if (filter != ChildrenFilter.ONLY_ELEMENTS) {
            return super.processChildren(globalSearchScope, transformer, filter, processor);
        }
        List<PsiElement> elements = new ArrayList<>();
        super.processChildren(globalSearchScope, transformer, filter, elements::add);
        for (PsiElement element : VisualBasicPartialTypes.merge(elements)) {
            if (!processor.test(element)) {
                return false;
            }
        }
        return true;
    }
}
