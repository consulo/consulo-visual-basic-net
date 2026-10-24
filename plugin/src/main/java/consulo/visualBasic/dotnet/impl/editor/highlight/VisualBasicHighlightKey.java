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

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
public interface VisualBasicHighlightKey {
    TextAttributesKey KEYWORD = TextAttributesKey.of("VISUAL_BASIC_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    TextAttributesKey CONTEXTUAL_KEYWORD = TextAttributesKey.of("VISUAL_BASIC_CONTEXTUAL_KEYWORD", KEYWORD);
    TextAttributesKey IDENTIFIER = TextAttributesKey.of("VISUAL_BASIC_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    TextAttributesKey NUMBER = TextAttributesKey.of("VISUAL_BASIC_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    TextAttributesKey STRING = TextAttributesKey.of("VISUAL_BASIC_STRING", DefaultLanguageHighlighterColors.STRING);
    TextAttributesKey DATE = TextAttributesKey.of("VISUAL_BASIC_DATE", DefaultLanguageHighlighterColors.NUMBER);
    TextAttributesKey COMMENT = TextAttributesKey.of("VISUAL_BASIC_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    TextAttributesKey DOC_COMMENT = TextAttributesKey.of("VISUAL_BASIC_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);
    TextAttributesKey PREPROCESSOR_DIRECTIVE = TextAttributesKey.of("VISUAL_BASIC_PREPROCESSOR_DIRECTIVE", DefaultLanguageHighlighterColors.MACRO_KEYWORD);
    TextAttributesKey OPERATION_SIGN = TextAttributesKey.of("VISUAL_BASIC_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    TextAttributesKey PARENTHESES = TextAttributesKey.of("VISUAL_BASIC_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    TextAttributesKey BRACES = TextAttributesKey.of("VISUAL_BASIC_BRACES", DefaultLanguageHighlighterColors.BRACES);
    TextAttributesKey DOT = TextAttributesKey.of("VISUAL_BASIC_DOT", DefaultLanguageHighlighterColors.DOT);
    TextAttributesKey COMMA = TextAttributesKey.of("VISUAL_BASIC_COMMA", DefaultLanguageHighlighterColors.COMMA);
    TextAttributesKey LINE_CONTINUATION = TextAttributesKey.of("VISUAL_BASIC_LINE_CONTINUATION", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    TextAttributesKey DISABLED_CODE = TextAttributesKey.of("VISUAL_BASIC_DISABLED_CODE", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    TextAttributesKey XML_TAG = TextAttributesKey.of("VISUAL_BASIC_XML_TAG", DefaultLanguageHighlighterColors.MARKUP_TAG);
    TextAttributesKey XML_ATTRIBUTE = TextAttributesKey.of("VISUAL_BASIC_XML_ATTRIBUTE", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE);
    TextAttributesKey XML_ATTRIBUTE_VALUE = TextAttributesKey.of("VISUAL_BASIC_XML_ATTRIBUTE_VALUE", DefaultLanguageHighlighterColors.STRING);
    TextAttributesKey XML_TEXT = TextAttributesKey.of("VISUAL_BASIC_XML_TEXT", DefaultLanguageHighlighterColors.MARKUP_ENTITY);
    TextAttributesKey XML_COMMENT = TextAttributesKey.of("VISUAL_BASIC_XML_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    TextAttributesKey XML_EMBEDDED = TextAttributesKey.of("VISUAL_BASIC_XML_EMBEDDED", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR);
    TextAttributesKey BAD_CHARACTER = TextAttributesKey.of("VISUAL_BASIC_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    // names, colored by what they declare or resolve to (fallbacks as in C#)
    TextAttributesKey CLASS_NAME = TextAttributesKey.of("VISUAL_BASIC_CLASS_NAME", DefaultLanguageHighlighterColors.CLASS_NAME);
    TextAttributesKey MODULE_NAME = TextAttributesKey.of("VISUAL_BASIC_MODULE_NAME", CLASS_NAME);
    TextAttributesKey STRUCTURE_NAME = TextAttributesKey.of("VISUAL_BASIC_STRUCTURE_NAME", CLASS_NAME);
    TextAttributesKey INTERFACE_NAME = TextAttributesKey.of("VISUAL_BASIC_INTERFACE_NAME", DefaultLanguageHighlighterColors.INTERFACE_NAME);
    TextAttributesKey ENUM_NAME = TextAttributesKey.of("VISUAL_BASIC_ENUM_NAME", CLASS_NAME);
    TextAttributesKey DELEGATE_NAME = TextAttributesKey.of("VISUAL_BASIC_DELEGATE_NAME", CLASS_NAME);
    TextAttributesKey ATTRIBUTE_NAME = TextAttributesKey.of("VISUAL_BASIC_ATTRIBUTE_NAME", DefaultLanguageHighlighterColors.METADATA);
    TextAttributesKey GENERIC_PARAMETER_NAME = TextAttributesKey.of("VISUAL_BASIC_GENERIC_PARAMETER_NAME", DefaultLanguageHighlighterColors.TYPE_ALIAS_NAME);
    TextAttributesKey METHOD_NAME = TextAttributesKey.of("VISUAL_BASIC_METHOD_NAME", DefaultLanguageHighlighterColors.INSTANCE_METHOD);
    TextAttributesKey STATIC_METHOD_CALL = TextAttributesKey.of("VISUAL_BASIC_STATIC_METHOD_CALL", DefaultLanguageHighlighterColors.STATIC_METHOD);
    TextAttributesKey INSTANCE_METHOD_CALL = TextAttributesKey.of("VISUAL_BASIC_INSTANCE_METHOD_CALL", DefaultLanguageHighlighterColors.IDENTIFIER);
    TextAttributesKey CONSTANT = TextAttributesKey.of("VISUAL_BASIC_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    TextAttributesKey STATIC_FIELD = TextAttributesKey.of("VISUAL_BASIC_STATIC_FIELD", DefaultLanguageHighlighterColors.STATIC_FIELD);
    TextAttributesKey STATIC_PROPERTY = TextAttributesKey.of("VISUAL_BASIC_STATIC_PROPERTY", STATIC_FIELD);
    TextAttributesKey STATIC_EVENT = TextAttributesKey.of("VISUAL_BASIC_STATIC_EVENT", STATIC_FIELD);
    TextAttributesKey INSTANCE_FIELD = TextAttributesKey.of("VISUAL_BASIC_INSTANCE_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    TextAttributesKey INSTANCE_PROPERTY = TextAttributesKey.of("VISUAL_BASIC_INSTANCE_PROPERTY", INSTANCE_FIELD);
    TextAttributesKey INSTANCE_EVENT = TextAttributesKey.of("VISUAL_BASIC_INSTANCE_EVENT", INSTANCE_FIELD);
    TextAttributesKey PARAMETER = TextAttributesKey.of("VISUAL_BASIC_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    TextAttributesKey LOCAL_VARIABLE = TextAttributesKey.of("VISUAL_BASIC_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
}
