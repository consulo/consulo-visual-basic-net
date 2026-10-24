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

package consulo.visualBasic.dotnet.impl.editor.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.CodeInsightColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.language.ast.IElementType;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoHolder;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.editor.rawHighlight.HighlightVisitor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.ResolveResult;
import consulo.visualBasic.dotnet.impl.lang.lexer.VisualBasicKeywords;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicReferenceExpressionImpl;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Highlighting which needs the parser: names colored by what they declare or resolve to (see
 * {@link VisualBasicHighlightUtil}), contextual keywords used as keywords, reserved keywords used as names after a member
 * access ({@code Console.Error}), and code made inactive by conditional compilation.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicHighlightVisitor implements HighlightVisitor {
    private @Nullable HighlightInfoHolder myHolder;

    @Override
    @RequiredReadAction
    public void visit(PsiElement element) {
        HighlightInfoHolder holder = myHolder;
        if (holder != null) {
            highlight(element, (range, key) -> holder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION)
                .range(range)
                .textAttributes(key)
                .create()));
        }
    }

    /**
     * Passes the highlighted ranges of {@code element} to {@code sink}; tests call it directly.
     */
    @RequiredReadAction
    public static void highlight(PsiElement element, BiConsumer<PsiElement, TextAttributesKey> sink) {
        if (element instanceof VisualBasicReferenceExpressionImpl reference) {
            highlightReference(sink, reference);
            return;
        }
        if (element.getFirstChild() != null) {
            if (element instanceof PsiNameIdentifierOwner owner) {
                highlightDeclaration(sink, owner);
            }
            return;
        }
        IElementType elementType = PsiUtilCore.getElementType(element);
        if (elementType == VisualBasicTokens.DISABLED_TEXT || elementType == VisualBasicTokens.DISABLED_DIRECTIVE) {
            sink.accept(element, VisualBasicHighlightKey.DISABLED_CODE);
        }
        else if (VisualBasicTokens.CONTEXTUAL_KEYWORDS.contains(elementType)) {
            sink.accept(element, VisualBasicHighlightKey.CONTEXTUAL_KEYWORD);
        }
        else if (elementType == VisualBasicTokens.IDENTIFIER
            && !(element.getParent() instanceof VisualBasicReferenceExpressionImpl)
            && isKeywordText(element.getText())) {
            sink.accept(element, VisualBasicHighlightKey.IDENTIFIER);
        }
    }

    @RequiredReadAction
    private static void highlightDeclaration(BiConsumer<PsiElement, TextAttributesKey> sink, PsiNameIdentifierOwner owner) {
        PsiElement nameIdentifier = owner.getNameIdentifier();
        // Sub New, Get, Set and operator names are keywords or signs
        if (nameIdentifier == null || PsiUtilCore.getElementType(nameIdentifier) != VisualBasicTokens.IDENTIFIER) {
            return;
        }
        TextAttributesKey key = VisualBasicHighlightUtil.getKey(owner, true);
        if (key != null) {
            sink.accept(nameIdentifier, key);
        }
    }

    @RequiredReadAction
    private static void highlightReference(BiConsumer<PsiElement, TextAttributesKey> sink, VisualBasicReferenceExpressionImpl reference) {
        PsiElement name = reference.getReferenceElement();
        if (name == null || PsiUtilCore.getElementType(name) != VisualBasicTokens.IDENTIFIER) {
            return;
        }
        // overloads stay ambiguous until argument types are known; any candidate tells the kind
        PsiElement target = null;
        for (ResolveResult result : reference.multiResolve(false)) {
            if (result.isValidResult()) {
                target = result.getElement();
                break;
            }
        }
        TextAttributesKey key = target == null ? null : VisualBasicHighlightUtil.getKey(target, false);
        if (key != null) {
            sink.accept(name, key);
            if (DotNetAttributeUtil.hasAttribute(target, DotNetTypes.System.ObsoleteAttribute)) {
                sink.accept(name, CodeInsightColors.DEPRECATED_ATTRIBUTES);
            }
        }
        else if (isKeywordText(name.getText())) {
            sink.accept(name, VisualBasicHighlightKey.IDENTIFIER);
        }
    }

    private static boolean isKeywordText(String text) {
        return !text.startsWith("[") && VisualBasicKeywords.findReserved(text) != null;
    }

    @Override
    public boolean analyze(PsiFile file, boolean updateWholeFile, HighlightInfoHolder holder, Runnable action) {
        myHolder = holder;
        try {
            action.run();
        }
        finally {
            myHolder = null;
        }
        return true;
    }
}
