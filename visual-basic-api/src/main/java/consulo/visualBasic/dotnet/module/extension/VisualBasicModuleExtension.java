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

package consulo.visualBasic.dotnet.module.extension;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.module.extension.DotNetModuleLangExtension;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiElement;
import consulo.module.extension.ModuleExtension;
import consulo.visualBasic.dotnet.VisualBasicFileType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Visual Basic of a module: the project level settings of {@code .vbproj}.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public interface VisualBasicModuleExtension<T extends VisualBasicModuleExtension<T>> extends ModuleExtension<T>, DotNetModuleLangExtension<T> {
    /**
     * {@code RootNamespace}: the namespace of the declarations of the module, which {@code Namespace} declarations
     * nest in, except {@code Namespace Global}. Empty for none.
     */
    String getRootNamespace();

    /**
     * Project level {@code Imports}: {@code System.Text}, or an alias {@code IO = System.IO}.
     */
    List<String> getImports();

    /**
     * Conditional compilation constants with their values as expressions: {@code DEBUG -> -1},
     * {@code CONFIG -> "Debug"}.
     */
    Map<String, String> getConstants();

    boolean isOptionStrict();

    boolean isOptionExplicit();

    boolean isOptionInfer();

    VisualBasicCompareMode getOptionCompare();

    @RequiredReadAction
    @Override
    default PsiElement[] getEntryPointElements() {
        // TODO [VISTALL] Sub Main of the module
        return PsiElement.EMPTY_ARRAY;
    }

    @RequiredReadAction
    @Override
    default @Nullable String getAssemblyTitle() {
        // TODO [VISTALL] AssemblyTitle attribute of My Project/AssemblyInfo.vb
        return null;
    }

    @Override
    default LanguageFileType getFileType() {
        return VisualBasicFileType.INSTANCE;
    }
}
