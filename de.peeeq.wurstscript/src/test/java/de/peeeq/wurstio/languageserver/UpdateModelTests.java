package de.peeeq.wurstio.languageserver;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Putting a file into the model reconciles it with the units which are there: what imports its packages (or sees its
 * Jass names) has to be checked again. A load of a project starts with no unit checked and every unit it adds is
 * unchecked, so there is nothing to find out: the reconciliation looked at the whole model for each file, and found
 * what was unchecked already.
 */
public class UpdateModelTests {

    @Test
    public void aFreshLoadReconcilesNoFileWithTheOthersAndLeavesEveryUnitUnchecked() throws IOException {
        Path root = ParallelLoadTests.project(false);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());

        manager.loadProject();

        assertNotNull(manager.getModel());
        assertEquals(manager.uncheckedCount(), manager.getModel().size(),
            "a load leaves every unit unchecked, common.j and blizzard.j and the files of the project");
        assertEquals(manager.reconciliations(), 0, "no file of a fresh load needs to be reconciled with the others");
    }

    @Test
    public void theLibrariesOfAFreshBuildAreAddedWithoutReconciliation() throws IOException {
        Path root = ParallelLoadTests.project(false);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());

        manager.buildProject();

        assertTrue(manager.getModel().size() > 40, "the project and its libraries are in the model");
        assertEquals(manager.reconciliations(), 0, "the units of a fresh build are all unchecked while it loads");
    }

    @Test
    public void aJassFileAddedAfterACheckMakesEveryUnitUncheckedAgain() throws IOException {
        Path root = ParallelLoadTests.project(false);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        manager.buildProject();
        assertEquals(manager.uncheckedCount(), 0, "the build checked every unit");
        int before = manager.reconciliations();
        WFile jass = WFile.create(root.resolve("wurst/extra.j").toFile());
        Files.writeString(jass.getFile().toPath(), "function JassNewFunc takes nothing returns nothing\nendfunction\n");

        manager.syncCompilationUnitContent(jass, Files.readString(jass.getFile().toPath()));

        assertEquals(manager.reconciliations(), before + 1, "a file added to a checked model is reconciled with it");
        assertEquals(manager.uncheckedCount(), manager.getModel().size(),
            "plain Jass names are visible everywhere: every unit has to be checked again");
    }

    /** Only a model whose units are all unchecked may skip the reconciliation, not one which is partly checked. */
    @Test
    public void aFileAddedWhileSomeUnitsAreUncheckedIsReconciled() throws IOException {
        Path root = ParallelLoadTests.project(false);
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        manager.buildProject();
        WFile p00 = WFile.create(root.resolve("wurst/P00.wurst").toFile());
        manager.syncCompilationUnitContent(p00, Files.readString(root.resolve("wurst/P00.wurst")) + "// edited\n");
        int unchecked = manager.uncheckedCount();
        assertTrue(unchecked > 0 && unchecked < manager.getModel().size(), "a partly checked model: " + unchecked);

        // a second definition of P15: the other P15 and everything importing it (P14 .. P00) must be checked again
        Path dup = root.resolve("wurst/dup/P15.wurst");
        String text = "package P15\nimport NoWurst\npublic function p15() returns int\n    return 0\n";
        Files.createDirectories(dup.getParent());
        Files.writeString(dup, text);
        int before = manager.reconciliations();
        manager.syncCompilationUnitContent(WFile.create(dup.toFile()), text);

        assertEquals(manager.reconciliations(), before + 1, "a partly checked model reconciles a new unit");
        assertTrue(manager.uncheckedCount() >= 17,
            "the new unit, the other P15 and P14 .. P00: " + manager.uncheckedCount());
    }
}
