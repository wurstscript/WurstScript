package de.peeeq.wurstscript;

import java.awt.Desktop;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.peeeq.wurstscript.gui.About;
import de.peeeq.wurstscript.utils.Utils;

public class ErrorReporting {
		
	
	public static void handleSevere(Throwable t) {
		t.printStackTrace();
		WLogger.severe(t);
		
		
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// ignore
		}
		
		String title  = "Sorry!";
		String message = "You have encountered a bug in the Wurst Compiler.\n" +
				"What do you want to do in order to help us fix this bug?";
		
		Object[] options = {
				"Nothing",
				"Send automatic error report",
				"Create manual bug report"
			            };
		JFrame parent = new JFrame();
		parent.pack();
		parent.setVisible(true);
		Utils.setWindowToCenterOfScreen(parent);
		int n = JOptionPane.showOptionDialog(parent,
			message,
			title,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,     //do not use a custom Icon
			options,  //the titles of buttons
			options[1]); //default button titles
		
		if (n == 1) {
			boolean r = ErrorReporting.sendErrorReport(t);
			if (r) {
				JOptionPane.showMessageDialog(parent, "Thank you!");
			}else {
				JOptionPane.showMessageDialog(parent, "Error report could not be sent.");
			}
		} else if (n == 2) {
			Desktop desk = Desktop.getDesktop();
			try {
				desk.browse(new URI("http://code.google.com/p/pscript-lang/issues/entry"));
			} catch (Exception e) {
				WLogger.severe(e);
				JOptionPane.showMessageDialog(parent, "Could not open browser.");
			}
		}
		parent.setVisible(false);
		parent.dispose();
	}
	
	public static boolean sendErrorReport(Throwable t) {
		
		HttpURLConnection connection = null;
		try {
			
			// Construct data
		    String data = URLEncoder.encode("errormessage", "UTF-8") + "=" + URLEncoder.encode(t.getMessage(), "UTF-8");
		    data += "&" + URLEncoder.encode("stacktrace", "UTF-8") + "=" + URLEncoder.encode(Utils.printStackTrace(t.getStackTrace()), "UTF-8");
		    data += "&" + URLEncoder.encode("version", "UTF-8") + "=" + URLEncoder.encode(About.version, "UTF-8");
			
			String request = "http://peeeq.de/wursterrors.php";
			URL url = new URL(request); 
			connection = (HttpURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
			connection.setUseCaches (false);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
			wr.writeBytes(data);
			wr.flush();
			wr.close();
//			String output = CharStreams.toString(new InputStreamReader(connection.getInputStream()));
			
			 // Get response data.
		    InputStream input = connection.getInputStream ();
		    int ch;
		    StringBuilder output = new StringBuilder();
		    while (((ch = input.read())) >= 0)    {
		    	output.append((char) ch);
		    }
		    input.close ();

			
			if (!output.toString().startsWith("Success")) {
				handleError("Could not send error report:\n" + output);
				return false;
			} else {
				return true;
			}
		} catch (MalformedURLException e) {
			WLogger.severe(e);
			e.printStackTrace();
			handleError("Malformed URL\n" + e.getMessage());
		} catch (ProtocolException e) {
			WLogger.severe(e);
			e.printStackTrace();
			handleError("ProtocolException\n" + e.getMessage());
		} catch (FileNotFoundException e) {
			WLogger.severe(e);
			e.printStackTrace();
			handleError("Error reporting URL not found...\n" + e.getMessage());
		} catch (IOException e) {
			WLogger.severe(e);
			e.printStackTrace();
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
}
