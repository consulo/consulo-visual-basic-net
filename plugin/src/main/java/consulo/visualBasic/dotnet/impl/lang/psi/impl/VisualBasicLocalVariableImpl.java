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

import consulo.dotnet.psi.DotNetLocalVariable;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;

/**
 * Local of {@code Dim}, {@code Const}, {@code Static}, {@code Using}, {@code For}, {@code For Each} or {@code Catch}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicLocalVariableImpl extends VisualBasicVariableImpl implements DotNetLocalVariable {
    public VisualBasicLocalVariableImpl(ASTNode node) {
        super(node);
    }

    @Override
    protected PsiElement getModifierListOwner() {
        PsiElement declarator = getParent();
        if (declarator != null && declarator.getNode().getElementType() == VisualBasicElements.VARIABLE_DECLARATOR) {
            PsiElement statement = declarator.getParent();
            if (statement != null) {
                return statement;
            }
        }
        return this;
    }
}
