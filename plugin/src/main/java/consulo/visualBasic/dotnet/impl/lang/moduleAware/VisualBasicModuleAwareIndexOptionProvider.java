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

import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.language.psi.stub.IndexOption;
import consulo.language.psi.stub.ModuleAwareIndexOptionProvider;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.visualBasic.dotnet.VisualBasicFileType;
import consulo.visualBasic.dotnet.module.extension.VisualBasicModuleExtension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Makes VB stubs depend on the project settings of the module ({@link VisualBasicCompileOptions}): the platform
 * reindexes the files of a module whose root namespace or constants change, e.g. when a {@code .vbproj} import sets
 * them after the first indexing.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
@ExtensionImpl
public class VisualBasicModuleAwareIndexOptionProvider implements ModuleAwareIndexOptionProvider {
    private static final Set<FileType> FILE_TYPES = Set.of(VisualBasicFileType.INSTANCE);

    @Override
    public String getId() {
        return VisualBasicCompileEnv.PROVIDER_ID;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public Set<FileType> getInputFileTypes() {
        return FILE_TYPES;
    }

    @Override
    public IndexOption getOptions(Module module, VirtualFile file) {
        VisualBasicCompileOptions options = optionsOf(module);
        if (options.isEmpty()) {
            return IndexOption.fullySharable();
        }
        return IndexOption.sharablePerOption(options, VisualBasicCompileOptionsExternalizer.INSTANCE, LocalizeValue.of(options.format()));
    }

    /**
     * Constants of the VB settings, or the variables of the .NET module (as {@code True}) when there are none.
     */
    public static VisualBasicCompileOptions optionsOf(Module module) {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        VisualBasicModuleExtension<?> visualBasic = rootManager.getExtension(VisualBasicModuleExtension.class);
        String rootNamespace = visualBasic == null ? "" : visualBasic.getRootNamespace();
        Map<String, String> constants = new LinkedHashMap<>();
        if (visualBasic != null) {
            constants.putAll(visualBasic.getConstants());
        }
        if (constants.isEmpty()) {
            DotNetSimpleModuleExtension<?> dotNet = rootManager.getExtension(DotNetSimpleModuleExtension.class);
            if (dotNet != null) {
                for (String variable : dotNet.getVariables()) {
                    if (!variable.isBlank()) {
                        constants.put(variable.strip(), "True");
                    }
                }
            }
        }
        return new VisualBasicCompileOptions(rootNamespace, Collections.unmodifiableMap(constants));
    }
}
