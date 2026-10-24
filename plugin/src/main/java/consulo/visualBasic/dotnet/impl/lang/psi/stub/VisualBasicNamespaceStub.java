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

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicNamespaceDeclarationImpl;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicNamespaceStub extends StubBase<VisualBasicNamespaceDeclarationImpl> {
    private final @Nullable String myName;
    private final @Nullable String myParentQName;

    public VisualBasicNamespaceStub(@Nullable StubElement parent,
                                    IStubElementType elementType,
                                    @Nullable String name,
                                    @Nullable String parentQName) {
        super(parent, elementType);
        myName = name;
        myParentQName = parentQName;
    }

    /**
     * Declared name, may be qualified: {@code A.B} in {@code Namespace A.B}.
     */
    public @Nullable String getName() {
        return myName;
    }

    public @Nullable String getParentQName() {
        return myParentQName;
    }
}
