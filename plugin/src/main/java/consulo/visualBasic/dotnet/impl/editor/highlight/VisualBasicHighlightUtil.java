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

package consulo.visualBasic.dotnet.impl.editor.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.colorScheme.TextAttributesKey;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetLocalVariable;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicMethodDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import org.jspecify.annotations.Nullable;

/**
 * Colors of names by the element they declare or resolve to. Works on {@code DotNet*} so that references to C#, F#
 * and MSIL declarations are colored too.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicHighlightUtil {
    private VisualBasicHighlightUtil() {
    }

    /**
     * @param declaration the name is the declaration's own name rather than a reference to it
     */
    @RequiredReadAction
    public static @Nullable TextAttributesKey getKey(PsiElement element, boolean declaration) {
        if (element instanceof DotNetTypeDeclaration type) {
            return getTypeKey(type);
        }
        if (element instanceof DotNetConstructorDeclaration) {
            // Sub New: the keyword is not colored; a reference to a constructor names its type
            PsiElement parent = element.getParent();
            return declaration || parent == null ? null : getKey(parent, false);
        }
        if (element instanceof DotNetGenericParameter) {
            return VisualBasicHighlightKey.GENERIC_PARAMETER_NAME;
        }
        if (element instanceof DotNetParameter) {
            return VisualBasicHighlightKey.PARAMETER;
        }
        if (element instanceof DotNetMethodDeclaration method) {
            if (method instanceof VisualBasicMethodDeclarationImpl vbMethod && vbMethod.isOperator()) {
                return null;
            }
            if (declaration) {
                return VisualBasicHighlightKey.METHOD_NAME;
            }
            return isShared(method) ? VisualBasicHighlightKey.STATIC_METHOD_CALL : VisualBasicHighlightKey.INSTANCE_METHOD_CALL;
        }
        if (element instanceof DotNetLocalVariable) {
            return VisualBasicHighlightKey.LOCAL_VARIABLE;
        }
        if (element instanceof DotNetEventDeclaration event) {
            return isShared(event) ? VisualBasicHighlightKey.STATIC_EVENT : VisualBasicHighlightKey.INSTANCE_EVENT;
        }
        if (element instanceof DotNetVariable variable) {
            if (variable.isConstant()) {
                return VisualBasicHighlightKey.CONSTANT;
            }
            if (variable instanceof DotNetPropertyDeclaration) {
                return isShared(variable) ? VisualBasicHighlightKey.STATIC_PROPERTY : VisualBasicHighlightKey.INSTANCE_PROPERTY;
            }
            return isShared(variable) ? VisualBasicHighlightKey.STATIC_FIELD : VisualBasicHighlightKey.INSTANCE_FIELD;
        }
        return null;
    }

    @RequiredReadAction
    private static TextAttributesKey getTypeKey(DotNetTypeDeclaration type) {
        if (type instanceof VisualBasicTypeDeclarationImpl vbType) {
            switch (vbType.getKind()) {
                case MODULE:
                    return VisualBasicHighlightKey.MODULE_NAME;
                case DELEGATE:
                    return VisualBasicHighlightKey.DELEGATE_NAME;
                default:
                    break;
            }
        }
        if (type.isInterface()) {
            return VisualBasicHighlightKey.INTERFACE_NAME;
        }
        if (type.isEnum()) {
            return VisualBasicHighlightKey.ENUM_NAME;
        }
        if (type.isStruct()) {
            return VisualBasicHighlightKey.STRUCTURE_NAME;
        }
        if (DotNetInheritUtil.isAttribute(type)) {
            return VisualBasicHighlightKey.ATTRIBUTE_NAME;
        }
        if (!(type instanceof VisualBasicTypeDeclarationImpl) && DotNetInheritUtil.isParent(DotNetTypes.System.MulticastDelegate, type, false)) {
            return VisualBasicHighlightKey.DELEGATE_NAME;
        }
        return VisualBasicHighlightKey.CLASS_NAME;
    }

    @RequiredReadAction
    private static boolean isShared(DotNetModifierListOwner owner) {
        return owner.hasModifier(DotNetModifier.STATIC);
    }
}
