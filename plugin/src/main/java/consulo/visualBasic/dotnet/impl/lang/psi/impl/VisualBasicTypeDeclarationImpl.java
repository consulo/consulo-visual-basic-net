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
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclarationUtil;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicStubElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeRefByQName;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeRefByTypeDeclaration;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicTypeStub;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Class}, {@code Structure}, {@code Module}, {@code Interface}, {@code Enum} and {@code Delegate}.
 * A module is a sealed class with shared members.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicTypeDeclarationImpl extends VisualBasicStubMemberImpl<VisualBasicTypeStub> implements DotNetTypeDeclaration, DotNetParameterListOwner {
    public VisualBasicTypeDeclarationImpl(ASTNode node) {
        super(node);
    }

    public VisualBasicTypeDeclarationImpl(VisualBasicTypeStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @RequiredReadAction
    public VisualBasicTypeKind getKind() {
        VisualBasicTypeStub stub = getGreenStub();
        if (stub != null) {
            return stub.getKind();
        }
        IElementType elementType = getNode().getElementType();
        if (elementType == VisualBasicStubElements.ENUM_DECLARATION) {
            return VisualBasicTypeKind.ENUM;
        }
        if (elementType == VisualBasicStubElements.DELEGATE_DECLARATION) {
            return VisualBasicTypeKind.DELEGATE;
        }
        if (getNode().findChildByType(VisualBasicTokens.STRUCTURE_KEYWORD) != null) {
            return VisualBasicTypeKind.STRUCTURE;
        }
        if (getNode().findChildByType(VisualBasicTokens.MODULE_KEYWORD) != null) {
            return VisualBasicTypeKind.MODULE;
        }
        if (getNode().findChildByType(VisualBasicTokens.INTERFACE_KEYWORD) != null) {
            return VisualBasicTypeKind.INTERFACE;
        }
        return VisualBasicTypeKind.CLASS;
    }

    @RequiredReadAction
    @Override
    public boolean isInterface() {
        return getKind() == VisualBasicTypeKind.INTERFACE;
    }

    @RequiredReadAction
    @Override
    public boolean isStruct() {
        return getKind() == VisualBasicTypeKind.STRUCTURE;
    }

    @RequiredReadAction
    @Override
    public boolean isEnum() {
        return getKind() == VisualBasicTypeKind.ENUM;
    }

    @RequiredReadAction
    @Override
    public boolean isNested() {
        VisualBasicTypeStub stub = getGreenStub();
        if (stub != null) {
            return stub.isNested();
        }
        return getParent() instanceof DotNetTypeDeclaration;
    }

    @Override
    public @Nullable DotNetTypeList getExtendList() {
        return null;
    }

    /**
     * Types of {@code Inherits} and {@code Implements}; the implicit base type when there is no {@code Inherits}.
     */
    @RequiredReadAction
    @Override
    public DotNetTypeRef[] getExtendTypeRefs() {
        List<DotNetTypeRef> result = new ArrayList<>();
        boolean hasBaseType = false;
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType type = child.getNode().getElementType();
            if (type == VisualBasicElements.INHERITS_STATEMENT || type == VisualBasicElements.IMPLEMENTS_STATEMENT) {
                for (DotNetType extendType : PsiTreeUtil.getChildrenOfTypeAsList(child, DotNetType.class)) {
                    result.add(extendType.toTypeRef());
                    hasBaseType |= type == VisualBasicElements.INHERITS_STATEMENT && !isInterface();
                }
            }
        }
        String implicitBase = switch (getKind()) {
            case CLASS, MODULE -> DotNetTypes.System.Object;
            case STRUCTURE -> DotNetTypes.System.ValueType;
            case ENUM -> DotNetTypes.System.Enum;
            case DELEGATE -> DotNetTypes.System.MulticastDelegate;
            case INTERFACE -> null;
        };
        if (!hasBaseType && implicitBase != null) {
            result.add(0, new VisualBasicTypeRefByQName(this, implicitBase));
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
        return new VisualBasicTypeRefByTypeDeclaration(this);
    }

    /**
     * Underlying type of an enum: {@code Enum Color As Byte}.
     */
    @RequiredReadAction
    public @Nullable DotNetType getEnumUnderlyingType() {
        return isEnum() ? VisualBasicPsiUtil.getTypeAfterAs(this) : null;
    }

    @RequiredReadAction
    @Override
    public @Nullable String getVmQName() {
        VisualBasicTypeStub stub = getGreenStub();
        if (stub != null) {
            return stub.getVmQName();
        }
        // nested types: Outer`1/Inner; others: the qualified name, the root namespace of the project included
        String vmName = getVmName();
        if (getParent() instanceof DotNetTypeDeclaration parent) {
            String parentVmQName = parent.getVmQName();
            return parentVmQName == null ? vmName : parentVmQName + DotNetTypeDeclarationUtil.NESTED_SEPARATOR_IN_NAME + vmName;
        }
        String parentQName = getPresentableParentQName();
        return parentQName == null || parentQName.isEmpty() ? vmName : parentQName + DotNetTypeDeclarationUtil.NORMAL_SEPARATOR_IN_NAME + vmName;
    }

    @RequiredReadAction
    @Override
    public @Nullable String getVmName() {
        return DotNetTypeDeclarationUtil.getVmName(this);
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetGenericParameterList getGenericParameterList() {
        PsiElement list = VisualBasicPsiUtil.findChild(this, VisualBasicElements.GENERIC_PARAMETER_LIST);
        return list instanceof DotNetGenericParameterList genericParameterList ? genericParameterList : null;
    }

    @RequiredReadAction
    @Override
    public DotNetGenericParameter[] getGenericParameters() {
        DotNetGenericParameterList list = getGenericParameterList();
        return list == null ? DotNetGenericParameter.EMPTY_ARRAY : list.getParameters();
    }

    @RequiredReadAction
    @Override
    public int getGenericParametersCount() {
        DotNetGenericParameterList list = getGenericParameterList();
        return list == null ? 0 : list.getGenericParametersCount();
    }

    @RequiredReadAction
    @Override
    public DotNetNamedElement[] getMembers() {
        // TODO [VISTALL] Invoke and other members of delegates, as light elements
        return VisualBasicMemberUtil.getMembers(this);
    }

    // delegate parameters

    @RequiredReadAction
    @Override
    public @Nullable DotNetParameterList getParameterList() {
        PsiElement list = VisualBasicPsiUtil.findChild(this, VisualBasicElements.PARAMETER_LIST);
        return list instanceof DotNetParameterList parameterList ? parameterList : null;
    }

    @RequiredReadAction
    @Override
    public DotNetParameter[] getParameters() {
        DotNetParameterList list = getParameterList();
        return list == null ? DotNetParameter.EMPTY_ARRAY : list.getParameters();
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef[] getParameterTypeRefs() {
        DotNetParameterList list = getParameterList();
        return list == null ? DotNetTypeRef.EMPTY_ARRAY : list.getParameterTypeRefs();
    }
}
