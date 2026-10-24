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
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public final class VisualBasicPsiUtil {
    private VisualBasicPsiUtil() {
    }

    /**
     * Name of an identifier token: {@code [Class]} is {@code Class}, {@code count%} is {@code count}.
     */
    public static String getIdentifierName(String text) {
        if (text.length() > 1 && text.charAt(0) == '[' && text.charAt(text.length() - 1) == ']') {
            return text.substring(1, text.length() - 1);
        }
        if (!text.isEmpty() && isTypeCharacter(text.charAt(text.length() - 1))) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static boolean isTypeCharacter(char c) {
        return c == '%' || c == '&' || c == '@' || c == '!' || c == '#' || c == '$';
    }

    /**
     * Type character of an identifier: {@code %} in {@code count%}, or 0.
     */
    public static char getTypeCharacter(String text) {
        if (text.isEmpty() || text.charAt(0) == '[') {
            return 0;
        }
        char last = text.charAt(text.length() - 1);
        return isTypeCharacter(last) ? last : 0;
    }

    @RequiredReadAction
    public static @Nullable PsiElement findChild(PsiElement element, IElementType type) {
        ASTNode child = element.getNode().findChildByType(type);
        return child == null ? null : child.getPsi();
    }

    @RequiredReadAction
    public static @Nullable String getName(PsiElement owner) {
        PsiElement identifier = findChild(owner, VisualBasicTokens.IDENTIFIER);
        return identifier == null ? null : getIdentifierName(identifier.getText());
    }

    /**
     * Type of an {@code As} clause among the children of the element.
     */
    @RequiredReadAction
    public static @Nullable DotNetType getTypeAfterAs(PsiElement element) {
        boolean afterAs = false;
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType type = child.getNode().getElementType();
            if (type == VisualBasicTokens.AS_KEYWORD) {
                afterAs = true;
            }
            else if (afterAs && child instanceof DotNetType dotNetType) {
                return dotNetType;
            }
        }
        return null;
    }

    /**
     * Qualified name of the enclosing type or namespace, the root namespace of the project otherwise.
     */
    @RequiredReadAction
    public static @Nullable String getParentQName(PsiElement element) {
        for (PsiElement parent = element.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof DotNetQualifiedElement qualified) {
                return qualified.getPresentableQName();
            }
        }
        String rootNamespace = VisualBasicProjectSettings.getRootNamespace(element);
        return rootNamespace.isEmpty() ? null : rootNamespace;
    }

    public static String qualify(@Nullable String parent, @Nullable String name) {
        if (parent == null || parent.isEmpty()) {
            return name == null ? "" : name;
        }
        return name == null ? parent : parent + "." + name;
    }
}
