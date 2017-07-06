package tests.wurstscript.tests;

import de.peeeq.wurstio.IOUtils;
import org.junit.Test;

import java.io.File;

/**
 * Helper class to download the standard library, which is required by some test
 * cases
 */
public class StdLib {

    /**
     * the repo to download
     */
    private final static String gitRepo = "https://github.com/peq/wurstStdlib.git";

    /**
     * version to use for the tests
     */
    private final static String version = "7ea2806aa56320bf00d6a9fa3615adfca851d66c";

    /**
     * flag so that initialization in only done once
     */
    private static boolean isInitialized = false;

    private static File tempFolder = new File("./temp");
    private static File stdLibFolder = new File(tempFolder, "wurstStdlib");

    @Test
    public void download() {
        downloadStandardlib();
    }

    public synchronized static void downloadStandardlib() {
        if (isInitialized) {
            return;
        }
        try {


            if (!stdLibFolder.exists()) {
                tempFolder.mkdirs();
                IOUtils.exec(tempFolder, "git", "clone", gitRepo, "wurstStdlib");
            }

            String revision = IOUtils.exec(stdLibFolder, "git", "rev-parse", "HEAD");
            System.out.println("revision = " + revision);
            if (!revision.equals(version)) {
                IOUtils.exec(stdLibFolder, "git", "checkout", "master");
                IOUtils.exec(stdLibFolder, "git", "pull");
                IOUtils.exec(stdLibFolder, "git", "checkout", version);
            }

            // reset all possible changes
            IOUtils.exec(stdLibFolder, "git", "clean", "-fdx");
            IOUtils.exec(stdLibFolder, "git", "checkout", ".");
            IOUtils.exec(stdLibFolder, "git", "checkout", version);

            isInitialized = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLib() {
        downloadStandardlib();
        return stdLibFolder.getPath();
    }


}
