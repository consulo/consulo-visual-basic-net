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
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Members of a type by name, inherited ones included: members of the type itself come first.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class DotNetMemberLookup {
    /**
     * @param extractor generic parameters of {@code owner}, to substitute in the member type
     */
    public record Member(DotNetNamedElement element, DotNetTypeDeclaration owner, DotNetGenericExtractor extractor) {
    }

    private DotNetMemberLookup() {
    }

    @RequiredReadAction
    public static List<Member> findMembers(DotNetTypeDeclaration type,
                                           DotNetGenericExtractor extractor,
                                           String name,
                                           NameMatcher matcher,
                                           Predicate<DotNetNamedElement> filter,
                                           TypeRefFactory factory) {
        List<Member> result = new ArrayList<>();
        for (DotNetTypeHierarchy.Entry entry : DotNetTypeHierarchy.getTypeAndSupertypes(type, extractor, factory)) {
            for (DotNetNamedElement member : entry.type().getMembers()) {
                if (matcher.matches(member.getName(), name) && filter.test(member)) {
                    result.add(new Member(member, entry.type(), entry.extractor()));
                }
            }
        }
        return result;
    }
}
