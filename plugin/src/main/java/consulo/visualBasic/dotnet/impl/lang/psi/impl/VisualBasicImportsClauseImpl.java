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
import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

/**
 * {@code System.Text}, alias {@code IO = System.IO}, or an XML namespace.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicImportsClauseImpl extends VisualBasicElementImpl {
    public VisualBasicImportsClauseImpl(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    public @Nullable String getAlias() {
        if (getNode().findChildByType(VisualBasicTokens.EQ) == null) {
            return null;
        }
        return VisualBasicPsiUtil.getName(this);
    }

    public @Nullable VisualBasicReferenceExpressionImpl getReference() {
        return PsiTreeUtil.getChildOfType(this, VisualBasicReferenceExpressionImpl.class);
    }

    /**
     * Imported namespace or type, {@code null} for XML namespaces.
     */
    @RequiredReadAction
    public @Nullable String getImportedQName() {
        VisualBasicReferenceExpressionImpl reference = getReference();
        return reference == null ? null : reference.getQualifiedNameText();
    }
}
