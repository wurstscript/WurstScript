package tests.wurstscript.tests;


import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.testng.Assert.assertEquals;

public class DependencyFileParserTest {

    @Test
    public void testDepFileParsing() throws IOException {
        File projectFolder = new File("./temp/testDepFileParsing/");
        newCleanFolder(projectFolder);
        File a = new File(projectFolder, "a");
        File b = new File(projectFolder, "b");
        File c = new File(projectFolder, "c");

        a.mkdirs();
        //b.mkdirs();
        c.mkdirs();

        File dep = new File(projectFolder, "wurst.dependencies");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(dep))) {
            w.write(a.getAbsolutePath() + "\n");
            w.write(b.getAbsolutePath() + "\n");
            w.write(c.getAbsolutePath() + "\n");
        }


        WurstGui gui = new WurstGuiLogger();
        WurstCompilerJassImpl comp = new WurstCompilerJassImpl(projectFolder, gui, null, new RunArgs());
        comp.loadWurstFilesInDir(projectFolder);

        assertEquals(gui.getErrorList().size(), 1);
        CompileError err = gui.getErrorList().get(0);
        assertEquals(err.getSource().getLine(), 2);
        assertEquals(err.getSource().getStartColumn(), 1);
        assertEquals(err.getSource().getEndLine(), 2);
        assertEquals(err.getSource().getEndColumn(), b.getAbsolutePath().length() + 1);

        PublishDiagnosticsParams diag = Convert.createDiagnostics("", WFile.create(dep), gui.getErrorList());
        assertEquals(diag.getDiagnostics().size(), 1);
        Diagnostic d = diag.getDiagnostics().get(0);
        assertEquals(d.getRange().getStart().getLine(), 1);
        assertEquals(d.getRange().getStart().getCharacter(), 0);
        assertEquals(d.getRange().getEnd().getLine(), 1);
        assertEquals(d.getRange().getEnd().getCharacter(), b.getAbsolutePath().length());

    }

    private void newCleanFolder(File f) throws IOException {
        FileUtils.deleteRecursively(f);
        Files.createDirectories(f.toPath());
    }

}
