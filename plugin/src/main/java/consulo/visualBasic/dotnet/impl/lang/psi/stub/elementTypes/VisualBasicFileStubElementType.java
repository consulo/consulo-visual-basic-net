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

package consulo.visualBasic.dotnet.impl.lang.psi.stub.elementTypes;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.language.psi.stub.DefaultStubBuilder;
import consulo.language.psi.stub.IStubFileElementType;
import consulo.language.psi.stub.StubBuilder;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import consulo.visualBasic.dotnet.impl.lang.parser.VisualBasicBuilder;
import consulo.visualBasic.dotnet.impl.lang.parser.VisualBasicParser;
import consulo.visualBasic.dotnet.impl.lang.parser.preprocessor.VisualBasicPreprocessorDefines;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicFileImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicFileStub;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicFileStubElementType extends IStubFileElementType<VisualBasicFileStub> {
    public VisualBasicFileStubElementType() {
        super("VISUAL_BASIC_FILE", VisualBasicLanguage.INSTANCE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new DefaultStubBuilder() {
            @Override
            protected StubElement createStubForFile(PsiFile file) {
                if (file instanceof VisualBasicFileImpl visualBasicFile) {
                    return new VisualBasicFileStub(visualBasicFile);
                }
                return super.createStubForFile(file);
            }
        };
    }

    /**
     * Parses with the conditional compilation names of the module.
     */
    @RequiredReadAction
    @Override
    protected @Nullable ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        Language language = getLanguageForParser(psi);
        LanguageVersion languageVersion = psi.getLanguageVersion();
        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(psi.getProject(), chameleon, null, language, languageVersion, chameleon.getChars());
        if (psi instanceof PsiFile file) {
            builder.putUserData(VisualBasicBuilder.PREPROCESSOR_DEFINES, VisualBasicPreprocessorDefines.find(file));
        }
        return new VisualBasicParser().parse(this, builder, languageVersion).getFirstChildNode();
    }

    @Override
    public VisualBasicFileStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) {
        return new VisualBasicFileStub(null);
    }

    @Override
    public int getStubVersion() {
        return 2;
    }

    @Override
    public String getExternalId() {
        return "visualBasic.file";
    }
}
