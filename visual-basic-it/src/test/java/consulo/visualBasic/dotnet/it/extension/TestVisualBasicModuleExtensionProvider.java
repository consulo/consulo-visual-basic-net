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

package consulo.visualBasic.dotnet.it.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import consulo.visualBasic.dotnet.icon.VisualBasicIconGroup;
import org.jspecify.annotations.Nullable;

/**
 * Visual Basic extension of the mono .NET modules of tests.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
@ExtensionImpl
public class TestVisualBasicModuleExtensionProvider implements ModuleExtensionProvider<TestVisualBasicModuleExtension> {
    public static final String ID = "visualBasic-test";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public @Nullable String getParentId() {
        return "mono-dotnet";
    }

    @Override
    public LocalizeValue getName() {
        return LocalizeValue.of("Visual Basic (tests)");
    }

    @Override
    public Image getIcon() {
        return VisualBasicIconGroup.visualbasic();
    }

    @Override
    public ModuleExtension<TestVisualBasicModuleExtension> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new TestVisualBasicModuleExtension(getId(), moduleRootLayer);
    }

    @Override
    public MutableModuleExtension<TestVisualBasicModuleExtension> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new TestVisualBasicMutableModuleExtension(getId(), moduleRootLayer);
    }
}
