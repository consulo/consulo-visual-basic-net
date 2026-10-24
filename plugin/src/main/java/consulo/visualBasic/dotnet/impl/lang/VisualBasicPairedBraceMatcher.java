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

package consulo.visualBasic.dotnet.impl.lang;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiFile;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
@ExtensionImpl
public class VisualBasicPairedBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
        new BracePair(VisualBasicTokens.LPAR, VisualBasicTokens.RPAR, false),
        new BracePair(VisualBasicTokens.LBRACE, VisualBasicTokens.RBRACE, false),
        new BracePair(VisualBasicTokens.INTERPOLATION_START, VisualBasicTokens.INTERPOLATION_END, false)
    };

    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(IElementType lbraceType, @Nullable IElementType contextType) {
        return contextType == null
            || VisualBasicTokens.WHITESPACES.contains(contextType)
            || VisualBasicTokens.COMMENTS.contains(contextType)
            || contextType == VisualBasicTokens.NEW_LINE
            || contextType == VisualBasicTokens.COMMA
            || contextType == VisualBasicTokens.COLON
            || contextType == VisualBasicTokens.RPAR
            || contextType == VisualBasicTokens.RBRACE;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }

    @Override
    public Language getLanguage() {
        return VisualBasicLanguage.INSTANCE;
    }
}
