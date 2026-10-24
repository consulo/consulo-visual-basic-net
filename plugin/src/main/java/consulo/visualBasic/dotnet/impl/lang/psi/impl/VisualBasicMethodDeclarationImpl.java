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
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicStubElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicExpressionOperators;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import org.jspecify.annotations.Nullable;

/**
 * Methods, {@code Declare} methods and operators. Operators are named as in metadata: {@code op_Addition}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicMethodDeclarationImpl extends VisualBasicLikeMethodDeclarationImpl<VisualBasicMemberStub<VisualBasicMethodDeclarationImpl>>
    implements DotNetMethodDeclaration {
    public VisualBasicMethodDeclarationImpl(ASTNode node) {
        super(node);
    }

    public VisualBasicMethodDeclarationImpl(VisualBasicMemberStub<VisualBasicMethodDeclarationImpl> stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @RequiredReadAction
    public boolean isOperator() {
        return getElementType() == VisualBasicStubElements.OPERATOR_DECLARATION;
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getNameIdentifier() {
        if (isOperator()) {
            return getOperatorElement();
        }
        return super.getNameIdentifier();
    }

    @RequiredReadAction
    private @Nullable PsiElement getOperatorElement() {
        PsiElement operatorKeyword = VisualBasicPsiUtil.findChild(this, VisualBasicTokens.OPERATOR_KEYWORD);
        if (operatorKeyword == null) {
            return null;
        }
        PsiElement next = operatorKeyword.getNextSibling();
        while (next != null && next.getNode().getElementType() == VisualBasicTokens.WHITE_SPACE) {
            next = next.getNextSibling();
        }
        return next;
    }

    @RequiredReadAction
    @Override
    public @Nullable String getName() {
        VisualBasicMemberStub<VisualBasicMethodDeclarationImpl> stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        }
        if (isOperator()) {
            return getOperatorName();
        }
        return super.getName();
    }

    @RequiredReadAction
    private @Nullable String getOperatorName() {
        PsiElement operator = getOperatorElement();
        if (operator == null) {
            return null;
        }
        IElementType type = operator.getNode().getElementType();
        if (type == VisualBasicTokens.CTYPE_KEYWORD) {
            boolean narrowing = VisualBasicModifiers.has(VisualBasicModifierUtil.explicitMask(getModifierList()), VisualBasicModifier.NARROWING);
            return narrowing ? "op_Explicit" : "op_Implicit";
        }
        if (getParameters().length == 1) {
            if (type == VisualBasicTokens.PLUS) {
                return "op_UnaryPlus";
            }
            if (type == VisualBasicTokens.MINUS) {
                return "op_UnaryNegation";
            }
        }
        return VisualBasicExpressionOperators.getMetadataName(type);
    }

    @Override
    public @Nullable DotNetType getTypeForImplement() {
        return null;
    }

    @Override
    public DotNetTypeRef getTypeRefForImplement() {
        return DotNetTypeRef.ERROR_TYPE;
    }
}
