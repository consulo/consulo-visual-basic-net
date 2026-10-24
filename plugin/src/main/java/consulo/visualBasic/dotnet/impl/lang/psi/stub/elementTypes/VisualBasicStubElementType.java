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

import consulo.language.ast.IElementTypeAsPsiFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.visualBasic.dotnet.VisualBasicLanguage;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public abstract class VisualBasicStubElementType<S extends StubElement, P extends PsiElement> extends IStubElementType<S, P>
    implements IElementTypeAsPsiFactory {
    public VisualBasicStubElementType(String debugName) {
        super(debugName, VisualBasicLanguage.INSTANCE);
    }

    @Override
    public String getExternalId() {
        return "visualBasic." + this;
    }

    @Override
    public void indexStub(S stub, IndexSink sink) {
    }
}
