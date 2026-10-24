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
import consulo.application.util.RecursionManager;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetArrayTypeRef;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.application.util.CachedValueProvider;
import consulo.language.psi.PsiModificationTracker;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicElements;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTypeKind;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicLiteralTypes;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicPsiUtil;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicReferenceExpressionImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeDeclarationImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.DotNetMemberLookup;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.DotNetTypeHierarchy;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.DotNetTypeRefSubstitutor;
import consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet.NameMatcher;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Types of expressions, by the rules of Visual Basic.
 * <p>
 * TODO [VISTALL] lambdas, queries, anonymous types, generic method inference, user conversions
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicExpressionTypes {
    private static final Map<IElementType, String> PREDEFINED_CASTS = Map.ofEntries(
        Map.entry(VisualBasicTokens.CBOOL_KEYWORD, DotNetTypes.System.Boolean),
        Map.entry(VisualBasicTokens.CBYTE_KEYWORD, DotNetTypes.System.Byte),
        Map.entry(VisualBasicTokens.CCHAR_KEYWORD, DotNetTypes.System.Char),
        Map.entry(VisualBasicTokens.CDATE_KEYWORD, "System.DateTime"),
        Map.entry(VisualBasicTokens.CDEC_KEYWORD, DotNetTypes.System.Decimal),
        Map.entry(VisualBasicTokens.CDBL_KEYWORD, DotNetTypes.System.Double),
        Map.entry(VisualBasicTokens.CINT_KEYWORD, DotNetTypes.System.Int32),
        Map.entry(VisualBasicTokens.CLNG_KEYWORD, DotNetTypes.System.Int64),
        Map.entry(VisualBasicTokens.COBJ_KEYWORD, DotNetTypes.System.Object),
        Map.entry(VisualBasicTokens.CSBYTE_KEYWORD, DotNetTypes.System.SByte),
        Map.entry(VisualBasicTokens.CSHORT_KEYWORD, DotNetTypes.System.Int16),
        Map.entry(VisualBasicTokens.CSNG_KEYWORD, DotNetTypes.System.Single),
        Map.entry(VisualBasicTokens.CSTR_KEYWORD, DotNetTypes.System.String),
        Map.entry(VisualBasicTokens.CUINT_KEYWORD, DotNetTypes.System.UInt32),
        Map.entry(VisualBasicTokens.CULNG_KEYWORD, DotNetTypes.System.UInt64),
        Map.entry(VisualBasicTokens.CUSHORT_KEYWORD, DotNetTypes.System.UInt16)
    );

    private VisualBasicExpressionTypes() {
    }

    @RequiredReadAction
    public static DotNetTypeRef typeOf(PsiElement expression) {
        return LanguageCachedValueUtil.getCachedValue(expression, () -> {
            DotNetTypeRef typeRef = RecursionManager.doPreventingRecursion(expression, false, () -> compute(expression));
            return CachedValueProvider.Result.create(typeRef == null ? DotNetTypeRef.ERROR_TYPE : typeRef, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    private static DotNetTypeRef byName(PsiElement context, String vmQName) {
        return new VisualBasicTypeRefByQName(context, vmQName);
    }

    @RequiredReadAction
    private static DotNetTypeRef compute(PsiElement expression) {
        IElementType type = expression.getNode().getElementType();
        if (type == VisualBasicElements.LITERAL_EXPRESSION) {
            String vmQName = VisualBasicLiteralTypes.getVmQName(expression);
            return vmQName == null ? DotNetTypeRef.ERROR_TYPE : byName(expression, vmQName);
        }
        if (type == VisualBasicElements.INTERPOLATED_STRING_EXPRESSION || type == VisualBasicElements.NAME_OF_EXPRESSION) {
            return byName(expression, DotNetTypes.System.String);
        }
        if (type == VisualBasicElements.TYPE_OF_EXPRESSION) {
            return byName(expression, DotNetTypes.System.Boolean);
        }
        if (type == VisualBasicElements.GET_TYPE_EXPRESSION) {
            return byName(expression, DotNetTypes.System.Type);
        }
        if (type == VisualBasicElements.GET_XML_NAMESPACE_EXPRESSION) {
            return byName(expression, "System.Xml.Linq.XNamespace");
        }
        if (type == VisualBasicElements.PARENTHESIZED_EXPRESSION) {
            PsiElement inner = firstExpression(expression);
            return inner == null ? DotNetTypeRef.ERROR_TYPE : typeOf(inner);
        }
        if (type == VisualBasicElements.REFERENCE_EXPRESSION && expression instanceof VisualBasicReferenceExpressionImpl reference) {
            return typeOfReference(reference);
        }
        if (type == VisualBasicElements.INVOCATION_EXPRESSION) {
            return typeOfInvocation(expression);
        }
        if (type == VisualBasicElements.DICTIONARY_ACCESS_EXPRESSION) {
            PsiElement qualifier = firstExpression(expression);
            return qualifier == null ? DotNetTypeRef.ERROR_TYPE : defaultPropertyType(typeOf(qualifier), expression);
        }
        if (type == VisualBasicElements.INSTANCE_EXPRESSION) {
            return typeOfInstance(expression);
        }
        if (type == VisualBasicElements.TYPE_EXPRESSION) {
            DotNetType predefined = PsiTreeUtil.getChildOfType(expression, DotNetType.class);
            return predefined == null ? DotNetTypeRef.ERROR_TYPE : predefined.toTypeRef();
        }
        if (type == VisualBasicElements.NEW_EXPRESSION || type == VisualBasicElements.CAST_EXPRESSION) {
            DotNetType createdType = PsiTreeUtil.getChildOfType(expression, DotNetType.class);
            return createdType == null ? DotNetTypeRef.ERROR_TYPE : createdType.toTypeRef();
        }
        if (type == VisualBasicElements.ARRAY_CREATION_EXPRESSION) {
            return typeOfArrayCreation(expression);
        }
        if (type == VisualBasicElements.COLLECTION_INITIALIZER) {
            // TODO [VISTALL] dominant type of the elements
            PsiElement first = firstExpression(expression);
            DotNetTypeRef element = first == null ? byName(expression, DotNetTypes.System.Object) : typeOf(first);
            return new VisualBasicArrayTypeRef(expression, element, 1);
        }
        if (type == VisualBasicElements.PREDEFINED_CAST_EXPRESSION) {
            IElementType keyword = firstTokenType(expression);
            String vmQName = keyword == null ? null : PREDEFINED_CASTS.get(keyword);
            return vmQName == null ? DotNetTypeRef.ERROR_TYPE : byName(expression, vmQName);
        }
        if (type == VisualBasicElements.IF_EXPRESSION) {
            return typeOfIf(expression);
        }
        if (type == VisualBasicElements.BINARY_EXPRESSION) {
            return typeOfBinary(expression);
        }
        if (type == VisualBasicElements.PREFIX_EXPRESSION) {
            PsiElement operand = firstExpression(expression);
            if (operand == null) {
                return DotNetTypeRef.ERROR_TYPE;
            }
            DotNetTypeRef operandType = typeOf(operand);
            // Not of a non numeric type is Boolean
            if (firstTokenType(expression) == VisualBasicTokens.NOT_KEYWORD
                && !VisualBasicPrimitiveTypes.isIntegral(operandType.getVmQName())) {
                return byName(expression, DotNetTypes.System.Boolean);
            }
            return operandType;
        }
        if (type == VisualBasicElements.AWAIT_EXPRESSION) {
            PsiElement operand = firstExpression(expression);
            return operand == null ? DotNetTypeRef.ERROR_TYPE : awaitedType(typeOf(operand), expression);
        }
        if (type == VisualBasicElements.TUPLE_EXPRESSION) {
            List<DotNetTypeRef> elements = new ArrayList<>();
            for (PsiElement child = expression.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child instanceof DotNetExpression) {
                    elements.add(typeOf(child));
                }
                else if (child.getNode().getElementType() == VisualBasicElements.NAMED_ARGUMENT) {
                    PsiElement value = VisualBasicResolveUtil.getArgumentExpression(child);
                    elements.add(value == null ? DotNetTypeRef.ERROR_TYPE : typeOf(value));
                }
            }
            return VisualBasicTypeResolver.tuple(expression, elements);
        }
        if (type == VisualBasicElements.XML_LITERAL_EXPRESSION) {
            return typeOfXmlLiteral(expression);
        }
        if (type == VisualBasicElements.XML_MEMBER_ACCESS_EXPRESSION) {
            if (expression.getNode().findChildByType(VisualBasicTokens.AT) != null) {
                return byName(expression, DotNetTypes.System.String);
            }
            DotNetTypeRef element = byName(expression, "System.Xml.Linq.XElement");
            return new VisualBasicGenericWrapperTypeRef(expression, byName(expression, "System.Collections.Generic.IEnumerable`1"),
                new DotNetTypeRef[]{element});
        }
        return DotNetTypeRef.ERROR_TYPE;
    }

    private static @Nullable IElementType firstTokenType(PsiElement element) {
        PsiElement first = element.getFirstChild();
        return first == null ? null : first.getNode().getElementType();
    }

    private static @Nullable PsiElement firstExpression(PsiElement element) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetExpression) {
                return child;
            }
        }
        return null;
    }

    private static List<PsiElement> expressions(PsiElement element) {
        List<PsiElement> result = new ArrayList<>();
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetExpression) {
                result.add(child);
            }
        }
        return result;
    }

    // references and invocations

    @RequiredReadAction
    private static DotNetTypeRef typeOfReference(VisualBasicReferenceExpressionImpl reference) {
        ResolveResult[] results = reference.multiResolve(false);
        if (results.length == 0) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        PsiElement element = results[0].getElement();
        DotNetGenericExtractor extractor = results[0] instanceof VisualBasicResolveResult result ? result.getExtractor() : DotNetGenericExtractor.EMPTY;
        return element == null ? DotNetTypeRef.ERROR_TYPE : typeOfMember(element, extractor, reference);
    }

    /**
     * Type of a resolved name: of a variable, the return type of a method (Visual Basic calls a method named without
     * parentheses), a type for a type name.
     */
    @RequiredReadAction
    public static DotNetTypeRef typeOfMember(PsiElement element, DotNetGenericExtractor extractor, PsiElement context) {
        DotNetTypeRef typeRef;
        if (element instanceof DotNetVariable variable) {
            typeRef = variable.toTypeRef(true);
        }
        else if (element instanceof DotNetLikeMethodDeclaration method) {
            typeRef = method.getReturnTypeRef();
        }
        else if (element instanceof DotNetTypeDeclaration typeDeclaration) {
            return new VisualBasicTypeRefByTypeDeclaration(typeDeclaration);
        }
        else {
            return DotNetTypeRef.ERROR_TYPE;
        }
        return DotNetTypeRefSubstitutor.substitute(typeRef, extractor, new VisualBasicTypeRefFactory(context));
    }

    @RequiredReadAction
    private static DotNetTypeRef typeOfInvocation(PsiElement invocation) {
        PsiElement callee = invocation.getFirstChild();
        if (callee instanceof VisualBasicReferenceExpressionImpl reference) {
            ResolveResult[] results = reference.multiResolve(false);
            if (results.length > 0 && (results[0].getElement() instanceof DotNetLikeMethodDeclaration || results[0].getElement() instanceof DotNetPropertyDeclaration
                && hasParameters(results[0].getElement()))) {
                // a call, or a parameterized property
                return typeOfReference(reference);
            }
        }
        if (callee == null || !(callee instanceof DotNetExpression)) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        DotNetTypeRef calleeType = typeOf(callee);
        // array element
        if (calleeType instanceof DotNetArrayTypeRef array) {
            return array.getInnerTypeRef();
        }
        PsiElement calleeTypeElement = calleeType.resolve().getElement();
        if (calleeTypeElement instanceof VisualBasicTypeDeclarationImpl delegate && delegate.getKind() == VisualBasicTypeKind.DELEGATE) {
            DotNetType returnType = VisualBasicPsiUtil.getTypeAfterAs(delegate);
            return returnType == null ? byName(invocation, DotNetTypes.System.Void) : returnType.toTypeRef();
        }
        if (calleeTypeElement instanceof DotNetTypeDeclaration typeDeclaration && typeDeclaration.isInheritor(DotNetTypes.System.MulticastDelegate, true)) {
            List<VisualBasicResolveResult> invoke = VisualBasicExpressionResolver.membersOfType(calleeType, "Invoke", invocation);
            if (!invoke.isEmpty()) {
                return typeOfMember(invoke.get(0).getElement(), invoke.get(0).getExtractor(), invocation);
            }
        }
        return defaultPropertyType(calleeType, invocation);
    }

    private static boolean hasParameters(@Nullable PsiElement property) {
        return property != null && property.getNode() != null
            && property.getNode().findChildByType(VisualBasicElements.PARAMETER_LIST) != null;
    }

    /**
     * {@code list(0)}, {@code dictionary!key}: the default property, {@code Item} or {@code Chars} of {@code String}.
     */
    @RequiredReadAction
    private static DotNetTypeRef defaultPropertyType(DotNetTypeRef typeRef, PsiElement context) {
        if (DotNetTypes.System.String.equals(typeRef.getVmQName())) {
            return byName(context, DotNetTypes.System.Char);
        }
        // TODO [VISTALL] DefaultMemberAttribute and Default properties of Visual Basic
        for (VisualBasicResolveResult result : VisualBasicExpressionResolver.membersOfType(typeRef, "Item", context)) {
            if (result.getElement() instanceof DotNetPropertyDeclaration) {
                return typeOfMember(result.getElement(), result.getExtractor(), context);
            }
        }
        return DotNetTypeRef.ERROR_TYPE;
    }

    @RequiredReadAction
    private static DotNetTypeRef typeOfInstance(PsiElement expression) {
        VisualBasicTypeDeclarationImpl type = PsiTreeUtil.getParentOfType(expression, VisualBasicTypeDeclarationImpl.class);
        if (type == null) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        if (expression.getNode().findChildByType(VisualBasicTokens.MYBASE_KEYWORD) != null) {
            DotNetTypeRef[] extendTypeRefs = type.getExtendTypeRefs();
            return extendTypeRefs.length == 0 ? DotNetTypeRef.ERROR_TYPE : extendTypeRefs[0];
        }
        return new VisualBasicTypeRefByTypeDeclaration(type);
    }

    @RequiredReadAction
    private static DotNetTypeRef typeOfArrayCreation(PsiElement expression) {
        DotNetType elementType = PsiTreeUtil.getChildOfType(expression, DotNetType.class);
        DotNetTypeRef typeRef = elementType == null ? DotNetTypeRef.ERROR_TYPE : elementType.toTypeRef();
        List<Integer> ranks = new ArrayList<>();
        for (PsiElement child = expression.getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType childType = child.getNode().getElementType();
            if (childType == VisualBasicElements.ARGUMENT_LIST || childType == VisualBasicElements.ARRAY_RANK_SPECIFIER) {
                ranks.add(VisualBasicTypeResolver.getRank(child));
            }
        }
        if (ranks.isEmpty()) {
            ranks.add(1);
        }
        // New Integer(1)() {} is an array of arrays: the last rank is the innermost
        for (int i = ranks.size() - 1; i >= 0; i--) {
            typeRef = new VisualBasicArrayTypeRef(expression, typeRef, ranks.get(i));
        }
        return typeRef;
    }

    /**
     * {@code If(c, a, b)} has the type of {@code a} and {@code b}, {@code If(a, b)} the one of {@code a}; a
     * {@code Nothing} branch takes the type of the other one.
     */
    @RequiredReadAction
    private static DotNetTypeRef typeOfIf(PsiElement expression) {
        List<PsiElement> operands = expressions(expression);
        if (operands.isEmpty()) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        List<PsiElement> branches = operands.size() >= 3 ? operands.subList(1, 3) : operands;
        DotNetTypeRef result = DotNetTypeRef.ERROR_TYPE;
        for (PsiElement branch : branches) {
            if (isNothing(branch)) {
                continue;
            }
            DotNetTypeRef branchType = typeOf(branch);
            if (result == DotNetTypeRef.ERROR_TYPE) {
                result = branchType;
            }
            else {
                String widened = VisualBasicPrimitiveTypes.widen(result.getVmQName(), branchType.getVmQName());
                if (widened != null) {
                    result = byName(expression, widened);
                }
            }
        }
        return result == DotNetTypeRef.ERROR_TYPE ? byName(expression, DotNetTypes.System.Object) : result;
    }

    private static boolean isNothing(PsiElement expression) {
        return expression.getNode().getElementType() == VisualBasicElements.LITERAL_EXPRESSION
            && expression.getNode().findChildByType(VisualBasicTokens.NOTHING_KEYWORD) != null;
    }

    // operators

    @RequiredReadAction
    private static DotNetTypeRef typeOfBinary(PsiElement expression) {
        List<PsiElement> operands = expressions(expression);
        if (operands.size() < 2) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        IElementType operator = null;
        for (PsiElement child = operands.get(0).getNextSibling(); child != null && child != operands.get(1); child = child.getNextSibling()) {
            IElementType childType = child.getNode().getElementType();
            if (!VisualBasicTokens.WHITESPACES.contains(childType) && !VisualBasicTokens.COMMENTS.contains(childType)) {
                operator = childType;
                break;
            }
        }
        if (operator == null) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        DotNetTypeRef left = typeOf(operands.get(0));
        DotNetTypeRef right = typeOf(operands.get(1));

        DotNetTypeRef userDefined = userDefinedOperator(operator, left, right, expression);
        if (userDefined != null) {
            return userDefined;
        }

        String leftName = left.getVmQName();
        String rightName = right.getVmQName();
        if (operator == VisualBasicTokens.ANDALSO_KEYWORD || operator == VisualBasicTokens.ORELSE_KEYWORD
            || VisualBasicExpressionOperators.isComparison(operator)) {
            return byName(expression, DotNetTypes.System.Boolean);
        }
        if (operator == VisualBasicTokens.AMPERSAND) {
            return byName(expression, DotNetTypes.System.String);
        }
        if (operator == VisualBasicTokens.CARET) {
            return byName(expression, DotNetTypes.System.Double);
        }
        if (operator == VisualBasicTokens.LTLT || operator == VisualBasicTokens.GTGT) {
            return left;
        }
        if (operator == VisualBasicTokens.AND_KEYWORD || operator == VisualBasicTokens.OR_KEYWORD || operator == VisualBasicTokens.XOR_KEYWORD) {
            if (DotNetTypes.System.Boolean.equals(leftName) && DotNetTypes.System.Boolean.equals(rightName)) {
                return byName(expression, DotNetTypes.System.Boolean);
            }
            String widened = VisualBasicPrimitiveTypes.widen(leftName, rightName);
            return widened == null ? DotNetTypeRef.ERROR_TYPE : byName(expression, widened);
        }
        if (operator == VisualBasicTokens.PLUS && (DotNetTypes.System.String.equals(leftName) || DotNetTypes.System.String.equals(rightName))) {
            return byName(expression, DotNetTypes.System.String);
        }
        String widened = VisualBasicPrimitiveTypes.widen(leftName, rightName);
        if (widened == null) {
            return DotNetTypeRef.ERROR_TYPE;
        }
        if (operator == VisualBasicTokens.SLASH && VisualBasicPrimitiveTypes.isIntegral(widened)) {
            // division of integers is Double
            return byName(expression, DotNetTypes.System.Double);
        }
        if (operator == VisualBasicTokens.BACKSLASH && !VisualBasicPrimitiveTypes.isIntegral(widened)) {
            return byName(expression, DotNetTypes.System.Int64);
        }
        return byName(expression, widened);
    }

    /**
     * {@code op_Addition} and others declared by the operand types.
     */
    @RequiredReadAction
    private static @Nullable DotNetTypeRef userDefinedOperator(IElementType operator, DotNetTypeRef left, DotNetTypeRef right, PsiElement context) {
        String name = VisualBasicExpressionOperators.getMetadataName(operator);
        if (name == null) {
            return null;
        }
        for (DotNetTypeRef operand : new DotNetTypeRef[]{left, right}) {
            if (VisualBasicPrimitiveTypes.isNumeric(operand.getVmQName()) || DotNetTypes.System.String.equals(operand.getVmQName())
                || DotNetTypes.System.Boolean.equals(operand.getVmQName())) {
                continue;
            }
            DotNetTypeResolveResult resolved = operand.resolve();
            if (!(resolved.getElement() instanceof DotNetTypeDeclaration type)) {
                continue;
            }
            for (DotNetMemberLookup.Member member : DotNetMemberLookup.findMembers(type, resolved.getGenericExtractor(), name, NameMatcher.CASE_SENSITIVE,
                it -> it instanceof DotNetLikeMethodDeclaration method && method.getParameters().length == 2, new VisualBasicTypeRefFactory(context))) {
                return typeOfMember(member.element(), member.extractor(), context);
            }
        }
        return null;
    }

    // Await, collections, XML

    /**
     * {@code Task(Of T)} gives {@code T}, other awaitables the result of {@code GetAwaiter().GetResult()}.
     */
    @RequiredReadAction
    private static DotNetTypeRef awaitedType(DotNetTypeRef typeRef, PsiElement context) {
        if (DotNetTypes.System.Threading.Tasks.Task.equals(typeRef.getVmQName())) {
            return byName(context, DotNetTypes.System.Void);
        }
        if (typeRef instanceof DotNetGenericWrapperTypeRef wrapper && wrapper.getArgumentTypeRefs().length == 1
            && DotNetTypes.System.Threading.Tasks.Task$1.equals(wrapper.getInnerTypeRef().getVmQName())) {
            return wrapper.getArgumentTypeRefs()[0];
        }
        for (VisualBasicResolveResult awaiter : VisualBasicExpressionResolver.membersOfType(typeRef, "GetAwaiter", context)) {
            DotNetTypeRef awaiterType = typeOfMember(awaiter.getElement(), awaiter.getExtractor(), context);
            for (VisualBasicResolveResult getResult : VisualBasicExpressionResolver.membersOfType(awaiterType, "GetResult", context)) {
                return typeOfMember(getResult.getElement(), getResult.getExtractor(), context);
            }
        }
        return DotNetTypeRef.ERROR_TYPE;
    }

    /**
     * Element type of a collection in {@code For Each}: of an array, {@code T} of {@code IEnumerable(Of T)}, {@code Char}
     * of a string, {@code Object} otherwise.
     */
    @RequiredReadAction
    public static DotNetTypeRef elementTypeOf(DotNetTypeRef collection, PsiElement context) {
        if (collection instanceof DotNetArrayTypeRef array) {
            return array.getInnerTypeRef();
        }
        if (DotNetTypes.System.String.equals(collection.getVmQName())) {
            return byName(context, DotNetTypes.System.Char);
        }
        DotNetTypeResolveResult resolved = collection.resolve();
        if (resolved.getElement() instanceof DotNetTypeDeclaration type) {
            for (DotNetTypeHierarchy.Entry entry : DotNetTypeHierarchy.getTypeAndSupertypes(type, resolved.getGenericExtractor(),
                new VisualBasicTypeRefFactory(context))) {
                if (DotNetTypes.System.Collections.Generic.IEnumerable$1.equals(entry.type().getVmQName())) {
                    DotNetTypeRef element = entry.extractor().extract(entry.type().getGenericParameters()[0]);
                    if (element != null) {
                        return element;
                    }
                }
            }
        }
        return byName(context, DotNetTypes.System.Object);
    }

    private static DotNetTypeRef typeOfXmlLiteral(PsiElement expression) {
        IElementType firstType = firstTokenType(expression);
        String vmQName;
        if (firstType == VisualBasicTokens.XML_COMMENT) {
            vmQName = "System.Xml.Linq.XComment";
        }
        else if (firstType == VisualBasicTokens.XML_CDATA) {
            vmQName = "System.Xml.Linq.XCData";
        }
        else if (firstType == VisualBasicTokens.XML_PI) {
            vmQName = expression.getText().startsWith("<?xml") ? "System.Xml.Linq.XDocument" : "System.Xml.Linq.XProcessingInstruction";
        }
        else {
            vmQName = "System.Xml.Linq.XElement";
        }
        return byName(expression, vmQName);
    }
}
