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
import consulo.dotnet.psi.DotNetArrayType;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicArrayTypeRef;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;

/**
 * {@code Integer()}, {@code Integer(,)}
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicArrayTypeImpl extends VisualBasicElementImpl implements DotNetArrayType {
    public VisualBasicArrayTypeImpl(ASTNode node) {
        super(node);
    }

    @Override
    public DotNetType getInnerType() {
        DotNetType type = PsiTreeUtil.getChildOfType(this, DotNetType.class);
        if (type == null) {
            throw new IllegalStateException("array type without element type: " + getText());
        }
        return type;
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef() {
        PsiElement rank = VisualBasicPsiUtil.findChild(this, VisualBasicElements.ARRAY_RANK_SPECIFIER);
        return new VisualBasicArrayTypeRef(this, getInnerType().toTypeRef(), rank == null ? 1 : VisualBasicTypeResolver.getRank(rank));
    }
}
