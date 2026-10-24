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

import org.jspecify.annotations.Nullable;

/**
 * How a language compares names: Visual Basic ignores case, C# and F# do not.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public interface NameMatcher {
    NameMatcher CASE_SENSITIVE = (declared, referenced) -> referenced.equals(declared);

    NameMatcher IGNORE_CASE = (declared, referenced) -> referenced.equalsIgnoreCase(declared);

    boolean matches(@Nullable String declared, String referenced);
}
