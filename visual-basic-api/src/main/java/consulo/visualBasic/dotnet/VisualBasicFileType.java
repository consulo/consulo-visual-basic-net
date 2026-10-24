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

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.visualBasic.dotnet.icon.VisualBasicIconGroup;
import consulo.visualBasic.dotnet.localize.VisualBasicLocalize;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicFileType extends LanguageFileType {
    public static final VisualBasicFileType INSTANCE = new VisualBasicFileType();

    private VisualBasicFileType() {
        super(VisualBasicLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "VisualBasic.NET";
    }

    @Override
    public LocalizeValue getDisplayName() {
        return VisualBasicLocalize.languageName();
    }

    @Override
    public LocalizeValue getDescription() {
        return VisualBasicLocalize.fileTypeDescription();
    }

    @Override
    public String getDefaultExtension() {
        return "vb";
    }

    @Override
    public Image getIcon() {
        return VisualBasicIconGroup.visualbasicfile();
    }
}
