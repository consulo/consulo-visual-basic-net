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
import consulo.dotnet.psi.DotNetCodeBodyProxy;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeRefByQName;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import org.jspecify.annotations.Nullable;

/**
 * {@code Sub}, {@code Function}, {@code Sub New}, {@code Operator} and {@code Declare}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public abstract class VisualBasicLikeMethodDeclarationImpl<S extends VisualBasicMemberStub<?>> extends VisualBasicStubMemberImpl<S>
    implements DotNetLikeMethodDeclaration {
    protected VisualBasicLikeMethodDeclarationImpl(ASTNode node) {
        super(node);
    }

    protected VisualBasicLikeMethodDeclarationImpl(S stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @RequiredReadAction
    public boolean isSub() {
        return getNode().findChildByType(VisualBasicTokens.SUB_KEYWORD) != null;
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetType getReturnType() {
        return VisualBasicPsiUtil.getTypeAfterAs(this);
    }

    /**
     * {@code System.Void} for {@code Sub}; a {@code Function} without {@code As} returns the type of its type character,
     * or {@code Object}.
     */
    @RequiredReadAction
    @Override
    public DotNetTypeRef getReturnTypeRef() {
        if (isSub()) {
            return new VisualBasicTypeRefByQName(this, DotNetTypes.System.Void);
        }
        DotNetType type = getReturnType();
        if (type != null) {
            return type.toTypeRef();
        }
        PsiElement identifier = getNameIdentifier();
        String vmQName = identifier == null
            ? null
            : VisualBasicTypeResolver.getTypeCharacterVmQName(VisualBasicPsiUtil.getTypeCharacter(identifier.getText()));
        return new VisualBasicTypeRefByQName(this, vmQName == null ? DotNetTypes.System.Object : vmQName);
    }

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

    @Override
    public DotNetCodeBodyProxy getCodeBlock() {
        return new VisualBasicCodeBodyProxy(this);
    }
}
