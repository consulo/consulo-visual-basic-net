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

package consulo.visualBasic.dotnet.impl.lang.psi.stub.index;

import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.stub.StubIndexKey;

/**
 * Keys keep the declared case of names. Visual Basic lookups are case-insensitive, other .NET languages see
 * the declared names.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public interface VisualBasicIndexKeys {
    StubIndexKey<String, DotNetQualifiedElement> MEMBER_BY_NAMESPACE_QNAME_INDEX =
        StubIndexKey.createIndexKey("visualBasic.member.by.namespace.qname.index");
    StubIndexKey<String, DotNetQualifiedElement> MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX =
        StubIndexKey.createIndexKey("visualBasic.member.by.all.namespace.qname.index");

    StubIndexKey<String, DotNetTypeDeclaration> TYPE_INDEX = StubIndexKey.createIndexKey("visualBasic.type.index");
    StubIndexKey<Integer, DotNetTypeDeclaration> TYPE_BY_VMQNAME_INDEX = StubIndexKey.createIndexKey("visualBasic.type.by.vmqname.index");

    StubIndexKey<String, DotNetLikeMethodDeclaration> METHOD_INDEX = StubIndexKey.createIndexKey("visualBasic.method.index");
    StubIndexKey<String, DotNetPropertyDeclaration> PROPERTY_INDEX = StubIndexKey.createIndexKey("visualBasic.property.index");
    StubIndexKey<String, DotNetEventDeclaration> EVENT_INDEX = StubIndexKey.createIndexKey("visualBasic.event.index");
    StubIndexKey<String, DotNetFieldDeclaration> FIELD_INDEX = StubIndexKey.createIndexKey("visualBasic.field.index");

    /**
     * {@code <Extension>} methods by the namespace of their module.
     */
    StubIndexKey<String, DotNetLikeMethodDeclaration> EXTENSION_METHOD_BY_NAMESPACE_INDEX =
        StubIndexKey.createIndexKey("visualBasic.extension.method.by.namespace.index");
}
