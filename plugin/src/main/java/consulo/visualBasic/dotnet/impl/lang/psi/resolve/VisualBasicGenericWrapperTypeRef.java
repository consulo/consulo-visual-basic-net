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
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.dotnet.psi.resolve.SimpleTypeResolveResult;
import consulo.language.psi.PsiElement;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Generic type with arguments: {@code List(Of Integer)}. The extractor maps the generic parameters of the resolved
 * type by position.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicGenericWrapperTypeRef extends VisualBasicTypeRefWithCachedResult implements DotNetGenericWrapperTypeRef {
    private final DotNetTypeRef myInnerTypeRef;
    private final DotNetTypeRef[] myArguments;

    public VisualBasicGenericWrapperTypeRef(PsiElement context, DotNetTypeRef innerTypeRef, DotNetTypeRef[] arguments) {
        super(context);
        myInnerTypeRef = innerTypeRef;
        myArguments = arguments;
    }

    @Override
    public DotNetTypeRef getInnerTypeRef() {
        return myInnerTypeRef;
    }

    @Override
    public DotNetTypeRef[] getArgumentTypeRefs() {
        return myArguments;
    }

    @RequiredReadAction
    @Override
    protected DotNetTypeResolveResult resolveResult() {
        PsiElement element = myInnerTypeRef.resolve().getElement();
        if (!(element instanceof DotNetGenericParameterListOwner owner)) {
            return new SimpleTypeResolveResult(element);
        }
        DotNetGenericParameter[] parameters = owner.getGenericParameters();
        DotNetGenericExtractor extractor = parameter -> {
            for (int i = 0; i < parameters.length && i < myArguments.length; i++) {
                if (parameters[i].equals(parameter)) {
                    return myArguments[i];
                }
            }
            return null;
        };
        return new SimpleTypeResolveResult(element, extractor);
    }

    @RequiredReadAction
    @Override
    public String getVmQName() {
        return myInnerTypeRef.getVmQName()
            + Arrays.stream(myArguments).map(DotNetTypeRef::getVmQName).collect(Collectors.joining(", ", "<", ">"));
    }

    @RequiredReadAction
    @Override
    public boolean equals(Object obj) {
        return obj instanceof VisualBasicGenericWrapperTypeRef other
            && myInnerTypeRef.equals(other.myInnerTypeRef)
            && Arrays.equals(myArguments, other.myArguments);
    }

    @RequiredReadAction
    @Override
    public int hashCode() {
        return myInnerTypeRef.hashCode() * 31 + Arrays.hashCode(myArguments);
    }
}
