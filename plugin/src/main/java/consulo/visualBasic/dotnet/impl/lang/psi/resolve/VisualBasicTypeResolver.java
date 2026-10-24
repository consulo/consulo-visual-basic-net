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

package consulo.visualBasic.dotnet.impl.lang.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.project.DumbService;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicAttributeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicFileImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicImportsClauseImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicNamespaceDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicReferenceExpressionImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeArgumentListImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicUserTypeImpl;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Names of types and namespaces. Looks in the generic parameters, the containing types, the enclosing namespaces
 * and the {@code Imports} of the file, in this order.
 * <p>
 * TODO [VISTALL] case-insensitive lookup, the root namespace of the project, project-level imports, members
 * of modules. Expressions do not resolve yet.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public final class VisualBasicTypeResolver {
    private static final Map<IElementType, String> PREDEFINED_TYPES = Map.ofEntries(
        Map.entry(VisualBasicTokens.BOOLEAN_KEYWORD, DotNetTypes.System.Boolean),
        Map.entry(VisualBasicTokens.BYTE_KEYWORD, DotNetTypes.System.Byte),
        Map.entry(VisualBasicTokens.CHAR_KEYWORD, DotNetTypes.System.Char),
        Map.entry(VisualBasicTokens.DATE_KEYWORD, "System.DateTime"),
        Map.entry(VisualBasicTokens.DECIMAL_KEYWORD, DotNetTypes.System.Decimal),
        Map.entry(VisualBasicTokens.DOUBLE_KEYWORD, DotNetTypes.System.Double),
        Map.entry(VisualBasicTokens.INTEGER_KEYWORD, DotNetTypes.System.Int32),
        Map.entry(VisualBasicTokens.LONG_KEYWORD, DotNetTypes.System.Int64),
        Map.entry(VisualBasicTokens.OBJECT_KEYWORD, DotNetTypes.System.Object),
        Map.entry(VisualBasicTokens.SBYTE_KEYWORD, DotNetTypes.System.SByte),
        Map.entry(VisualBasicTokens.SHORT_KEYWORD, DotNetTypes.System.Int16),
        Map.entry(VisualBasicTokens.SINGLE_KEYWORD, DotNetTypes.System.Single),
        Map.entry(VisualBasicTokens.STRING_KEYWORD, DotNetTypes.System.String),
        Map.entry(VisualBasicTokens.UINTEGER_KEYWORD, DotNetTypes.System.UInt32),
        Map.entry(VisualBasicTokens.ULONG_KEYWORD, DotNetTypes.System.UInt64),
        Map.entry(VisualBasicTokens.USHORT_KEYWORD, DotNetTypes.System.UInt16)
    );

    private static final Map<Character, String> TYPE_CHARACTERS = Map.of(
        '%', DotNetTypes.System.Int32,
        '&', DotNetTypes.System.Int64,
        '@', DotNetTypes.System.Decimal,
        '!', DotNetTypes.System.Single,
        '#', DotNetTypes.System.Double,
        '$', DotNetTypes.System.String
    );

    private VisualBasicTypeResolver() {
    }

    public static @Nullable String getPredefinedTypeVmQName(IElementType keyword) {
        return PREDEFINED_TYPES.get(keyword);
    }

    public static @Nullable String getTypeCharacterVmQName(char typeCharacter) {
        return TYPE_CHARACTERS.get(typeCharacter);
    }

    /**
     * Rank of {@code ()}, {@code (,)} or of array bounds {@code (1, 2)}.
     */
    public static int getRank(PsiElement rankOrBounds) {
        int commas = 0;
        for (PsiElement child = rankOrBounds.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() == VisualBasicTokens.COMMA) {
                commas++;
            }
        }
        return commas + 1;
    }

    @RequiredReadAction
    public static DotNetTypeRef nullable(PsiElement context, DotNetTypeRef inner) {
        DotNetTypeRef nullable = new VisualBasicTypeRefByQName(context, "System.Nullable`1");
        return new VisualBasicGenericWrapperTypeRef(context, nullable, new DotNetTypeRef[]{inner});
    }

    @RequiredReadAction
    public static DotNetTypeRef tuple(PsiElement context, List<DotNetTypeRef> elements) {
        DotNetTypeRef tuple = new VisualBasicTypeRefByQName(context, "System.ValueTuple`" + elements.size());
        return new VisualBasicGenericWrapperTypeRef(context, tuple, elements.toArray(DotNetTypeRef[]::new));
    }

    @RequiredReadAction
    public static DotNetTypeRef resolveUserType(VisualBasicUserTypeImpl type) {
        return resolveUserType(type, false);
    }

    /**
     * {@code <Obsolete>} is {@code ObsoleteAttribute} or {@code Obsolete}.
     */
    @RequiredReadAction
    public static DotNetTypeRef resolveAttributeType(VisualBasicUserTypeImpl type) {
        return resolveUserType(type, true);
    }

    @RequiredReadAction
    private static DotNetTypeRef resolveUserType(VisualBasicUserTypeImpl type, boolean attribute) {
        VisualBasicReferenceExpressionImpl reference = type.getReferenceExpression();
        VisualBasicTypeArgumentListImpl typeArguments = reference.getTypeArgumentList();
        String name = reference.getQualifiedNameText();

        if (reference.getQualifier() == null && typeArguments == null && !attribute) {
            DotNetGenericParameter parameter = findGenericParameter(type, name);
            if (parameter != null) {
                return new VisualBasicGenericParameterTypeRef(parameter);
            }
        }

        String vmName = typeArguments == null ? name : name + "`" + typeArguments.getTypesCount();
        List<String> candidates = new ArrayList<>();
        if (attribute && !vmName.endsWith("Attribute")) {
            candidates.addAll(getCandidates(type, vmName + "Attribute", isGlobal(reference)));
        }
        candidates.addAll(getCandidates(type, vmName, isGlobal(reference)));

        DotNetTypeRef typeRef = new VisualBasicTypeRefByQName(type, vmName, candidates);
        if (typeArguments != null) {
            DotNetType[] arguments = typeArguments.getTypes();
            if (arguments.length > 0) {
                return new VisualBasicGenericWrapperTypeRef(type, typeRef, typeArguments.getTypeRefs());
            }
        }
        return typeRef;
    }

    private static boolean isGlobal(VisualBasicReferenceExpressionImpl reference) {
        VisualBasicReferenceExpressionImpl first = reference;
        while (first.getQualifier() instanceof VisualBasicReferenceExpressionImpl qualifier) {
            first = qualifier;
        }
        return first.isGlobal();
    }

    @RequiredReadAction
    private static @Nullable DotNetGenericParameter findGenericParameter(PsiElement element, String name) {
        for (PsiElement parent = element.getParent(); parent != null && !(parent instanceof PsiFile); parent = parent.getParent()) {
            if (parent instanceof DotNetGenericParameterListOwner owner) {
                for (DotNetGenericParameter parameter : owner.getGenericParameters()) {
                    if (name.equalsIgnoreCase(parameter.getName())) {
                        return parameter;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Qualified names the name may stand for, the most specific first.
     */
    @RequiredReadAction
    public static List<String> getCandidates(PsiElement context, String name, boolean global) {
        List<String> candidates = new ArrayList<>();
        if (global) {
            candidates.add(name);
            return candidates;
        }
        for (PsiElement parent = context.getParent(); parent != null && !(parent instanceof PsiFile); parent = parent.getParent()) {
            if (parent instanceof VisualBasicTypeDeclarationImpl type) {
                String vmQName = type.getVmQName();
                if (vmQName != null) {
                    candidates.add(vmQName + "/" + name);
                }
            }
            else if (parent instanceof VisualBasicNamespaceDeclarationImpl namespace) {
                String qName = namespace.getPresentableQName();
                if (qName != null && !qName.isEmpty()) {
                    candidates.add(qName + "." + name);
                }
            }
        }
        candidates.add(name);
        int dot = name.indexOf('.');
        String first = dot < 0 ? name : name.substring(0, dot);
        for (VisualBasicImport imported : VisualBasicImport.getImports(context)) {
            if (imported.alias() == null) {
                candidates.add(imported.qName() + "." + name);
            }
            else if (imported.alias().equalsIgnoreCase(first)) {
                candidates.add(dot < 0 ? imported.qName() : imported.qName() + name.substring(dot));
            }
        }
        return dot < 0 ? candidates : withNestedTypes(candidates, name);
    }

    /**
     * {@code Outer.Inner} may name a nested type, {@code Outer/Inner}: each candidate is followed by its variants with
     * the dots of the name, from the last one, as nested separators.
     */
    private static List<String> withNestedTypes(List<String> candidates, String name) {
        int dots = (int) name.chars().filter(c -> c == '.').count();
        List<String> result = new ArrayList<>(candidates.size() * (dots + 1));
        for (String candidate : candidates) {
            result.add(candidate);
            String nested = candidate;
            for (int i = 0; i < dots; i++) {
                int last = nested.lastIndexOf('.');
                if (last < 0) {
                    break;
                }
                nested = nested.substring(0, last) + "/" + nested.substring(last + 1);
                result.add(nested);
            }
        }
        return result;
    }

    /**
     * Type or namespace for a simple name in an expression: {@code Console}, {@code System}.
     */
    @RequiredReadAction
    public static @Nullable PsiElement resolveTypeOrNamespace(PsiElement context, String name, int arity) {
        String vmName = arity > 0 ? name + "`" + arity : name;
        DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(context.getProject());
        GlobalSearchScope scope = context.getResolveScope();
        List<String> candidates = getCandidates(context, vmName, false);
        for (String candidate : candidates) {
            DotNetTypeDeclaration type = searcher.findType(candidate, scope);
            if (type != null) {
                return type;
            }
        }
        DotNetNamespaceAsElement namespace = searcher.findNamespace(name, scope);
        if (namespace != null) {
            return namespace;
        }
        for (String candidate : candidates) {
            DotNetTypeDeclaration type = VisualBasicCaseInsensitiveTypes.findType(context.getProject(), candidate, scope);
            if (type != null) {
                return type;
            }
        }
        return findNamespaceIgnoreCase(searcher, name, scope);
    }

    /**
     * Target of a name in a type, namespace or {@code Imports} name: a type or a namespace.
     */
    @RequiredReadAction
    public static @Nullable PsiElement resolveNameReference(VisualBasicReferenceExpressionImpl reference) {
        VisualBasicReferenceExpressionImpl top = reference;
        while (top.getParent() instanceof VisualBasicReferenceExpressionImpl parent && parent.getQualifier() == top) {
            top = parent;
        }
        PsiElement owner = top.getParent();
        IElementType ownerType = owner == null ? null : owner.getNode().getElementType();
        if (ownerType != VisualBasicElements.USER_TYPE
            && ownerType != VisualBasicElements.IMPORTS_CLAUSE
            && ownerType != VisualBasicElements.NAMESPACE_DECLARATION) {
            return null;
        }
        if (reference.isGlobal() || DumbService.isDumb(reference.getProject())) {
            return null;
        }
        if (top == reference && owner instanceof VisualBasicUserTypeImpl userType) {
            // <Obsolete> names ObsoleteAttribute
            DotNetTypeRef typeRef = userType.getParent() instanceof VisualBasicAttributeImpl ? resolveAttributeType(userType) : userType.toTypeRef();
            PsiElement element = typeRef.resolve().getElement();
            if (element != null) {
                return element;
            }
        }

        String qName = reference.getQualifiedNameText();
        DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(reference.getProject());
        if (owner instanceof VisualBasicNamespaceDeclarationImpl namespace) {
            String parentQName = namespace.getPresentableParentQName();
            qName = parentQName == null || parentQName.isEmpty() ? qName : parentQName + "." + qName;
            return searcher.findNamespace(qName, reference.getResolveScope());
        }

        List<String> candidates = ownerType == VisualBasicElements.IMPORTS_CLAUSE ? List.of(qName) : getCandidates(reference, qName, false);
        GlobalSearchScope scope = reference.getResolveScope();
        for (String candidate : candidates) {
            DotNetNamespaceAsElement namespace = searcher.findNamespace(candidate, scope);
            if (namespace != null) {
                return namespace;
            }
            DotNetTypeDeclaration type = searcher.findType(candidate, scope);
            if (type != null) {
                return type;
            }
        }
        // Visual Basic names are case-insensitive
        for (String candidate : candidates) {
            DotNetNamespaceAsElement namespace = findNamespaceIgnoreCase(searcher, candidate, scope);
            if (namespace != null) {
                return namespace;
            }
            DotNetTypeDeclaration type = VisualBasicCaseInsensitiveTypes.findType(reference.getProject(), candidate, scope);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    /**
     * Walks the namespaces from the root, comparing the names ignoring case.
     */
    @RequiredReadAction
    private static @Nullable DotNetNamespaceAsElement findNamespaceIgnoreCase(DotNetPsiSearcher searcher, String qName, GlobalSearchScope scope) {
        DotNetNamespaceAsElement namespace = searcher.findNamespace("", scope);
        for (String segment : qName.split("\\.")) {
            if (namespace == null) {
                return null;
            }
            DotNetNamespaceAsElement next = null;
            for (PsiElement child : namespace.getChildren(scope, DotNetNamespaceAsElement.ChildrenFilter.ONLY_NAMESPACES)) {
                if (child instanceof DotNetNamespaceAsElement childNamespace && segment.equalsIgnoreCase(childNamespace.getName())) {
                    next = childNamespace;
                    break;
                }
            }
            namespace = next;
        }
        return namespace;
    }
}
