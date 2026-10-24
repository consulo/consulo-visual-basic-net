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
import consulo.language.psi.stub.StubElement;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicTypeStub extends VisualBasicMemberStub<VisualBasicTypeDeclarationImpl> {
    private final @Nullable String myVmQName;
    private final VisualBasicTypeKind myKind;

    public VisualBasicTypeStub(@Nullable StubElement parent,
                               IStubElementType elementType,
                               @Nullable String name,
                               @Nullable String parentQName,
                               long modifierMask,
                               @Nullable String vmQName,
                               VisualBasicTypeKind kind) {
        super(parent, elementType, name, parentQName, modifierMask, false);
        myVmQName = vmQName;
        myKind = kind;
    }

    public @Nullable String getVmQName() {
        return myVmQName;
    }

    public VisualBasicTypeKind getKind() {
        return myKind;
    }

    public boolean isNested() {
        return getParentStub() instanceof VisualBasicTypeStub;
    }
}
