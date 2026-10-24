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
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Supertypes of a type, the type first and nearer ones before farther ones, each with the extractor for its
 * generic parameters: {@code Collection(Of T)} under {@code List(Of Integer)} has {@code T = Integer}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class DotNetTypeHierarchy {
    public record Entry(DotNetTypeDeclaration type, DotNetGenericExtractor extractor) {
    }

    private DotNetTypeHierarchy() {
    }

    @RequiredReadAction
    public static List<Entry> getTypeAndSupertypes(DotNetTypeDeclaration type, DotNetGenericExtractor extractor, TypeRefFactory factory) {
        List<Entry> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<Entry> queue = new ArrayDeque<>();
        queue.add(new Entry(type, extractor));
        while (!queue.isEmpty()) {
            Entry entry = queue.poll();
            String key = entry.type().getVmQName();
            if (!visited.add(key == null ? entry.type().toString() : key)) {
                continue;
            }
            result.add(entry);
            for (DotNetTypeRef extendTypeRef : entry.type().getExtendTypeRefs()) {
                DotNetTypeRef substituted = DotNetTypeRefSubstitutor.substitute(extendTypeRef, entry.extractor(), factory);
                DotNetTypeResolveResult resolved = substituted.resolve();
                PsiElement element = resolved.getElement();
                if (element instanceof DotNetTypeDeclaration superType) {
                    queue.add(new Entry(superType, resolved.getGenericExtractor()));
                }
            }
        }
        return result;
    }
}
