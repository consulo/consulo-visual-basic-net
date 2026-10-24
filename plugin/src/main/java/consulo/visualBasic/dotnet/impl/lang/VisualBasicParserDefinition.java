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
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import consulo.visualBasic.dotnet.impl.lang.lexer.VisualBasicLexer;
import consulo.visualBasic.dotnet.impl.lang.parser.VisualBasicParser;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicFileImpl;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
@ExtensionImpl
public class VisualBasicParserDefinition implements ParserDefinition {
    @Override
    public Language getLanguage() {
        return VisualBasicLanguage.INSTANCE;
    }

    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new VisualBasicLexer();
    }

    @Override
    public PsiParser createParser(LanguageVersion languageVersion) {
        return new VisualBasicParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return VisualBasicElements.FILE;
    }

    @Override
    public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
        return VisualBasicTokens.WHITESPACES;
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return VisualBasicTokens.COMMENTS;
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return VisualBasicTokens.STRINGS;
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        throw new UnsupportedOperationException(node.getElementType().toString());
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new VisualBasicFileImpl(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
