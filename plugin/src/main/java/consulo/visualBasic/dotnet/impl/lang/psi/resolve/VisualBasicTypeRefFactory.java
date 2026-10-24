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

import consulo.dotnet.psi.resolve.DotNetArrayTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.msil.impl.lang.psi.impl.type.MsilArrayTypRefImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.TypeRefFactory;

/**
 * Visual Basic type references for types made by substitution, resolved in the scope of {@code context}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public class VisualBasicTypeRefFactory implements TypeRefFactory {
    private final PsiElement myContext;

    public VisualBasicTypeRefFactory(PsiElement context) {
        myContext = context;
    }

    @Override
    public DotNetTypeRef createArray(DotNetTypeRef innerTypeRef, int rank) {
        return new VisualBasicArrayTypeRef(myContext, innerTypeRef, rank);
    }

    @Override
    public DotNetTypeRef createGeneric(DotNetTypeRef innerTypeRef, DotNetTypeRef[] arguments) {
        return new VisualBasicGenericWrapperTypeRef(myContext, innerTypeRef, arguments);
    }

    @Override
    public int getRank(DotNetArrayTypeRef array) {
        if (array instanceof VisualBasicArrayTypeRef visualBasicArray) {
            return visualBasicArray.getRank();
        }
        if (array instanceof MsilArrayTypRefImpl msilArray) {
            return Math.max(1, msilArray.getLowerValues().length);
        }
        return 1;
    }
}
