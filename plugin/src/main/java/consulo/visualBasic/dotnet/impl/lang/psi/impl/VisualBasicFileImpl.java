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
import consulo.dotnet.psi.DotNetFile;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.visualBasic.dotnet.VisualBasicFileType;
import consulo.visualBasic.dotnet.VisualBasicLanguage;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public class VisualBasicFileImpl extends PsiFileBase implements DotNetFile {
    public VisualBasicFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, VisualBasicLanguage.INSTANCE);
    }

    @Override
    public FileType getFileType() {
        return VisualBasicFileType.INSTANCE;
    }

    @RequiredReadAction
    @Override
    public DotNetNamedElement[] getMembers() {
        return VisualBasicMemberUtil.getMembers(this);
    }

    /**
     * {@code Imports} clauses of the file.
     */
    @RequiredReadAction
    public List<VisualBasicImportsClauseImpl> getImports() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, VisualBasicImportsStatementImpl.class)
            .stream()
            .flatMap(statement -> statement.getClauses().stream())
            .toList();
    }
}
