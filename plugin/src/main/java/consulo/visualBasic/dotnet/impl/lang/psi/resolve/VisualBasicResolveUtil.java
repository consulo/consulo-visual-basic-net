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

package consulo.visualBasic.dotnet.impl.lang.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicResolveUtil {
    private VisualBasicResolveUtil() {
    }

    /**
     * Parameters of the {@code PARAMETER_LIST} child: lambdas, accessors, properties.
     */
    @RequiredReadAction
    public static DotNetParameter[] getParameters(PsiElement owner) {
        for (PsiElement child = owner.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetParameterList list) {
                return list.getParameters();
            }
        }
        return DotNetParameter.EMPTY_ARRAY;
    }

    /**
     * Argument list of the invocation which {@code callee} is the target of.
     */
    @RequiredReadAction
    public static @Nullable PsiElement getInvocationArgumentList(PsiElement callee) {
        PsiElement parent = callee.getParent();
        if (parent == null || parent.getNode().getElementType() != VisualBasicElements.INVOCATION_EXPRESSION || parent.getFirstChild() != callee) {
            return null;
        }
        for (PsiElement child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() == VisualBasicElements.ARGUMENT_LIST) {
                return child;
            }
        }
        return null;
    }

    /**
     * Argument expressions of the invocation which {@code callee} is the target of, {@code null} when it is not invoked.
     * Omitted arguments are not counted.
     */
    @RequiredReadAction
    public static @Nullable List<PsiElement> getInvocationArguments(PsiElement callee) {
        PsiElement argumentList = getInvocationArgumentList(callee);
        if (argumentList == null) {
            return null;
        }
        List<PsiElement> arguments = new ArrayList<>();
        for (PsiElement argument = argumentList.getFirstChild(); argument != null; argument = argument.getNextSibling()) {
            if (argument.getNode().getElementType() == VisualBasicElements.SIMPLE_ARGUMENT
                || argument.getNode().getElementType() == VisualBasicElements.NAMED_ARGUMENT
                || argument.getNode().getElementType() == VisualBasicElements.OMITTED_ARGUMENT) {
                PsiElement expression = getArgumentExpression(argument);
                arguments.add(expression == null ? argument : expression);
            }
        }
        return arguments;
    }

    public static @Nullable PsiElement getArgumentExpression(PsiElement argument) {
        PsiElement last = null;
        for (PsiElement child = argument.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetExpression) {
                last = child;
            }
        }
        return last;
    }
}
