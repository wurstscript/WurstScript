package ui;


import file.GlobalWurstConfig;

public class Init {

    static MainWindow mainFrame;

    public static void init(boolean withoutUi) {
        GlobalWurstConfig.load();
        if(!withoutUi) {
            initUI();
        }
    }

    private static void initUI() {
        mainFrame = new MainWindow();
    }

    public static void log(String message) {
        mainFrame.ui.jTextArea.append(message);
        mainFrame.ui.jTextArea.setCaretPosition(mainFrame.ui.jTextArea.getText().length()-1);
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
        mainFrame.ui.refreshComponents();
    }
}
