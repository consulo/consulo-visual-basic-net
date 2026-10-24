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

import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicAccessorImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicArrayTypeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicAttributeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicAttributeListImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicExpressionImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicGenericParameterImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicGenericParameterListImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicImportsClauseImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicImportsStatementImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicLocalVariableImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicModifierListImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicNullableTypeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicParameterImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicParameterListImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicPredefinedTypeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicReferenceExpressionImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTupleTypeImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicTypeArgumentListImpl;
import consulo.visualBasic.dotnet.impl.lang.psi.impl.VisualBasicUserTypeImpl;

/**
 * Element types. Declarations are stubbed, see {@link VisualBasicStubElements}.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public interface VisualBasicElements {
    IFileElementType FILE = VisualBasicStubElements.FILE;

    // declarations
    IElementType OPTION_STATEMENT = new VisualBasicElementType("OPTION_STATEMENT");
    IElementType IMPORTS_STATEMENT = new VisualBasicElementType("IMPORTS_STATEMENT", VisualBasicImportsStatementImpl::new);
    IElementType IMPORTS_CLAUSE = new VisualBasicElementType("IMPORTS_CLAUSE", VisualBasicImportsClauseImpl::new);
    IElementType NAMESPACE_DECLARATION = VisualBasicStubElements.NAMESPACE_DECLARATION;
    IElementType TYPE_DECLARATION = VisualBasicStubElements.TYPE_DECLARATION;
    IElementType ENUM_DECLARATION = VisualBasicStubElements.ENUM_DECLARATION;
    IElementType ENUM_MEMBER_DECLARATION = VisualBasicStubElements.ENUM_MEMBER_DECLARATION;
    IElementType DELEGATE_DECLARATION = VisualBasicStubElements.DELEGATE_DECLARATION;
    IElementType INHERITS_STATEMENT = new VisualBasicElementType("INHERITS_STATEMENT");
    IElementType IMPLEMENTS_STATEMENT = new VisualBasicElementType("IMPLEMENTS_STATEMENT");
    IElementType METHOD_DECLARATION = VisualBasicStubElements.METHOD_DECLARATION;
    IElementType CONSTRUCTOR_DECLARATION = VisualBasicStubElements.CONSTRUCTOR_DECLARATION;
    IElementType OPERATOR_DECLARATION = VisualBasicStubElements.OPERATOR_DECLARATION;
    IElementType EXTERNAL_METHOD_DECLARATION = VisualBasicStubElements.EXTERNAL_METHOD_DECLARATION;
    IElementType PROPERTY_DECLARATION = VisualBasicStubElements.PROPERTY_DECLARATION;
    IElementType EVENT_DECLARATION = VisualBasicStubElements.EVENT_DECLARATION;
    IElementType ACCESSOR_DECLARATION = new VisualBasicElementType("ACCESSOR_DECLARATION", VisualBasicAccessorImpl::new);
    IElementType FIELD_DECLARATION_STATEMENT = new VisualBasicElementType("FIELD_DECLARATION_STATEMENT");
    IElementType VARIABLE_DECLARATOR = new VisualBasicElementType("VARIABLE_DECLARATOR");
    IElementType FIELD_DECLARATION = VisualBasicStubElements.FIELD_DECLARATION;
    IElementType MODIFIER_LIST = new VisualBasicElementType("MODIFIER_LIST", VisualBasicModifierListImpl::new);
    IElementType ATTRIBUTE_LIST = new VisualBasicElementType("ATTRIBUTE_LIST", VisualBasicAttributeListImpl::new);
    IElementType ATTRIBUTE = new VisualBasicElementType("ATTRIBUTE", VisualBasicAttributeImpl::new);
    IElementType ATTRIBUTE_TARGET = new VisualBasicElementType("ATTRIBUTE_TARGET");
    IElementType PARAMETER_LIST = new VisualBasicElementType("PARAMETER_LIST", VisualBasicParameterListImpl::new);
    IElementType PARAMETER = new VisualBasicElementType("PARAMETER", VisualBasicParameterImpl::new);
    IElementType GENERIC_PARAMETER_LIST = new VisualBasicElementType("GENERIC_PARAMETER_LIST", VisualBasicGenericParameterListImpl::new);
    IElementType GENERIC_PARAMETER = new VisualBasicElementType("GENERIC_PARAMETER", VisualBasicGenericParameterImpl::new);
    IElementType HANDLES_CLAUSE = new VisualBasicElementType("HANDLES_CLAUSE");
    IElementType IMPLEMENTS_CLAUSE = new VisualBasicElementType("IMPLEMENTS_CLAUSE");
    IElementType ARRAY_BOUNDS = new VisualBasicElementType("ARRAY_BOUNDS");
    IElementType LOCAL_DECLARATION_STATEMENT = new VisualBasicElementType("LOCAL_DECLARATION_STATEMENT");
    IElementType LOCAL_VARIABLE = new VisualBasicElementType("LOCAL_VARIABLE", VisualBasicLocalVariableImpl::new);

    // types
    IElementType PREDEFINED_TYPE = new VisualBasicElementType("PREDEFINED_TYPE", VisualBasicPredefinedTypeImpl::new);
    IElementType USER_TYPE = new VisualBasicElementType("USER_TYPE", VisualBasicUserTypeImpl::new);
    IElementType ARRAY_TYPE = new VisualBasicElementType("ARRAY_TYPE", VisualBasicArrayTypeImpl::new);
    IElementType NULLABLE_TYPE = new VisualBasicElementType("NULLABLE_TYPE", VisualBasicNullableTypeImpl::new);
    IElementType TUPLE_TYPE = new VisualBasicElementType("TUPLE_TYPE", VisualBasicTupleTypeImpl::new);
    IElementType TUPLE_TYPE_ELEMENT = new VisualBasicElementType("TUPLE_TYPE_ELEMENT");
    IElementType TYPE_ARGUMENT_LIST = new VisualBasicElementType("TYPE_ARGUMENT_LIST", VisualBasicTypeArgumentListImpl::new);
    IElementType ARRAY_RANK_SPECIFIER = new VisualBasicElementType("ARRAY_RANK_SPECIFIER");

    // statements
    IElementType CODE_BLOCK = new VisualBasicElementType("CODE_BLOCK");
    IElementType EXPRESSION_STATEMENT = new VisualBasicElementType("EXPRESSION_STATEMENT");
    IElementType ASSIGNMENT_STATEMENT = new VisualBasicElementType("ASSIGNMENT_STATEMENT");
    IElementType CALL_STATEMENT = new VisualBasicElementType("CALL_STATEMENT");
    IElementType IF_STATEMENT = new VisualBasicElementType("IF_STATEMENT");
    IElementType ELSE_IF_CLAUSE = new VisualBasicElementType("ELSE_IF_CLAUSE");
    IElementType ELSE_CLAUSE = new VisualBasicElementType("ELSE_CLAUSE");
    IElementType SELECT_STATEMENT = new VisualBasicElementType("SELECT_STATEMENT");
    IElementType CASE_BLOCK = new VisualBasicElementType("CASE_BLOCK");
    IElementType SIMPLE_CASE_CLAUSE = new VisualBasicElementType("SIMPLE_CASE_CLAUSE");
    IElementType RANGE_CASE_CLAUSE = new VisualBasicElementType("RANGE_CASE_CLAUSE");
    IElementType RELATIONAL_CASE_CLAUSE = new VisualBasicElementType("RELATIONAL_CASE_CLAUSE");
    IElementType WHILE_STATEMENT = new VisualBasicElementType("WHILE_STATEMENT");
    IElementType DO_LOOP_STATEMENT = new VisualBasicElementType("DO_LOOP_STATEMENT");
    IElementType LOOP_CONDITION = new VisualBasicElementType("LOOP_CONDITION");
    IElementType FOR_STATEMENT = new VisualBasicElementType("FOR_STATEMENT");
    IElementType FOR_EACH_STATEMENT = new VisualBasicElementType("FOR_EACH_STATEMENT");
    IElementType TRY_STATEMENT = new VisualBasicElementType("TRY_STATEMENT");
    IElementType CATCH_CLAUSE = new VisualBasicElementType("CATCH_CLAUSE");
    IElementType FINALLY_CLAUSE = new VisualBasicElementType("FINALLY_CLAUSE");
    IElementType USING_STATEMENT = new VisualBasicElementType("USING_STATEMENT");
    IElementType SYNC_LOCK_STATEMENT = new VisualBasicElementType("SYNC_LOCK_STATEMENT");
    IElementType WITH_STATEMENT = new VisualBasicElementType("WITH_STATEMENT");
    IElementType RETURN_STATEMENT = new VisualBasicElementType("RETURN_STATEMENT");
    IElementType EXIT_STATEMENT = new VisualBasicElementType("EXIT_STATEMENT");
    IElementType CONTINUE_STATEMENT = new VisualBasicElementType("CONTINUE_STATEMENT");
    IElementType THROW_STATEMENT = new VisualBasicElementType("THROW_STATEMENT");
    IElementType GOTO_STATEMENT = new VisualBasicElementType("GOTO_STATEMENT");
    IElementType LABEL_STATEMENT = new VisualBasicElementType("LABEL_STATEMENT");
    IElementType STOP_STATEMENT = new VisualBasicElementType("STOP_STATEMENT");
    IElementType END_STATEMENT = new VisualBasicElementType("END_STATEMENT");
    IElementType ON_ERROR_STATEMENT = new VisualBasicElementType("ON_ERROR_STATEMENT");
    IElementType RESUME_STATEMENT = new VisualBasicElementType("RESUME_STATEMENT");
    IElementType ERROR_STATEMENT = new VisualBasicElementType("ERROR_STATEMENT");
    IElementType RAISE_EVENT_STATEMENT = new VisualBasicElementType("RAISE_EVENT_STATEMENT");
    IElementType ADD_HANDLER_STATEMENT = new VisualBasicElementType("ADD_HANDLER_STATEMENT");
    IElementType REMOVE_HANDLER_STATEMENT = new VisualBasicElementType("REMOVE_HANDLER_STATEMENT");
    IElementType ERASE_STATEMENT = new VisualBasicElementType("ERASE_STATEMENT");
    IElementType REDIM_STATEMENT = new VisualBasicElementType("REDIM_STATEMENT");
    IElementType REDIM_CLAUSE = new VisualBasicElementType("REDIM_CLAUSE");
    IElementType YIELD_STATEMENT = new VisualBasicElementType("YIELD_STATEMENT");
    IElementType MID_ASSIGNMENT_STATEMENT = new VisualBasicElementType("MID_ASSIGNMENT_STATEMENT");

    // expressions
    IElementType LITERAL_EXPRESSION = new VisualBasicElementType("LITERAL_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType INTERPOLATED_STRING_EXPRESSION = new VisualBasicElementType("INTERPOLATED_STRING_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType INTERPOLATION = new VisualBasicElementType("INTERPOLATION");
    IElementType INTERPOLATION_ALIGNMENT = new VisualBasicElementType("INTERPOLATION_ALIGNMENT");
    IElementType INTERPOLATION_FORMAT_CLAUSE = new VisualBasicElementType("INTERPOLATION_FORMAT_CLAUSE");
    IElementType REFERENCE_EXPRESSION = new VisualBasicElementType("REFERENCE_EXPRESSION", VisualBasicReferenceExpressionImpl::new);
    IElementType INSTANCE_EXPRESSION = new VisualBasicElementType("INSTANCE_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType PARENTHESIZED_EXPRESSION = new VisualBasicElementType("PARENTHESIZED_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType TUPLE_EXPRESSION = new VisualBasicElementType("TUPLE_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType INVOCATION_EXPRESSION = new VisualBasicElementType("INVOCATION_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType ARGUMENT_LIST = new VisualBasicElementType("ARGUMENT_LIST");
    IElementType SIMPLE_ARGUMENT = new VisualBasicElementType("SIMPLE_ARGUMENT");
    IElementType NAMED_ARGUMENT = new VisualBasicElementType("NAMED_ARGUMENT");
    IElementType OMITTED_ARGUMENT = new VisualBasicElementType("OMITTED_ARGUMENT");
    IElementType RANGE_ARGUMENT = new VisualBasicElementType("RANGE_ARGUMENT");
    IElementType DICTIONARY_ACCESS_EXPRESSION = new VisualBasicElementType("DICTIONARY_ACCESS_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType BINARY_EXPRESSION = new VisualBasicElementType("BINARY_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType PREFIX_EXPRESSION = new VisualBasicElementType("PREFIX_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType AWAIT_EXPRESSION = new VisualBasicElementType("AWAIT_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType ADDRESS_OF_EXPRESSION = new VisualBasicElementType("ADDRESS_OF_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType TYPE_OF_EXPRESSION = new VisualBasicElementType("TYPE_OF_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType GET_TYPE_EXPRESSION = new VisualBasicElementType("GET_TYPE_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType NAME_OF_EXPRESSION = new VisualBasicElementType("NAME_OF_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType GET_XML_NAMESPACE_EXPRESSION = new VisualBasicElementType("GET_XML_NAMESPACE_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType XML_LITERAL_EXPRESSION = new VisualBasicElementType("XML_LITERAL_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType XML_EMBEDDED_EXPRESSION = new VisualBasicElementType("XML_EMBEDDED_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType XML_MEMBER_ACCESS_EXPRESSION = new VisualBasicElementType("XML_MEMBER_ACCESS_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType XML_NAME = new VisualBasicElementType("XML_NAME");
    IElementType CAST_EXPRESSION = new VisualBasicElementType("CAST_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType PREDEFINED_CAST_EXPRESSION = new VisualBasicElementType("PREDEFINED_CAST_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType IF_EXPRESSION = new VisualBasicElementType("IF_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType NEW_EXPRESSION = new VisualBasicElementType("NEW_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType ARRAY_CREATION_EXPRESSION = new VisualBasicElementType("ARRAY_CREATION_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType ANONYMOUS_OBJECT_CREATION_EXPRESSION = new VisualBasicElementType("ANONYMOUS_OBJECT_CREATION_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType OBJECT_INITIALIZER = new VisualBasicElementType("OBJECT_INITIALIZER");
    IElementType FIELD_INITIALIZER = new VisualBasicElementType("FIELD_INITIALIZER");
    IElementType COLLECTION_INITIALIZER = new VisualBasicElementType("COLLECTION_INITIALIZER", VisualBasicExpressionImpl::new);
    IElementType LAMBDA_EXPRESSION = new VisualBasicElementType("LAMBDA_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType TYPE_EXPRESSION = new VisualBasicElementType("TYPE_EXPRESSION", VisualBasicExpressionImpl::new);

    // queries
    IElementType QUERY_EXPRESSION = new VisualBasicElementType("QUERY_EXPRESSION", VisualBasicExpressionImpl::new);
    IElementType FROM_CLAUSE = new VisualBasicElementType("FROM_CLAUSE");
    IElementType AGGREGATE_CLAUSE = new VisualBasicElementType("AGGREGATE_CLAUSE");
    IElementType WHERE_CLAUSE = new VisualBasicElementType("WHERE_CLAUSE");
    IElementType SELECT_CLAUSE = new VisualBasicElementType("SELECT_CLAUSE");
    IElementType ORDER_BY_CLAUSE = new VisualBasicElementType("ORDER_BY_CLAUSE");
    IElementType ORDERING = new VisualBasicElementType("ORDERING");
    IElementType LET_CLAUSE = new VisualBasicElementType("LET_CLAUSE");
    IElementType DISTINCT_CLAUSE = new VisualBasicElementType("DISTINCT_CLAUSE");
    IElementType PARTITION_CLAUSE = new VisualBasicElementType("PARTITION_CLAUSE");
    IElementType PARTITION_WHILE_CLAUSE = new VisualBasicElementType("PARTITION_WHILE_CLAUSE");
    IElementType JOIN_CLAUSE = new VisualBasicElementType("JOIN_CLAUSE");
    IElementType JOIN_CONDITION = new VisualBasicElementType("JOIN_CONDITION");
    IElementType GROUP_BY_CLAUSE = new VisualBasicElementType("GROUP_BY_CLAUSE");
    IElementType GROUP_JOIN_CLAUSE = new VisualBasicElementType("GROUP_JOIN_CLAUSE");
    IElementType COLLECTION_RANGE_VARIABLE = new VisualBasicElementType("COLLECTION_RANGE_VARIABLE");
    IElementType EXPRESSION_RANGE_VARIABLE = new VisualBasicElementType("EXPRESSION_RANGE_VARIABLE");
    IElementType AGGREGATION_RANGE_VARIABLE = new VisualBasicElementType("AGGREGATION_RANGE_VARIABLE");

    TokenSet TYPES = TokenSet.create(
        PREDEFINED_TYPE,
        USER_TYPE,
        ARRAY_TYPE,
        NULLABLE_TYPE,
        TUPLE_TYPE
    );

    TokenSet STATEMENTS = TokenSet.create(
        EXPRESSION_STATEMENT,
        ASSIGNMENT_STATEMENT,
        CALL_STATEMENT,
        IF_STATEMENT,
        SELECT_STATEMENT,
        WHILE_STATEMENT,
        DO_LOOP_STATEMENT,
        FOR_STATEMENT,
        FOR_EACH_STATEMENT,
        TRY_STATEMENT,
        USING_STATEMENT,
        SYNC_LOCK_STATEMENT,
        WITH_STATEMENT,
        RETURN_STATEMENT,
        EXIT_STATEMENT,
        CONTINUE_STATEMENT,
        THROW_STATEMENT,
        GOTO_STATEMENT,
        LABEL_STATEMENT,
        STOP_STATEMENT,
        END_STATEMENT,
        ON_ERROR_STATEMENT,
        RESUME_STATEMENT,
        ERROR_STATEMENT,
        RAISE_EVENT_STATEMENT,
        ADD_HANDLER_STATEMENT,
        REMOVE_HANDLER_STATEMENT,
        ERASE_STATEMENT,
        REDIM_STATEMENT,
        YIELD_STATEMENT,
        MID_ASSIGNMENT_STATEMENT,
        LOCAL_DECLARATION_STATEMENT
    );

    TokenSet EXPRESSIONS = TokenSet.create(
        LITERAL_EXPRESSION,
        INTERPOLATED_STRING_EXPRESSION,
        REFERENCE_EXPRESSION,
        INSTANCE_EXPRESSION,
        PARENTHESIZED_EXPRESSION,
        TUPLE_EXPRESSION,
        INVOCATION_EXPRESSION,
        DICTIONARY_ACCESS_EXPRESSION,
        BINARY_EXPRESSION,
        PREFIX_EXPRESSION,
        AWAIT_EXPRESSION,
        ADDRESS_OF_EXPRESSION,
        TYPE_OF_EXPRESSION,
        GET_TYPE_EXPRESSION,
        NAME_OF_EXPRESSION,
        GET_XML_NAMESPACE_EXPRESSION,
        XML_LITERAL_EXPRESSION,
        XML_MEMBER_ACCESS_EXPRESSION,
        CAST_EXPRESSION,
        PREDEFINED_CAST_EXPRESSION,
        IF_EXPRESSION,
        NEW_EXPRESSION,
        ARRAY_CREATION_EXPRESSION,
        ANONYMOUS_OBJECT_CREATION_EXPRESSION,
        LAMBDA_EXPRESSION,
        TYPE_EXPRESSION,
        QUERY_EXPRESSION
    );

}
