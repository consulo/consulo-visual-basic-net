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

package consulo.visualBasic.dotnet.impl.lang.parser;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiParser;
import consulo.language.version.LanguageVersion;

/**
 * Hand-written recursive descent parser; the grammar and recovery rules follow Roslyn
 * {@code src/Compilers/VisualBasic/Portable/Parser} (MIT).
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicParser implements PsiParser {
    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder, LanguageVersion languageVersion) {
        VisualBasicBuilder vbBuilder = new VisualBasicBuilder(builder);
        PsiBuilder.Marker marker = vbBuilder.mark();
        DeclarationParsing.parseFile(vbBuilder);
        while (!vbBuilder.eof()) {
            vbBuilder.advanceLexer();
        }
        marker.done(root);
        return builder.getTreeBuilt();
    }
}
