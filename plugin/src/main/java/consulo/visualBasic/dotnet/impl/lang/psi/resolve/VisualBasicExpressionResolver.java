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
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.DumbService;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicModifier;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicAccessorImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicLocalVariableImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicNamespaceDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicReferenceExpressionImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeArgumentListImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.DotNetMemberLookup;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.NameMatcher;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Names in expressions. Unqualified names are looked up in: locals declared before in enclosing blocks, statement
 * variables ({@code For}, {@code Using}, {@code Catch}), lambda and method parameters, members of the containing
 * types and their supertypes, members of modules of the enclosing and imported namespaces, members of imported types,
 * then types and namespaces. Names are case-insensitive.
 * <p>
 * TODO [VISTALL] extension methods, query range variables, implicit {@code Value} of property setters, late binding
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicExpressionResolver {
    private static final String STANDARD_MODULE_ATTRIBUTE = "Microsoft.VisualBasic.CompilerServices.StandardModuleAttribute";

    private static final ResolveCache.PolyVariantResolver<VisualBasicReferenceExpressionImpl> RESOLVER = (reference, incompleteCode) -> resolveImpl(reference);

    private VisualBasicExpressionResolver() {
    }

    @RequiredReadAction
    public static ResolveResult[] resolve(VisualBasicReferenceExpressionImpl reference, boolean incompleteCode) {
        return ResolveCache.getInstance(reference.getProject()).resolveWithCaching(reference, RESOLVER, true, incompleteCode);
    }

    @RequiredReadAction
    private static ResolveResult[] resolveImpl(VisualBasicReferenceExpressionImpl reference) {
        String name = reference.getReferenceName();
        if (name == null || DumbService.isDumb(reference.getProject())) {
            return ResolveResult.EMPTY_ARRAY;
        }
        List<VisualBasicResolveResult> results;
        PsiElement qualifier = reference.getQualifier();
        if (qualifier != null) {
            results = resolveQualified(reference, qualifier, name);
        }
        else if (reference.getNode().getFirstChildNode() != null && reference.getNode().getFirstChildNode().getElementType() == VisualBasicTokens.DOT) {
            results = resolveInWith(reference, name);
        }
        else {
            results = resolveUnqualified(reference, name);
            if (reference.getParent() instanceof VisualBasicReferenceExpressionImpl member && member.getQualifier() == reference) {
                results = resolveSameNamedType(member, name, results);
            }
        }
        return chooseOverload(reference, results).toArray(ResolveResult[]::new);
    }

    /**
     * A variable named like its type ({@code shape As Shape}) stands for the type when the member after it is shared,
     * a constant or a nested type: {@code Shape.Tags.Circle}.
     */
    @RequiredReadAction
    private static List<VisualBasicResolveResult> resolveSameNamedType(VisualBasicReferenceExpressionImpl member,
                                                                       String name,
                                                                       List<VisualBasicResolveResult> results) {
        String memberName = member.getReferenceName();
        if (memberName == null || results.size() != 1 || !(results.get(0).getElement() instanceof DotNetVariable variable)) {
            return results;
        }
        DotNetTypeResolveResult typeResult = variable.toTypeRef(true).resolve();
        if (!(typeResult.getElement() instanceof DotNetTypeDeclaration type) || !name.equalsIgnoreCase(type.getName())) {
            return results;
        }
        List<VisualBasicResolveResult> members = members(type, typeResult.getGenericExtractor(), memberName, member);
        if (members.isEmpty()) {
            return results;
        }
        for (VisualBasicResolveResult result : members) {
            PsiElement element = result.getElement();
            boolean shared = element instanceof DotNetTypeDeclaration
                || element instanceof DotNetVariable memberVariable && memberVariable.isConstant()
                || element instanceof DotNetModifierListOwner owner && owner.hasModifier(DotNetModifier.STATIC);
            if (!shared) {
                return results;
            }
        }
        return List.of(new VisualBasicResolveResult(type, typeResult.getGenericExtractor()));
    }

    // qualified names

    @RequiredReadAction
    private static List<VisualBasicResolveResult> resolveQualified(VisualBasicReferenceExpressionImpl reference, PsiElement qualifier, String name) {
        if (qualifier instanceof VisualBasicReferenceExpressionImpl qualifierReference) {
            ResolveResult[] qualifierResults = qualifierReference.multiResolve(false);
            if (qualifierResults.length > 0) {
                PsiElement target = qualifierResults[0].getElement();
                if (target instanceof DotNetNamespaceAsElement namespace) {
                    return resolveInNamespace(reference, namespace, name);
                }
                if (target instanceof DotNetTypeDeclaration type) {
                    return members(type, DotNetGenericExtractor.EMPTY, name, reference);
                }
            }
        }
        IElementType qualifierType = qualifier.getNode().getElementType();
        if (qualifierType == VisualBasicElements.INSTANCE_EXPRESSION) {
            if (qualifier.getNode().findChildByType(VisualBasicTokens.MYBASE_KEYWORD) != null) {
                VisualBasicTypeDeclarationImpl type = PsiTreeUtil.getParentOfType(reference, VisualBasicTypeDeclarationImpl.class);
                if (type == null) {
                    return List.of();
                }
                for (DotNetTypeRef extendTypeRef : type.getExtendTypeRefs()) {
                    DotNetTypeResolveResult baseResult = extendTypeRef.resolve();
                    if (baseResult.getElement() instanceof DotNetTypeDeclaration base && !base.isInterface()) {
                        return members(base, baseResult.getGenericExtractor(), name, reference);
                    }
                }
                return List.of();
            }
        }
        DotNetTypeRef typeRef = VisualBasicExpressionTypes.typeOf(qualifier);
        return membersOfType(typeRef, name, reference);
    }

    @RequiredReadAction
    static List<VisualBasicResolveResult> membersOfType(DotNetTypeRef typeRef, String name, PsiElement context) {
        DotNetTypeResolveResult resolved = typeRef.resolve();
        PsiElement element = resolved.getElement();
        if (element instanceof DotNetTypeDeclaration type) {
            return members(type, resolved.getGenericExtractor(), name, context);
        }
        if (element instanceof DotNetGenericParameter) {
            // TODO [VISTALL] members of the constraints
            DotNetTypeDeclaration object = DotNetPsiSearcher.getInstance(context.getProject())
                .findType(DotNetTypes.System.Object, context.getResolveScope());
            return object == null ? List.of() : members(object, DotNetGenericExtractor.EMPTY, name, context);
        }
        return List.of();
    }

    @RequiredReadAction
    private static List<VisualBasicResolveResult> members(DotNetTypeDeclaration type, DotNetGenericExtractor extractor, String name, PsiElement context) {
        List<VisualBasicResolveResult> results = new ArrayList<>();
        DotNetTypeDeclaration owner = null;
        for (DotNetMemberLookup.Member member : DotNetMemberLookup.findMembers(type, extractor, name, NameMatcher.IGNORE_CASE,
            it -> isAccessible(it, context), new VisualBasicTypeRefFactory(context))) {
            if (!results.isEmpty() && !(member.element() instanceof DotNetLikeMethodDeclaration) && member.owner() != owner) {
                // fields, properties and events of a type hide the ones of its supertypes by name
                break;
            }
            owner = member.owner();
            results.add(new VisualBasicResolveResult(member.element(), member.extractor()));
        }
        return results;
    }

    /**
     * Private members are accessible inside their type only: this also skips explicit interface implementations of
     * libraries, as {@code IList.Item} of {@code List(Of T)}.
     * <p>
     * TODO [VISTALL] Protected and Friend
     */
    @RequiredReadAction
    private static boolean isAccessible(DotNetNamedElement member, PsiElement context) {
        if (!(member instanceof DotNetModifierListOwner owner) || !owner.hasModifier(DotNetModifier.PRIVATE)) {
            return true;
        }
        PsiElement memberType = PsiTreeUtil.getParentOfType(member, DotNetTypeDeclaration.class);
        if (memberType == null) {
            return false;
        }
        for (PsiElement parent = context.getParent(); parent != null; parent = parent.getParent()) {
            if (parent == memberType || parent.isEquivalentTo(memberType)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static List<VisualBasicResolveResult> resolveInNamespace(VisualBasicReferenceExpressionImpl reference, DotNetNamespaceAsElement namespace, String name) {
        int arity = getTypeArgumentCount(reference);
        GlobalSearchScope scope = reference.getResolveScope();
        List<VisualBasicResolveResult> types = new ArrayList<>();
        List<VisualBasicResolveResult> namespaces = new ArrayList<>();
        for (PsiElement child : namespace.getChildren(scope, DotNetNamespaceAsElement.ChildrenFilter.NONE)) {
            if (child instanceof DotNetNamespaceAsElement childNamespace && name.equalsIgnoreCase(childNamespace.getName())) {
                namespaces.add(new VisualBasicResolveResult(childNamespace, DotNetGenericExtractor.EMPTY));
            }
            else if (child instanceof DotNetTypeDeclaration type && isTypeNamed(type, name, arity)) {
                types.add(new VisualBasicResolveResult(type, DotNetGenericExtractor.EMPTY));
            }
        }
        return types.isEmpty() ? namespaces : types;
    }

    /**
     * {@code List} names {@code List`1} when used with one type argument.
     */
    @RequiredReadAction
    private static boolean isTypeNamed(DotNetTypeDeclaration type, String name, int arity) {
        String typeName = type.getName();
        if (typeName == null) {
            return false;
        }
        int marker = typeName.indexOf('`');
        String plainName = marker < 0 ? typeName : typeName.substring(0, marker);
        if (!plainName.equalsIgnoreCase(name)) {
            return false;
        }
        return arity < 0 || type.getGenericParametersCount() == arity;
    }

    @RequiredReadAction
    private static int getTypeArgumentCount(VisualBasicReferenceExpressionImpl reference) {
        VisualBasicTypeArgumentListImpl typeArguments = reference.getTypeArgumentList();
        return typeArguments == null ? 0 : typeArguments.getTypesCount();
    }

    // With blocks

    @RequiredReadAction
    private static List<VisualBasicResolveResult> resolveInWith(VisualBasicReferenceExpressionImpl reference, String name) {
        PsiElement with = PsiTreeUtil.findFirstParent(reference, it -> it.getNode().getElementType() == VisualBasicElements.WITH_STATEMENT);
        if (with == null) {
            return List.of();
        }
        PsiElement expression = PsiTreeUtil.findChildOfType(with, DotNetExpression.class, false);
        if (expression == null || PsiTreeUtil.isAncestor(expression, reference, false)) {
            return List.of();
        }
        return membersOfType(VisualBasicExpressionTypes.typeOf(expression), name, reference);
    }

    // unqualified names

    @RequiredReadAction
    private static List<VisualBasicResolveResult> resolveUnqualified(VisualBasicReferenceExpressionImpl reference, String name) {
        PsiElement current = reference;
        for (PsiElement parent = reference.getParent(); parent != null && !(parent instanceof PsiFile); current = parent, parent = parent.getParent()) {
            List<VisualBasicResolveResult> results = new ArrayList<>();
            IElementType parentType = parent.getNode().getElementType();
            if (parentType == VisualBasicElements.CODE_BLOCK) {
                collectLocals(parent, current, name, results);
            }
            else if (parentType == VisualBasicElements.FOR_STATEMENT
                || parentType == VisualBasicElements.FOR_EACH_STATEMENT
                || parentType == VisualBasicElements.USING_STATEMENT
                || parentType == VisualBasicElements.CATCH_CLAUSE) {
                collectStatementVariables(parent, name, results);
            }
            else if (parentType == VisualBasicElements.LAMBDA_EXPRESSION) {
                collectParameters(VisualBasicResolveUtil.getParameters(parent), name, results);
            }
            else if (parent instanceof VisualBasicAccessorImpl accessor) {
                collectParameters(VisualBasicResolveUtil.getParameters(accessor), name, results);
                if (accessor.getParent() != null) {
                    collectParameters(VisualBasicResolveUtil.getParameters(accessor.getParent()), name, results);
                }
            }
            else if (parent instanceof DotNetParameterListOwner owner && !(parent instanceof DotNetTypeDeclaration)) {
                collectParameters(owner.getParameters(), name, results);
            }
            else if (parent instanceof DotNetTypeDeclaration type) {
                results.addAll(members(type, DotNetGenericExtractor.EMPTY, name, reference));
            }
            if (!results.isEmpty()) {
                return results;
            }
        }

        List<VisualBasicResolveResult> moduleMembers = resolveInModulesAndImportedTypes(reference, name);
        if (!moduleMembers.isEmpty()) {
            return moduleMembers;
        }

        PsiElement typeOrNamespace = VisualBasicTypeResolver.resolveTypeOrNamespace(reference, name, getTypeArgumentCount(reference));
        return typeOrNamespace == null ? List.of() : List.of(new VisualBasicResolveResult(typeOrNamespace, DotNetGenericExtractor.EMPTY));
    }

    /**
     * Locals of the block declared before {@code place}.
     */
    @RequiredReadAction
    private static void collectLocals(PsiElement block, PsiElement place, String name, List<VisualBasicResolveResult> results) {
        for (PsiElement statement = block.getFirstChild(); statement != null && statement != place; statement = statement.getNextSibling()) {
            if (statement.getNode().getElementType() == VisualBasicElements.LOCAL_DECLARATION_STATEMENT) {
                for (VisualBasicLocalVariableImpl local : PsiTreeUtil.findChildrenOfType(statement, VisualBasicLocalVariableImpl.class)) {
                    if (name.equalsIgnoreCase(local.getName())) {
                        results.add(new VisualBasicResolveResult(local, DotNetGenericExtractor.EMPTY));
                    }
                }
            }
        }
    }

    /**
     * Variables declared by the statement itself, not by statements of its body.
     */
    @RequiredReadAction
    private static void collectStatementVariables(PsiElement statement, String name, List<VisualBasicResolveResult> results) {
        for (PsiElement child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof VisualBasicLocalVariableImpl local && name.equalsIgnoreCase(local.getName())) {
                results.add(new VisualBasicResolveResult(local, DotNetGenericExtractor.EMPTY));
            }
            else if (child.getNode().getElementType() == VisualBasicElements.VARIABLE_DECLARATOR) {
                for (PsiElement variable = child.getFirstChild(); variable != null; variable = variable.getNextSibling()) {
                    if (variable instanceof VisualBasicLocalVariableImpl local && name.equalsIgnoreCase(local.getName())) {
                        results.add(new VisualBasicResolveResult(local, DotNetGenericExtractor.EMPTY));
                    }
                }
            }
        }
    }

    @RequiredReadAction
    private static void collectParameters(DotNetParameter[] parameters, String name, List<VisualBasicResolveResult> results) {
        for (DotNetParameter parameter : parameters) {
            if (name.equalsIgnoreCase(parameter.getName())) {
                results.add(new VisualBasicResolveResult(parameter, DotNetGenericExtractor.EMPTY));
            }
        }
    }

    /**
     * Members of modules in the enclosing and imported namespaces, and of imported types: {@code Imports System.Math}.
     */
    @RequiredReadAction
    private static List<VisualBasicResolveResult> resolveInModulesAndImportedTypes(VisualBasicReferenceExpressionImpl reference, String name) {
        DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(reference.getProject());
        GlobalSearchScope scope = reference.getResolveScope();
        Set<String> namespaces = new LinkedHashSet<>();
        for (PsiElement parent = reference.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof VisualBasicNamespaceDeclarationImpl namespace) {
                String qName = namespace.getPresentableQName();
                if (qName != null) {
                    namespaces.add(qName);
                }
            }
        }
        namespaces.add("");

        List<DotNetTypeDeclaration> importedTypes = new ArrayList<>();
        for (VisualBasicImport imported : VisualBasicImport.getImports(reference)) {
            if (imported.alias() != null) {
                continue;
            }
            DotNetTypeDeclaration type = searcher.findType(imported.qName(), scope);
            if (type != null) {
                importedTypes.add(type);
            }
            else {
                namespaces.add(imported.qName());
            }
        }

        List<VisualBasicResolveResult> results = new ArrayList<>();
        for (String qName : namespaces) {
            DotNetNamespaceAsElement namespace = searcher.findNamespace(qName, scope);
            if (namespace == null) {
                continue;
            }
            for (PsiElement child : namespace.getChildren(scope, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS)) {
                if (child instanceof DotNetTypeDeclaration type && isModule(type)) {
                    results.addAll(members(type, DotNetGenericExtractor.EMPTY, name, reference));
                }
            }
            if (!results.isEmpty()) {
                return results;
            }
        }
        for (DotNetTypeDeclaration type : importedTypes) {
            results.addAll(members(type, DotNetGenericExtractor.EMPTY, name, reference));
        }
        return results;
    }

    @RequiredReadAction
    private static boolean isModule(DotNetTypeDeclaration type) {
        if (type instanceof VisualBasicTypeDeclarationImpl visualBasicType) {
            return visualBasicType.getKind() == VisualBasicTypeKind.MODULE;
        }
        DotNetModifierList modifierList = type.getModifierList();
        if (modifierList == null) {
            return false;
        }
        for (DotNetAttribute attribute : modifierList.getAttributes()) {
            DotNetTypeDeclaration attributeType = attribute.resolveToType();
            if (attributeType != null && STANDARD_MODULE_ATTRIBUTE.equals(attributeType.getVmQName())) {
                return true;
            }
        }
        return false;
    }

    // overloads

    /**
     * Among methods, the ones callable with the arguments of the invocation, the best first: methods taking as many
     * arguments, then the ones whose parameter types match the argument types.
     * <p>
     * TODO [VISTALL] overload resolution of the language specification: widening conversions, generic inference
     */
    @RequiredReadAction
    private static List<VisualBasicResolveResult> chooseOverload(VisualBasicReferenceExpressionImpl reference, List<VisualBasicResolveResult> results) {
        if (results.size() < 2 || !(results.get(0).getElement() instanceof DotNetLikeMethodDeclaration)) {
            return results;
        }
        List<PsiElement> arguments = VisualBasicResolveUtil.getInvocationArguments(reference);
        int argumentCount = arguments == null ? 0 : arguments.size();
        VisualBasicResolveResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (VisualBasicResolveResult result : results) {
            if (!(result.getElement() instanceof DotNetLikeMethodDeclaration method)) {
                continue;
            }
            int score = score(method, arguments, argumentCount);
            if (score > bestScore) {
                bestScore = score;
                best = result;
            }
        }
        return best == null ? results : List.of(best);
    }

    @RequiredReadAction
    private static int score(DotNetLikeMethodDeclaration method, @Nullable List<PsiElement> arguments, int argumentCount) {
        DotNetParameter[] parameters = method.getParameters();
        int required = 0;
        boolean paramArray = false;
        for (DotNetParameter parameter : parameters) {
            boolean optional = parameter.getInitializer() != null || parameter.hasModifier(VisualBasicModifier.OPTIONAL);
            boolean array = parameter.hasModifier(VisualBasicModifier.PARAMARRAY) || isParamArray(parameter);
            paramArray |= array;
            if (!optional && !array) {
                required++;
            }
        }
        boolean applicable = argumentCount >= required && (argumentCount <= parameters.length || paramArray);
        if (!applicable) {
            return -1000 + (parameters.length == argumentCount ? 1 : 0);
        }
        int score = parameters.length == argumentCount ? 100 : 50;
        if (arguments != null) {
            for (int i = 0; i < arguments.size() && i < parameters.length; i++) {
                String argumentType = VisualBasicExpressionTypes.typeOf(arguments.get(i)).getVmQName();
                String parameterType = parameters[i].toTypeRef(true).getVmQName();
                if (argumentType.equals(parameterType)) {
                    score += 10;
                }
                else if (parameterType.equals(DotNetTypes.System.Object)) {
                    score += 1;
                }
            }
        }
        return score;
    }

    @RequiredReadAction
    private static boolean isParamArray(DotNetParameter parameter) {
        DotNetModifierList modifierList = parameter.getModifierList();
        if (modifierList == null) {
            return false;
        }
        for (DotNetAttribute attribute : modifierList.getAttributes()) {
            DotNetTypeDeclaration type = attribute.resolveToType();
            if (type != null && "System.ParamArrayAttribute".equals(type.getVmQName())) {
                return true;
            }
        }
        return false;
    }
}
