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
import consulo.dotnet.psi.DotNetNativeType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeRefByQName;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;

/**
 * {@code Integer}, {@code String}...
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicPredefinedTypeImpl extends VisualBasicElementImpl implements DotNetNativeType {
    public VisualBasicPredefinedTypeImpl(ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement getTypeElement() {
        PsiElement keyword = getFirstChild();
        if (keyword == null) {
            throw new IllegalStateException("predefined type without keyword");
        }
        return keyword;
    }

    @Override
    public IElementType getTypeElementType() {
        return getTypeElement().getNode().getElementType();
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef() {
        String vmQName = VisualBasicTypeResolver.getPredefinedTypeVmQName(getTypeElementType());
        return vmQName == null ? DotNetTypeRef.ERROR_TYPE : new VisualBasicTypeRefByQName(this, vmQName);
    }
}
