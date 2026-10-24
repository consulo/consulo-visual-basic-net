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
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetShortNameSearcher;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Case-insensitive type lookup for Visual Basic names, by the type names of all .NET languages.
 * Names are looked up by their declared case first, this is the fallback.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicCaseInsensitiveTypes {
    private VisualBasicCaseInsensitiveTypes() {
    }

    @RequiredReadAction
    public static @Nullable DotNetTypeDeclaration findType(Project project, String vmQName, GlobalSearchScope scope) {
        int separator = Math.max(vmQName.lastIndexOf('.'), vmQName.lastIndexOf('/'));
        String simpleName = vmQName.substring(separator + 1);
        int marker = simpleName.indexOf('`');
        String withoutMarker = marker < 0 ? simpleName : simpleName.substring(0, marker);

        DotNetShortNameSearcher searcher = DotNetShortNameSearcher.getInstance(project);
        List<String> keys = new ArrayList<>(getKeys(project, scope).getOrDefault(simpleName.toLowerCase(Locale.ROOT), List.of()));
        if (marker >= 0) {
            keys.addAll(getKeys(project, scope).getOrDefault(withoutMarker.toLowerCase(Locale.ROOT), List.of()));
        }
        for (String key : keys) {
            for (DotNetTypeDeclaration type : searcher.getTypes(key, scope, null)) {
                String typeVmQName = type.getVmQName();
                if (typeVmQName != null && typeVmQName.equalsIgnoreCase(vmQName)) {
                    return type;
                }
            }
        }
        return null;
    }

    /**
     * Type name keys of the short name indexes, by lower case name.
     */
    private static Map<String, List<String>> getKeys(Project project, GlobalSearchScope scope) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            Map<String, List<String>> keys = new HashMap<>();
            DotNetShortNameSearcher.getInstance(project).collectTypeNames(name -> {
                keys.computeIfAbsent(name.toLowerCase(Locale.ROOT), it -> new ArrayList<>(1)).add(name);
                return true;
            }, GlobalSearchScope.allScope(project), null);
            return CachedValueProvider.Result.create(keys, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }
}
