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

package consulo.visualBasic.dotnet.impl.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.IndexingDataKeys;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import consulo.visualBasic.dotnet.impl.lang.moduleAware.VisualBasicCompileEnv;
import consulo.visualBasic.dotnet.impl.lang.moduleAware.VisualBasicCompileOptions;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.module.extension.VisualBasicModuleExtension;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Project level settings of the module of a file ({@link VisualBasicModuleExtension}), and the {@code Option}
 * statements of the file overriding them.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public final class VisualBasicProjectSettings {
    private VisualBasicProjectSettings() {
    }

    @RequiredReadAction
    public static @Nullable VisualBasicModuleExtension<?> getExtension(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return null;
        }
        VirtualFile virtualFile = getModuleFile(file);
        if (virtualFile == null) {
            return null;
        }
        Module module = ModuleUtilCore.findModuleForFile(virtualFile, file.getProject());
        return module == null ? null : ModuleUtilCore.getExtension(module, VisualBasicModuleExtension.class);
    }

    /**
     * File to find the module by: while indexing the PSI is made for a light copy of the file.
     *
     * @return null for text without a file
     */
    public static @Nullable VirtualFile getModuleFile(PsiFile file) {
        VirtualFile indexed = file.getUserData(IndexingDataKeys.VIRTUAL_FILE);
        if (indexed != null) {
            return indexed;
        }
        VirtualFile virtualFile = file.getOriginalFile().getViewProvider().getVirtualFile();
        while (virtualFile instanceof LightVirtualFile light && light.getOriginalFile() != null) {
            virtualFile = light.getOriginalFile();
        }
        return virtualFile instanceof LightVirtualFile ? null : virtualFile;
    }

    /**
     * {@code RootNamespace} of the project, empty for none.
     */
    @RequiredReadAction
    public static String getRootNamespace(PsiElement element) {
        // the value the file was indexed under, so that stubs and the tree agree
        VisualBasicCompileOptions options = VisualBasicCompileEnv.optionsFor(element);
        if (options != null) {
            return options.rootNamespace();
        }
        VisualBasicModuleExtension<?> extension = getExtension(element);
        return extension == null ? "" : extension.getRootNamespace();
    }

    @RequiredReadAction
    public static List<String> getProjectImports(PsiElement element) {
        VisualBasicModuleExtension<?> extension = getExtension(element);
        return extension == null ? List.of() : extension.getImports();
    }

    /**
     * {@code Option Infer} of the file, of the project otherwise (on by default).
     */
    @RequiredReadAction
    public static boolean isOptionInfer(PsiElement element) {
        Boolean fileOption = getFileOption(element, "infer");
        if (fileOption != null) {
            return fileOption;
        }
        VisualBasicModuleExtension<?> extension = getExtension(element);
        return extension == null || extension.isOptionInfer();
    }

    /**
     * Value of an {@code Option X On|Off} statement of the file.
     */
    @RequiredReadAction
    private static @Nullable Boolean getFileOption(PsiElement element, String name) {
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return null;
        }
        for (PsiElement child = file.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() != VisualBasicElements.OPTION_STATEMENT) {
                continue;
            }
            String text = child.getText().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").strip();
            if (text.startsWith("option " + name)) {
                return child.getNode().findChildByType(VisualBasicTokens.OFF_KEYWORD) == null;
            }
        }
        return null;
    }
}
