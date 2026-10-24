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

import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicParameterImpl extends VisualBasicVariableImpl implements DotNetParameter {
    public VisualBasicParameterImpl(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DotNetParameterListOwner getOwner() {
        PsiElement list = getParent();
        PsiElement owner = list == null ? null : list.getParent();
        return owner instanceof DotNetParameterListOwner parameterListOwner ? parameterListOwner : null;
    }

    @Override
    public int getIndex() {
        PsiElement list = getParent();
        if (!(list instanceof VisualBasicParameterListImpl parameterList)) {
            return -1;
        }
        DotNetParameter[] parameters = parameterList.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == this) {
                return i;
            }
        }
        return -1;
    }
}
