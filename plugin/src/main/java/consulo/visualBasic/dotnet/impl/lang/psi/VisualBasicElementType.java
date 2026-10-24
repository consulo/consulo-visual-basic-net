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

package consulo.visualBasic.dotnet.impl.lang.psi;

import consulo.language.ast.ASTNode;
import consulo.language.ast.ElementTypeAsPsiFactory;
import consulo.language.psi.PsiElement;
import consulo.visualBasic.dotnet.VisualBasicLanguage;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicElementImpl;

import java.util.function.Function;

/**
 * Composite element type creating its PSI.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicElementType extends ElementTypeAsPsiFactory {
    public VisualBasicElementType(String debugName) {
        this(debugName, VisualBasicElementImpl::new);
    }

    public VisualBasicElementType(String debugName, Function<ASTNode, ? extends PsiElement> factory) {
        super(debugName, VisualBasicLanguage.INSTANCE, factory);
    }
}
