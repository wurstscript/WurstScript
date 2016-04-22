package de.peeeq.wurstio.languageserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;

import de.peeeq.wurstscript.utils.Utils;

public class LanguageServer {

	private boolean stopped = false;

	private ModelManager modelManager;
	private ExecutorService requestExecutor = Executors.newSingleThreadExecutor();
	private ExecutorService responseExecutor = Executors.newSingleThreadExecutor();
	
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
		try {
			logger.info("req = " + req);
			switch (req.getPath()) {
			case "fileChanged":
				handleFileChanged(req.getData().getAsJsonPrimitive().getAsString());
				break;
			case "init":
				handleInit(req.getData().getAsJsonPrimitive().getAsString());
				break;
			default:
				logger.info("unhandled request: " + req.getPath());
			}
		} catch (IOException e) {
			logger.warning("request got exception: " + Utils.printExceptionWithStackTrace(e));
		}

	}

	private void handleInit(String rootPath) throws IOException {
		modelManager = new ModelManagerImpl(rootPath);
		modelManager.onCompilationResult(this::onCompilationResult);
		
		requestExecutor.submit(() -> {
			modelManager.buildProject();
		});
		
		
	}
	
	private void onCompilationResult(CompilationResult res) {
		responseExecutor.submit(() -> {
			Gson gson = new Gson();
			EventPackage p = new EventPackage("compilationResult", gson.toJsonTree(res));
			System.out.println(gson.toJson(p));
		});
	}

	private void handleFileChanged(String changedFilePath) {
		requestExecutor.submit(() -> {
			// TODO only sync one file and typecheck the changed file and its dependencies
			modelManager.syncCompilationUnit(changedFilePath);
//			modelManager.buildProject();
		});
	}

}
