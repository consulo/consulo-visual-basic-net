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
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import org.jspecify.annotations.Nullable;

/**
 * Property with accessors, or auto-implemented.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicPropertyDeclarationImpl extends VisualBasicStubVariableImpl<VisualBasicMemberStub<VisualBasicPropertyDeclarationImpl>>
    implements DotNetPropertyDeclaration {
    public VisualBasicPropertyDeclarationImpl(ASTNode node) {
        super(node);
    }

    public VisualBasicPropertyDeclarationImpl(VisualBasicMemberStub<VisualBasicPropertyDeclarationImpl> stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    /**
     * Declared accessors.
     */
    @Override
    public DotNetXAccessor[] getAccessors() {
        // TODO [VISTALL] accessors of auto-implemented properties, as light elements
        return PsiTreeUtil.getChildrenOfTypeAsList(this, VisualBasicAccessorImpl.class).toArray(DotNetXAccessor[]::new);
    }

    @Override
    public DotNetNamedElement[] getMembers() {
        return getAccessors();
    }

    @Override
    public @Nullable DotNetType getTypeForImplement() {
        return null;
    }

    @Override
    public DotNetTypeRef getTypeRefForImplement() {
        return DotNetTypeRef.ERROR_TYPE;
    }
}
