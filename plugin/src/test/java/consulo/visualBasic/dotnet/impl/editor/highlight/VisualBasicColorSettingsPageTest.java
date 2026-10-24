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

import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Set;

/**
 * The demo text of the color settings page is valid Visual Basic and uses every tag.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
@ExtendWith(ConsuloProjectLoader.class)
public class VisualBasicColorSettingsPageTest {
    @Test
    public void demoText(PsiFileFactory factory) {
        VisualBasicColorSettingsPage page = new VisualBasicColorSettingsPage();
        String demo = page.getDemoText();
        Set<String> tags = page.getAdditionalHighlightingTagToDescriptorMap().keySet();

        String text = demo;
        for (String tag : tags) {
            Assertions.assertTrue(demo.contains("<" + tag + ">"), "unused tag " + tag);
            text = text.replace("<" + tag + ">", "").replace("</" + tag + ">", "");
        }

        PsiFile file = factory.createFileFromText("Demo.vb", VisualBasicLanguage.INSTANCE, text);
        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        StringBuilder messages = new StringBuilder();
        for (PsiErrorElement error : errors) {
            messages.append(error.getTextOffset()).append(": ").append(error.getErrorDescription()).append('\n');
        }
        Assertions.assertTrue(errors.isEmpty(), messages.toString());
    }
}
