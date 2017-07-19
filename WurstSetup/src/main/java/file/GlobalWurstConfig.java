package file;

import org.yaml.snakeyaml.Yaml;
import ui.Init;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static us.monoid.web.Resty.path;

/**
 * Created by Frotty on 28.06.2017.
 */
public class GlobalWurstConfig {
    private static final String FOLDER_PATH = ".wurst";
    private static final String FILE_NAME = "wurst_config.yml";

    private static boolean isFreshInstall = true;

    private static File wurstConfigFolder;
    private static File globalConfigFile;

    private static int latestBuildAvailable = -1;

    public static final Yaml yaml = new Yaml();

    public static final Resty resty = new Resty();
    public static WurstConfigData configData;
    public static boolean updateAvailable = false;
    private static File wurstCompilerJar;

    public static void load() {
        String userHome = System.getProperty("user.home");
        if (userHome.length() <= 0) {
            throw new RuntimeException("No user.home available.");
        }
        wurstConfigFolder = new File(userHome, FOLDER_PATH);
        globalConfigFile = new File(wurstConfigFolder, FILE_NAME);
        isFreshInstall = !wurstConfigFolder.exists();
        try {
            getLatestBuildNumber();
            if (!isFreshInstall) {
                if (globalConfigFile.exists()) {
                    configData = yaml.loadAs(new String(Files.readAllBytes(globalConfigFile.toPath())), WurstConfigData.class);
                    wurstCompilerJar = new File(GlobalWurstConfig.getWurstConfigFolder(), "wurstscript.jar");
                    updateAvailable = latestBuildAvailable > configData.getBuildNumber();

                } else {
                    isFreshInstall = true;
//                    Init.log("Wurst installation invalid. Please reinstall.\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save(WurstConfigData wurstConfig) throws IOException {
        configData = wurstConfig;
        isFreshInstall = false;
        wurstConfigFolder.mkdirs();

        String output = yaml.dump(wurstConfig);

        Files.write(globalConfigFile.toPath(), output.getBytes());
    }

    public static boolean isFreshInstall() {
        return isFreshInstall;
    }

    public static File getWurstConfigFolder() {
        return wurstConfigFolder;
    }

    public static void handleUpdate() {
        new Thread(() -> {
            try {
                Init.log(isFreshInstall ? "Installing WurstScript..\n" : "Updating WursScript..\n");

                Init.log("Downloading compiler..");
                File file = Download.downloadCompiler();
                Init.log("done\n");

                Init.log("Extracting compiler..");
                ZipArchiveExtractor zipArchiveExtractor = new ZipArchiveExtractor();
                boolean extractSuccess = zipArchiveExtractor.extractArchive(file, GlobalWurstConfig.getWurstConfigFolder());
                if (extractSuccess) {
                    Init.log("done\n");
                    WurstConfigData wurstConfigData = new WurstConfigData();
                    wurstConfigData.setBuildNumber(latestBuildAvailable);
                    Init.log(isFreshInstall ? "Generating Config.." : "Updating Config..");
                    save(wurstConfigData);
                    Init.log("done\n");

                    wurstCompilerJar = new File(GlobalWurstConfig.getWurstConfigFolder(), "wurstscript.jar");
                    if(!wurstCompilerJar.exists()) {
                        Init.log("ERROR");
                    } else {
                        Init.log("Installation complete\n");
                    }

                } else {
                    Init.log("error\n");
                    JOptionPane.showMessageDialog(null,
                            "Error: Cannot extract patch files.\nWurst might still be in use.\nClose VSCode and Eclipse before updating.",
                            "Error Massage", JOptionPane.ERROR_MESSAGE);
                }

                Init.refreshUi();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * @return The latest working build number from jenkins
     * @throws Exception
     */
    private static void getLatestBuildNumber() throws Exception {
        String s = String.valueOf(GlobalWurstConfig.resty.json("http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/api/json")
                .get(path("actions[2].buildsByBranchName")));
        JSONObject inner = new JSONObject(String.valueOf(new JSONObject(s).get("refs/remotes/origin/master")));
        latestBuildAvailable = Integer.parseInt(String.valueOf(inner.get("buildNumber")));
    }


    public static File getWurstCompilerJar() {
        return wurstCompilerJar;
    }
}
