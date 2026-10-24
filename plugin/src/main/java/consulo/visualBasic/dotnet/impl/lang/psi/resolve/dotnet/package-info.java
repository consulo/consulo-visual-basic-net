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

/**
 * Resolve helpers over the consulo-dotnet {@code DotNet*} interfaces only: member lookup, the supertype walk and
 * generic substitution. Nothing here is Visual Basic specific beyond {@link consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.NameMatcher},
 * so the package can move to consulo-dotnet as is.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
package consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet;
