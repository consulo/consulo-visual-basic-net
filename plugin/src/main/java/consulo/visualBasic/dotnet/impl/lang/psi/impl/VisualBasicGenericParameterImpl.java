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
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicGenericParameterImpl extends VisualBasicElementImpl implements DotNetGenericParameter {
    public VisualBasicGenericParameterImpl(ASTNode node) {
        super(node);
    }

    @Override
    public int getIndex() {
        if (getParent() instanceof VisualBasicGenericParameterListImpl list) {
            DotNetGenericParameter[] parameters = list.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == this) {
                    return i;
                }
            }
        }
        return -1;
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

    @RequiredWriteAction
    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("not supported yet");
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetModifierList getModifierList() {
        PsiElement list = VisualBasicPsiUtil.findChild(this, VisualBasicElements.MODIFIER_LIST);
        return list instanceof DotNetModifierList modifierList ? modifierList : null;
    }

    @RequiredReadAction
    @Override
    public boolean hasModifier(DotNetModifier modifier) {
        DotNetModifierList list = getModifierList();
        return list != null && list.hasModifier(modifier);
    }

    @Override
    public DotNetAttribute[] getAttributes() {
        return DotNetAttribute.EMPTY_ARRAY;
    }
}
