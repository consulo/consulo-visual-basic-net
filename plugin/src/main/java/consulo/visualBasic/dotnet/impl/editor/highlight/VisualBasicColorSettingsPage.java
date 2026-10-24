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

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;
import consulo.util.lang.lazy.LazyValue;
import consulo.visualBasic.dotnet.impl.localize.VisualBasicColorsLocalize;
import consulo.visualBasic.dotnet.localize.VisualBasicLocalize;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Settings / Editor / Colors &amp; Fonts / Visual Basic (.NET). Names colored by resolve are tagged in the demo text.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
@ExtensionImpl
public class VisualBasicColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = {
        new AttributesDescriptor(VisualBasicColorsLocalize.colorKeyword(), VisualBasicHighlightKey.KEYWORD),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorContextualKeyword(), VisualBasicHighlightKey.CONTEXTUAL_KEYWORD),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorIdentifier(), VisualBasicHighlightKey.IDENTIFIER),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorNumber(), VisualBasicHighlightKey.NUMBER),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorString(), VisualBasicHighlightKey.STRING),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorDate(), VisualBasicHighlightKey.DATE),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorComment(), VisualBasicHighlightKey.COMMENT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorDocComment(), VisualBasicHighlightKey.DOC_COMMENT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorPreprocessorDirective(), VisualBasicHighlightKey.PREPROCESSOR_DIRECTIVE),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorDisabledCode(), VisualBasicHighlightKey.DISABLED_CODE),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorOperationSign(), VisualBasicHighlightKey.OPERATION_SIGN),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorParentheses(), VisualBasicHighlightKey.PARENTHESES),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorBraces(), VisualBasicHighlightKey.BRACES),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorDot(), VisualBasicHighlightKey.DOT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorComma(), VisualBasicHighlightKey.COMMA),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorLineContinuation(), VisualBasicHighlightKey.LINE_CONTINUATION),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorXmlTag(), VisualBasicHighlightKey.XML_TAG),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorXmlAttribute(), VisualBasicHighlightKey.XML_ATTRIBUTE),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorXmlAttributeValue(), VisualBasicHighlightKey.XML_ATTRIBUTE_VALUE),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorXmlText(), VisualBasicHighlightKey.XML_TEXT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorXmlComment(), VisualBasicHighlightKey.XML_COMMENT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorXmlEmbedded(), VisualBasicHighlightKey.XML_EMBEDDED),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorBadCharacter(), VisualBasicHighlightKey.BAD_CHARACTER),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorClassName(), VisualBasicHighlightKey.CLASS_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorModuleName(), VisualBasicHighlightKey.MODULE_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorStructureName(), VisualBasicHighlightKey.STRUCTURE_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorInterfaceName(), VisualBasicHighlightKey.INTERFACE_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorEnumName(), VisualBasicHighlightKey.ENUM_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorDelegateName(), VisualBasicHighlightKey.DELEGATE_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorAttributeName(), VisualBasicHighlightKey.ATTRIBUTE_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorGenericParameterName(), VisualBasicHighlightKey.GENERIC_PARAMETER_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorMethodName(), VisualBasicHighlightKey.METHOD_NAME),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorStaticMethodCall(), VisualBasicHighlightKey.STATIC_METHOD_CALL),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorInstanceMethodCall(), VisualBasicHighlightKey.INSTANCE_METHOD_CALL),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorConstant(), VisualBasicHighlightKey.CONSTANT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorStaticField(), VisualBasicHighlightKey.STATIC_FIELD),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorInstanceField(), VisualBasicHighlightKey.INSTANCE_FIELD),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorStaticProperty(), VisualBasicHighlightKey.STATIC_PROPERTY),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorInstanceProperty(), VisualBasicHighlightKey.INSTANCE_PROPERTY),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorStaticEvent(), VisualBasicHighlightKey.STATIC_EVENT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorInstanceEvent(), VisualBasicHighlightKey.INSTANCE_EVENT),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorParameter(), VisualBasicHighlightKey.PARAMETER),
        new AttributesDescriptor(VisualBasicColorsLocalize.colorLocalVariable(), VisualBasicHighlightKey.LOCAL_VARIABLE),
    };

    // tags of the demo text for what the highlight visitor colors
    private static final Map<String, TextAttributesKey> TAGS = Map.ofEntries(
        Map.entry("contextual_keyword", VisualBasicHighlightKey.CONTEXTUAL_KEYWORD),
        Map.entry("disabled", VisualBasicHighlightKey.DISABLED_CODE),
        Map.entry("class_name", VisualBasicHighlightKey.CLASS_NAME),
        Map.entry("module_name", VisualBasicHighlightKey.MODULE_NAME),
        Map.entry("structure_name", VisualBasicHighlightKey.STRUCTURE_NAME),
        Map.entry("interface_name", VisualBasicHighlightKey.INTERFACE_NAME),
        Map.entry("enum_name", VisualBasicHighlightKey.ENUM_NAME),
        Map.entry("delegate_name", VisualBasicHighlightKey.DELEGATE_NAME),
        Map.entry("attribute_name", VisualBasicHighlightKey.ATTRIBUTE_NAME),
        Map.entry("generic_parameter_name", VisualBasicHighlightKey.GENERIC_PARAMETER_NAME),
        Map.entry("method_name", VisualBasicHighlightKey.METHOD_NAME),
        Map.entry("static_call", VisualBasicHighlightKey.STATIC_METHOD_CALL),
        Map.entry("instance_call", VisualBasicHighlightKey.INSTANCE_METHOD_CALL),
        Map.entry("constant", VisualBasicHighlightKey.CONSTANT),
        Map.entry("static_field", VisualBasicHighlightKey.STATIC_FIELD),
        Map.entry("instance_field", VisualBasicHighlightKey.INSTANCE_FIELD),
        Map.entry("static_property", VisualBasicHighlightKey.STATIC_PROPERTY),
        Map.entry("instance_property", VisualBasicHighlightKey.INSTANCE_PROPERTY),
        Map.entry("static_event", VisualBasicHighlightKey.STATIC_EVENT),
        Map.entry("instance_event", VisualBasicHighlightKey.INSTANCE_EVENT),
        Map.entry("parameter", VisualBasicHighlightKey.PARAMETER),
        Map.entry("local_var", VisualBasicHighlightKey.LOCAL_VARIABLE)
    );

    private final Supplier<String> myDemoText = LazyValue.notNull(VisualBasicColorSettingsPage::loadDemoText);

    private static String loadDemoText() {
        try (InputStream stream = VisualBasicColorSettingsPage.class.getResourceAsStream("VisualBasicColorSettingsDemo.vb")) {
            if (stream == null) {
                throw new IllegalStateException("VisualBasicColorSettingsDemo.vb not found");
            }
            // the editor works with \n line separators
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public SyntaxHighlighter getHighlighter() {
        return new VisualBasicSyntaxHighlighter();
    }

    @Override
    public String getDemoText() {
        return myDemoText.get();
    }

    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return TAGS;
    }

    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public LocalizeValue getDisplayName() {
        return VisualBasicLocalize.languageName();
    }
}
