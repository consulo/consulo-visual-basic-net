import org.jspecify.annotations.NullMarked;

/**
 * @author VISTALL
 * @since 2026-10-23
 */
@NullMarked
module consulo.visualBasic.dotnet.api {
    requires transitive consulo.dotnet.api;
    requires transitive consulo.dotnet.psi.api;

    requires consulo.language.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires org.jdom;
    requires consulo.localize.api;
    requires consulo.ui.api;
    requires consulo.virtual.file.system.api;

    exports consulo.visualBasic.dotnet;
    exports consulo.visualBasic.dotnet.icon;
    exports consulo.visualBasic.dotnet.localize;
    exports consulo.visualBasic.dotnet.module;
    exports consulo.visualBasic.dotnet.module.extension;
}
