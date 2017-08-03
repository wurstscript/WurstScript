package file;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import ui.Init;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class SetupMain {

    @Option(name = "-silent", usage = "check for updates without opening UI")
    private boolean silent = false;

    private File projectDir = null;

    @Option(name = "-projectdir", usage = "sets the project dir to check dependencies for")
    public void setDir(File dir) {
        if(dir.exists()) {
            projectDir = dir;
        }
    }

    public static void main(String[] args) throws IOException, CmdLineException {
        new SetupMain().doMain(args);
    }

    private void doMain(String[] args) throws CmdLineException {
        setupExceptionHandler();
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(Arrays.asList(args));
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }


        Init.init(silent, projectDir);
    }

    private static void setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            exception.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            JTextArea jTextField = new JTextArea();
            jTextField.setText("Please report this crash with the following info:\n" + sw.toString());
            jTextField.setEditable(false);
            JOptionPane.showMessageDialog(null, jTextField, "Sorry, Exception occured :(", JOptionPane.ERROR_MESSAGE);
        });
    }
}


