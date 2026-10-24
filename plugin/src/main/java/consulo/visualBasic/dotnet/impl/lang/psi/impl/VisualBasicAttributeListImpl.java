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
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;

/**
 * {@code <A, B(1)>}, a target ({@code Assembly:} or {@code Module:}) is given per attribute.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicAttributeListImpl extends VisualBasicElementImpl implements DotNetAttributeList {
    public VisualBasicAttributeListImpl(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public DotNetAttributeTargetType getTargetType() {
        for (VisualBasicAttributeImpl attribute : PsiTreeUtil.getChildrenOfTypeAsList(this, VisualBasicAttributeImpl.class)) {
            PsiElement target = VisualBasicPsiUtil.findChild(attribute, VisualBasicElements.ATTRIBUTE_TARGET);
            if (target != null) {
                return target.getNode().findChildByType(VisualBasicTokens.MODULE_KEYWORD) != null
                    ? DotNetAttributeTargetType.MODULE
                    : DotNetAttributeTargetType.ASSEMBLY;
            }
        }
        // TODO [VISTALL] by owner
        return DotNetAttributeTargetType.TYPE;
    }

    @RequiredReadAction
    @Override
    public DotNetAttribute[] getAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, VisualBasicAttributeImpl.class).toArray(DotNetAttribute[]::new);
    }
}
