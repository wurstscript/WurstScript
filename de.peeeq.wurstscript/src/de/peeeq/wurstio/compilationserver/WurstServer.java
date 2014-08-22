package de.peeeq.wurstio.compilationserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstio.Main;

public class WurstServer {

	private int portNumber = 27425;
	private @Nullable ServerSocket serverSocket;
	private volatile boolean stopped;

	public void start() {
		try {
			connect();
			while (!stopped) {
				handleRequest();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void connect() throws IOException {
		serverSocket = new ServerSocket(portNumber, 1,
				InetAddress.getLoopbackAddress());
	}

	public void stop() {
		stopped = true;
	}

	private void handleRequest() {
		final ServerSocket sock = serverSocket;
		if (sock == null) {
			throw new RuntimeException("Socket is null.");
		}
		try (Socket s = sock.accept();
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						s.getInputStream()));) {
			System.out.println("accepted");
			String inputLine = null;
			final List<String> args = new ArrayList<>();
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.equals("<<<<")) {
					break;
				}
				args.add(inputLine);
			}
			System.out.println(args);

			wurstMain(args);

			out.println("ok");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void wurstMain(final List<String> args) {
		String[] array = args.toArray(new String[0]);
		assert array != null;
		Main.main(array);
	}
	

	public static void startServer() {
		// don't know why this is needed ...
		SwingUtilities.invokeLater(() -> {});
		
		
		WurstServer s = new WurstServer();
		System.out.println("starting");
		s.start();
		System.out.println("stopped");
	}

	public static void main(String[] args) {
		startServer();
	}
}
