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

package consulo.visualBasic.dotnet.impl.lang.psi.partial;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.navigation.Navigatable;
import consulo.util.dataholder.Key;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicModifierUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The parts of a partial type ({@code Partial Class X} and the other declarations of {@code X} in the same project) as
 * one type: the members, base types and modifiers of all parts. Two composites of the same parts are equal.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public class VisualBasicCompositeTypeDeclaration extends LightElement implements DotNetTypeDeclaration {
    private static final Set<DotNetModifier> ACCESS_MODIFIERS =
        Set.of(DotNetModifier.PUBLIC, DotNetModifier.PROTECTED, DotNetModifier.INTERNAL, DotNetModifier.PRIVATE);

    private final List<VisualBasicTypeDeclarationImpl> myParts;

    public VisualBasicCompositeTypeDeclaration(List<VisualBasicTypeDeclarationImpl> parts) {
        super(parts.get(0).getManager(), parts.get(0).getLanguage());
        myParts = List.copyOf(parts);
    }

    public List<VisualBasicTypeDeclarationImpl> getParts() {
        return myParts;
    }

    private VisualBasicTypeDeclarationImpl first() {
        return myParts.get(0);
    }

    @RequiredReadAction
    public VisualBasicTypeKind getKind() {
        return first().getKind();
    }

    @Override
    public <T> @Nullable T getUserData(Key<T> key) {
        if (key == ModuleUtilCore.KEY_MODULE) {
            @SuppressWarnings("unchecked") T module = (T) ModuleUtilCore.findModuleForPsiElement(first());
            return module;
        }
        return super.getUserData(key);
    }

    @Override
    public PsiElement getParent() {
        return first().getParent();
    }

    @Override
    public GlobalSearchScope getResolveScope() {
        return first().getResolveScope();
    }

    @Override
    public PsiElement getNavigationElement() {
        return first();
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public void navigate(boolean requestFocus) {
        ((Navigatable) first()).navigate(requestFocus);
    }

    @Override
    public boolean isValid() {
        for (VisualBasicTypeDeclarationImpl part : myParts) {
            if (!part.isValid()) {
                return false;
            }
        }
        return true;
    }

    @RequiredReadAction
    @Override
    public boolean isInterface() {
        return first().isInterface();
    }

    @RequiredReadAction
    @Override
    public boolean isStruct() {
        return first().isStruct();
    }

    @RequiredReadAction
    @Override
    public boolean isEnum() {
        return first().isEnum();
    }

    @RequiredReadAction
    @Override
    public boolean isNested() {
        return first().isNested();
    }

    @Override
    public @Nullable DotNetTypeList getExtendList() {
        return null;
    }

    /**
     * {@code Inherits} and {@code Implements} of all parts; the implicit base type when no part has {@code Inherits}.
     */
    @RequiredReadAction
    @Override
    public DotNetTypeRef[] getExtendTypeRefs() {
        List<DotNetTypeRef> result = new ArrayList<>();
        boolean hasBaseType = false;
        for (VisualBasicTypeDeclarationImpl part : myParts) {
            hasBaseType |= part.collectDeclaredExtendTypeRefs(result);
        }
        if (!hasBaseType) {
            DotNetTypeRef implicitBase = first().getImplicitBaseTypeRef();
            if (implicitBase != null) {
                result.add(0, implicitBase);
            }
        }
        return result.toArray(DotNetTypeRef[]::new);
    }

    @RequiredReadAction
    @Override
    public boolean isInheritor(String otherVmQName, boolean deep) {
        return DotNetInheritUtil.isInheritor(this, otherVmQName, deep);
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef getTypeRefForEnumConstants() {
        return first().getTypeRefForEnumConstants();
    }

    @RequiredReadAction
    @Override
    public @Nullable String getVmQName() {
        return first().getVmQName();
    }

    @RequiredReadAction
    @Override
    public @Nullable String getVmName() {
        return first().getVmName();
    }

    @RequiredReadAction
    @Override
    public @Nullable String getPresentableParentQName() {
        return first().getPresentableParentQName();
    }

    @RequiredReadAction
    @Override
    public @Nullable String getPresentableQName() {
        return first().getPresentableQName();
    }

    @RequiredReadAction
    @Override
    public String getName() {
        return String.valueOf(first().getName());
    }

    @RequiredWriteAction
    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        for (VisualBasicTypeDeclarationImpl part : myParts) {
            part.setName(name);
        }
        return this;
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return null;
    }

    /**
     * Access is the one written on a part; the others take it ({@code Partial Class X} without modifiers). Other
     * modifiers count when a part has them, except {@code Partial}.
     */
    @RequiredReadAction
    @Override
    public boolean hasModifier(DotNetModifier modifier) {
        if (modifier == VisualBasicModifier.PARTIAL) {
            return false;
        }
        if (ACCESS_MODIFIERS.contains(modifier)) {
            for (VisualBasicTypeDeclarationImpl part : myParts) {
                long explicitMask = VisualBasicModifierUtil.explicitMask(part.getModifierList());
                if (ACCESS_MODIFIERS.stream().anyMatch(access -> VisualBasicModifiers.has(explicitMask, access))) {
                    return VisualBasicModifiers.has(explicitMask, modifier);
                }
            }
            return first().hasModifier(modifier);
        }
        for (VisualBasicTypeDeclarationImpl part : myParts) {
            if (part.hasModifier(modifier)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetModifierList getModifierList() {
        return null;
    }

    @Override
    public @Nullable DotNetGenericParameterList getGenericParameterList() {
        return first().getGenericParameterList();
    }

    @Override
    public DotNetGenericParameter[] getGenericParameters() {
        return first().getGenericParameters();
    }

    @Override
    public int getGenericParametersCount() {
        return first().getGenericParametersCount();
    }

    @RequiredReadAction
    @Override
    public DotNetNamedElement[] getMembers() {
        List<DotNetNamedElement> members = new ArrayList<>();
        for (VisualBasicTypeDeclarationImpl part : myParts) {
            Collections.addAll(members, part.getMembers());
        }
        return members.toArray(DotNetNamedElement[]::new);
    }

    @Override
    public boolean isEquivalentTo(@Nullable PsiElement another) {
        if (equals(another)) {
            return true;
        }
        for (VisualBasicTypeDeclarationImpl part : myParts) {
            if (part.isEquivalentTo(another)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof VisualBasicCompositeTypeDeclaration other && myParts.equals(other.myParts);
    }

    @Override
    public int hashCode() {
        return myParts.hashCode();
    }

    @Override
    public String toString() {
        return "VisualBasicCompositeTypeDeclaration: " + myParts.size() + " parts";
    }
}
