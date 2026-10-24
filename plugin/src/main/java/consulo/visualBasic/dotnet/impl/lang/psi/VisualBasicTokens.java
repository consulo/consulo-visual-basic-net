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
import consulo.language.ast.StandardTokenTypes;
import consulo.language.ast.TokenSet;

/**
 * Visual Basic token types. Debug names follow Roslyn {@code SyntaxKind}.
 * <p>
 * Only reserved keywords are produced by the lexer. Contextual keywords ({@link #CONTEXTUAL_KEYWORDS}) are lexed as
 * {@link #IDENTIFIER} and remapped by the parser where they act as keywords.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public interface VisualBasicTokens extends StandardTokenTypes {
    IElementType IDENTIFIER = new VisualBasicTokenType("IdentifierToken");
    IElementType INTEGER_LITERAL = new VisualBasicTokenType("IntegerLiteralToken");
    IElementType FLOATING_LITERAL = new VisualBasicTokenType("FloatingLiteralToken");
    IElementType DECIMAL_LITERAL = new VisualBasicTokenType("DecimalLiteralToken");
    IElementType DATE_LITERAL = new VisualBasicTokenType("DateLiteralToken");
    IElementType STRING_LITERAL = new VisualBasicTokenType("StringLiteralToken");
    IElementType CHARACTER_LITERAL = new VisualBasicTokenType("CharacterLiteralToken");
    IElementType INTERPOLATED_STRING_START = new VisualBasicTokenType("DollarSignDoubleQuoteToken");
    IElementType INTERPOLATED_STRING_TEXT = new VisualBasicTokenType("InterpolatedStringTextToken");
    IElementType INTERPOLATED_STRING_END = new VisualBasicTokenType("DoubleQuoteToken");
    IElementType INTERPOLATION_START = new VisualBasicTokenType("InterpolationOpenBraceToken");
    IElementType INTERPOLATION_END = new VisualBasicTokenType("InterpolationCloseBraceToken");
    IElementType INTERPOLATION_FORMAT = new VisualBasicTokenType("InterpolationFormatStringToken");
    IElementType NEW_LINE = new VisualBasicTokenType("StatementTerminatorToken");
    IElementType LINE_CONTINUATION = new VisualBasicTokenType("LineContinuationTrivia");
    /**
     * Line break consumed by the parser, as a statement terminator or as an implicit line continuation:
     * the parser remaps {@link #NEW_LINE} to it, so line breaks are whitespace in the tree.
     */
    IElementType END_OF_LINE = new VisualBasicTokenType("EndOfLineTrivia");
    IElementType COMMENT = new VisualBasicTokenType("CommentTrivia");
    IElementType DOC_COMMENT = new VisualBasicTokenType("DocumentationCommentTrivia");
    IElementType PREPROCESSOR_DIRECTIVE = new VisualBasicTokenType("DirectiveTrivia");
    IElementType DISABLED_TEXT = new VisualBasicTokenType("DisabledTextTrivia");
    /**
     * {@link #PREPROCESSOR_DIRECTIVE} after the parser applied it, in active or in inactive code.
     */
    IElementType ACTIVE_DIRECTIVE = new VisualBasicTokenType("ActiveDirectiveTrivia");
    IElementType DISABLED_DIRECTIVE = new VisualBasicTokenType("DisabledDirectiveTrivia");

    // punctuation and operators
    IElementType AT = new VisualBasicTokenType("AtToken");
    IElementType LPAR = new VisualBasicTokenType("OpenParenToken");
    IElementType RPAR = new VisualBasicTokenType("CloseParenToken");
    IElementType LBRACE = new VisualBasicTokenType("OpenBraceToken");
    IElementType RBRACE = new VisualBasicTokenType("CloseBraceToken");
    IElementType COMMA = new VisualBasicTokenType("CommaToken");
    IElementType DOT = new VisualBasicTokenType("DotToken");
    IElementType COLON = new VisualBasicTokenType("ColonToken");
    IElementType EXCLAMATION = new VisualBasicTokenType("ExclamationToken");
    IElementType QUESTION = new VisualBasicTokenType("QuestionToken");
    IElementType HASH = new VisualBasicTokenType("HashToken");
    IElementType PLUS = new VisualBasicTokenType("PlusToken");
    IElementType MINUS = new VisualBasicTokenType("MinusToken");
    IElementType ASTERISK = new VisualBasicTokenType("AsteriskToken");
    IElementType SLASH = new VisualBasicTokenType("SlashToken");
    IElementType BACKSLASH = new VisualBasicTokenType("BackslashToken");
    IElementType CARET = new VisualBasicTokenType("CaretToken");
    IElementType AMPERSAND = new VisualBasicTokenType("AmpersandToken");
    IElementType EQ = new VisualBasicTokenType("EqualsToken");
    IElementType LT = new VisualBasicTokenType("LessThanToken");
    IElementType GT = new VisualBasicTokenType("GreaterThanToken");
    IElementType LTEQ = new VisualBasicTokenType("LessThanEqualsToken");
    IElementType GTEQ = new VisualBasicTokenType("GreaterThanEqualsToken");
    IElementType LTGT = new VisualBasicTokenType("LessThanGreaterThanToken");
    IElementType LTLT = new VisualBasicTokenType("LessThanLessThanToken");
    IElementType GTGT = new VisualBasicTokenType("GreaterThanGreaterThanToken");
    IElementType PLUSEQ = new VisualBasicTokenType("PlusEqualsToken");
    IElementType MINUSEQ = new VisualBasicTokenType("MinusEqualsToken");
    IElementType ASTERISKEQ = new VisualBasicTokenType("AsteriskEqualsToken");
    IElementType SLASHEQ = new VisualBasicTokenType("SlashEqualsToken");
    IElementType BACKSLASHEQ = new VisualBasicTokenType("BackslashEqualsToken");
    IElementType CARETEQ = new VisualBasicTokenType("CaretEqualsToken");
    IElementType AMPERSANDEQ = new VisualBasicTokenType("AmpersandEqualsToken");
    IElementType LTLTEQ = new VisualBasicTokenType("LessThanLessThanEqualsToken");
    IElementType GTGTEQ = new VisualBasicTokenType("GreaterThanGreaterThanEqualsToken");
    IElementType COLONEQ = new VisualBasicTokenType("ColonEqualsToken");

    // XML literals
    IElementType XML_TAG_START = new VisualBasicTokenType("XmlLessThanToken");
    IElementType XML_END_TAG_START = new VisualBasicTokenType("XmlLessThanSlashToken");
    IElementType XML_TAG_END = new VisualBasicTokenType("XmlGreaterThanToken");
    IElementType XML_EMPTY_TAG_END = new VisualBasicTokenType("XmlSlashGreaterThanToken");
    IElementType XML_TAG_NAME = new VisualBasicTokenType("XmlNameToken");
    IElementType XML_ATTRIBUTE_NAME = new VisualBasicTokenType("XmlAttributeNameToken");
    IElementType XML_EQ = new VisualBasicTokenType("XmlEqualsToken");
    IElementType XML_ATTRIBUTE_VALUE = new VisualBasicTokenType("XmlAttributeValueToken");
    IElementType XML_TEXT = new VisualBasicTokenType("XmlTextLiteralToken");
    IElementType XML_COMMENT = new VisualBasicTokenType("XmlComment");
    IElementType XML_CDATA = new VisualBasicTokenType("XmlCDataSection");
    IElementType XML_PI = new VisualBasicTokenType("XmlProcessingInstruction");
    IElementType XML_EMBED_START = new VisualBasicTokenType("LessThanPercentEqualsToken");
    IElementType XML_EMBED_END = new VisualBasicTokenType("PercentGreaterThanToken");

    // reserved keywords
    IElementType ADDHANDLER_KEYWORD = new VisualBasicKeywordTokenType("AddHandlerKeyword", "AddHandler", false);
    IElementType ADDRESSOF_KEYWORD = new VisualBasicKeywordTokenType("AddressOfKeyword", "AddressOf", false);
    IElementType ALIAS_KEYWORD = new VisualBasicKeywordTokenType("AliasKeyword", "Alias", false);
    IElementType AND_KEYWORD = new VisualBasicKeywordTokenType("AndKeyword", "And", false);
    IElementType ANDALSO_KEYWORD = new VisualBasicKeywordTokenType("AndAlsoKeyword", "AndAlso", false);
    IElementType AS_KEYWORD = new VisualBasicKeywordTokenType("AsKeyword", "As", false);
    IElementType BOOLEAN_KEYWORD = new VisualBasicKeywordTokenType("BooleanKeyword", "Boolean", false);
    IElementType BYREF_KEYWORD = new VisualBasicKeywordTokenType("ByRefKeyword", "ByRef", false);
    IElementType BYTE_KEYWORD = new VisualBasicKeywordTokenType("ByteKeyword", "Byte", false);
    IElementType BYVAL_KEYWORD = new VisualBasicKeywordTokenType("ByValKeyword", "ByVal", false);
    IElementType CALL_KEYWORD = new VisualBasicKeywordTokenType("CallKeyword", "Call", false);
    IElementType CASE_KEYWORD = new VisualBasicKeywordTokenType("CaseKeyword", "Case", false);
    IElementType CATCH_KEYWORD = new VisualBasicKeywordTokenType("CatchKeyword", "Catch", false);
    IElementType CBOOL_KEYWORD = new VisualBasicKeywordTokenType("CBoolKeyword", "CBool", false);
    IElementType CBYTE_KEYWORD = new VisualBasicKeywordTokenType("CByteKeyword", "CByte", false);
    IElementType CCHAR_KEYWORD = new VisualBasicKeywordTokenType("CCharKeyword", "CChar", false);
    IElementType CDATE_KEYWORD = new VisualBasicKeywordTokenType("CDateKeyword", "CDate", false);
    IElementType CDEC_KEYWORD = new VisualBasicKeywordTokenType("CDecKeyword", "CDec", false);
    IElementType CDBL_KEYWORD = new VisualBasicKeywordTokenType("CDblKeyword", "CDbl", false);
    IElementType CHAR_KEYWORD = new VisualBasicKeywordTokenType("CharKeyword", "Char", false);
    IElementType CINT_KEYWORD = new VisualBasicKeywordTokenType("CIntKeyword", "CInt", false);
    IElementType CLASS_KEYWORD = new VisualBasicKeywordTokenType("ClassKeyword", "Class", false);
    IElementType CLNG_KEYWORD = new VisualBasicKeywordTokenType("CLngKeyword", "CLng", false);
    IElementType COBJ_KEYWORD = new VisualBasicKeywordTokenType("CObjKeyword", "CObj", false);
    IElementType CONST_KEYWORD = new VisualBasicKeywordTokenType("ConstKeyword", "Const", false);
    IElementType CONTINUE_KEYWORD = new VisualBasicKeywordTokenType("ContinueKeyword", "Continue", false);
    IElementType CSBYTE_KEYWORD = new VisualBasicKeywordTokenType("CSByteKeyword", "CSByte", false);
    IElementType CSHORT_KEYWORD = new VisualBasicKeywordTokenType("CShortKeyword", "CShort", false);
    IElementType CSNG_KEYWORD = new VisualBasicKeywordTokenType("CSngKeyword", "CSng", false);
    IElementType CSTR_KEYWORD = new VisualBasicKeywordTokenType("CStrKeyword", "CStr", false);
    IElementType CTYPE_KEYWORD = new VisualBasicKeywordTokenType("CTypeKeyword", "CType", false);
    IElementType CUINT_KEYWORD = new VisualBasicKeywordTokenType("CUIntKeyword", "CUInt", false);
    IElementType CULNG_KEYWORD = new VisualBasicKeywordTokenType("CULngKeyword", "CULng", false);
    IElementType CUSHORT_KEYWORD = new VisualBasicKeywordTokenType("CUShortKeyword", "CUShort", false);
    IElementType DATE_KEYWORD = new VisualBasicKeywordTokenType("DateKeyword", "Date", false);
    IElementType DECIMAL_KEYWORD = new VisualBasicKeywordTokenType("DecimalKeyword", "Decimal", false);
    IElementType DECLARE_KEYWORD = new VisualBasicKeywordTokenType("DeclareKeyword", "Declare", false);
    IElementType DEFAULT_KEYWORD = new VisualBasicKeywordTokenType("DefaultKeyword", "Default", false);
    IElementType DELEGATE_KEYWORD = new VisualBasicKeywordTokenType("DelegateKeyword", "Delegate", false);
    IElementType DIM_KEYWORD = new VisualBasicKeywordTokenType("DimKeyword", "Dim", false);
    IElementType DIRECTCAST_KEYWORD = new VisualBasicKeywordTokenType("DirectCastKeyword", "DirectCast", false);
    IElementType DO_KEYWORD = new VisualBasicKeywordTokenType("DoKeyword", "Do", false);
    IElementType DOUBLE_KEYWORD = new VisualBasicKeywordTokenType("DoubleKeyword", "Double", false);
    IElementType EACH_KEYWORD = new VisualBasicKeywordTokenType("EachKeyword", "Each", false);
    IElementType ELSE_KEYWORD = new VisualBasicKeywordTokenType("ElseKeyword", "Else", false);
    IElementType ELSEIF_KEYWORD = new VisualBasicKeywordTokenType("ElseIfKeyword", "ElseIf", false);
    IElementType END_KEYWORD = new VisualBasicKeywordTokenType("EndKeyword", "End", false);
    IElementType ENUM_KEYWORD = new VisualBasicKeywordTokenType("EnumKeyword", "Enum", false);
    IElementType ERASE_KEYWORD = new VisualBasicKeywordTokenType("EraseKeyword", "Erase", false);
    IElementType ERROR_KEYWORD = new VisualBasicKeywordTokenType("ErrorKeyword", "Error", false);
    IElementType EVENT_KEYWORD = new VisualBasicKeywordTokenType("EventKeyword", "Event", false);
    IElementType EXIT_KEYWORD = new VisualBasicKeywordTokenType("ExitKeyword", "Exit", false);
    IElementType FALSE_KEYWORD = new VisualBasicKeywordTokenType("FalseKeyword", "False", false);
    IElementType FINALLY_KEYWORD = new VisualBasicKeywordTokenType("FinallyKeyword", "Finally", false);
    IElementType FOR_KEYWORD = new VisualBasicKeywordTokenType("ForKeyword", "For", false);
    IElementType FRIEND_KEYWORD = new VisualBasicKeywordTokenType("FriendKeyword", "Friend", false);
    IElementType FUNCTION_KEYWORD = new VisualBasicKeywordTokenType("FunctionKeyword", "Function", false);
    IElementType GET_KEYWORD = new VisualBasicKeywordTokenType("GetKeyword", "Get", false);
    IElementType GETTYPE_KEYWORD = new VisualBasicKeywordTokenType("GetTypeKeyword", "GetType", false);
    IElementType GETXMLNAMESPACE_KEYWORD = new VisualBasicKeywordTokenType("GetXmlNamespaceKeyword", "GetXmlNamespace", false);
    IElementType GLOBAL_KEYWORD = new VisualBasicKeywordTokenType("GlobalKeyword", "Global", false);
    IElementType GOTO_KEYWORD = new VisualBasicKeywordTokenType("GoToKeyword", "GoTo", false);
    IElementType HANDLES_KEYWORD = new VisualBasicKeywordTokenType("HandlesKeyword", "Handles", false);
    IElementType IF_KEYWORD = new VisualBasicKeywordTokenType("IfKeyword", "If", false);
    IElementType IMPLEMENTS_KEYWORD = new VisualBasicKeywordTokenType("ImplementsKeyword", "Implements", false);
    IElementType IMPORTS_KEYWORD = new VisualBasicKeywordTokenType("ImportsKeyword", "Imports", false);
    IElementType IN_KEYWORD = new VisualBasicKeywordTokenType("InKeyword", "In", false);
    IElementType INHERITS_KEYWORD = new VisualBasicKeywordTokenType("InheritsKeyword", "Inherits", false);
    IElementType INTEGER_KEYWORD = new VisualBasicKeywordTokenType("IntegerKeyword", "Integer", false);
    IElementType INTERFACE_KEYWORD = new VisualBasicKeywordTokenType("InterfaceKeyword", "Interface", false);
    IElementType IS_KEYWORD = new VisualBasicKeywordTokenType("IsKeyword", "Is", false);
    IElementType ISNOT_KEYWORD = new VisualBasicKeywordTokenType("IsNotKeyword", "IsNot", false);
    IElementType LET_KEYWORD = new VisualBasicKeywordTokenType("LetKeyword", "Let", false);
    IElementType LIB_KEYWORD = new VisualBasicKeywordTokenType("LibKeyword", "Lib", false);
    IElementType LIKE_KEYWORD = new VisualBasicKeywordTokenType("LikeKeyword", "Like", false);
    IElementType LONG_KEYWORD = new VisualBasicKeywordTokenType("LongKeyword", "Long", false);
    IElementType LOOP_KEYWORD = new VisualBasicKeywordTokenType("LoopKeyword", "Loop", false);
    IElementType ME_KEYWORD = new VisualBasicKeywordTokenType("MeKeyword", "Me", false);
    IElementType MOD_KEYWORD = new VisualBasicKeywordTokenType("ModKeyword", "Mod", false);
    IElementType MODULE_KEYWORD = new VisualBasicKeywordTokenType("ModuleKeyword", "Module", false);
    IElementType MUSTINHERIT_KEYWORD = new VisualBasicKeywordTokenType("MustInheritKeyword", "MustInherit", false);
    IElementType MUSTOVERRIDE_KEYWORD = new VisualBasicKeywordTokenType("MustOverrideKeyword", "MustOverride", false);
    IElementType MYBASE_KEYWORD = new VisualBasicKeywordTokenType("MyBaseKeyword", "MyBase", false);
    IElementType MYCLASS_KEYWORD = new VisualBasicKeywordTokenType("MyClassKeyword", "MyClass", false);
    IElementType NAMEOF_KEYWORD = new VisualBasicKeywordTokenType("NameOfKeyword", "NameOf", false);
    IElementType NAMESPACE_KEYWORD = new VisualBasicKeywordTokenType("NamespaceKeyword", "Namespace", false);
    IElementType NARROWING_KEYWORD = new VisualBasicKeywordTokenType("NarrowingKeyword", "Narrowing", false);
    IElementType NEXT_KEYWORD = new VisualBasicKeywordTokenType("NextKeyword", "Next", false);
    IElementType NEW_KEYWORD = new VisualBasicKeywordTokenType("NewKeyword", "New", false);
    IElementType NOT_KEYWORD = new VisualBasicKeywordTokenType("NotKeyword", "Not", false);
    IElementType NOTHING_KEYWORD = new VisualBasicKeywordTokenType("NothingKeyword", "Nothing", false);
    IElementType NOTINHERITABLE_KEYWORD = new VisualBasicKeywordTokenType("NotInheritableKeyword", "NotInheritable", false);
    IElementType NOTOVERRIDABLE_KEYWORD = new VisualBasicKeywordTokenType("NotOverridableKeyword", "NotOverridable", false);
    IElementType OBJECT_KEYWORD = new VisualBasicKeywordTokenType("ObjectKeyword", "Object", false);
    IElementType OF_KEYWORD = new VisualBasicKeywordTokenType("OfKeyword", "Of", false);
    IElementType ON_KEYWORD = new VisualBasicKeywordTokenType("OnKeyword", "On", false);
    IElementType OPERATOR_KEYWORD = new VisualBasicKeywordTokenType("OperatorKeyword", "Operator", false);
    IElementType OPTION_KEYWORD = new VisualBasicKeywordTokenType("OptionKeyword", "Option", false);
    IElementType OPTIONAL_KEYWORD = new VisualBasicKeywordTokenType("OptionalKeyword", "Optional", false);
    IElementType OR_KEYWORD = new VisualBasicKeywordTokenType("OrKeyword", "Or", false);
    IElementType ORELSE_KEYWORD = new VisualBasicKeywordTokenType("OrElseKeyword", "OrElse", false);
    IElementType OVERLOADS_KEYWORD = new VisualBasicKeywordTokenType("OverloadsKeyword", "Overloads", false);
    IElementType OVERRIDABLE_KEYWORD = new VisualBasicKeywordTokenType("OverridableKeyword", "Overridable", false);
    IElementType OVERRIDES_KEYWORD = new VisualBasicKeywordTokenType("OverridesKeyword", "Overrides", false);
    IElementType PARAMARRAY_KEYWORD = new VisualBasicKeywordTokenType("ParamArrayKeyword", "ParamArray", false);
    IElementType PARTIAL_KEYWORD = new VisualBasicKeywordTokenType("PartialKeyword", "Partial", false);
    IElementType PRIVATE_KEYWORD = new VisualBasicKeywordTokenType("PrivateKeyword", "Private", false);
    IElementType PROPERTY_KEYWORD = new VisualBasicKeywordTokenType("PropertyKeyword", "Property", false);
    IElementType PROTECTED_KEYWORD = new VisualBasicKeywordTokenType("ProtectedKeyword", "Protected", false);
    IElementType PUBLIC_KEYWORD = new VisualBasicKeywordTokenType("PublicKeyword", "Public", false);
    IElementType RAISEEVENT_KEYWORD = new VisualBasicKeywordTokenType("RaiseEventKeyword", "RaiseEvent", false);
    IElementType READONLY_KEYWORD = new VisualBasicKeywordTokenType("ReadOnlyKeyword", "ReadOnly", false);
    IElementType REDIM_KEYWORD = new VisualBasicKeywordTokenType("ReDimKeyword", "ReDim", false);
    IElementType REM_KEYWORD = new VisualBasicKeywordTokenType("REMKeyword", "REM", false);
    IElementType REMOVEHANDLER_KEYWORD = new VisualBasicKeywordTokenType("RemoveHandlerKeyword", "RemoveHandler", false);
    IElementType RESUME_KEYWORD = new VisualBasicKeywordTokenType("ResumeKeyword", "Resume", false);
    IElementType RETURN_KEYWORD = new VisualBasicKeywordTokenType("ReturnKeyword", "Return", false);
    IElementType SBYTE_KEYWORD = new VisualBasicKeywordTokenType("SByteKeyword", "SByte", false);
    IElementType SELECT_KEYWORD = new VisualBasicKeywordTokenType("SelectKeyword", "Select", false);
    IElementType SET_KEYWORD = new VisualBasicKeywordTokenType("SetKeyword", "Set", false);
    IElementType SHADOWS_KEYWORD = new VisualBasicKeywordTokenType("ShadowsKeyword", "Shadows", false);
    IElementType SHARED_KEYWORD = new VisualBasicKeywordTokenType("SharedKeyword", "Shared", false);
    IElementType SHORT_KEYWORD = new VisualBasicKeywordTokenType("ShortKeyword", "Short", false);
    IElementType SINGLE_KEYWORD = new VisualBasicKeywordTokenType("SingleKeyword", "Single", false);
    IElementType STATIC_KEYWORD = new VisualBasicKeywordTokenType("StaticKeyword", "Static", false);
    IElementType STEP_KEYWORD = new VisualBasicKeywordTokenType("StepKeyword", "Step", false);
    IElementType STOP_KEYWORD = new VisualBasicKeywordTokenType("StopKeyword", "Stop", false);
    IElementType STRING_KEYWORD = new VisualBasicKeywordTokenType("StringKeyword", "String", false);
    IElementType STRUCTURE_KEYWORD = new VisualBasicKeywordTokenType("StructureKeyword", "Structure", false);
    IElementType SUB_KEYWORD = new VisualBasicKeywordTokenType("SubKeyword", "Sub", false);
    IElementType SYNCLOCK_KEYWORD = new VisualBasicKeywordTokenType("SyncLockKeyword", "SyncLock", false);
    IElementType THEN_KEYWORD = new VisualBasicKeywordTokenType("ThenKeyword", "Then", false);
    IElementType THROW_KEYWORD = new VisualBasicKeywordTokenType("ThrowKeyword", "Throw", false);
    IElementType TO_KEYWORD = new VisualBasicKeywordTokenType("ToKeyword", "To", false);
    IElementType TRUE_KEYWORD = new VisualBasicKeywordTokenType("TrueKeyword", "True", false);
    IElementType TRY_KEYWORD = new VisualBasicKeywordTokenType("TryKeyword", "Try", false);
    IElementType TRYCAST_KEYWORD = new VisualBasicKeywordTokenType("TryCastKeyword", "TryCast", false);
    IElementType TYPEOF_KEYWORD = new VisualBasicKeywordTokenType("TypeOfKeyword", "TypeOf", false);
    IElementType UINTEGER_KEYWORD = new VisualBasicKeywordTokenType("UIntegerKeyword", "UInteger", false);
    IElementType ULONG_KEYWORD = new VisualBasicKeywordTokenType("ULongKeyword", "ULong", false);
    IElementType USHORT_KEYWORD = new VisualBasicKeywordTokenType("UShortKeyword", "UShort", false);
    IElementType USING_KEYWORD = new VisualBasicKeywordTokenType("UsingKeyword", "Using", false);
    IElementType WHEN_KEYWORD = new VisualBasicKeywordTokenType("WhenKeyword", "When", false);
    IElementType WHILE_KEYWORD = new VisualBasicKeywordTokenType("WhileKeyword", "While", false);
    IElementType WIDENING_KEYWORD = new VisualBasicKeywordTokenType("WideningKeyword", "Widening", false);
    IElementType WITH_KEYWORD = new VisualBasicKeywordTokenType("WithKeyword", "With", false);
    IElementType WITHEVENTS_KEYWORD = new VisualBasicKeywordTokenType("WithEventsKeyword", "WithEvents", false);
    IElementType WRITEONLY_KEYWORD = new VisualBasicKeywordTokenType("WriteOnlyKeyword", "WriteOnly", false);
    IElementType XOR_KEYWORD = new VisualBasicKeywordTokenType("XorKeyword", "Xor", false);
    IElementType ENDIF_KEYWORD = new VisualBasicKeywordTokenType("EndIfKeyword", "EndIf", false);
    IElementType GOSUB_KEYWORD = new VisualBasicKeywordTokenType("GosubKeyword", "Gosub", false);
    IElementType VARIANT_KEYWORD = new VisualBasicKeywordTokenType("VariantKeyword", "Variant", false);
    IElementType WEND_KEYWORD = new VisualBasicKeywordTokenType("WendKeyword", "Wend", false);

    // contextual keywords, see the class comment
    IElementType REFERENCE_KEYWORD = new VisualBasicKeywordTokenType("ReferenceKeyword", "Reference", true);
    IElementType AGGREGATE_KEYWORD = new VisualBasicKeywordTokenType("AggregateKeyword", "Aggregate", true);
    IElementType ALL_KEYWORD = new VisualBasicKeywordTokenType("AllKeyword", "All", true);
    IElementType ANSI_KEYWORD = new VisualBasicKeywordTokenType("AnsiKeyword", "Ansi", true);
    IElementType ASCENDING_KEYWORD = new VisualBasicKeywordTokenType("AscendingKeyword", "Ascending", true);
    IElementType ASSEMBLY_KEYWORD = new VisualBasicKeywordTokenType("AssemblyKeyword", "Assembly", true);
    IElementType AUTO_KEYWORD = new VisualBasicKeywordTokenType("AutoKeyword", "Auto", true);
    IElementType BINARY_KEYWORD = new VisualBasicKeywordTokenType("BinaryKeyword", "Binary", true);
    IElementType BY_KEYWORD = new VisualBasicKeywordTokenType("ByKeyword", "By", true);
    IElementType COMPARE_KEYWORD = new VisualBasicKeywordTokenType("CompareKeyword", "Compare", true);
    IElementType CUSTOM_KEYWORD = new VisualBasicKeywordTokenType("CustomKeyword", "Custom", true);
    IElementType DESCENDING_KEYWORD = new VisualBasicKeywordTokenType("DescendingKeyword", "Descending", true);
    IElementType DISABLE_KEYWORD = new VisualBasicKeywordTokenType("DisableKeyword", "Disable", true);
    IElementType DISTINCT_KEYWORD = new VisualBasicKeywordTokenType("DistinctKeyword", "Distinct", true);
    IElementType ENABLE_KEYWORD = new VisualBasicKeywordTokenType("EnableKeyword", "Enable", true);
    IElementType EQUALS_KEYWORD = new VisualBasicKeywordTokenType("EqualsKeyword", "Equals", true);
    IElementType EXPLICIT_KEYWORD = new VisualBasicKeywordTokenType("ExplicitKeyword", "Explicit", true);
    IElementType EXTERNALSOURCE_KEYWORD = new VisualBasicKeywordTokenType("ExternalSourceKeyword", "ExternalSource", true);
    IElementType EXTERNALCHECKSUM_KEYWORD = new VisualBasicKeywordTokenType("ExternalChecksumKeyword", "ExternalChecksum", true);
    IElementType FROM_KEYWORD = new VisualBasicKeywordTokenType("FromKeyword", "From", true);
    IElementType GROUP_KEYWORD = new VisualBasicKeywordTokenType("GroupKeyword", "Group", true);
    IElementType INFER_KEYWORD = new VisualBasicKeywordTokenType("InferKeyword", "Infer", true);
    IElementType INTO_KEYWORD = new VisualBasicKeywordTokenType("IntoKeyword", "Into", true);
    IElementType ISFALSE_KEYWORD = new VisualBasicKeywordTokenType("IsFalseKeyword", "IsFalse", true);
    IElementType ISTRUE_KEYWORD = new VisualBasicKeywordTokenType("IsTrueKeyword", "IsTrue", true);
    IElementType JOIN_KEYWORD = new VisualBasicKeywordTokenType("JoinKeyword", "Join", true);
    IElementType KEY_KEYWORD = new VisualBasicKeywordTokenType("KeyKeyword", "Key", true);
    IElementType MID_KEYWORD = new VisualBasicKeywordTokenType("MidKeyword", "Mid", true);
    IElementType OFF_KEYWORD = new VisualBasicKeywordTokenType("OffKeyword", "Off", true);
    IElementType ORDER_KEYWORD = new VisualBasicKeywordTokenType("OrderKeyword", "Order", true);
    IElementType OUT_KEYWORD = new VisualBasicKeywordTokenType("OutKeyword", "Out", true);
    IElementType PRESERVE_KEYWORD = new VisualBasicKeywordTokenType("PreserveKeyword", "Preserve", true);
    IElementType REGION_KEYWORD = new VisualBasicKeywordTokenType("RegionKeyword", "Region", true);
    IElementType SKIP_KEYWORD = new VisualBasicKeywordTokenType("SkipKeyword", "Skip", true);
    IElementType STRICT_KEYWORD = new VisualBasicKeywordTokenType("StrictKeyword", "Strict", true);
    IElementType TEXT_KEYWORD = new VisualBasicKeywordTokenType("TextKeyword", "Text", true);
    IElementType TAKE_KEYWORD = new VisualBasicKeywordTokenType("TakeKeyword", "Take", true);
    IElementType UNICODE_KEYWORD = new VisualBasicKeywordTokenType("UnicodeKeyword", "Unicode", true);
    IElementType UNTIL_KEYWORD = new VisualBasicKeywordTokenType("UntilKeyword", "Until", true);
    IElementType WARNING_KEYWORD = new VisualBasicKeywordTokenType("WarningKeyword", "Warning", true);
    IElementType WHERE_KEYWORD = new VisualBasicKeywordTokenType("WhereKeyword", "Where", true);
    IElementType ASYNC_KEYWORD = new VisualBasicKeywordTokenType("AsyncKeyword", "Async", true);
    IElementType AWAIT_KEYWORD = new VisualBasicKeywordTokenType("AwaitKeyword", "Await", true);
    IElementType ITERATOR_KEYWORD = new VisualBasicKeywordTokenType("IteratorKeyword", "Iterator", true);
    IElementType YIELD_KEYWORD = new VisualBasicKeywordTokenType("YieldKeyword", "Yield", true);
    IElementType TYPE_KEYWORD = new VisualBasicKeywordTokenType("TypeKeyword", "Type", true);

    TokenSet RESERVED_KEYWORDS = TokenSet.create(
        ADDHANDLER_KEYWORD,
        ADDRESSOF_KEYWORD,
        ALIAS_KEYWORD,
        AND_KEYWORD,
        ANDALSO_KEYWORD,
        AS_KEYWORD,
        BOOLEAN_KEYWORD,
        BYREF_KEYWORD,
        BYTE_KEYWORD,
        BYVAL_KEYWORD,
        CALL_KEYWORD,
        CASE_KEYWORD,
        CATCH_KEYWORD,
        CBOOL_KEYWORD,
        CBYTE_KEYWORD,
        CCHAR_KEYWORD,
        CDATE_KEYWORD,
        CDEC_KEYWORD,
        CDBL_KEYWORD,
        CHAR_KEYWORD,
        CINT_KEYWORD,
        CLASS_KEYWORD,
        CLNG_KEYWORD,
        COBJ_KEYWORD,
        CONST_KEYWORD,
        CONTINUE_KEYWORD,
        CSBYTE_KEYWORD,
        CSHORT_KEYWORD,
        CSNG_KEYWORD,
        CSTR_KEYWORD,
        CTYPE_KEYWORD,
        CUINT_KEYWORD,
        CULNG_KEYWORD,
        CUSHORT_KEYWORD,
        DATE_KEYWORD,
        DECIMAL_KEYWORD,
        DECLARE_KEYWORD,
        DEFAULT_KEYWORD,
        DELEGATE_KEYWORD,
        DIM_KEYWORD,
        DIRECTCAST_KEYWORD,
        DO_KEYWORD,
        DOUBLE_KEYWORD,
        EACH_KEYWORD,
        ELSE_KEYWORD,
        ELSEIF_KEYWORD,
        END_KEYWORD,
        ENUM_KEYWORD,
        ERASE_KEYWORD,
        ERROR_KEYWORD,
        EVENT_KEYWORD,
        EXIT_KEYWORD,
        FALSE_KEYWORD,
        FINALLY_KEYWORD,
        FOR_KEYWORD,
        FRIEND_KEYWORD,
        FUNCTION_KEYWORD,
        GET_KEYWORD,
        GETTYPE_KEYWORD,
        GETXMLNAMESPACE_KEYWORD,
        GLOBAL_KEYWORD,
        GOTO_KEYWORD,
        HANDLES_KEYWORD,
        IF_KEYWORD,
        IMPLEMENTS_KEYWORD,
        IMPORTS_KEYWORD,
        IN_KEYWORD,
        INHERITS_KEYWORD,
        INTEGER_KEYWORD,
        INTERFACE_KEYWORD,
        IS_KEYWORD,
        ISNOT_KEYWORD,
        LET_KEYWORD,
        LIB_KEYWORD,
        LIKE_KEYWORD,
        LONG_KEYWORD,
        LOOP_KEYWORD,
        ME_KEYWORD,
        MOD_KEYWORD,
        MODULE_KEYWORD,
        MUSTINHERIT_KEYWORD,
        MUSTOVERRIDE_KEYWORD,
        MYBASE_KEYWORD,
        MYCLASS_KEYWORD,
        NAMEOF_KEYWORD,
        NAMESPACE_KEYWORD,
        NARROWING_KEYWORD,
        NEXT_KEYWORD,
        NEW_KEYWORD,
        NOT_KEYWORD,
        NOTHING_KEYWORD,
        NOTINHERITABLE_KEYWORD,
        NOTOVERRIDABLE_KEYWORD,
        OBJECT_KEYWORD,
        OF_KEYWORD,
        ON_KEYWORD,
        OPERATOR_KEYWORD,
        OPTION_KEYWORD,
        OPTIONAL_KEYWORD,
        OR_KEYWORD,
        ORELSE_KEYWORD,
        OVERLOADS_KEYWORD,
        OVERRIDABLE_KEYWORD,
        OVERRIDES_KEYWORD,
        PARAMARRAY_KEYWORD,
        PARTIAL_KEYWORD,
        PRIVATE_KEYWORD,
        PROPERTY_KEYWORD,
        PROTECTED_KEYWORD,
        PUBLIC_KEYWORD,
        RAISEEVENT_KEYWORD,
        READONLY_KEYWORD,
        REDIM_KEYWORD,
        REM_KEYWORD,
        REMOVEHANDLER_KEYWORD,
        RESUME_KEYWORD,
        RETURN_KEYWORD,
        SBYTE_KEYWORD,
        SELECT_KEYWORD,
        SET_KEYWORD,
        SHADOWS_KEYWORD,
        SHARED_KEYWORD,
        SHORT_KEYWORD,
        SINGLE_KEYWORD,
        STATIC_KEYWORD,
        STEP_KEYWORD,
        STOP_KEYWORD,
        STRING_KEYWORD,
        STRUCTURE_KEYWORD,
        SUB_KEYWORD,
        SYNCLOCK_KEYWORD,
        THEN_KEYWORD,
        THROW_KEYWORD,
        TO_KEYWORD,
        TRUE_KEYWORD,
        TRY_KEYWORD,
        TRYCAST_KEYWORD,
        TYPEOF_KEYWORD,
        UINTEGER_KEYWORD,
        ULONG_KEYWORD,
        USHORT_KEYWORD,
        USING_KEYWORD,
        WHEN_KEYWORD,
        WHILE_KEYWORD,
        WIDENING_KEYWORD,
        WITH_KEYWORD,
        WITHEVENTS_KEYWORD,
        WRITEONLY_KEYWORD,
        XOR_KEYWORD,
        ENDIF_KEYWORD,
        GOSUB_KEYWORD,
        VARIANT_KEYWORD,
        WEND_KEYWORD
    );

    TokenSet CONTEXTUAL_KEYWORDS = TokenSet.create(
        REFERENCE_KEYWORD,
        AGGREGATE_KEYWORD,
        ALL_KEYWORD,
        ANSI_KEYWORD,
        ASCENDING_KEYWORD,
        ASSEMBLY_KEYWORD,
        AUTO_KEYWORD,
        BINARY_KEYWORD,
        BY_KEYWORD,
        COMPARE_KEYWORD,
        CUSTOM_KEYWORD,
        DESCENDING_KEYWORD,
        DISABLE_KEYWORD,
        DISTINCT_KEYWORD,
        ENABLE_KEYWORD,
        EQUALS_KEYWORD,
        EXPLICIT_KEYWORD,
        EXTERNALSOURCE_KEYWORD,
        EXTERNALCHECKSUM_KEYWORD,
        FROM_KEYWORD,
        GROUP_KEYWORD,
        INFER_KEYWORD,
        INTO_KEYWORD,
        ISFALSE_KEYWORD,
        ISTRUE_KEYWORD,
        JOIN_KEYWORD,
        KEY_KEYWORD,
        MID_KEYWORD,
        OFF_KEYWORD,
        ORDER_KEYWORD,
        OUT_KEYWORD,
        PRESERVE_KEYWORD,
        REGION_KEYWORD,
        SKIP_KEYWORD,
        STRICT_KEYWORD,
        TEXT_KEYWORD,
        TAKE_KEYWORD,
        UNICODE_KEYWORD,
        UNTIL_KEYWORD,
        WARNING_KEYWORD,
        WHERE_KEYWORD,
        ASYNC_KEYWORD,
        AWAIT_KEYWORD,
        ITERATOR_KEYWORD,
        YIELD_KEYWORD,
        TYPE_KEYWORD
    );

    TokenSet KEYWORDS = TokenSet.orSet(RESERVED_KEYWORDS, CONTEXTUAL_KEYWORDS);

    /**
     * Skipped by the parser. {@link #NEW_LINE} is significant: it terminates statements.
     */
    TokenSet WHITESPACES = TokenSet.create(
        WHITE_SPACE,
        LINE_CONTINUATION,
        END_OF_LINE
    );

    /**
     * {@link #PREPROCESSOR_DIRECTIVE} is not here: the parser applies directives, see {@code VisualBasicBuilder}.
     */
    TokenSet COMMENTS = TokenSet.create(
        COMMENT,
        DOC_COMMENT,
        ACTIVE_DIRECTIVE,
        DISABLED_DIRECTIVE,
        DISABLED_TEXT
    );

    TokenSet XML_TOKENS = TokenSet.create(
        XML_TAG_START,
        XML_END_TAG_START,
        XML_TAG_END,
        XML_EMPTY_TAG_END,
        XML_TAG_NAME,
        XML_ATTRIBUTE_NAME,
        XML_EQ,
        XML_ATTRIBUTE_VALUE,
        XML_TEXT,
        XML_COMMENT,
        XML_CDATA,
        XML_PI,
        XML_EMBED_START,
        XML_EMBED_END
    );

    /**
     * Tokens starting an XML literal: the lexer decides where XML may start, see {@code _VisualBasicLexer.flex}.
     */
    TokenSet XML_LITERAL_STARTS = TokenSet.create(XML_TAG_START, XML_COMMENT, XML_CDATA, XML_PI);

    TokenSet STRINGS = TokenSet.create(
        STRING_LITERAL,
        CHARACTER_LITERAL
    );

    TokenSet NUMBERS = TokenSet.create(
        INTEGER_LITERAL,
        FLOATING_LITERAL,
        DECIMAL_LITERAL
    );

    TokenSet OPERATORS = TokenSet.create(
        EXCLAMATION,
        QUESTION,
        PLUS,
        MINUS,
        ASTERISK,
        SLASH,
        BACKSLASH,
        CARET,
        AMPERSAND,
        EQ,
        LT,
        GT,
        LTEQ,
        GTEQ,
        LTGT,
        LTLT,
        GTGT,
        PLUSEQ,
        MINUSEQ,
        ASTERISKEQ,
        SLASHEQ,
        BACKSLASHEQ,
        CARETEQ,
        AMPERSANDEQ,
        LTLTEQ,
        GTGTEQ,
        COLONEQ
    );

    TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(
        EQ,
        PLUSEQ,
        MINUSEQ,
        ASTERISKEQ,
        SLASHEQ,
        BACKSLASHEQ,
        CARETEQ,
        AMPERSANDEQ,
        LTLTEQ,
        GTGTEQ
    );

}
