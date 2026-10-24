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

package consulo.visualBasic.dotnet.impl.lang.parser.preprocessor;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import consulo.visualBasic.dotnet.impl.lang.moduleAware.VisualBasicCompileEnv;
import consulo.visualBasic.dotnet.impl.lang.moduleAware.VisualBasicCompileOptions;
import consulo.visualBasic.dotnet.impl.lang.moduleAware.VisualBasicModuleAwareIndexOptionProvider;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicProjectSettings;

import java.util.Map;

/**
 * Conditional compilation constants of the module of a file: the ones of its Visual Basic settings, otherwise the
 * variables of its .NET module extension, which are {@code True}.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicPreprocessorDefines {
    private VisualBasicPreprocessorDefines() {
    }

    /**
     * @return values as expressions, by name
     */
    @RequiredReadAction
    public static Map<String, String> find(PsiFile file) {
        VisualBasicCompileOptions options = VisualBasicCompileEnv.optionsFor(file);
        if (options != null) {
            return options.constants();
        }
        VirtualFile virtualFile = VisualBasicProjectSettings.getModuleFile(file);
        Module module = virtualFile == null ? null : ModuleUtilCore.findModuleForFile(virtualFile, file.getProject());
        return module == null ? Map.of() : VisualBasicModuleAwareIndexOptionProvider.optionsOf(module).constants();
    }
}
