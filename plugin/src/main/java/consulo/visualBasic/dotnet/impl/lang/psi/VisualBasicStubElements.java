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

package consulo.visualBasic.dotnet.impl.lang.psi;

import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicConstructorDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicEnumMemberDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicEventDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicFieldDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicMethodDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicPropertyDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.elementTypes.VisualBasicFileStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.elementTypes.VisualBasicMemberStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.elementTypes.VisualBasicNamespaceStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.elementTypes.VisualBasicTypeStubElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.index.VisualBasicIndexKeys;

/**
 * Stubbed element types: declarations. Registered by {@link VisualBasicStubElementsHolder}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public interface VisualBasicStubElements {
    VisualBasicFileStubElementType FILE = new VisualBasicFileStubElementType();

    VisualBasicNamespaceStubElementType NAMESPACE_DECLARATION = new VisualBasicNamespaceStubElementType();

    VisualBasicTypeStubElementType TYPE_DECLARATION = new VisualBasicTypeStubElementType("TYPE_DECLARATION");
    VisualBasicTypeStubElementType ENUM_DECLARATION = new VisualBasicTypeStubElementType("ENUM_DECLARATION");
    VisualBasicTypeStubElementType DELEGATE_DECLARATION = new VisualBasicTypeStubElementType("DELEGATE_DECLARATION");

    VisualBasicMemberStubElementType<VisualBasicMethodDeclarationImpl> METHOD_DECLARATION =
        new VisualBasicMemberStubElementType<>("METHOD_DECLARATION",
            VisualBasicMethodDeclarationImpl::new,
            VisualBasicMethodDeclarationImpl::new,
            () -> VisualBasicIndexKeys.METHOD_INDEX);
    VisualBasicMemberStubElementType<VisualBasicMethodDeclarationImpl> EXTERNAL_METHOD_DECLARATION =
        new VisualBasicMemberStubElementType<>("EXTERNAL_METHOD_DECLARATION",
            VisualBasicMethodDeclarationImpl::new,
            VisualBasicMethodDeclarationImpl::new,
            () -> VisualBasicIndexKeys.METHOD_INDEX);
    VisualBasicMemberStubElementType<VisualBasicMethodDeclarationImpl> OPERATOR_DECLARATION =
        new VisualBasicMemberStubElementType<>("OPERATOR_DECLARATION",
            VisualBasicMethodDeclarationImpl::new,
            VisualBasicMethodDeclarationImpl::new,
            null);
    VisualBasicMemberStubElementType<VisualBasicConstructorDeclarationImpl> CONSTRUCTOR_DECLARATION =
        new VisualBasicMemberStubElementType<>("CONSTRUCTOR_DECLARATION",
            VisualBasicConstructorDeclarationImpl::new,
            VisualBasicConstructorDeclarationImpl::new,
            null);
    VisualBasicMemberStubElementType<VisualBasicPropertyDeclarationImpl> PROPERTY_DECLARATION =
        new VisualBasicMemberStubElementType<>("PROPERTY_DECLARATION",
            VisualBasicPropertyDeclarationImpl::new,
            VisualBasicPropertyDeclarationImpl::new,
            () -> VisualBasicIndexKeys.PROPERTY_INDEX);
    VisualBasicMemberStubElementType<VisualBasicEventDeclarationImpl> EVENT_DECLARATION =
        new VisualBasicMemberStubElementType<>("EVENT_DECLARATION",
            VisualBasicEventDeclarationImpl::new,
            VisualBasicEventDeclarationImpl::new,
            () -> VisualBasicIndexKeys.EVENT_INDEX);
    VisualBasicMemberStubElementType<VisualBasicFieldDeclarationImpl> FIELD_DECLARATION =
        new VisualBasicMemberStubElementType<>("FIELD_DECLARATION",
            VisualBasicFieldDeclarationImpl::new,
            VisualBasicFieldDeclarationImpl::new,
            () -> VisualBasicIndexKeys.FIELD_INDEX);
    VisualBasicMemberStubElementType<VisualBasicEnumMemberDeclarationImpl> ENUM_MEMBER_DECLARATION =
        new VisualBasicMemberStubElementType<>("ENUM_MEMBER_DECLARATION",
            VisualBasicEnumMemberDeclarationImpl::new,
            VisualBasicEnumMemberDeclarationImpl::new,
            () -> VisualBasicIndexKeys.FIELD_INDEX);
}
