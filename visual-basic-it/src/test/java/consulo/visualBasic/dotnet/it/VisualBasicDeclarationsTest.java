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

package consulo.visualBasic.dotnet.it;

import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.application.ReadAction;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifiers;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicStubElements;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicMemberStub;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicNamespaceStub;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.VisualBasicTypeStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stub tree of {@code declarations/<name>.vb} with the .NET view of each declaration: qualified and VM names,
 * effective modifiers, types (resolved against mscorlib). Compared with {@code <name>.txt}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
@ExtendWith(HeadlessProjectExtension.class)
public class VisualBasicDeclarationsTest {
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Declarations"})
    public void declarations(String testName, HeadlessProjects projects) throws Exception {
        Path directory = VisualBasicTesting.resource("declarations");
        String text = Files.readString(directory.resolve(testName + ".vb"), StandardCharsets.UTF_8);
        PsiFile file = VisualBasicTesting.openSource(projects, testName + ".vb", text);
        String actual = ReadAction.compute(() -> dump(file));
        VisualBasicTesting.assertGold("declarations/" + testName + ".txt", actual);
    }

    private static String dump(PsiFile file) {
        StubElement<?> root = VisualBasicStubElements.FILE.getBuilder().buildStubTree(file);
        Assertions.assertNotNull(root);
        // the built stub tree is not bound to the AST: pair stubs with stubbed AST elements, both in pre-order
        List<PsiElement> elements = new ArrayList<>();
        collectStubbed(file, elements);
        List<StubElement<?>> stubs = new ArrayList<>();
        collectStubs(root, stubs);
        Assertions.assertEquals(elements.size(), stubs.size());

        StringBuilder builder = new StringBuilder();
        dump(builder, root, 0, stubs, elements);
        return builder.toString().strip();
    }

    private static void collectStubbed(PsiElement element, List<PsiElement> result) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() instanceof IStubElementType) {
                result.add(child);
            }
            collectStubbed(child, result);
        }
    }

    private static void collectStubs(StubElement<?> stub, List<StubElement<?>> result) {
        for (StubElement<?> child : stub.getChildrenStubs()) {
            result.add(child);
            collectStubs(child, result);
        }
    }

    private static void dump(StringBuilder builder, StubElement<?> stub, int depth, List<StubElement<?>> stubs, List<PsiElement> elements) {
        if (depth > 0) {
            PsiElement psi = elements.get(stubs.indexOf(stub));
            builder.append("  ".repeat(depth - 1)).append(describe(stub, psi)).append('\n');
        }
        for (StubElement<?> child : stub.getChildrenStubs()) {
            dump(builder, child, depth + 1, stubs, elements);
        }
    }

    private static String describe(StubElement<?> stub, PsiElement psi) {
        StringBuilder builder = new StringBuilder(String.valueOf(stub.getStubType()));
        if (stub instanceof VisualBasicNamespaceStub namespace) {
            builder.append(" name=").append(namespace.getName()).append(" parent=").append(namespace.getParentQName());
            return builder.toString();
        }
        if (stub instanceof VisualBasicMemberStub<?> member) {
            builder.append(" name=").append(member.getName()).append(" parent=").append(member.getParentQName());
            String modifiers = Arrays.stream(VisualBasicModifiers.toArray(member.getModifierMask()))
                .map(DotNetModifier::getPresentableText)
                .collect(Collectors.joining(","));
            builder.append(" modifiers=[").append(modifiers).append(']');
        }
        if (stub instanceof VisualBasicTypeStub type) {
            builder.append(" vm=").append(type.getVmQName()).append(" kind=").append(type.getKind());
        }
        if (psi instanceof DotNetTypeDeclaration type) {
            builder.append(" extends=").append(typeRefs(type.getExtendTypeRefs()));
        }
        if (psi instanceof DotNetLikeMethodDeclaration method) {
            builder.append(" returns=").append(typeRef(method.getReturnTypeRef()));
            builder.append(" parameters=").append(Arrays.stream(method.getParameters()).map(VisualBasicDeclarationsTest::parameter)
                .collect(Collectors.joining(", ", "(", ")")));
        }
        if (psi instanceof DotNetVariable variable) {
            builder.append(" type=").append(typeRef(variable.toTypeRef(false)));
            if (variable.isConstant()) {
                builder.append(" const");
            }
        }
        return builder.toString();
    }

    private static String parameter(DotNetParameter parameter) {
        return parameter.getName() + " As " + typeRef(parameter.toTypeRef(false));
    }

    private static String typeRefs(DotNetTypeRef[] typeRefs) {
        return Arrays.stream(typeRefs).map(VisualBasicDeclarationsTest::typeRef).collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * VM name, marked with {@code ?} when the type is not found.
     */
    private static String typeRef(DotNetTypeRef typeRef) {
        return (typeRef.resolve().getElement() == null ? "?" : "") + typeRef.getVmQName();
    }
}
