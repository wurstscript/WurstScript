package tests.wurstscript.tests;

import de.peeeq.wurstscript.WLogger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
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
    private final static String version = "a85001e8e93a1271ccfc8edb0810c07041f24661";

    /**
     * flag so that initialization in only done once
     */
    private static boolean isInitialized = false;

    private static final File tempFolder = new File("./temp");
    private static final File stdLibFolder = new File(tempFolder, "WurstStdlib2");

    @Test
    public void download() {
        assert(downloadStandardlib());
    }

    /**
     * Entry point for the build, which fetches the library once before the tests fork.
     * <p>
     * The guard below is a lock and a flag in one process, and every fork is a process of its own,
     * so several of them starting on a clean checkout would clone into the same directory at the
     * same time. Doing it here, from the build's own JVM, means they all find it already there.
     */
    public static void main(String[] args) {
        // Deep clean here and only here: this runs alone, before the workers, so it is the one
        // moment when rewriting the directory disturbs nobody.
        if (!ensureCheckout(true)) {
            throw new RuntimeException("Could not fetch the standard library the tests compile against.");
        }
    }

    private static boolean isUsableCheckout() {
        try (Git git = Git.open(stdLibFolder)) {
            return git.getRepository().resolve(Constants.HEAD) != null;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }

    public synchronized static boolean downloadStandardlib() {
        return ensureCheckout(false);
    }

    /**
     * @param deepClean also discard ignored build artefacts. Only the build's prefetch asks for
     *     this. A worker must not: the artefacts are what a previous run left inside the library
     *     folder, and removing them while other workers are reading it is the contention this
     *     avoids. Leaving them is not harmless either - the compiler scans the library folder
     *     recursively and a stale `_build` can hold `.wurst` files that take a pinned package's
     *     name - which is why the prefetch does it once for everyone.
     */
    private synchronized static boolean ensureCheckout(boolean deepClean) {
        if (isInitialized) {
            return true;
        }

        try {
            // A checkout that cannot be opened or has no HEAD is worse than none: every test now
            // waits on this, so a half-written directory left by an interrupted run would stop the
            // whole suite rather than the few cases that need the library.
            if (stdLibFolder.exists() && !isUsableCheckout()) {
                System.out.println("Discarding an unusable standard library checkout at " + stdLibFolder);
                deleteRecursively(stdLibFolder);
            }
            if (!stdLibFolder.exists()) {
                tempFolder.mkdirs();
                try (Git git = Git
                        .cloneRepository()
                        .setDirectory(stdLibFolder)
                        .setURI(gitRepo)
                        .call()) {
                    git.checkout().setName(Constants.MASTER).call();
                }
            }

            boolean repaired = false;
            try (Git git = Git.open(stdLibFolder)) {
                String head = git.getRepository().resolve(Constants.HEAD).getName();
                if (!head.equals(version)) {
                    repaired = true;
                    System.out.println("Wrong version '" + head + "', fetching to get '" + version + "'");

                    // Straight to the pinned commit rather than by way of master. A checkout left
                    // detached - which is what this leaves behind, so it is the normal state - has
                    // no local master to check out, and asking for one fails with "Ref master
                    // cannot be resolved" before the fetch that would have created it.
                    git.fetch()
                        .setRemote(Constants.DEFAULT_REMOTE_NAME)
                        .setRefSpecs("+refs/heads/*:refs/remotes/origin/*")
                        .call();
                    git.checkout().setName(version).setForceRefUpdate(true).call();
                }
            }

            // Undo whatever a previous run left behind, but only when there is something to undo.
            // Every worker is its own process and so runs this once; unconditionally cleaning and
            // checking out means several of them rewriting one directory at the same time, each
            // taking the index lock, while other workers are reading the same files. Reading the
            // status does not write anything, and the ordinary case has nothing to repair.
            try (Git git = Git.open(stdLibFolder)) {
                if (repaired || deepClean || !git.status().call().isClean()) {
                    // Reset rather than checkout: checking out the commit HEAD already points at
                    // does nothing, so a modified file survived what claimed to undo it.
                    git.reset().setMode(ResetCommand.ResetType.HARD).setRef(version).call();
                    git.clean().setForce(true).setCleanDirectories(true).setIgnore(false).call();
                }
            }

            isInitialized = true;
        } catch (IOException | GitAPIException e) {
            // The array's identity is no use to anyone; there is an overload that prints the trace.
            WLogger.severe(e);
            // And on the console too: the build waits on this now, so whoever is looking at a
            // failed run needs to see why here rather than in a log file.
            e.printStackTrace(System.err);
            return false;
        }

        return true;
    }

    public static String getLib() {
        downloadStandardlib();
        return stdLibFolder.getPath();
    }
}
