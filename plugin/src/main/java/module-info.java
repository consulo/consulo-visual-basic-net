import org.jspecify.annotations.NullMarked;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
@NullMarked
module consulo.visualBasic.dotnet {
    requires consulo.visualBasic.dotnet.api;

    requires consulo.dotnet.psi.impl;
    requires consulo.dotnet.psi.api;
    requires consulo.dotnet.msil.impl;

    requires consulo.code.editor.api;
    requires consulo.color.scheme.api;
    requires consulo.language.api;
    requires consulo.language.editor.api;
    requires consulo.language.impl;
    requires consulo.localize.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.virtual.file.system.api;

    exports consulo.visualBasic.dotnet.impl;
    exports consulo.visualBasic.dotnet.impl.editor.highlight;
    exports consulo.visualBasic.dotnet.impl.localize;
    exports consulo.visualBasic.dotnet.impl.lang;
    exports consulo.visualBasic.dotnet.impl.lang.lexer;
    exports consulo.visualBasic.dotnet.impl.lang.moduleAware;
    exports consulo.visualBasic.dotnet.impl.lang.parser;
    exports consulo.visualBasic.dotnet.impl.lang.parser.preprocessor;
    exports consulo.visualBasic.dotnet.impl.lang.psi;
    exports consulo.visualBasic.dotnet.impl.lang.psi.impl;
    exports consulo.visualBasic.dotnet.impl.lang.psi.partial;
    exports consulo.visualBasic.dotnet.impl.lang.psi.resolve;
    exports consulo.visualBasic.dotnet.impl.lang.psi.resolve.dotnet;
    exports consulo.visualBasic.dotnet.impl.lang.psi.stub;
    exports consulo.visualBasic.dotnet.impl.lang.psi.stub.elementTypes;
    exports consulo.visualBasic.dotnet.impl.lang.psi.stub.index;
}
