package de.peeeq.wurstio.languageserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;

public class LanguageServer {

	private boolean stopped = false;

	private Logger logger = Logger.getLogger("wurstlog");

	public LanguageServer() throws IOException {
		setupLogger();
	}

	private void setupLogger() throws IOException {
		File logFolder = new File("logs");
		logFolder.mkdirs();
		File logFile = new File(logFolder, "languageServer.log");
		Handler handler = new FileHandler(logFile.getAbsolutePath(), Integer.MAX_VALUE, 20);
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
		logger.setLevel(Level.INFO);
		System.out.println("logging to " + logFile.getAbsolutePath());
	}

	public void start() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		Gson gson = new Gson();

		logger.info("Started language server");

		while (!stopped) {
			String line = in.readLine();
			logger.info("Got message: " + line);
			RequestPacket req = gson.fromJson(line, RequestPacket.class);
			handleRequest(req);
		}
	}

	private void handleRequest(RequestPacket req) {
		logger.info("req = " + req);
		switch (req.getPath()) {
		case "fileChanged":
			handleFileChanged(req.getData().getAsJsonPrimitive().getAsString());
			break;
		default:
			

		}

	}

	private void handleFileChanged(String asString) {
		// TODO Auto-generated method stub

	}

}
