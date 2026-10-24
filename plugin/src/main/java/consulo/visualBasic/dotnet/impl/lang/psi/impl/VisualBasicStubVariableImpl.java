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
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import org.jspecify.annotations.Nullable;

/**
 * Stubbed variable: field, property or event.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public abstract class VisualBasicStubVariableImpl<S extends VisualBasicMemberStub<?>> extends VisualBasicStubMemberImpl<S> implements DotNetVariable {
    protected VisualBasicStubVariableImpl(ASTNode node) {
        super(node);
    }

    protected VisualBasicStubVariableImpl(S stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @RequiredReadAction
    @Override
    public boolean isConstant() {
        return VisualBasicModifiers.has(getModifierMask(), VisualBasicModifier.CONST);
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getConstantKeywordElement() {
        return getModifierList() == null ? null : getModifierList().getModifierElement(VisualBasicModifier.CONST);
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
