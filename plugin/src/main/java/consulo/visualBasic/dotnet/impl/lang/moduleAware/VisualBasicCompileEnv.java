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

import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.ModuleAwareIndexOptions;
import org.jspecify.annotations.Nullable;

/**
 * Reads back the options the module-aware index recorded for a file, so that stubs, the editor and resolve all see
 * the settings the file was indexed under.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicCompileEnv {
    public static final String PROVIDER_ID = "visualBasic-dotnet-module-aware";

    private VisualBasicCompileEnv() {
    }

    /**
     * @return {@code null} when nothing was recorded, e.g. without a module or in a light test environment
     */
    public static @Nullable VisualBasicCompileOptions optionsFor(PsiElement context) {
        try {
            return ModuleAwareIndexOptions.getOptions(context, PROVIDER_ID, VisualBasicCompileOptionsExternalizer.INSTANCE);
        }
        catch (IllegalArgumentException serviceNotBound) {
            // a light fixture has no module-aware index service
            return null;
        }
    }
}
