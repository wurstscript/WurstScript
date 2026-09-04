package tests.prettyprint;

import de.peeeq.wurstscript.attributes.prettyPrint.PrettyUtils;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Argument dispatch for the -prettyPrint CLI entry point.
 *
 * <p>The "..." branch used to compare the argument List itself to a String, so it was never taken
 * and "..." was treated as a file name instead; readFile then swallowed the resulting
 * FileNotFoundException, so the mistake produced no visible failure.
 */
public class PrettyUtilsArgsTest {

    @Test
    public void tripleDotSelectsDirectoryFormatting() {
        assertEquals(PrettyUtils.PrettyAction.ALL,
            PrettyUtils.selectAction(Collections.singletonList("...")));
    }

    @Test
    public void aFileNameIsNotTreatedAsDirectoryFormatting() {
        assertEquals(PrettyUtils.PrettyAction.SINGLE_FILE,
            PrettyUtils.selectAction(Collections.singletonList("some/file.wurst")));
    }

    @Test
    public void treeWithAFileSelectsTreeDump() {
        assertEquals(PrettyUtils.PrettyAction.TREE,
            PrettyUtils.selectAction(Arrays.asList("tree", "some/file.wurst")));
    }

    @Test
    public void treeWithoutAFileIsTreatedAsAFileName() {
        assertEquals(PrettyUtils.PrettyAction.SINGLE_FILE,
            PrettyUtils.selectAction(Collections.singletonList("tree")));
    }

    @Test
    public void noArgumentsDoesNothing() {
        assertEquals(PrettyUtils.PrettyAction.NONE,
            PrettyUtils.selectAction(Collections.emptyList()));
    }
}
