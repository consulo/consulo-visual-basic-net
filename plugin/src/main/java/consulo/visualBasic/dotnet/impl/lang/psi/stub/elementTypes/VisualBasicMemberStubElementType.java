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
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubIndexKey;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicMethodDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicStubMemberImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.VisualBasicExtensionMethods;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicTypeStub;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.index.VisualBasicIndexKeys;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Members kept in stubs: methods, constructors, operators, properties, events, fields and enum members.
 * <p>
 * The name index key is supplied lazily: index keys need the application, element types are created before it.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicMemberStubElementType<P extends VisualBasicStubMemberImpl<VisualBasicMemberStub<P>>>
    extends VisualBasicStubElementType<VisualBasicMemberStub<P>, P> {
    private final Function<ASTNode, P> myAstFactory;
    private final BiFunction<VisualBasicMemberStub<P>, IStubElementType, P> myStubFactory;
    private final @Nullable Supplier<StubIndexKey<String, ? super P>> myNameIndexKey;

    public VisualBasicMemberStubElementType(String debugName,
                                            Function<ASTNode, P> astFactory,
                                            BiFunction<VisualBasicMemberStub<P>, IStubElementType, P> stubFactory,
                                            @Nullable Supplier<StubIndexKey<String, ? super P>> nameIndexKey) {
        super(debugName);
        myAstFactory = astFactory;
        myStubFactory = stubFactory;
        myNameIndexKey = nameIndexKey;
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return myAstFactory.apply(node);
    }

    @Override
    public P createPsi(VisualBasicMemberStub<P> stub) {
        return myStubFactory.apply(stub, this);
    }

    @RequiredReadAction
    @Override
    public VisualBasicMemberStub<P> createStub(P psi, StubElement parentStub) {
        boolean extension = (PsiElement) psi instanceof VisualBasicMethodDeclarationImpl method && VisualBasicExtensionMethods.hasExtensionAttribute(method);
        return new VisualBasicMemberStub<>(parentStub, this, psi.getName(), psi.getPresentableParentQName(), psi.getModifierMask(), extension);
    }

    @Override
    public void serialize(VisualBasicMemberStub<P> stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getParentQName());
        dataStream.writeLong(stub.getModifierMask());
        dataStream.writeBoolean(stub.isExtension());
    }

    @Override
    public VisualBasicMemberStub<P> deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        String parentQName = dataStream.readNameString();
        long modifierMask = dataStream.readLong();
        boolean extension = dataStream.readBoolean();
        return new VisualBasicMemberStub<>(parentStub, this, name, parentQName, modifierMask, extension);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void indexStub(VisualBasicMemberStub<P> stub, IndexSink sink) {
        String name = stub.getName();
        if (myNameIndexKey != null && name != null && !name.isEmpty()) {
            sink.occurrence((StubIndexKey<String, P>) myNameIndexKey.get(), name);
        }
        // extension methods are declared in modules, which are never nested
        if (stub.isExtension() && stub.getParentStub() instanceof VisualBasicTypeStub module) {
            sink.occurrence(VisualBasicIndexKeys.EXTENSION_METHOD_BY_NAMESPACE_INDEX, DotNetNamespaceStubUtil.getIndexableNamespace(module.getParentQName()));
        }
    }
}
