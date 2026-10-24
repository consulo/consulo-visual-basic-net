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
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicNamespaceDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicNamespaceStub;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.index.VisualBasicIndexKeys;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicNamespaceStubElementType extends VisualBasicStubElementType<VisualBasicNamespaceStub, VisualBasicNamespaceDeclarationImpl> {
    public VisualBasicNamespaceStubElementType() {
        super("NAMESPACE_DECLARATION");
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return new VisualBasicNamespaceDeclarationImpl(node);
    }

    @Override
    public VisualBasicNamespaceDeclarationImpl createPsi(VisualBasicNamespaceStub stub) {
        return new VisualBasicNamespaceDeclarationImpl(stub, this);
    }

    @RequiredReadAction
    @Override
    public VisualBasicNamespaceStub createStub(VisualBasicNamespaceDeclarationImpl psi, StubElement parentStub) {
        return new VisualBasicNamespaceStub(parentStub, this, psi.getName(), psi.getPresentableParentQName());
    }

    @Override
    public void serialize(VisualBasicNamespaceStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getParentQName());
    }

    @Override
    public VisualBasicNamespaceStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        String parentQName = dataStream.readNameString();
        return new VisualBasicNamespaceStub(parentStub, this, name, parentQName);
    }

    @Override
    public void indexStub(VisualBasicNamespaceStub stub, IndexSink sink) {
        String name = stub.getName();
        if (name == null || name.isEmpty()) {
            return;
        }
        // makes the namespace itself known, even without types
        DotNetNamespaceStubUtil.indexStub(sink,
            VisualBasicIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX,
            VisualBasicIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX,
            stub.getParentQName(),
            name);
    }
}
