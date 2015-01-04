package de.peeeq.wurstio.compilationserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import de.peeeq.wurstio.Main;
import de.peeeq.wurstscript.WLogger;

public class WurstServer {

	private int portNumber = 27425;
	private volatile boolean stopped;
	private Consumer<String> printer = System.out::println;

	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(portNumber, 1, InetAddress.getLoopbackAddress())) {
			serverSocket.setSoTimeout(1000);
			println("Server started.");
			while (!stopped) {
				handleRequest(serverSocket);
			}
			println("Server stopped.");
		} catch (IOException e) {
			println("Server had a problem: " + e.getMessage());
			WLogger.severe(e);
		} finally {
			System.out.println("end start #################");
		}
	}
	
	public void startInNewThread() {
		new Thread(this::start).start();
	}

	private void println(String string) {
		printer.accept(string);
	}

	public void stop() {
		stopped = true;
	}

	private void handleRequest(ServerSocket sock) {
		try (Socket s = sock.accept();
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						s.getInputStream()));) {
			println("Server accepted compilation request");
			String inputLine = null;
			final List<String> args = new ArrayList<>();
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.equals("<<<<")) {
					break;
				}
				args.add(inputLine);
			}
			println(args.toString());
			long time = System.currentTimeMillis();
			wurstMain(args);
			println("Server finished compilation in " + (System.currentTimeMillis() - time) + "ms");

			out.println("ok");
		} catch (SocketTimeoutException e) {
			// expected exception
		} catch (IOException e) {
			println("Error in server: " + e.getMessage());
			WLogger.severe(e);
		}
	}

	private void wurstMain(final List<String> args) {
		String[] array = args.toArray(new String[0]);
		assert array != null;
		try {
			SwingUtilities.invokeAndWait(() -> Main.main(array));
		} catch (InvocationTargetException | InterruptedException e) {
			println("Error when running wurst: " + e.getMessage());
			e.printStackTrace();
			throw new Error(e);
		}
	}
	

	public static void startServer() {
		// don't know why this is needed ...
		SwingUtilities.invokeLater(() -> {});
		
		WurstServer s = new WurstServer();
		s.start();
	}

	public static void main(String[] args) {
		startServer();
	}

	public void setPrinter(Consumer<String> printer) {
		this.printer = printer;
	}
}
