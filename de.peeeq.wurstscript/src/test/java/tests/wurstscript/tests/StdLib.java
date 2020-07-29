package tests.wurstscript.tests;

import de.peeeq.wurstscript.WLogger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Helper class to download the standard library, which is required by some test
 * cases
 */
public class StdLib {

    /**
     * the repo to download
     */
    private final static String gitRepo = "https://github.com/wurstscript/WurstStdlib2";

    /**
     * version to use for the tests
     */
    private final static String version = "51f3274640197f957c3c53fb8cbe97c4e96ae898";

    /**
     * flag so that initialization in only done once
     */
    private static boolean isInitialized = false;

    private static File tempFolder = new File("./temp");
    private static File stdLibFolder = new File(tempFolder, "WurstStdlib2");

    @Test
    public void download() {
        assert(downloadStandardlib());
    }

    public synchronized static boolean downloadStandardlib() {
        if (isInitialized) {
            return true;
        }

        try {
            if (!stdLibFolder.exists()) {
                tempFolder.mkdirs();
                try (Git git = Git
                        .cloneRepository()
                        .setDirectory(stdLibFolder)
                        .setURI(gitRepo)
                        .call()) {
                    git.checkout().setName(Constants.MASTER).call();
                };
            }

            try (Git git = Git.open(stdLibFolder)) {
                String head = git.getRepository().resolve(Constants.HEAD).getName();
                if (!head.equals(version)) {
                    System.out.println("Wrong version '" + head + "', executing git pull to get '" + version + "'");

                    git.checkout().setName(Constants.MASTER).call();
                    git.pull().call();
                    git.checkout().setName(version).setForceRefUpdate(true).call();
                }
            }

            // reset all possible changes
            Git.open(stdLibFolder).clean().setForce(true).setCleanDirectories(true).setIgnore(false).call();
            Git.open(stdLibFolder).checkout().setName(version).call();

            isInitialized = true;
        } catch (IOException | GitAPIException e) {
            WLogger.severe(e.getStackTrace().toString());
            return false;
        }

        return true;
    }

    public static String getLib() {
        downloadStandardlib();
        return stdLibFolder.getPath();
    }
}
