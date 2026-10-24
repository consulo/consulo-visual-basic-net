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
import consulo.dotnet.psi.DotNetCodeBodyProxy;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

/**
 * {@code Get}, {@code Set}, {@code AddHandler}, {@code RemoveHandler} and {@code RaiseEvent}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicAccessorImpl extends VisualBasicElementImpl implements DotNetXAccessor {
    public VisualBasicAccessorImpl(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getAccessorElement() {
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType type = child.getNode().getElementType();
            if (type == VisualBasicTokens.GET_KEYWORD
                || type == VisualBasicTokens.SET_KEYWORD
                || type == VisualBasicTokens.ADDHANDLER_KEYWORD
                || type == VisualBasicTokens.REMOVEHANDLER_KEYWORD
                || type == VisualBasicTokens.RAISEEVENT_KEYWORD) {
                return child;
            }
        }
        return null;
    }

    /**
     * {@code null} for {@code RaiseEvent}, which has no counterpart.
     */
    @RequiredReadAction
    @Override
    public @Nullable Kind getAccessorKind() {
        PsiElement element = getAccessorElement();
        if (element == null) {
            return null;
        }
        IElementType type = element.getNode().getElementType();
        if (type == VisualBasicTokens.GET_KEYWORD) {
            return Kind.GET;
        }
        if (type == VisualBasicTokens.SET_KEYWORD) {
            return Kind.SET;
        }
        if (type == VisualBasicTokens.ADDHANDLER_KEYWORD) {
            return Kind.ADD;
        }
        if (type == VisualBasicTokens.REMOVEHANDLER_KEYWORD) {
            return Kind.REMOVE;
        }
        return null;
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getAccessorElement();
    }

    @RequiredReadAction
    @Override
    public @Nullable String getName() {
        return getParent() instanceof DotNetNamedElement owner ? owner.getName() : null;
    }

    @RequiredWriteAction
    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("not supported");
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
    public DotNetCodeBodyProxy getCodeBlock() {
        return new VisualBasicCodeBodyProxy(this);
    }
}
