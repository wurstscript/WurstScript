package file;

import ui.Init;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frotty on 10.07.2017.
 */
public class WurstProjectConfig {
    private File projectRoot;
    private File gameRoot;

    public String releaseName = "myRelease.w3x";
    public List<String> dependencies = new ArrayList<>();

    public WurstProjectConfig(File projectRoot, File gameRoot) {
        this.projectRoot = projectRoot;
        this.gameRoot = gameRoot;
    }

    public void addDependency(String url) {
        dependencies.add(url);
    }

    public File getProjectRoot() {
        return projectRoot;
    }

    public static void handleCreate(WurstProjectConfig projectConfig) {
        new Thread(() -> {
            try {
                Init.log("Creating project root..");
                File projectRoot = projectConfig.projectRoot;
                if (projectRoot.exists()) {
                    Init.log("\nError: Project root already exists!");
                } else {
                    if (projectRoot.mkdirs()) {
                        Init.log("done\n");

                        Init.log("Download template..");
                        File file = Download.downloadBareboneProject();
                        Init.log("done\n");

                        Init.log("Extracting template..");
                        ZipArchiveExtractor zipArchiveExtractor = new ZipArchiveExtractor();
                        boolean extractSuccess = zipArchiveExtractor.extractArchive(file, projectRoot);

                        if (extractSuccess) {
                            Init.log("done\n");
                            Init.log("Clean up..");
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
                            new File(projectRoot, "imports" + File.pathSeparator + ".gitkeep").delete();

                            Init.log("done\n");

                            Init.log("Create config..");
                            setupVSCode(projectRoot, projectConfig.gameRoot.exists() ? projectConfig.gameRoot : null);

                            String projectYaml = GlobalWurstConfig.yaml.dump(projectConfig);
                            Files.write(new File(projectRoot, "wurst.build").toPath(), projectYaml.getBytes());
                            Init.log("done\n");

                            DependencyManager.updateDependencies(projectConfig);

                            Init.log("---\n\nYour project has been successfully created.\n" +
                                    "You can now open your project folder in VSCode. Open wurst/Hello.wurst to continue\n");
                        } else {
                            Init.log("error\n");
                            JOptionPane.showMessageDialog(null,
                                    "Error: Cannot extract patch files.\nWurst might still be in use.\nClose VSCode and Eclipse before updating.",
                                    "Error Massage", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }


                Init.refreshUi();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private static void setupVSCode(File projectRoot, File gamePath) throws IOException {
        Path vsCode = new File(projectRoot, ".vscode/settings.json").toPath();
        String json = new String(Files.readAllBytes(vsCode));
        String absolutePath = GlobalWurstConfig.getWurstCompilerJar().getAbsolutePath();
        json = json.replace("%wurstjar%", absolutePath.replaceAll("\\\\", "\\\\\\\\"));

        if (gamePath != null) {
            json = json.replace("%gamepath%", gamePath.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
        }
        Files.write(vsCode, json.getBytes());
    }
}
