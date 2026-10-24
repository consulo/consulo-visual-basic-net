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

package consulo.visualBasic.dotnet.impl.editor.highlight;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.rawHighlight.HighlightVisitor;
import consulo.language.editor.rawHighlight.HighlightVisitorFactory;
import consulo.language.psi.PsiFile;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicFileImpl;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
@ExtensionImpl
public class VisualBasicHighlightVisitorFactory implements HighlightVisitorFactory {
    @Override
    public boolean suitableForFile(PsiFile file) {
        return file instanceof VisualBasicFileImpl;
    }

    @Override
    public HighlightVisitor createVisitor() {
        return new VisualBasicHighlightVisitor();
    }
}
