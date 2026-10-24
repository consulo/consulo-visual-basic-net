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

package consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.resolve.DotNetArrayTypeRef;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

/**
 * Replaces generic parameters in a type: {@code IEnumerator(Of T)} of {@code List(Of Integer)} is
 * {@code IEnumerator(Of Integer)}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class DotNetTypeRefSubstitutor {
    private DotNetTypeRefSubstitutor() {
    }

    @RequiredReadAction
    public static DotNetTypeRef substitute(DotNetTypeRef typeRef, DotNetGenericExtractor extractor, TypeRefFactory factory) {
        if (extractor == DotNetGenericExtractor.EMPTY) {
            return typeRef;
        }
        if (typeRef instanceof DotNetGenericWrapperTypeRef wrapper) {
            DotNetTypeRef[] arguments = wrapper.getArgumentTypeRefs();
            DotNetTypeRef[] substituted = new DotNetTypeRef[arguments.length];
            boolean changed = false;
            for (int i = 0; i < arguments.length; i++) {
                substituted[i] = substitute(arguments[i], extractor, factory);
                changed |= substituted[i] != arguments[i];
            }
            return changed ? factory.createGeneric(wrapper.getInnerTypeRef(), substituted) : typeRef;
        }
        if (typeRef instanceof DotNetArrayTypeRef array) {
            DotNetTypeRef inner = substitute(array.getInnerTypeRef(), extractor, factory);
            return inner == array.getInnerTypeRef() ? typeRef : factory.createArray(inner, factory.getRank(array));
        }
        PsiElement element = typeRef.resolve().getElement();
        if (element instanceof DotNetGenericParameter parameter) {
            DotNetTypeRef extracted = extractor.extract(parameter);
            return extracted == null ? typeRef : extracted;
        }
        return typeRef;
    }
}
