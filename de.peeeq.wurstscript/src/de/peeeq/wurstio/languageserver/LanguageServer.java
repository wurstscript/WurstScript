package de.peeeq.wurstio.languageserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Utils;

public class LanguageServer {

	private ExecutorService responseExecutor = Executors.newSingleThreadExecutor();
	private LanguageWorker worker = new LanguageWorker(this);


	public LanguageServer() throws IOException {
		setupLogger();
	}


	private void setupLogger() throws IOException {
		File logFolder = new File("logs");
		logFolder.mkdirs();
		File logFile = new File(logFolder, "languageServer.log");
		Handler handler = new FileHandler(logFile.getAbsolutePath(), Integer.MAX_VALUE, 20);
		handler.setFormatter(new SimpleFormatter());
		System.out.println("logging to " + logFile.getAbsolutePath());
		WLogger.setHandler(handler);
	}

	public void start() throws IOException {
		new Thread(worker).start();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		Gson gson = new Gson();

		log("Started language server");

		while (!Thread.currentThread().isInterrupted()) {
			String line = in.readLine();
			RequestPacket req = gson.fromJson(line, RequestPacket.class);
			handleRequest(req);
		}
	}

	private void handleRequest(RequestPacket req) {
		switch (req.getPath()) {
			case "fileChanged": {
				String filePath = req.getData().getAsJsonPrimitive().getAsString();
				worker.handleFileChanged(filePath);
				break;
			}
			case "init": {
				String file = req.getData().getAsJsonPrimitive().getAsString();
				worker.handleInit(file);
				break;
			}
			case "reconcile": {
				JsonObject obj = req.getData().getAsJsonObject();
				String file = obj.get("filename").getAsString();
				String content = obj.get("content").getAsString();
				worker.handleReconcile(file, content);
				break;
			}
			default:
				log("unhandled request: " + req.getPath());
		}
	}


	private void log(String string) {
		WLogger.info(string);
	}


	void onCompilationResult(CompilationResult res) {
		log("Checked " + res.getFilename() + " in " + res.getExtra() + " -> " + res.getErrors().size() + " errors.");
		responseExecutor.submit(() -> {
			Gson gson = new Gson();
			EventPackage p = new EventPackage("compilationResult", gson.toJsonTree(res));
			System.out.println(gson.toJson(p));
		});
	}

}
