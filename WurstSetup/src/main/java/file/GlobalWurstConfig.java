package file;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import ui.Init;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import javax.swing.*;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

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

    private static final DumperOptions options = new DumperOptions();
    public static final Yaml yaml;

    private static class UnsortedPropertyUtils extends PropertyUtils {
        @Override
        protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) throws IntrospectionException {
            Set<Property> properties = new LinkedHashSet<>();
            Collection<Property> props = getPropertiesMap(type, bAccess).values();
            for (Property property : props) {
                if ((property.isReadable() || property.isWritable()) && property instanceof FieldProperty) {
                    properties.add(property);
                }
            }
            return properties;
        }
    }

    static {
        options.setTags(Collections.emptyMap());
        Representer representer = new Representer();
        representer.setPropertyUtils(new UnsortedPropertyUtils());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        representer.addClassTag(WurstProjectConfig.class, Tag.MAP);
        yaml = new Yaml(new Constructor(WurstProjectConfig.class), representer, options);
    }

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
            File ownJar = new File(SetupMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            if (ownJar.exists() && ownJar.getParentFile().compareTo(wurstConfigFolder) != 0) {
                Files.copy(ownJar.toPath(), new File(wurstConfigFolder, "wurstsetup.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            getLatestBuildNumber();
            if (!isFreshInstall) {
                if (globalConfigFile.exists()) {
                    configData = yaml.loadAs(new String(Files.readAllBytes(globalConfigFile.toPath())), WurstConfigData.class);
                    wurstCompilerJar = new File(GlobalWurstConfig.getWurstConfigFolder(), "wurstscript.jar");
                    if (!wurstCompilerJar.exists()) {
                        wurstCompilerJar = null;
                        isFreshInstall = true;
                    } else {
                        updateAvailable = latestBuildAvailable > configData.getBuildNumber();
                    }
                } else {
                    isFreshInstall = true;
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
        try {
            Init.print(isFreshInstall ? "Installing WurstScript..\n" : "Updating WursScript..\n");

            Init.print("Downloading compiler..");
            File file = Download.downloadCompiler();
            Init.print("done\n");

            Init.print("Extracting compiler..");
            ZipArchiveExtractor zipArchiveExtractor = new ZipArchiveExtractor();
            boolean extractSuccess = zipArchiveExtractor.extractArchive(file, GlobalWurstConfig.getWurstConfigFolder());
            if (extractSuccess) {
                Init.print("done\n");
                WurstConfigData wurstConfigData = new WurstConfigData();
                wurstConfigData.setBuildNumber(latestBuildAvailable);
                Init.print(isFreshInstall ? "Generating Config.." : "Updating Config..");
                save(wurstConfigData);
                Init.print("done\n");

                wurstCompilerJar = new File(GlobalWurstConfig.getWurstConfigFolder(), "wurstscript.jar");
                updateAvailable = false;
                isFreshInstall = false;

                if (!wurstCompilerJar.exists()) {
                    Init.print("ERROR");
                } else {
                    Init.print(isFreshInstall ? "Installation complete\n" : "Update complete\n");
                }

            } else {
                Init.print("error\n");
                JOptionPane.showMessageDialog(null,
                        "Error: Cannot extract patch files.\nWurst might still be in use.\nClose VSCode and Eclipse before updating.",
                        "Error Massage", JOptionPane.ERROR_MESSAGE);
            }

            Init.refreshUi();
        } catch (Exception e) {
            Init.print("\n===ERROR COMPILER UPDATE===\n" + e.getMessage() + "\nPlease report here: github.com/wurstscript/WurstScript/issues\n");
        }
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
