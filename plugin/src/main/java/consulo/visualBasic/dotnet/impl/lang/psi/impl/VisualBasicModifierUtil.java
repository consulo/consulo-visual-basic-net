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
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicStubElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import org.jspecify.annotations.Nullable;

/**
 * Effective modifiers: explicit ones plus the implicit accessibility and {@code Shared} of Visual Basic.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public final class VisualBasicModifierUtil {
    private VisualBasicModifierUtil() {
    }

    /**
     * Explicit modifiers of a modifier list.
     */
    @RequiredReadAction
    public static long explicitMask(@Nullable PsiElement modifierList) {
        long mask = 0;
        if (modifierList == null) {
            return mask;
        }
        for (PsiElement child = modifierList.getFirstChild(); child != null; child = child.getNextSibling()) {
            DotNetModifier modifier = VisualBasicModifiers.byKeyword(child.getNode().getElementType());
            if (modifier != null) {
                mask |= VisualBasicModifiers.maskOf(modifier);
            }
        }
        return mask;
    }

    @RequiredReadAction
    public static long computeMask(VisualBasicStubMemberImpl<?> member) {
        DotNetModifierList modifierList = member.getModifierList();
        long mask = explicitMask(modifierList);
        IElementType type = member.getNode().getElementType();
        VisualBasicTypeDeclarationImpl container = getContainingType(member);
        VisualBasicTypeKind containerKind = container == null ? null : container.getKind();

        if ((mask & VisualBasicModifiers.ACCESS_MASK) == 0) {
            mask |= VisualBasicModifiers.maskOf(defaultAccess(type, containerKind));
        }

        boolean isType = member instanceof VisualBasicTypeDeclarationImpl;
        if (isType && ((VisualBasicTypeDeclarationImpl) member).getKind() == VisualBasicTypeKind.MODULE) {
            mask |= VisualBasicModifiers.maskOf(DotNetModifier.STATIC) | VisualBasicModifiers.maskOf(DotNetModifier.SEALED);
        }
        if (!isType && containerKind == VisualBasicTypeKind.MODULE) {
            // members of a module are shared
            mask |= VisualBasicModifiers.maskOf(DotNetModifier.STATIC);
        }
        if (VisualBasicModifiers.has(mask, VisualBasicModifier.CONST)
            || type == VisualBasicStubElements.OPERATOR_DECLARATION
            || type == VisualBasicStubElements.ENUM_MEMBER_DECLARATION) {
            mask |= VisualBasicModifiers.maskOf(DotNetModifier.STATIC);
        }
        return mask;
    }

    private static DotNetModifier defaultAccess(IElementType type, @Nullable VisualBasicTypeKind containerKind) {
        if (type == VisualBasicStubElements.TYPE_DECLARATION
            || type == VisualBasicStubElements.ENUM_DECLARATION
            || type == VisualBasicStubElements.DELEGATE_DECLARATION) {
            // nested types are public, others are friend
            return containerKind == null ? DotNetModifier.INTERNAL : DotNetModifier.PUBLIC;
        }
        if (type == VisualBasicStubElements.FIELD_DECLARATION) {
            // Dim fields are private in classes and modules
            return containerKind == VisualBasicTypeKind.STRUCTURE || containerKind == VisualBasicTypeKind.INTERFACE
                ? DotNetModifier.PUBLIC
                : DotNetModifier.PRIVATE;
        }
        return DotNetModifier.PUBLIC;
    }

    @RequiredReadAction
    public static @Nullable VisualBasicTypeDeclarationImpl getContainingType(PsiElement element) {
        for (PsiElement parent = element.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof VisualBasicTypeDeclarationImpl type) {
                return type;
            }
            if (parent instanceof VisualBasicNamespaceDeclarationImpl || parent instanceof VisualBasicFileImpl) {
                return null;
            }
        }
        return null;
    }
}
