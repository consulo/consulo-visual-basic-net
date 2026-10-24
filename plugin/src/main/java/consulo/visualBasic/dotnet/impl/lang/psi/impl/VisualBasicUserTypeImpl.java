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
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.DotNetUserType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;

/**
 * Named type: {@code System.Collections.Generic.List(Of Integer)}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicUserTypeImpl extends VisualBasicElementImpl implements DotNetUserType {
    public VisualBasicUserTypeImpl(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public String getReferenceText() {
        return getReferenceExpression().getQualifiedNameText();
    }

    @Override
    public VisualBasicReferenceExpressionImpl getReferenceExpression() {
        VisualBasicReferenceExpressionImpl reference = PsiTreeUtil.getChildOfType(this, VisualBasicReferenceExpressionImpl.class);
        if (reference == null) {
            throw new IllegalStateException("user type without reference: " + getText());
        }
        return reference;
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef() {
        return VisualBasicTypeResolver.resolveUserType(this);
    }
}
