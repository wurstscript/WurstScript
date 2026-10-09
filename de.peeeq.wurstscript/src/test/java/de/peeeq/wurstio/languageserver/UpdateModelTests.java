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
}
