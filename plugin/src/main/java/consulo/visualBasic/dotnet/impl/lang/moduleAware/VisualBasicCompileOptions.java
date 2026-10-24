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

package consulo.visualBasic.dotnet.impl.lang.moduleAware;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Project settings which change the stubs of a file: the root namespace qualifies file-level declarations, and the
 * constants decide which code conditional compilation keeps.
 *
 * @param constants values as expressions, by name, in declaration order
 * @author VISTALL
 * @since 2026-10-24
 */
public record VisualBasicCompileOptions(String rootNamespace, Map<String, String> constants) {
    public boolean isEmpty() {
        return rootNamespace.isEmpty() && constants.isEmpty();
    }

    public String format() {
        String defines = constants.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(","));
        if (rootNamespace.isEmpty()) {
            return defines;
        }
        return defines.isEmpty() ? rootNamespace : rootNamespace + "; " + defines;
    }
}
