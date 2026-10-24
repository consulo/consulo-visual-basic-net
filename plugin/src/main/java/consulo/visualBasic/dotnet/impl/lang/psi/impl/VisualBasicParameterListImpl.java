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
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.Arrays;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicParameterListImpl extends VisualBasicElementImpl implements DotNetParameterList {
    public VisualBasicParameterListImpl(ASTNode node) {
        super(node);
    }

    @Override
    public int getParametersCount() {
        return getParameters().length;
    }

    @Override
    public DotNetParameter[] getParameters() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, VisualBasicParameterImpl.class).toArray(DotNetParameter[]::new);
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef[] getParameterTypeRefs() {
        return Arrays.stream(getParameters()).map(parameter -> parameter.toTypeRef(true)).toArray(DotNetTypeRef[]::new);
    }
}
