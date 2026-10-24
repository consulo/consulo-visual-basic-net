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

package consulo.visualBasic.dotnet.impl.lang.psi.partial;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.virtualFileSystem.VirtualFile;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.stub.index.VisualBasicIndexKeys;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Partial types: the declarations of a type with the same qualified name in one module, of which at least one is
 * {@code Partial}, are parts of one type, {@link VisualBasicCompositeTypeDeclaration}. Only one part needs the
 * modifier.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public final class VisualBasicPartialTypes {
    private record Composite(@Nullable VisualBasicCompositeTypeDeclaration value) {
    }

    private VisualBasicPartialTypes() {
    }

    /**
     * @return the composite of the parts of {@code type}, or {@code type} when it is not partial
     */
    @RequiredReadAction
    public static DotNetTypeDeclaration compositeOrSelf(DotNetTypeDeclaration type) {
        if (type instanceof VisualBasicTypeDeclarationImpl part) {
            VisualBasicCompositeTypeDeclaration composite = findComposite(part);
            if (composite != null) {
                return composite;
            }
        }
        return type;
    }

    /**
     * Replaces the parts of partial types by their composite, once.
     */
    @RequiredReadAction
    public static <T extends PsiElement> List<T> merge(Collection<? extends T> elements) {
        Set<T> result = new LinkedHashSet<>(elements.size());
        boolean changed = false;
        for (T element : elements) {
            if (element instanceof VisualBasicTypeDeclarationImpl part) {
                VisualBasicCompositeTypeDeclaration composite = findComposite(part);
                if (composite != null) {
                    @SuppressWarnings("unchecked") T merged = (T) composite;
                    result.add(merged);
                    changed = true;
                    continue;
                }
            }
            result.add(element);
        }
        return changed ? new ArrayList<>(result) : List.copyOf(elements);
    }

    /**
     * Kind of a Visual Basic type or composite, {@code null} for types of other languages.
     */
    @RequiredReadAction
    public static @Nullable VisualBasicTypeKind getKind(DotNetTypeDeclaration type) {
        if (type instanceof VisualBasicTypeDeclarationImpl part) {
            return part.getKind();
        }
        if (type instanceof VisualBasicCompositeTypeDeclaration composite) {
            return composite.getKind();
        }
        return null;
    }

    @RequiredReadAction
    public static @Nullable VisualBasicCompositeTypeDeclaration findComposite(VisualBasicTypeDeclarationImpl part) {
        if (DumbService.isDumb(part.getProject())) {
            return null;
        }
        return LanguageCachedValueUtil.getCachedValue(part,
            () -> CachedValueProvider.Result.create(new Composite(computeComposite(part)), PsiModificationTracker.MODIFICATION_COUNT)).value();
    }

    @RequiredReadAction
    private static @Nullable VisualBasicCompositeTypeDeclaration computeComposite(VisualBasicTypeDeclarationImpl part) {
        String vmQName = part.getVmQName();
        if (vmQName == null) {
            return null;
        }
        Module module = ModuleUtilCore.findModuleForPsiElement(part);
        Collection<DotNetTypeDeclaration> candidates = StubIndex.getElements(VisualBasicIndexKeys.TYPE_BY_VMQNAME_INDEX,
            vmQName.hashCode(),
            part.getProject(),
            part.getResolveScope(),
            DotNetTypeDeclaration.class);
        List<VisualBasicTypeDeclarationImpl> parts = new ArrayList<>();
        boolean partial = false;
        for (DotNetTypeDeclaration candidate : candidates) {
            if (candidate instanceof VisualBasicTypeDeclarationImpl other
                && vmQName.equals(other.getVmQName())
                && Objects.equals(module, ModuleUtilCore.findModuleForPsiElement(other))) {
                parts.add(other);
                partial |= other.hasModifier(VisualBasicModifier.PARTIAL);
            }
        }
        if (parts.size() < 2 || !partial || !parts.contains(part)) {
            return null;
        }
        // the same order for every part: equal composites
        parts.sort(Comparator.comparing(VisualBasicPartialTypes::filePath).thenComparingInt(PsiElement::getTextOffset));
        return new VisualBasicCompositeTypeDeclaration(parts);
    }

    private static String filePath(PsiElement element) {
        PsiFile file = element.getContainingFile();
        VirtualFile virtualFile = file == null ? null : file.getVirtualFile();
        return virtualFile == null ? "" : virtualFile.getPath();
    }
}
