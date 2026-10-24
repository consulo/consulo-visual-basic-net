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
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import org.jspecify.annotations.Nullable;

/**
 * One name of {@code Private a, b As Integer}: modifiers belong to the statement, the type to the declarator.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicFieldDeclarationImpl extends VisualBasicStubVariableImpl<VisualBasicMemberStub<VisualBasicFieldDeclarationImpl>>
    implements DotNetFieldDeclaration {
    public VisualBasicFieldDeclarationImpl(ASTNode node) {
        super(node);
    }

    public VisualBasicFieldDeclarationImpl(VisualBasicMemberStub<VisualBasicFieldDeclarationImpl> stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetModifierList getModifierList() {
        PsiElement declarator = getParent();
        PsiElement statement = declarator == null ? null : declarator.getParent();
        if (statement == null || statement.getNode().getElementType() != VisualBasicElements.FIELD_DECLARATION_STATEMENT) {
            return null;
        }
        PsiElement list = VisualBasicPsiUtil.findChild(statement, VisualBasicElements.MODIFIER_LIST);
        return list instanceof DotNetModifierList modifierList ? modifierList : null;
    }
}
