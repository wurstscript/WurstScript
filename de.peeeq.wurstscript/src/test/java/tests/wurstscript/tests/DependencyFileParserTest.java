package tests.wurstscript.tests;


import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.utils.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DependencyFileParserTest {

    @Test
    public void testAddDependenciesFromBuildFolder() throws IOException {
        File projectFolder = new File("./temp/testAddDependenciesFromBuildFolder/");
        newCleanFolder(projectFolder);
        File depsRoot = new File(new File(projectFolder, "_build"), "dependencies");
        File a = new File(depsRoot, "a");
        File b = new File(depsRoot, "b");
        File ignoredFile = new File(depsRoot, "not-a-dir.txt");

        a.mkdirs();
        b.mkdirs();
        Files.writeString(ignoredFile.toPath(), "x");

        List<File> dependencies = new ArrayList<>();
        WurstCompilerJassImpl.addDependenciesFromFolder(projectFolder, dependencies);

        assertEquals(dependencies.size(), 2);
        assertTrue(containsSameFile(dependencies, a));
        assertTrue(containsSameFile(dependencies, b));
    }

    @Test
    public void testAddDependenciesFromBuildFolderDoesNotDuplicate() throws IOException {
        File projectFolder = new File("./temp/testAddDependenciesFromBuildFolderDoesNotDuplicate/");
        newCleanFolder(projectFolder);
        File depsRoot = new File(new File(projectFolder, "_build"), "dependencies");
        File a = new File(depsRoot, "a");
        a.mkdirs();

        List<File> dependencies = new ArrayList<>();
        dependencies.add(a);

        WurstCompilerJassImpl.addDependenciesFromFolder(projectFolder, dependencies);
        assertEquals(dependencies.size(), 1);
    }

    private boolean containsSameFile(List<File> files, File expected) {
        for (File f : files) {
            if (FileUtils.sameFile(f, expected)) {
                return true;
            }
        }
        return false;
    }

    private void newCleanFolder(File f) throws IOException {
        FileUtils.deleteRecursively(f);
        Files.createDirectories(f.toPath());
    }

}
