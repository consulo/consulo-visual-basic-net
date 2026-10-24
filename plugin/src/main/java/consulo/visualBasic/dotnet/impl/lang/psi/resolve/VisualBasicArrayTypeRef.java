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
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.resolve.DotNetArrayTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;

/**
 * {@code Integer()}, {@code Integer(,)}: resolves to {@code System.Array}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicArrayTypeRef extends VisualBasicTypeRefWithCachedResult implements DotNetArrayTypeRef {
    private final DotNetTypeRef myInnerTypeRef;
    private final int myRank;

    public VisualBasicArrayTypeRef(PsiElement context, DotNetTypeRef innerTypeRef, int rank) {
        super(context);
        myInnerTypeRef = innerTypeRef;
        myRank = rank;
    }

    @Override
    public DotNetTypeRef getInnerTypeRef() {
        return myInnerTypeRef;
    }

    public int getRank() {
        return myRank;
    }

    @RequiredReadAction
    @Override
    protected DotNetTypeResolveResult resolveResult() {
        return new VisualBasicTypeRefByQName(myContext, DotNetTypes.System.Array).resolve();
    }

    @RequiredReadAction
    @Override
    public String getVmQName() {
        return myInnerTypeRef.getVmQName() + "[" + ",".repeat(myRank - 1) + "]";
    }

    @RequiredReadAction
    @Override
    public boolean equals(Object obj) {
        return obj instanceof VisualBasicArrayTypeRef other && myRank == other.myRank && myInnerTypeRef.equals(other.myInnerTypeRef);
    }

    @RequiredReadAction
    @Override
    public int hashCode() {
        return myInnerTypeRef.hashCode() * 31 + myRank;
    }
}
