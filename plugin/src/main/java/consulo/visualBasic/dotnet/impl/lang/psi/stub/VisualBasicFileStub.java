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

package consulo.visualBasic.dotnet.impl.lang.psi.stub;

import consulo.language.psi.stub.PsiFileStubImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicStubElements;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicFileImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.elementTypes.VisualBasicFileStubElementType;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicFileStub extends PsiFileStubImpl<VisualBasicFileImpl> {
    public VisualBasicFileStub(@Nullable VisualBasicFileImpl file) {
        super(file);
    }

    @Override
    public VisualBasicFileStubElementType getType() {
        return VisualBasicStubElements.FILE;
    }
}
