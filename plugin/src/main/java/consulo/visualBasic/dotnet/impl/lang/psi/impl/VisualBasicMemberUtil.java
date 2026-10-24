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

package consulo.visualBasic.dotnet.impl.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import consulo.language.psi.stub.StubElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;

import java.util.ArrayList;
import java.util.List;

/**
 * Members of files, namespaces and types. Fields are nested in {@code Dim} statements and declarators.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public final class VisualBasicMemberUtil {
    private static final TokenSet CONTAINERS = TokenSet.create(VisualBasicElements.FIELD_DECLARATION_STATEMENT, VisualBasicElements.VARIABLE_DECLARATOR);

    private VisualBasicMemberUtil() {
    }

    @RequiredReadAction
    public static DotNetNamedElement[] getMembers(PsiElement owner) {
        List<DotNetNamedElement> result = new ArrayList<>();
        if (owner instanceof StubBasedPsiElement<?> stubBased && stubBased.getStub() instanceof StubElement<?> stub) {
            for (StubElement<?> child : stub.getChildrenStubs()) {
                if (child.getPsi() instanceof DotNetNamedElement member) {
                    result.add(member);
                }
            }
            return result.toArray(DotNetNamedElement[]::new);
        }
        collect(owner, result);
        return result.toArray(DotNetNamedElement[]::new);
    }

    private static void collect(PsiElement element, List<DotNetNamedElement> result) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType type = child.getNode().getElementType();
            if (child instanceof DotNetNamedElement member && child instanceof StubBasedPsiElement<?>) {
                result.add(member);
            }
            else if (CONTAINERS.contains(type)) {
                collect(child, result);
            }
        }
    }
}
