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
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicAttributeImpl extends VisualBasicElementImpl implements DotNetAttribute {
    public VisualBasicAttributeImpl(ASTNode node) {
        super(node);
    }

    public @Nullable VisualBasicUserTypeImpl getType() {
        return PsiTreeUtil.getChildOfType(this, VisualBasicUserTypeImpl.class);
    }

    @RequiredReadAction
    @Override
    public @Nullable DotNetTypeDeclaration resolveToType() {
        PsiElement element = toTypeRef().resolve().getElement();
        return element instanceof DotNetTypeDeclaration type ? type : null;
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef() {
        VisualBasicUserTypeImpl type = getType();
        return type == null ? DotNetTypeRef.ERROR_TYPE : VisualBasicTypeResolver.resolveAttributeType(type);
    }
}
