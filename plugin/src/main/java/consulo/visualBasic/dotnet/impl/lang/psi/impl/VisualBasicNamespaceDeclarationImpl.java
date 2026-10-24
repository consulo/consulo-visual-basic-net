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
import consulo.annotation.access.RequiredWriteAction;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicNamespaceStub;
import org.jspecify.annotations.Nullable;

/**
 * {@code Namespace A.B}. {@code Namespace Global.A} is not nested in the enclosing namespaces.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicNamespaceDeclarationImpl extends VisualBasicStubElementImpl<VisualBasicNamespaceStub> implements DotNetNamespaceDeclaration {
    public VisualBasicNamespaceDeclarationImpl(ASTNode node) {
        super(node);
    }

    public VisualBasicNamespaceDeclarationImpl(VisualBasicNamespaceStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public @Nullable VisualBasicReferenceExpressionImpl getNameReference() {
        return PsiTreeUtil.getChildOfType(this, VisualBasicReferenceExpressionImpl.class);
    }

    public boolean isGlobal() {
        VisualBasicReferenceExpressionImpl reference = getNameReference();
        if (reference == null) {
            return false;
        }
        VisualBasicReferenceExpressionImpl first = reference;
        while (first.getQualifier() instanceof VisualBasicReferenceExpressionImpl qualifier) {
            first = qualifier;
        }
        return first.isGlobal();
    }

    @RequiredReadAction
    @Override
    public @Nullable String getName() {
        VisualBasicNamespaceStub stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        }
        VisualBasicReferenceExpressionImpl reference = getNameReference();
        return reference == null ? null : reference.getQualifiedNameText();
    }

    @RequiredWriteAction
    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("not supported yet");
    }

    @RequiredReadAction
    @Override
    public @Nullable String getPresentableParentQName() {
        VisualBasicNamespaceStub stub = getGreenStub();
        if (stub != null) {
            return stub.getParentQName();
        }
        if (isGlobal()) {
            return null;
        }
        return VisualBasicPsiUtil.getParentQName(this);
    }

    @RequiredReadAction
    @Override
    public @Nullable String getPresentableQName() {
        return VisualBasicPsiUtil.qualify(getPresentableParentQName(), getName());
    }

    @RequiredReadAction
    @Override
    public DotNetNamedElement[] getMembers() {
        return VisualBasicMemberUtil.getMembers(this);
    }

    @Override
    public boolean hasModifier(DotNetModifier modifier) {
        return false;
    }

    @Override
    public @Nullable DotNetModifierList getModifierList() {
        return null;
    }
}
