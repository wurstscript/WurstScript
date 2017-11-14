package file;

import ui.Init;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frotty on 10.07.2017.
 */
public class WurstProjectConfig {
    private File projectRoot;

    private File gameRoot;

    public String buildMapName = "MyMap 0.01";
    public String buildMapFileName = "MyMap_0.01";
    public String buildMapAuthor = "YourName";

    public String projectName;
    public List<String> dependencies = new ArrayList<>();

    public WurstProjectConfig(File projectRoot, File gameRoot, String projectName) {
        this.projectRoot = projectRoot;
        this.gameRoot = gameRoot;
        this.projectName = projectName;
    }

    public void addDependency(String url) {
        if (!dependencies.contains(url)) {
            dependencies.add(url);
        }
    }

    public WurstProjectConfig() {
    }

    public void setGameRoot(File gameRoot) {
        this.gameRoot = gameRoot;
    }

    public File getProjectRoot() {
        return projectRoot;
    }

    public static void handleCreate(WurstProjectConfig projectConfig) {
        try {
            createProject(projectConfig);
            Init.refreshUi();
        } catch (Exception e) {
            Init.print("\n===ERROR PROJECT CREATE===\n" + e.getMessage() + "\nPlease report here: github.com/wurstscript/WurstScript/issues\n");
        }
    }

    public static WurstProjectConfig loadProject(File buildFile) throws IOException {
        Init.print("Loading project..");
        if (buildFile.exists() && buildFile.getName().equalsIgnoreCase("wurst.build")) {
            String fileInput = new String(Files.readAllBytes(buildFile.toPath()));
            if (fileInput.startsWith("!!")) {
                fileInput = fileInput.substring(fileInput.indexOf("\n"));
            }
            WurstProjectConfig config = GlobalWurstConfig.yaml.loadAs(fileInput, WurstProjectConfig.class);
            config.projectRoot = buildFile.getParentFile();
            if (config.projectName == null || config.projectName.isEmpty()) {
                config.projectName = config.projectRoot.getName();
                saveProjectConfig(config);
            }
            Init.print("done\n");
            return config;
        }
        return null;
    }

    private static void createProject(WurstProjectConfig projectConfig) throws Exception {
        File projectRoot = projectConfig.projectRoot;
        File gameRoot = projectConfig.gameRoot;
        Init.print("Creating project root..");
        if (projectRoot.exists()) {
            Init.print("\nError: Project root already exists!\n");
        } else {
            if (projectRoot.mkdirs()) {
                Init.print("done\n");

                Init.print("Download template..");
                File file = Download.downloadBareboneProject();
                Init.print("done\n");

                Init.print("Extracting template..");
                ZipArchiveExtractor zipArchiveExtractor = new ZipArchiveExtractor();
                boolean extractSuccess = zipArchiveExtractor.extractArchive(file, projectRoot);

                if (extractSuccess) {
                    Init.print("done\n");
                    Init.print("Clean up..");
                    String[] children = projectRoot.list();
                    if (children != null && children.length == 1) {
                        File uselessFolder = new File(projectRoot, children[0]);
                        String[] list = uselessFolder.list();
                        if (list != null) {
                            for (String pathr : list) {
                                Path path1 = new File(projectRoot, children[0] + File.separator + pathr).toPath();
                                Files.move(path1, new File(projectRoot, pathr).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                        uselessFolder.delete();
                    }
                    Init.print("done\n");

                    setupVSCode(projectRoot, gameRoot);

                    saveProjectConfig(projectConfig);

                    DependencyManager.updateDependencies(projectConfig);

                    Init.print("---\n\n");
                    if (gameRoot == null || !gameRoot.exists()) {
                        Init.print("Warning: Your game path has not been set.\nThis means you will be able to develop, but not run maps.\n");
                    }
                    Init.print("Your project has been successfully created!\n" +
                            "You can now open your project folder in VSCode.\nOpen the wurst/Hello.wurst package to continue.\n");
                } else {
                    Init.print("error\n");
                    JOptionPane.showMessageDialog(null,
                            "Error: Cannot extract patch files.\nWurst might still be in use.\nClose any Wurst, VSCode or Eclipse instances before updating.",
                            "Error Massage", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private static void saveProjectConfig(WurstProjectConfig projectConfig) throws IOException {
        String projectYaml = GlobalWurstConfig.yaml.dump(projectConfig);
        Files.write(new File(projectConfig.projectRoot, "wurst.build").toPath(), projectYaml.getBytes());
    }

    private static void setupVSCode(File projectRoot, File gamePath) throws IOException {
        Init.print("Updating vsconfig..");
        if (!projectRoot.exists()) {
            throw new IOException("Project root does not exist!");
        }
        File vsCodeF = new File(projectRoot, ".vscode/settings.json");
        Path vsCode = vsCodeF.toPath();
        if (!vsCodeF.exists()) {
            Files.createDirectories(vsCode.getParent());
            Files.write(vsCode, VSCODE_MIN_CONFIG.getBytes(), StandardOpenOption.CREATE_NEW);
        }
        String json = new String(Files.readAllBytes(vsCode));
        String absolutePath = GlobalWurstConfig.getWurstCompilerJar().getAbsolutePath();
        json = json.replace("%wurstjar%", absolutePath.replaceAll("\\\\", "\\\\\\\\"));

        if (gamePath != null && gamePath.exists()) {
            json = json.replace("%gamepath%", gamePath.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
        }
        Files.write(vsCode, json.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        Init.print("done.\n");
    }

    public String getProjectName() {
        return projectName;
    }

    public static void handleUpdate(WurstProjectConfig config) {
        Init.print("Updating project...\n");
        try {
            setupVSCode(config.projectRoot, config.gameRoot);

            DependencyManager.updateDependencies(config);

            Init.print("Project successfully updated!\nReload vscode to apply the changed dependencies.\n");
            Init.refreshUi();
        } catch (Exception e) {
            Init.print("\n===ERROR PROJECT UPDATE===\n" + e.getMessage() + "\nPlease report here: github.com/wurstscript/WurstScript/issues\n");
        }

    }

    private static String VSCODE_MIN_CONFIG = "{\n" +
            "    \"wurst.wurstJar\": \"%wurstjar%\",\n" +
            "    \"wurst.wc3path\": \"%gamepath%\"\n" +
            "}";
}
