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
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicFileImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicImportsClauseImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicProjectSettings;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An import in effect in a file: an {@code Imports} clause of the file, or a project level import.
 *
 * @param alias {@code IO} of {@code IO = System.IO}
 * @author VISTALL
 * @since 2026-10-25
 */
public record VisualBasicImport(String qName, @Nullable String alias) {
    /**
     * Imports of the file, then the ones of the project.
     */
    @RequiredReadAction
    public static List<VisualBasicImport> getImports(PsiElement context) {
        List<VisualBasicImport> result = new ArrayList<>();
        if (context.getContainingFile() instanceof VisualBasicFileImpl file) {
            for (VisualBasicImportsClauseImpl clause : file.getImports()) {
                String imported = clause.getImportedQName();
                if (imported != null && !imported.isEmpty()) {
                    result.add(new VisualBasicImport(imported, clause.getAlias()));
                }
            }
        }
        for (String imported : VisualBasicProjectSettings.getProjectImports(context)) {
            int equals = imported.indexOf('=');
            if (equals < 0) {
                result.add(new VisualBasicImport(imported.strip(), null));
            }
            else {
                result.add(new VisualBasicImport(imported.substring(equals + 1).strip(), imported.substring(0, equals).strip()));
            }
        }
        return result;
    }
}
