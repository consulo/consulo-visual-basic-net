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
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;

import java.util.Arrays;

/**
 * {@code (Of Integer, String)}; arguments may be omitted in {@code GetType(Dictionary(Of ,))}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicTypeArgumentListImpl extends VisualBasicElementImpl implements DotNetTypeList {
    public VisualBasicTypeArgumentListImpl(ASTNode node) {
        super(node);
    }

    @Override
    public DotNetType[] getTypes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DotNetType.class).toArray(DotNetType[]::new);
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef[] getTypeRefs() {
        return Arrays.stream(getTypes()).map(DotNetType::toTypeRef).toArray(DotNetTypeRef[]::new);
    }

    /**
     * Arity, counting omitted arguments.
     */
    @Override
    public int getTypesCount() {
        int commas = 0;
        for (ASTNode child = getNode().getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == VisualBasicTokens.COMMA) {
                commas++;
            }
        }
        return commas + 1;
    }
}
