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
import consulo.dotnet.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicTypeStub;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.index.VisualBasicIndexKeys;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Classes, structures, modules, interfaces, enums and delegates.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicTypeStubElementType extends VisualBasicStubElementType<VisualBasicTypeStub, VisualBasicTypeDeclarationImpl> {
    public VisualBasicTypeStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return new VisualBasicTypeDeclarationImpl(node);
    }

    @Override
    public VisualBasicTypeDeclarationImpl createPsi(VisualBasicTypeStub stub) {
        return new VisualBasicTypeDeclarationImpl(stub, this);
    }

    @RequiredReadAction
    @Override
    public VisualBasicTypeStub createStub(VisualBasicTypeDeclarationImpl psi, StubElement parentStub) {
        return new VisualBasicTypeStub(parentStub,
            this,
            psi.getName(),
            psi.getPresentableParentQName(),
            psi.getModifierMask(),
            psi.getVmQName(),
            psi.getKind());
    }

    @Override
    public void serialize(VisualBasicTypeStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getParentQName());
        dataStream.writeLong(stub.getModifierMask());
        dataStream.writeName(stub.getVmQName());
        dataStream.writeVarInt(stub.getKind().ordinal());
    }

    @Override
    public VisualBasicTypeStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        String parentQName = dataStream.readNameString();
        long modifierMask = dataStream.readLong();
        String vmQName = dataStream.readNameString();
        VisualBasicTypeKind kind = VisualBasicTypeKind.values()[dataStream.readVarInt()];
        return new VisualBasicTypeStub(parentStub, this, name, parentQName, modifierMask, vmQName, kind);
    }

    @Override
    public void indexStub(VisualBasicTypeStub stub, IndexSink sink) {
        String name = stub.getName();
        if (name == null || name.isEmpty()) {
            return;
        }
        sink.occurrence(VisualBasicIndexKeys.TYPE_INDEX, name);
        if (!stub.isNested()) {
            DotNetNamespaceStubUtil.indexStub(sink,
                VisualBasicIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX,
                VisualBasicIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX,
                stub.getParentQName(),
                name);
        }
        String vmQName = stub.getVmQName();
        if (vmQName != null) {
            sink.occurrence(VisualBasicIndexKeys.TYPE_BY_VMQNAME_INDEX, vmQName.hashCode());
        }
    }
}
