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
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.language.ast.IElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicArrayTypeRef;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicExpressionTypes;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeRefByQName;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicTypeResolver;
import org.jspecify.annotations.Nullable;

/**
 * Types of declared names: {@code x As T}, {@code x As New T()}, {@code x%}, {@code x() As T}, {@code x? As T}.
 * Names of a declarator share its {@code As} clause: {@code Dim a, b As Integer}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public final class VisualBasicVariableUtil {
    private VisualBasicVariableUtil() {
    }

    /**
     * Element holding the {@code As} clause of a variable: its declarator, or the variable itself.
     */
    @RequiredReadAction
    public static PsiElement getAsClauseOwner(PsiElement variable) {
        PsiElement parent = variable.getParent();
        return parent != null && parent.getNode().getElementType() == VisualBasicElements.VARIABLE_DECLARATOR ? parent : variable;
    }

    @RequiredReadAction
    public static @Nullable DotNetType getType(PsiElement variable) {
        return VisualBasicPsiUtil.getTypeAfterAs(getAsClauseOwner(variable));
    }

    /**
     * {@code New T()} of {@code As New T()}.
     */
    @RequiredReadAction
    public static @Nullable PsiElement getAsNewExpression(PsiElement variable) {
        PsiElement owner = getAsClauseOwner(variable);
        boolean afterAs = false;
        for (PsiElement child = owner.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() == VisualBasicTokens.AS_KEYWORD) {
                afterAs = true;
            }
            else if (afterAs && child.getNode().getElementType() == VisualBasicElements.NEW_EXPRESSION) {
                return child;
            }
        }
        return null;
    }

    /**
     * Expression after {@code =}, when the declarator declares one name.
     */
    @RequiredReadAction
    public static @Nullable DotNetExpression getInitializer(PsiElement variable) {
        PsiElement owner = getAsClauseOwner(variable);
        boolean afterEq = false;
        for (PsiElement child = owner.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() == VisualBasicTokens.EQ) {
                afterEq = true;
            }
            else if (afterEq && child instanceof DotNetExpression expression) {
                return expression;
            }
        }
        return null;
    }

    @RequiredReadAction
    public static DotNetTypeRef toTypeRef(PsiElement variable) {
        DotNetTypeRef typeRef = baseTypeRef(variable);
        PsiElement rank = VisualBasicPsiUtil.findChild(variable, VisualBasicElements.ARRAY_RANK_SPECIFIER);
        PsiElement bounds = VisualBasicPsiUtil.findChild(variable, VisualBasicElements.ARRAY_BOUNDS);
        if (variable.getNode().findChildByType(VisualBasicTokens.QUESTION) != null) {
            typeRef = VisualBasicTypeResolver.nullable(variable, typeRef);
        }
        if (rank != null) {
            typeRef = new VisualBasicArrayTypeRef(variable, typeRef, VisualBasicTypeResolver.getRank(rank));
        }
        else if (bounds != null) {
            typeRef = new VisualBasicArrayTypeRef(variable, typeRef, VisualBasicTypeResolver.getRank(bounds));
        }
        return typeRef;
    }

    @RequiredReadAction
    private static DotNetTypeRef baseTypeRef(PsiElement variable) {
        DotNetType type = getType(variable);
        if (type != null) {
            return type.toTypeRef();
        }
        PsiElement newExpression = getAsNewExpression(variable);
        if (newExpression != null) {
            DotNetType createdType = PsiTreeUtil.getChildOfType(newExpression, DotNetType.class);
            return createdType == null ? DotNetTypeRef.ERROR_TYPE : createdType.toTypeRef();
        }
        PsiElement identifier = VisualBasicPsiUtil.findChild(variable, VisualBasicTokens.IDENTIFIER);
        if (identifier != null) {
            String vmQName = VisualBasicTypeResolver.getTypeCharacterVmQName(VisualBasicPsiUtil.getTypeCharacter(identifier.getText()));
            if (vmQName != null) {
                return new VisualBasicTypeRefByQName(variable, vmQName);
            }
        }
        // constants and locals (Option Infer On) take the type of their initializer
        if (isInferred(variable)) {
            PsiElement statement = variable.getParent();
            IElementType statementType = statement == null ? null : statement.getNode().getElementType();
            if (statement != null && statementType == VisualBasicElements.FOR_EACH_STATEMENT) {
                PsiElement collection = getExpressionAfter(statement, VisualBasicTokens.IN_KEYWORD);
                if (collection != null) {
                    return VisualBasicExpressionTypes.elementTypeOf(VisualBasicExpressionTypes.typeOf(collection), variable);
                }
            }
            else if (statement != null && statementType == VisualBasicElements.FOR_STATEMENT) {
                PsiElement start = getExpressionAfter(statement, VisualBasicTokens.EQ);
                if (start != null) {
                    return VisualBasicExpressionTypes.typeOf(start);
                }
            }
            else {
                DotNetExpression initializer = getInitializer(variable);
                if (initializer != null) {
                    DotNetTypeRef typeRef = VisualBasicExpressionTypes.typeOf(initializer);
                    if (typeRef != DotNetTypeRef.ERROR_TYPE) {
                        return typeRef;
                    }
                }
            }
        }
        // TODO [VISTALL] Option Infer and Option Strict
        return new VisualBasicTypeRefByQName(variable, DotNetTypes.System.Object);
    }

    private static @Nullable PsiElement getExpressionAfter(PsiElement statement, IElementType token) {
        boolean after = false;
        for (PsiElement child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() == token) {
                after = true;
            }
            else if (after && child instanceof DotNetExpression) {
                return child;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static boolean isInferred(PsiElement variable) {
        if (variable instanceof VisualBasicLocalVariableImpl) {
            return VisualBasicProjectSettings.isOptionInfer(variable);
        }
        return variable instanceof DotNetVariable dotNetVariable && dotNetVariable.isConstant();
    }
}
