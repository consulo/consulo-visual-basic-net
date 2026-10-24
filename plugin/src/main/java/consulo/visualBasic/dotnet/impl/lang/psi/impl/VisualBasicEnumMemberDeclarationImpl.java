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
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeRefByTypeDeclaration;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import org.jspecify.annotations.Nullable;

/**
 * Enum member: a shared constant of the enum type.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicEnumMemberDeclarationImpl extends VisualBasicStubMemberImpl<VisualBasicMemberStub<VisualBasicEnumMemberDeclarationImpl>>
    implements DotNetFieldDeclaration {
    public VisualBasicEnumMemberDeclarationImpl(ASTNode node) {
        super(node);
    }

    public VisualBasicEnumMemberDeclarationImpl(VisualBasicMemberStub<VisualBasicEnumMemberDeclarationImpl> stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public @Nullable PsiElement getConstantKeywordElement() {
        return null;
    }

    @Override
    public @Nullable DotNetType getType() {
        return null;
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetExpression getInitializer() {
        return VisualBasicVariableUtil.getInitializer(this);
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef(boolean resolveFromInitializer) {
        VisualBasicTypeDeclarationImpl type = VisualBasicModifierUtil.getContainingType(this);
        return type == null ? DotNetTypeRef.ERROR_TYPE : new VisualBasicTypeRefByTypeDeclaration(type);
    }
}
