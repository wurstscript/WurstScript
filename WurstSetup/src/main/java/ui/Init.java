package ui;


import file.DependencyManager;
import file.GlobalWurstConfig;
import file.WurstProjectConfig;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Init {

    static MainWindow mainFrame;

    public static void init(boolean withoutUi, File projectDir) {
        GlobalWurstConfig.load();
        if (withoutUi) {
            boolean wurstUpdt = GlobalWurstConfig.updateAvailable;
            boolean projectUpdt = false;
            // Display dialog asking to update, if updates present
            if (projectDir != null) {
                try {
                    WurstProjectConfig projectConfig = WurstProjectConfig.loadProject(projectDir);
                    projectUpdt = DependencyManager.isUpdateAvailable(projectConfig);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (wurstUpdt || projectUpdt) {
                int dialogResult = JOptionPane.showConfirmDialog(null, "An update has been found. Would you like to download it now?",
                        "Update available", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    System.out.println("user ordered update");
                }
            }
        } else {
            initUI();
        }
    }

    private static void initUI() {
        mainFrame = new MainWindow();
        try {
            mainFrame.setIconImage(ImageIO.read(Init.class.getResourceAsStream("/icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void print(String message) {
        if (mainFrame != null) {
            mainFrame.ui.jTextArea.append(message);
            mainFrame.ui.jTextArea.setCaretPosition(mainFrame.ui.jTextArea.getText().length() - 1);
        }
    }

    public static void setMaxProgress(int max) {
        mainFrame.ui.progressBar.setMaximum(max);
    }

    public static void setProgress(int current) {
        mainFrame.ui.progressBar.setValue(current);
    }

    public static void setProgress(String state) {
        mainFrame.ui.lblWelcome.setText(state);
    }


    public static void refreshUi() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> mainFrame.ui.refreshComponents());
        }
    }
}
