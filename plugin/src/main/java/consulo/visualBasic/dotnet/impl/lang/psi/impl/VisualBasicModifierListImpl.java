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
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Attributes and explicit modifiers. Owners report effective modifiers, see {@link VisualBasicModifierUtil}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicModifierListImpl extends VisualBasicElementImpl implements DotNetModifierList {
    public VisualBasicModifierListImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void addModifier(DotNetModifier modifier) {
        // TODO [VISTALL] code modification
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeModifier(DotNetModifier modifier) {
        throw new UnsupportedOperationException();
    }

    @RequiredReadAction
    @Override
    public DotNetModifier[] getModifiers() {
        return VisualBasicModifiers.toArray(VisualBasicModifierUtil.explicitMask(this));
    }

    @RequiredReadAction
    @Override
    public DotNetAttribute[] getAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, VisualBasicAttributeListImpl.class)
            .stream()
            .flatMap(list -> List.of(list.getAttributes()).stream())
            .toArray(DotNetAttribute[]::new);
    }

    @RequiredReadAction
    @Override
    public boolean hasModifier(DotNetModifier modifier) {
        return VisualBasicModifiers.has(VisualBasicModifierUtil.explicitMask(this), modifier);
    }

    @RequiredReadAction
    @Override
    public boolean hasModifierInTree(DotNetModifier modifier) {
        return hasModifier(modifier);
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getModifierElement(DotNetModifier modifier) {
        List<PsiElement> elements = getModifierElements(modifier);
        return elements.isEmpty() ? null : elements.get(0);
    }

    @RequiredReadAction
    @Override
    public List<PsiElement> getModifierElements(DotNetModifier modifier) {
        List<PsiElement> result = new ArrayList<>();
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
            if (VisualBasicModifiers.byKeyword(child.getNode().getElementType()) == modifier) {
                result.add(child);
            }
        }
        return result;
    }
}
