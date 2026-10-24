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
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.util.IncorrectOperationException;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import org.jspecify.annotations.Nullable;

/**
 * Declaration with a name, modifiers and a qualified name.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public abstract class VisualBasicStubMemberImpl<S extends VisualBasicMemberStub<?>> extends VisualBasicStubElementImpl<S>
    implements DotNetModifierListOwner, DotNetQualifiedElement, PsiNameIdentifierOwner {
    protected VisualBasicStubMemberImpl(ASTNode node) {
        super(node);
    }

    protected VisualBasicStubMemberImpl(S stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return VisualBasicPsiUtil.findChild(this, VisualBasicTokens.IDENTIFIER);
    }

    @RequiredReadAction
    @Override
    public @Nullable String getName() {
        S stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        }
        return VisualBasicPsiUtil.getName(this);
    }

    @RequiredReadAction
    @Override
    public int getTextOffset() {
        PsiElement nameIdentifier = getNameIdentifier();
        return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
    }

    @RequiredWriteAction
    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        // TODO [VISTALL] rename
        throw new IncorrectOperationException("not supported yet");
    }

    @RequiredReadAction
    @Override
    public @Nullable String getPresentableParentQName() {
        S stub = getGreenStub();
        if (stub != null) {
            return stub.getParentQName();
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
    public boolean hasModifier(DotNetModifier modifier) {
        return VisualBasicModifiers.has(getModifierMask(), modifier);
    }

    /**
     * Effective modifiers, see {@link VisualBasicModifierUtil}.
     */
    @RequiredReadAction
    public long getModifierMask() {
        S stub = getGreenStub();
        if (stub != null) {
            return stub.getModifierMask();
        }
        return VisualBasicModifierUtil.computeMask(this);
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetModifierList getModifierList() {
        PsiElement modifierList = VisualBasicPsiUtil.findChild(this, VisualBasicElements.MODIFIER_LIST);
        return modifierList instanceof DotNetModifierList list ? list : null;
    }
}
