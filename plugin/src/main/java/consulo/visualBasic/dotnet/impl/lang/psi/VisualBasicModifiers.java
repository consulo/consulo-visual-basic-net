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

import consulo.dotnet.psi.DotNetModifier;
import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modifier sets as bit masks, the form kept in stubs.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public final class VisualBasicModifiers {
    private static final DotNetModifier[] ALL;

    static {
        List<DotNetModifier> all = new ArrayList<>(List.of(
            DotNetModifier.PUBLIC,
            DotNetModifier.PRIVATE,
            DotNetModifier.PROTECTED,
            DotNetModifier.INTERNAL,
            DotNetModifier.STATIC,
            DotNetModifier.SEALED,
            DotNetModifier.ABSTRACT,
            DotNetModifier.COVARIANT,
            DotNetModifier.CONTRAVARIANT
        ));
        all.addAll(List.of(VisualBasicModifier.values()));
        ALL = all.toArray(DotNetModifier[]::new);
    }

    public static final long ACCESS_MASK = maskOf(DotNetModifier.PUBLIC)
        | maskOf(DotNetModifier.PRIVATE)
        | maskOf(DotNetModifier.PROTECTED)
        | maskOf(DotNetModifier.INTERNAL);

    private static final Map<IElementType, DotNetModifier> BY_KEYWORD = Map.ofEntries(
        Map.entry(VisualBasicTokens.PUBLIC_KEYWORD, DotNetModifier.PUBLIC),
        Map.entry(VisualBasicTokens.PRIVATE_KEYWORD, DotNetModifier.PRIVATE),
        Map.entry(VisualBasicTokens.PROTECTED_KEYWORD, DotNetModifier.PROTECTED),
        Map.entry(VisualBasicTokens.FRIEND_KEYWORD, DotNetModifier.INTERNAL),
        Map.entry(VisualBasicTokens.SHARED_KEYWORD, DotNetModifier.STATIC),
        Map.entry(VisualBasicTokens.MUSTINHERIT_KEYWORD, DotNetModifier.ABSTRACT),
        Map.entry(VisualBasicTokens.MUSTOVERRIDE_KEYWORD, DotNetModifier.ABSTRACT),
        Map.entry(VisualBasicTokens.NOTINHERITABLE_KEYWORD, DotNetModifier.SEALED),
        Map.entry(VisualBasicTokens.NOTOVERRIDABLE_KEYWORD, DotNetModifier.SEALED),
        Map.entry(VisualBasicTokens.OUT_KEYWORD, DotNetModifier.COVARIANT),
        Map.entry(VisualBasicTokens.IN_KEYWORD, DotNetModifier.CONTRAVARIANT),
        Map.entry(VisualBasicTokens.OVERRIDABLE_KEYWORD, VisualBasicModifier.OVERRIDABLE),
        Map.entry(VisualBasicTokens.OVERRIDES_KEYWORD, VisualBasicModifier.OVERRIDES),
        Map.entry(VisualBasicTokens.OVERLOADS_KEYWORD, VisualBasicModifier.OVERLOADS),
        Map.entry(VisualBasicTokens.SHADOWS_KEYWORD, VisualBasicModifier.SHADOWS),
        Map.entry(VisualBasicTokens.PARTIAL_KEYWORD, VisualBasicModifier.PARTIAL),
        Map.entry(VisualBasicTokens.READONLY_KEYWORD, VisualBasicModifier.READONLY),
        Map.entry(VisualBasicTokens.WRITEONLY_KEYWORD, VisualBasicModifier.WRITEONLY),
        Map.entry(VisualBasicTokens.WITHEVENTS_KEYWORD, VisualBasicModifier.WITHEVENTS),
        Map.entry(VisualBasicTokens.DEFAULT_KEYWORD, VisualBasicModifier.DEFAULT),
        Map.entry(VisualBasicTokens.CONST_KEYWORD, VisualBasicModifier.CONST),
        Map.entry(VisualBasicTokens.DIM_KEYWORD, VisualBasicModifier.DIM),
        Map.entry(VisualBasicTokens.STATIC_KEYWORD, VisualBasicModifier.STATIC),
        Map.entry(VisualBasicTokens.WIDENING_KEYWORD, VisualBasicModifier.WIDENING),
        Map.entry(VisualBasicTokens.NARROWING_KEYWORD, VisualBasicModifier.NARROWING),
        Map.entry(VisualBasicTokens.ASYNC_KEYWORD, VisualBasicModifier.ASYNC),
        Map.entry(VisualBasicTokens.ITERATOR_KEYWORD, VisualBasicModifier.ITERATOR),
        Map.entry(VisualBasicTokens.CUSTOM_KEYWORD, VisualBasicModifier.CUSTOM),
        Map.entry(VisualBasicTokens.OPTIONAL_KEYWORD, VisualBasicModifier.OPTIONAL),
        Map.entry(VisualBasicTokens.BYVAL_KEYWORD, VisualBasicModifier.BYVAL),
        Map.entry(VisualBasicTokens.BYREF_KEYWORD, VisualBasicModifier.BYREF),
        Map.entry(VisualBasicTokens.PARAMARRAY_KEYWORD, VisualBasicModifier.PARAMARRAY)
    );

    private VisualBasicModifiers() {
    }

    public static @Nullable DotNetModifier byKeyword(IElementType keyword) {
        return BY_KEYWORD.get(keyword);
    }

    public static long maskOf(DotNetModifier modifier) {
        for (int i = 0; i < ALL.length; i++) {
            if (ALL[i] == modifier) {
                return 1L << i;
            }
        }
        return 0;
    }

    public static boolean has(long mask, DotNetModifier modifier) {
        return (mask & maskOf(modifier)) != 0;
    }

    public static DotNetModifier[] toArray(long mask) {
        List<DotNetModifier> result = new ArrayList<>();
        for (int i = 0; i < ALL.length; i++) {
            if ((mask & 1L << i) != 0) {
                result.add(ALL[i]);
            }
        }
        return result.toArray(DotNetModifier[]::new);
    }
}
