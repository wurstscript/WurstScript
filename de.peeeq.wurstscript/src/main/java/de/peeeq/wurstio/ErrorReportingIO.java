package de.peeeq.wurstio;

import de.peeeq.wurstio.gui.AboutDialog;
import de.peeeq.wurstio.gui.GuiUtils;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.ErrorReporting;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ErrorReportingIO extends ErrorReporting {

    @Override
    public void handleSevere(final Throwable t, final String sourcecode) {
        WLogger.severe(t);

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore
        }

        String title = "Sor!";
        String message = "You have encountered a bug in the Wurst Compiler.\n" +
                "Your version is: " + AboutDialog.version + "\n" +
                "The Error message is: " + t.getMessage() + "\n" + Utils.printExceptionWithStackTrace(t) + "\n\n" +
                "What do you want to do in order to help us fix this bug?";

        Object[] options = {
                "Nothing",
                "Send automatic error report",
                "Create manual bug report"
        };
        JFrame parent = new JFrame();
        parent.pack();
        parent.setVisible(true);
        GuiUtils.setWindowToCenterOfScreen(parent);
        int n = JOptionPane.showOptionDialog(parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     //do not use a custom Icon
                options,  //the titles of buttons
                options[1]); //default button titles

        if (n == 1) {
            final boolean[] results = new boolean[3];
            Thread[] threads = new Thread[4];

            threads[0] = new Thread(() -> results[0] = sendErrorReport(t, ""));

            threads[1] = new Thread(() -> results[1] = sendErrorReport(t, "\n\nLog: \n\n" + WLogger.getLog()));

            threads[2] = new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                results[1] = sendErrorReport(t, "\n\nSource Code: \n\n" + sourcecode);
            });

            threads[3] = new Thread(() -> {
                String customMessage = showMultilineMessageDialog();
                results[2] = sendErrorReport(t, "Custom message:\n\n" + customMessage);
            });

            for (Thread tr : threads) {
                tr.start();
            }

            for (Thread tr : threads) {
                try {
                    tr.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            try {
                FileUtils.write(sourcecode, new File("errorreport_source.wurst"));
            } catch (IOException e) {
                WLogger.severe(e);
            }

            if (results[0] && results[1] && results[2]) {
                JOptionPane.showMessageDialog(parent, "Thank you!");
            } else if (results[0]) {
                JOptionPane.showMessageDialog(parent, "Error Report could only be sent partially.");
            } else {
                JOptionPane.showMessageDialog(parent, "Error report could not be sent.");
            }
        } else if (n == 2) {
            Desktop desk = Desktop.getDesktop();
            try {
                desk.browse(new URI("https://github.com/peq/WurstScript/issues"));
            } catch (Exception e) {
                WLogger.severe(e);
                JOptionPane.showMessageDialog(parent, "Could not open browser.");
            }
        }
        parent.setVisible(false);
        parent.dispose();
    }

    @Override
    public boolean sendErrorReport(Throwable t, String sourcecode) {

        HttpURLConnection connection = null;
        try {

            // Construct data
            String data = URLEncoder.encode("errormessage", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(t.getMessage(), StandardCharsets.UTF_8);
            data += "&" + URLEncoder.encode("stacktrace", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(Utils.printExceptionWithStackTrace(t), StandardCharsets.UTF_8);
            data += "&" + URLEncoder.encode("version", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(AboutDialog.version, StandardCharsets.UTF_8);
            data += "&" + URLEncoder.encode("source", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(sourcecode, StandardCharsets.UTF_8);

            String request = "http://peeeq.de/wursterrors.php";
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
            connection.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();
//			String output = CharStreams.toString(new InputStreamReader(connection.getInputStream()));

            // Get response data.
            InputStream input = connection.getInputStream();
            int ch;
            StringBuilder output = new StringBuilder();
            while (((ch = input.read())) >= 0) {
                output.append((char) ch);
            }
            input.close();


            if (!output.toString().startsWith("Success")) {
                handleError("Could not send error report:\n" + output);
                return false;
            } else {
                return true;
            }
        } catch (MalformedURLException e) {
            WLogger.severe(e);
            handleError("Malformed URL\n" + e.getMessage());
        } catch (ProtocolException e) {
            WLogger.severe(e);
            handleError("ProtocolException\n" + e.getMessage());
        } catch (FileNotFoundException e) {
            WLogger.severe(e);
            handleError("Error reporting URL not found...\n" + e.getMessage());
        } catch (IOException e) {
            WLogger.severe(e);
            handleError("IOException\n" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    private static void handleError(String msg) {
        WLogger.severe(msg);
        System.err.println(msg);
    }

    private String showMultilineMessageDialog() {
        final JTextArea textArea = new JTextArea();
        textArea.setRows(8);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        JComponent[] inputs = {
                new JLabel("Please add some contact information here in case we have further questions regarding this problem."),
                new JLabel("This can be your hive user-name or your mail address."),
                new JLabel("You can also add more information on how to reproduce the problem."),
                areaScrollPane
        };
        int r = JOptionPane.showConfirmDialog(null, inputs, "My custom dialog", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            return textArea.getText();
        } else {
            return "(cancel" + r + " selected) ";
        }
    }
}
