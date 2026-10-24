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

package consulo.visualBasic.dotnet.it;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.library.Library;
import consulo.it.HeadlessModules;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.extension.MutableModuleExtension;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import consulo.visualBasic.dotnet.it.extension.TestVisualBasicModuleExtensionProvider;
import consulo.visualBasic.dotnet.it.extension.TestVisualBasicMutableModuleExtension;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A project with one mono .NET module (mono-dotnet, and mono-csharp when C# is on the classpath) over Visual Basic files, with mscorlib of mono 4.5 (decompiled to msil,
 * from {@code mockSdk} in the repository root), indexed and ready to resolve in.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public final class VisualBasicTesting {
    private VisualBasicTesting() {
    }

    public static PsiFile openSource(HeadlessProjects projects, String fileName, String text) throws Exception {
        return openSources(projects, Map.of(fileName, text)).get(fileName);
    }

    /**
     * Several files in the source root of the module, by file name.
     */
    public static Map<String, PsiFile> openSources(HeadlessProjects projects, Map<String, String> files) throws Exception {
        return openSources(projects, files, extension -> {
        });
    }

    /**
     * @param settings Visual Basic settings of the module
     */
    public static Map<String, PsiFile> openSources(HeadlessProjects projects,
                                                   Map<String, String> files,
                                                   Consumer<TestVisualBasicMutableModuleExtension> settings) throws Exception {
        Path directory = projects.newDirectory();
        Path srcDirectory = Files.createDirectories(directory.resolve("src"));
        for (Map.Entry<String, String> file : files.entrySet()) {
            Files.writeString(srcDirectory.resolve(file.getKey()), file.getValue(), StandardCharsets.UTF_8);
        }

        Project project = projects.open(directory);

        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
        assertThat(root).isNotNull();
        VirtualFile srcRoot = root.findChild("src");
        assertThat(srcRoot).isNotNull();

        Module module = HeadlessModules.createModule(project, "main", root);
        HeadlessModules.addSourceRoot(module, srcRoot);
        setupModule(module, mockSdk().resolve("mscorlib.zip"), settings);

        HeadlessProjects.awaitIdle(project);

        Map<String, PsiFile> result = new LinkedHashMap<>();
        for (String fileName : files.keySet()) {
            VirtualFile file = srcRoot.findChild(fileName);
            assertThat(file).as(fileName).isNotNull();
            PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(project).findFile(file));
            assertThat(psiFile).as(fileName).isNotNull();
            result.put(fileName, psiFile);
        }
        return result;
    }

    private static Path mockSdk() {
        // surefire runs with the module directory as the working directory
        Path directory = Path.of("").toAbsolutePath().getParent().resolve("mockSdk").resolve("mono4.5");
        assertThat(directory).isDirectory();
        return directory;
    }

    private static void setupModule(Module module, Path library, Consumer<TestVisualBasicMutableModuleExtension> settings) {
        WriteAction.run(() -> {
            ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

            MutableModuleExtension<?> dotnet = rootModel.getExtensionWithoutCheck("mono-dotnet");
            assertThat(dotnet).as("mono-dotnet extension").isNotNull();
            dotnet.setEnabled(true);

            VirtualFile localFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(library);
            assertThat(localFile).as(library.toString()).isNotNull();
            VirtualFile archiveRoot = ArchiveVfsUtil.getArchiveRootForLocalFile(localFile);
            assertThat(archiveRoot).as("archive root of " + library).isNotNull();

            Library moduleLibrary = rootModel.getModuleLibraryTable().createLibrary(library.getFileName().toString());
            Library.ModifiableModel libraryModel = moduleLibrary.getModifiableModel();
            libraryModel.addRoot(archiveRoot, BinariesOrderRootType.ID);
            libraryModel.commit();

            TestVisualBasicMutableModuleExtension visualBasic = rootModel.getExtensionWithoutCheck(TestVisualBasicModuleExtensionProvider.ID);
            assertThat(visualBasic).as("Visual Basic extension").isNotNull();
            visualBasic.setEnabled(true);
            settings.accept(visualBasic);

            // C# files of interop tests
            MutableModuleExtension<?> csharp = rootModel.getExtensionWithoutCheck("mono-csharp");
            if (csharp != null) {
                csharp.setEnabled(true);
            }

            rootModel.commit();
        });
    }

    public static Path resource(String path) throws URISyntaxException {
        URL url = VisualBasicTesting.class.getClassLoader().getResource(path);
        assertThat(url).as(path).isNotNull();
        return Path.of(url.toURI());
    }

    /**
     * Compares with the gold file of a test resource, read from the source tree: copies on the test classpath may be
     * stale. A missing gold file is written from {@code actual}, and the test fails so it gets reviewed.
     *
     * @param gold resource path, as {@code resolve/TypeNames.txt}
     */
    public static void assertGold(String gold, String actual) throws Exception {
        // surefire runs with the module directory as the working directory
        Path source = Path.of("src/test/resources").resolve(gold);
        if (!Files.exists(source)) {
            Files.writeString(source, actual + "\n", StandardCharsets.UTF_8);
            throw new AssertionError("gold file created: " + source);
        }
        assertThat(actual).isEqualTo(Files.readString(source, StandardCharsets.UTF_8).replace("\r\n", "\n").strip());
    }
}
