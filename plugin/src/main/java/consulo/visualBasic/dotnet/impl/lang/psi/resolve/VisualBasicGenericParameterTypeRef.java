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

package consulo.visualBasic.dotnet.impl.lang.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.dotnet.psi.resolve.SimpleTypeResolveResult;

/**
 * Reference to a generic parameter: {@code T} in {@code Function F(Of T)(x As T)}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicGenericParameterTypeRef extends VisualBasicTypeRefWithCachedResult {
    private final DotNetGenericParameter myParameter;

    @RequiredReadAction
    public VisualBasicGenericParameterTypeRef(DotNetGenericParameter parameter) {
        super(parameter);
        myParameter = parameter;
    }

    @RequiredReadAction
    @Override
    protected DotNetTypeResolveResult resolveResult() {
        return new SimpleTypeResolveResult(myParameter);
    }

    @RequiredReadAction
    @Override
    public String getVmQName() {
        String name = myParameter.getName();
        return name == null ? "<unknown>" : name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VisualBasicGenericParameterTypeRef other && myParameter.equals(other.myParameter);
    }

    @Override
    public int hashCode() {
        return myParameter.hashCode();
    }
}
