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


package consulo.visualBasic.dotnet.impl.lang.lexer;

import consulo.language.ast.IElementType;
import consulo.visualBasic.dotnet.impl.lang.psi.VisualBasicTokens;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Case-insensitive keyword lookup.
 *
 * @author VISTALL
 * @since 2026-10-23
 */
public final class VisualBasicKeywords {
    private static final Map<String, IElementType> RESERVED = new HashMap<>();
    private static final Map<String, IElementType> CONTEXTUAL = new HashMap<>();

    static {
        RESERVED.put("addhandler", VisualBasicTokens.ADDHANDLER_KEYWORD);
        RESERVED.put("addressof", VisualBasicTokens.ADDRESSOF_KEYWORD);
        RESERVED.put("alias", VisualBasicTokens.ALIAS_KEYWORD);
        RESERVED.put("and", VisualBasicTokens.AND_KEYWORD);
        RESERVED.put("andalso", VisualBasicTokens.ANDALSO_KEYWORD);
        RESERVED.put("as", VisualBasicTokens.AS_KEYWORD);
        RESERVED.put("boolean", VisualBasicTokens.BOOLEAN_KEYWORD);
        RESERVED.put("byref", VisualBasicTokens.BYREF_KEYWORD);
        RESERVED.put("byte", VisualBasicTokens.BYTE_KEYWORD);
        RESERVED.put("byval", VisualBasicTokens.BYVAL_KEYWORD);
        RESERVED.put("call", VisualBasicTokens.CALL_KEYWORD);
        RESERVED.put("case", VisualBasicTokens.CASE_KEYWORD);
        RESERVED.put("catch", VisualBasicTokens.CATCH_KEYWORD);
        RESERVED.put("cbool", VisualBasicTokens.CBOOL_KEYWORD);
        RESERVED.put("cbyte", VisualBasicTokens.CBYTE_KEYWORD);
        RESERVED.put("cchar", VisualBasicTokens.CCHAR_KEYWORD);
        RESERVED.put("cdate", VisualBasicTokens.CDATE_KEYWORD);
        RESERVED.put("cdec", VisualBasicTokens.CDEC_KEYWORD);
        RESERVED.put("cdbl", VisualBasicTokens.CDBL_KEYWORD);
        RESERVED.put("char", VisualBasicTokens.CHAR_KEYWORD);
        RESERVED.put("cint", VisualBasicTokens.CINT_KEYWORD);
        RESERVED.put("class", VisualBasicTokens.CLASS_KEYWORD);
        RESERVED.put("clng", VisualBasicTokens.CLNG_KEYWORD);
        RESERVED.put("cobj", VisualBasicTokens.COBJ_KEYWORD);
        RESERVED.put("const", VisualBasicTokens.CONST_KEYWORD);
        RESERVED.put("continue", VisualBasicTokens.CONTINUE_KEYWORD);
        RESERVED.put("csbyte", VisualBasicTokens.CSBYTE_KEYWORD);
        RESERVED.put("cshort", VisualBasicTokens.CSHORT_KEYWORD);
        RESERVED.put("csng", VisualBasicTokens.CSNG_KEYWORD);
        RESERVED.put("cstr", VisualBasicTokens.CSTR_KEYWORD);
        RESERVED.put("ctype", VisualBasicTokens.CTYPE_KEYWORD);
        RESERVED.put("cuint", VisualBasicTokens.CUINT_KEYWORD);
        RESERVED.put("culng", VisualBasicTokens.CULNG_KEYWORD);
        RESERVED.put("cushort", VisualBasicTokens.CUSHORT_KEYWORD);
        RESERVED.put("date", VisualBasicTokens.DATE_KEYWORD);
        RESERVED.put("decimal", VisualBasicTokens.DECIMAL_KEYWORD);
        RESERVED.put("declare", VisualBasicTokens.DECLARE_KEYWORD);
        RESERVED.put("default", VisualBasicTokens.DEFAULT_KEYWORD);
        RESERVED.put("delegate", VisualBasicTokens.DELEGATE_KEYWORD);
        RESERVED.put("dim", VisualBasicTokens.DIM_KEYWORD);
        RESERVED.put("directcast", VisualBasicTokens.DIRECTCAST_KEYWORD);
        RESERVED.put("do", VisualBasicTokens.DO_KEYWORD);
        RESERVED.put("double", VisualBasicTokens.DOUBLE_KEYWORD);
        RESERVED.put("each", VisualBasicTokens.EACH_KEYWORD);
        RESERVED.put("else", VisualBasicTokens.ELSE_KEYWORD);
        RESERVED.put("elseif", VisualBasicTokens.ELSEIF_KEYWORD);
        RESERVED.put("end", VisualBasicTokens.END_KEYWORD);
        RESERVED.put("enum", VisualBasicTokens.ENUM_KEYWORD);
        RESERVED.put("erase", VisualBasicTokens.ERASE_KEYWORD);
        RESERVED.put("error", VisualBasicTokens.ERROR_KEYWORD);
        RESERVED.put("event", VisualBasicTokens.EVENT_KEYWORD);
        RESERVED.put("exit", VisualBasicTokens.EXIT_KEYWORD);
        RESERVED.put("false", VisualBasicTokens.FALSE_KEYWORD);
        RESERVED.put("finally", VisualBasicTokens.FINALLY_KEYWORD);
        RESERVED.put("for", VisualBasicTokens.FOR_KEYWORD);
        RESERVED.put("friend", VisualBasicTokens.FRIEND_KEYWORD);
        RESERVED.put("function", VisualBasicTokens.FUNCTION_KEYWORD);
        RESERVED.put("get", VisualBasicTokens.GET_KEYWORD);
        RESERVED.put("gettype", VisualBasicTokens.GETTYPE_KEYWORD);
        RESERVED.put("getxmlnamespace", VisualBasicTokens.GETXMLNAMESPACE_KEYWORD);
        RESERVED.put("global", VisualBasicTokens.GLOBAL_KEYWORD);
        RESERVED.put("goto", VisualBasicTokens.GOTO_KEYWORD);
        RESERVED.put("handles", VisualBasicTokens.HANDLES_KEYWORD);
        RESERVED.put("if", VisualBasicTokens.IF_KEYWORD);
        RESERVED.put("implements", VisualBasicTokens.IMPLEMENTS_KEYWORD);
        RESERVED.put("imports", VisualBasicTokens.IMPORTS_KEYWORD);
        RESERVED.put("in", VisualBasicTokens.IN_KEYWORD);
        RESERVED.put("inherits", VisualBasicTokens.INHERITS_KEYWORD);
        RESERVED.put("integer", VisualBasicTokens.INTEGER_KEYWORD);
        RESERVED.put("interface", VisualBasicTokens.INTERFACE_KEYWORD);
        RESERVED.put("is", VisualBasicTokens.IS_KEYWORD);
        RESERVED.put("isnot", VisualBasicTokens.ISNOT_KEYWORD);
        RESERVED.put("let", VisualBasicTokens.LET_KEYWORD);
        RESERVED.put("lib", VisualBasicTokens.LIB_KEYWORD);
        RESERVED.put("like", VisualBasicTokens.LIKE_KEYWORD);
        RESERVED.put("long", VisualBasicTokens.LONG_KEYWORD);
        RESERVED.put("loop", VisualBasicTokens.LOOP_KEYWORD);
        RESERVED.put("me", VisualBasicTokens.ME_KEYWORD);
        RESERVED.put("mod", VisualBasicTokens.MOD_KEYWORD);
        RESERVED.put("module", VisualBasicTokens.MODULE_KEYWORD);
        RESERVED.put("mustinherit", VisualBasicTokens.MUSTINHERIT_KEYWORD);
        RESERVED.put("mustoverride", VisualBasicTokens.MUSTOVERRIDE_KEYWORD);
        RESERVED.put("mybase", VisualBasicTokens.MYBASE_KEYWORD);
        RESERVED.put("myclass", VisualBasicTokens.MYCLASS_KEYWORD);
        RESERVED.put("nameof", VisualBasicTokens.NAMEOF_KEYWORD);
        RESERVED.put("namespace", VisualBasicTokens.NAMESPACE_KEYWORD);
        RESERVED.put("narrowing", VisualBasicTokens.NARROWING_KEYWORD);
        RESERVED.put("next", VisualBasicTokens.NEXT_KEYWORD);
        RESERVED.put("new", VisualBasicTokens.NEW_KEYWORD);
        RESERVED.put("not", VisualBasicTokens.NOT_KEYWORD);
        RESERVED.put("nothing", VisualBasicTokens.NOTHING_KEYWORD);
        RESERVED.put("notinheritable", VisualBasicTokens.NOTINHERITABLE_KEYWORD);
        RESERVED.put("notoverridable", VisualBasicTokens.NOTOVERRIDABLE_KEYWORD);
        RESERVED.put("object", VisualBasicTokens.OBJECT_KEYWORD);
        RESERVED.put("of", VisualBasicTokens.OF_KEYWORD);
        RESERVED.put("on", VisualBasicTokens.ON_KEYWORD);
        RESERVED.put("operator", VisualBasicTokens.OPERATOR_KEYWORD);
        RESERVED.put("option", VisualBasicTokens.OPTION_KEYWORD);
        RESERVED.put("optional", VisualBasicTokens.OPTIONAL_KEYWORD);
        RESERVED.put("or", VisualBasicTokens.OR_KEYWORD);
        RESERVED.put("orelse", VisualBasicTokens.ORELSE_KEYWORD);
        RESERVED.put("overloads", VisualBasicTokens.OVERLOADS_KEYWORD);
        RESERVED.put("overridable", VisualBasicTokens.OVERRIDABLE_KEYWORD);
        RESERVED.put("overrides", VisualBasicTokens.OVERRIDES_KEYWORD);
        RESERVED.put("paramarray", VisualBasicTokens.PARAMARRAY_KEYWORD);
        RESERVED.put("partial", VisualBasicTokens.PARTIAL_KEYWORD);
        RESERVED.put("private", VisualBasicTokens.PRIVATE_KEYWORD);
        RESERVED.put("property", VisualBasicTokens.PROPERTY_KEYWORD);
        RESERVED.put("protected", VisualBasicTokens.PROTECTED_KEYWORD);
        RESERVED.put("public", VisualBasicTokens.PUBLIC_KEYWORD);
        RESERVED.put("raiseevent", VisualBasicTokens.RAISEEVENT_KEYWORD);
        RESERVED.put("readonly", VisualBasicTokens.READONLY_KEYWORD);
        RESERVED.put("redim", VisualBasicTokens.REDIM_KEYWORD);
        RESERVED.put("rem", VisualBasicTokens.REM_KEYWORD);
        RESERVED.put("removehandler", VisualBasicTokens.REMOVEHANDLER_KEYWORD);
        RESERVED.put("resume", VisualBasicTokens.RESUME_KEYWORD);
        RESERVED.put("return", VisualBasicTokens.RETURN_KEYWORD);
        RESERVED.put("sbyte", VisualBasicTokens.SBYTE_KEYWORD);
        RESERVED.put("select", VisualBasicTokens.SELECT_KEYWORD);
        RESERVED.put("set", VisualBasicTokens.SET_KEYWORD);
        RESERVED.put("shadows", VisualBasicTokens.SHADOWS_KEYWORD);
        RESERVED.put("shared", VisualBasicTokens.SHARED_KEYWORD);
        RESERVED.put("short", VisualBasicTokens.SHORT_KEYWORD);
        RESERVED.put("single", VisualBasicTokens.SINGLE_KEYWORD);
        RESERVED.put("static", VisualBasicTokens.STATIC_KEYWORD);
        RESERVED.put("step", VisualBasicTokens.STEP_KEYWORD);
        RESERVED.put("stop", VisualBasicTokens.STOP_KEYWORD);
        RESERVED.put("string", VisualBasicTokens.STRING_KEYWORD);
        RESERVED.put("structure", VisualBasicTokens.STRUCTURE_KEYWORD);
        RESERVED.put("sub", VisualBasicTokens.SUB_KEYWORD);
        RESERVED.put("synclock", VisualBasicTokens.SYNCLOCK_KEYWORD);
        RESERVED.put("then", VisualBasicTokens.THEN_KEYWORD);
        RESERVED.put("throw", VisualBasicTokens.THROW_KEYWORD);
        RESERVED.put("to", VisualBasicTokens.TO_KEYWORD);
        RESERVED.put("true", VisualBasicTokens.TRUE_KEYWORD);
        RESERVED.put("try", VisualBasicTokens.TRY_KEYWORD);
        RESERVED.put("trycast", VisualBasicTokens.TRYCAST_KEYWORD);
        RESERVED.put("typeof", VisualBasicTokens.TYPEOF_KEYWORD);
        RESERVED.put("uinteger", VisualBasicTokens.UINTEGER_KEYWORD);
        RESERVED.put("ulong", VisualBasicTokens.ULONG_KEYWORD);
        RESERVED.put("ushort", VisualBasicTokens.USHORT_KEYWORD);
        RESERVED.put("using", VisualBasicTokens.USING_KEYWORD);
        RESERVED.put("when", VisualBasicTokens.WHEN_KEYWORD);
        RESERVED.put("while", VisualBasicTokens.WHILE_KEYWORD);
        RESERVED.put("widening", VisualBasicTokens.WIDENING_KEYWORD);
        RESERVED.put("with", VisualBasicTokens.WITH_KEYWORD);
        RESERVED.put("withevents", VisualBasicTokens.WITHEVENTS_KEYWORD);
        RESERVED.put("writeonly", VisualBasicTokens.WRITEONLY_KEYWORD);
        RESERVED.put("xor", VisualBasicTokens.XOR_KEYWORD);
        RESERVED.put("endif", VisualBasicTokens.ENDIF_KEYWORD);
        RESERVED.put("gosub", VisualBasicTokens.GOSUB_KEYWORD);
        RESERVED.put("variant", VisualBasicTokens.VARIANT_KEYWORD);
        RESERVED.put("wend", VisualBasicTokens.WEND_KEYWORD);
        CONTEXTUAL.put("reference", VisualBasicTokens.REFERENCE_KEYWORD);
        CONTEXTUAL.put("aggregate", VisualBasicTokens.AGGREGATE_KEYWORD);
        CONTEXTUAL.put("all", VisualBasicTokens.ALL_KEYWORD);
        CONTEXTUAL.put("ansi", VisualBasicTokens.ANSI_KEYWORD);
        CONTEXTUAL.put("ascending", VisualBasicTokens.ASCENDING_KEYWORD);
        CONTEXTUAL.put("assembly", VisualBasicTokens.ASSEMBLY_KEYWORD);
        CONTEXTUAL.put("auto", VisualBasicTokens.AUTO_KEYWORD);
        CONTEXTUAL.put("binary", VisualBasicTokens.BINARY_KEYWORD);
        CONTEXTUAL.put("by", VisualBasicTokens.BY_KEYWORD);
        CONTEXTUAL.put("compare", VisualBasicTokens.COMPARE_KEYWORD);
        CONTEXTUAL.put("custom", VisualBasicTokens.CUSTOM_KEYWORD);
        CONTEXTUAL.put("descending", VisualBasicTokens.DESCENDING_KEYWORD);
        CONTEXTUAL.put("disable", VisualBasicTokens.DISABLE_KEYWORD);
        CONTEXTUAL.put("distinct", VisualBasicTokens.DISTINCT_KEYWORD);
        CONTEXTUAL.put("enable", VisualBasicTokens.ENABLE_KEYWORD);
        CONTEXTUAL.put("equals", VisualBasicTokens.EQUALS_KEYWORD);
        CONTEXTUAL.put("explicit", VisualBasicTokens.EXPLICIT_KEYWORD);
        CONTEXTUAL.put("externalsource", VisualBasicTokens.EXTERNALSOURCE_KEYWORD);
        CONTEXTUAL.put("externalchecksum", VisualBasicTokens.EXTERNALCHECKSUM_KEYWORD);
        CONTEXTUAL.put("from", VisualBasicTokens.FROM_KEYWORD);
        CONTEXTUAL.put("group", VisualBasicTokens.GROUP_KEYWORD);
        CONTEXTUAL.put("infer", VisualBasicTokens.INFER_KEYWORD);
        CONTEXTUAL.put("into", VisualBasicTokens.INTO_KEYWORD);
        CONTEXTUAL.put("isfalse", VisualBasicTokens.ISFALSE_KEYWORD);
        CONTEXTUAL.put("istrue", VisualBasicTokens.ISTRUE_KEYWORD);
        CONTEXTUAL.put("join", VisualBasicTokens.JOIN_KEYWORD);
        CONTEXTUAL.put("key", VisualBasicTokens.KEY_KEYWORD);
        CONTEXTUAL.put("mid", VisualBasicTokens.MID_KEYWORD);
        CONTEXTUAL.put("off", VisualBasicTokens.OFF_KEYWORD);
        CONTEXTUAL.put("order", VisualBasicTokens.ORDER_KEYWORD);
        CONTEXTUAL.put("out", VisualBasicTokens.OUT_KEYWORD);
        CONTEXTUAL.put("preserve", VisualBasicTokens.PRESERVE_KEYWORD);
        CONTEXTUAL.put("region", VisualBasicTokens.REGION_KEYWORD);
        CONTEXTUAL.put("skip", VisualBasicTokens.SKIP_KEYWORD);
        CONTEXTUAL.put("strict", VisualBasicTokens.STRICT_KEYWORD);
        CONTEXTUAL.put("text", VisualBasicTokens.TEXT_KEYWORD);
        CONTEXTUAL.put("take", VisualBasicTokens.TAKE_KEYWORD);
        CONTEXTUAL.put("unicode", VisualBasicTokens.UNICODE_KEYWORD);
        CONTEXTUAL.put("until", VisualBasicTokens.UNTIL_KEYWORD);
        CONTEXTUAL.put("warning", VisualBasicTokens.WARNING_KEYWORD);
        CONTEXTUAL.put("where", VisualBasicTokens.WHERE_KEYWORD);
        CONTEXTUAL.put("async", VisualBasicTokens.ASYNC_KEYWORD);
        CONTEXTUAL.put("await", VisualBasicTokens.AWAIT_KEYWORD);
        CONTEXTUAL.put("iterator", VisualBasicTokens.ITERATOR_KEYWORD);
        CONTEXTUAL.put("yield", VisualBasicTokens.YIELD_KEYWORD);
        CONTEXTUAL.put("type", VisualBasicTokens.TYPE_KEYWORD);
    }

    private VisualBasicKeywords() {
    }

    /**
     * @return reserved keyword token for the identifier text, or {@code null}
     */
    public static @Nullable IElementType findReserved(CharSequence text) {
        if (text.length() > 16) {
            return null;
        }
        return RESERVED.get(text.toString().toLowerCase(Locale.ROOT));
    }

    /**
     * @return contextual keyword token for the identifier text, or {@code null}
     */
    public static @Nullable IElementType findContextual(CharSequence text) {
        if (text.length() > 16) {
            return null;
        }
        return CONTEXTUAL.get(text.toString().toLowerCase(Locale.ROOT));
    }
}
