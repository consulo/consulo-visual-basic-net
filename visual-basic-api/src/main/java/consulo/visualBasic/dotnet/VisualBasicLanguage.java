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

package consulo.visualBasic.dotnet;

import consulo.language.Language;
import consulo.localize.LocalizeValue;
import consulo.visualBasic.dotnet.localize.VisualBasicLocalize;

/**
 * Visual Basic .NET. Identifiers and keywords are case-insensitive.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicLanguage extends Language {
    public static final VisualBasicLanguage INSTANCE = new VisualBasicLanguage();

    private VisualBasicLanguage() {
        super("VisualBasic.NET");
    }

    @Override
    public LocalizeValue getDisplayName() {
        return VisualBasicLocalize.languageName();
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }
}
