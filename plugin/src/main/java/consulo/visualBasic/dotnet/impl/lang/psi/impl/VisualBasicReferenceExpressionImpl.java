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
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.util.TextRange;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import consulo.language.util.IncorrectOperationException;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicExpressionResolver;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicExpressionTypes;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;
import org.jspecify.annotations.Nullable;

/**
 * Name with an optional qualifier: {@code a}, {@code a.b}, {@code a?.b}, {@code .b} (in a {@code With} block),
 * {@code Global}. Used in expressions and in type, namespace and import names.
 * <p>
 * Only references in type and namespace names resolve for now.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicReferenceExpressionImpl extends VisualBasicElementImpl implements DotNetReferenceExpression {
    public VisualBasicReferenceExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getQualifier() {
        // the qualifier is the only composite child coming first
        PsiElement first = getFirstChild();
        return first != null && first.getNode().getFirstChildNode() != null ? first : null;
    }

    /**
     * Name token: the last identifier (keywords after a dot are remapped to identifiers), or {@code Global}.
     */
    @RequiredReadAction
    public @Nullable PsiElement getReferenceElement() {
        PsiElement result = null;
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType type = child.getNode().getElementType();
            if (type == VisualBasicTokens.IDENTIFIER || type == VisualBasicTokens.GLOBAL_KEYWORD || type == VisualBasicTokens.INTEGER_LITERAL) {
                result = child;
            }
        }
        return result;
    }

    @RequiredReadAction
    @Override
    public @Nullable String getReferenceName() {
        PsiElement element = getReferenceElement();
        return element == null ? null : VisualBasicPsiUtil.getIdentifierName(element.getText());
    }

    public boolean isGlobal() {
        return getNode().findChildByType(VisualBasicTokens.GLOBAL_KEYWORD) != null && getNode().findChildByType(VisualBasicTokens.DOT) == null;
    }

    /**
     * Dotted name without type arguments: {@code System.Collections.Generic.List}.
     */
    @RequiredReadAction
    public String getQualifiedNameText() {
        String name = getReferenceName();
        if (getQualifier() instanceof VisualBasicReferenceExpressionImpl qualifier) {
            if (qualifier.isGlobal()) {
                return name == null ? "" : name;
            }
            return VisualBasicPsiUtil.qualify(qualifier.getQualifiedNameText(), name);
        }
        return name == null ? "" : name;
    }

    @RequiredReadAction
    public @Nullable VisualBasicTypeArgumentListImpl getTypeArgumentList() {
        PsiElement list = VisualBasicPsiUtil.findChild(this, VisualBasicElements.TYPE_ARGUMENT_LIST);
        return list instanceof VisualBasicTypeArgumentListImpl typeArguments ? typeArguments : null;
    }

    @Override
    public PsiReference getReference() {
        return this;
    }

    @Override
    public PsiElement getElement() {
        return this;
    }

    @RequiredReadAction
    @Override
    public TextRange getRangeInElement() {
        PsiElement element = getReferenceElement();
        if (element == null) {
            return new TextRange(0, getTextLength());
        }
        int start = element.getStartOffsetInParent();
        return new TextRange(start, start + element.getTextLength());
    }

    /**
     * In a type, namespace or {@code Imports} name rather than in an expression.
     */
    public boolean isInTypeOrNamespaceName() {
        PsiElement top = this;
        while (top.getParent() instanceof VisualBasicReferenceExpressionImpl parent && parent.getQualifier() == top) {
            top = parent;
        }
        PsiElement owner = top.getParent();
        if (owner == null) {
            return false;
        }
        IElementType ownerType = owner.getNode().getElementType();
        return ownerType == VisualBasicElements.USER_TYPE
            || ownerType == VisualBasicElements.IMPORTS_CLAUSE
            || ownerType == VisualBasicElements.NAMESPACE_DECLARATION
            || ownerType == VisualBasicElements.IMPLEMENTS_CLAUSE;
    }

    @RequiredReadAction
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        if (isInTypeOrNamespaceName()) {
            PsiElement target = VisualBasicTypeResolver.resolveNameReference(this);
            return target == null ? ResolveResult.EMPTY_ARRAY : new ResolveResult[]{new PsiElementResolveResult(target)};
        }
        // TODO [VISTALL] labels of GoTo, Handles and Implements clauses
        return VisualBasicExpressionResolver.resolve(this, incompleteCode);
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @RequiredReadAction
    @Override
    public String getCanonicalText() {
        return getText();
    }

    @RequiredWriteAction
    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        // TODO [VISTALL] rename
        throw new IncorrectOperationException("not supported yet");
    }

    @RequiredWriteAction
    @Override
    public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
        throw new IncorrectOperationException("not supported yet");
    }

    @RequiredReadAction
    @Override
    public boolean isReferenceTo(PsiElement element) {
        return getManager().areElementsEquivalent(resolve(), element);
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRef(boolean resolveFromParent) {
        return VisualBasicExpressionTypes.typeOf(this);
    }
}
