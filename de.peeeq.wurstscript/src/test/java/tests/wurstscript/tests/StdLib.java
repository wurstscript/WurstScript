package tests.wurstscript.tests;

import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

import java.io.File;

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
    private final static String version = "1296bd9779bf534184158f7321a5d7c55826d5e1";

    /**
     * flag so that initialization in only done once
     */
    private static boolean isInitialized = false;

    private static File tempFolder = new File("./temp");
    private static File stdLibFolder = new File(tempFolder, "WurstStdlib2");

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
                Utils.exec(tempFolder, "git", "clone", gitRepo, stdLibFolder.getName());
            }

            String revision = Utils.exec(stdLibFolder, "git", "rev-parse", "HEAD").trim();
            if (!revision.equals(version)) {
                System.out.println("Wrong version '" + revision + "', executing git pull to get '" + version + "'");
                Utils.exec(stdLibFolder, "git", "checkout", "master");
                Utils.exec(stdLibFolder, "git", "pull");
                Utils.exec(stdLibFolder, "git", "checkout", version, "-f");
            }

            // reset all possible changes
            Utils.exec(stdLibFolder, "git", "clean", "-fdx");
            Utils.exec(stdLibFolder, "git", "checkout", ".");
            Utils.exec(stdLibFolder, "git", "checkout", version);

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
