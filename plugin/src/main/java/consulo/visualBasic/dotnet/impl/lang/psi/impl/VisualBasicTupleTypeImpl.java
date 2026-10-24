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
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code (Integer, name As String)} - {@code System.ValueTuple(Of Integer, String)}
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicTupleTypeImpl extends VisualBasicElementImpl implements DotNetType {
    public VisualBasicTupleTypeImpl(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef() {
        List<DotNetTypeRef> elements = new ArrayList<>();
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() == VisualBasicElements.TUPLE_TYPE_ELEMENT) {
                DotNetType type = VisualBasicPsiUtil.getTypeAfterAs(child);
                if (type == null) {
                    type = child.getFirstChild() instanceof DotNetType first ? first : null;
                }
                elements.add(type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef());
            }
        }
        return VisualBasicTypeResolver.tuple(this, elements);
    }
}
