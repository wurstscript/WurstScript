package de.peeeq.wurstio.map.importer;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Drives the import cache against a real MPQ, the way every run and build does: the same cached map is
 * imported into again and again while the files under imports/ change in between.
 */
public class ImportFileEditorTests {
    private Path project;
    private File mpq;

    @BeforeMethod
    public void setup() throws Exception {
        project = Files.createTempDirectory("wurst-import-editor");
        mpq = project.resolve("cached.w3x").toFile();
        MpqEditorFactory.createEmptyArchive(mpq);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup() throws IOException {
        FileUtils.deleteRecursively(project.toFile());
    }

    private void importFiles() throws Exception {
        try (MpqEditor editor = MpqEditorFactory.getEditor(Optional.of(mpq))) {
            ImportFile.importFilesFromImports(project.toFile(), editor);
        }
    }

    private String contentInMpq(String name) throws Exception {
        try (MpqEditor editor = MpqEditorFactory.getEditor(Optional.of(mpq), true)) {
            if (!editor.hasFile(name)) {
                return null;
            }
            return new String(editor.extractFile(name), StandardCharsets.UTF_8);
        }
    }

    private Path write(String relative, String content, long modified) throws IOException {
        Path file = project.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(modified));
        return file;
    }

    @Test
    public void changedImportReplacesTheCachedCopy() throws Exception {
        write("imports/ReplaceableTextures/a.blp", "v1", 1_000_000L);
        importFiles();
        assertEquals(contentInMpq("ReplaceableTextures\\a.blp"), "v1");

        write("imports/ReplaceableTextures/a.blp", "v2", 2_000_000L);
        importFiles();
        assertEquals(contentInMpq("ReplaceableTextures\\a.blp"), "v2");
    }

    @Test
    public void removedImportLeavesTheMap() throws Exception {
        Path file = write("imports/war3mapImported/a.blp", "v1", 1_000_000L);
        importFiles();
        assertEquals(contentInMpq("war3mapImported\\a.blp"), "v1");

        Files.delete(file);
        importFiles();
        assertFalse(contentInMpq("war3mapImported\\a.blp") != null, "the removed import is still in the map");
    }

    @Test
    public void importRenamedOnlyByCaseKeepsItsFile() throws Exception {
        Path old = write("imports/Textures/Foo.blp", "v1", 1_000_000L);
        importFiles();
        assertEquals(contentInMpq("Textures\\Foo.blp"), "v1");

        // The same file under a differently cased name: MPQ names are case insensitive.
        Path tmp = project.resolve("imports/Textures/Foo.tmp");
        Files.move(old, tmp);
        Files.move(tmp, project.resolve("imports/Textures/foo.blp"));
        importFiles();
        assertEquals(contentInMpq("Textures\\foo.blp"), "v1",
            "the renamed import was dropped from the map");
    }

    @Test
    public void projectImportOverridesADependencyImportOfTheSamePath() throws Exception {
        write("_build/dependencies/dep/imports/Textures/x.blp", "dependency", 1_000_000L);
        write("imports/Textures/x.blp", "project", 1_000_000L);
        importFiles();
        assertEquals(contentInMpq("Textures\\x.blp"), "project");
    }

    @Test
    public void importedFilesAreKeptAcrossRepeatedUnchangedImports() throws Exception {
        write("imports/war3mapImported/a.blp", "v1", 1_000_000L);
        write("_build/dependencies/dep/imports/war3mapImported/b.dds", "dep", 1_000_000L);
        importFiles();
        importFiles();
        importFiles();
        assertEquals(contentInMpq("war3mapImported\\a.blp"), "v1");
        assertEquals(contentInMpq("war3mapImported\\b.dds"), "dep");
        assertTrue(contentInMpq("war3map.imp") != null, "war3map.imp is missing");
    }
}
