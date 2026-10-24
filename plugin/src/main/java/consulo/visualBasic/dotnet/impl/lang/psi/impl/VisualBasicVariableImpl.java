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
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

/**
 * Variable which is not kept in stubs: parameter or local.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public abstract class VisualBasicVariableImpl extends VisualBasicElementImpl implements DotNetVariable {
    protected VisualBasicVariableImpl(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return VisualBasicPsiUtil.findChild(this, VisualBasicTokens.IDENTIFIER);
    }

    @RequiredReadAction
    @Override
    public @Nullable String getName() {
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

    /**
     * Element owning the modifier list: the declaration statement for locals.
     */
    protected PsiElement getModifierListOwner() {
        return this;
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetModifierList getModifierList() {
        PsiElement list = VisualBasicPsiUtil.findChild(getModifierListOwner(), VisualBasicElements.MODIFIER_LIST);
        return list instanceof DotNetModifierList modifierList ? modifierList : null;
    }

    @RequiredReadAction
    @Override
    public boolean hasModifier(DotNetModifier modifier) {
        DotNetModifierList list = getModifierList();
        return list != null && list.hasModifier(modifier);
    }

    @RequiredReadAction
    @Override
    public boolean isConstant() {
        return VisualBasicModifiers.has(VisualBasicModifierUtil.explicitMask((PsiElement) getModifierList()), VisualBasicModifier.CONST);
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getConstantKeywordElement() {
        DotNetModifierList list = getModifierList();
        return list == null ? null : list.getModifierElement(VisualBasicModifier.CONST);
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetType getType() {
        return VisualBasicVariableUtil.getType(this);
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetExpression getInitializer() {
        return VisualBasicVariableUtil.getInitializer(this);
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef(boolean resolveFromInitializer) {
        return VisualBasicVariableUtil.toTypeRef(this);
    }
}
