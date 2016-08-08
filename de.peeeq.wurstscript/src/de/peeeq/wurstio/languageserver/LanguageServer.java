package de.peeeq.wurstio.languageserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.peeeq.wurstscript.WLogger;

public class LanguageServer {

    private ExecutorService responseExecutor = Executors.newSingleThreadExecutor();
    private LanguageWorker worker = new LanguageWorker(this);

    public LanguageServer() throws IOException {
        setupLogger();
    }

    private void setupLogger() throws IOException {
        FileHandler handler = new FileHandler("%t/wurst_langserver%g.log", Integer.MAX_VALUE, 20);
        handler.setFormatter(new SimpleFormatter());
        WLogger.setHandler(handler);
    }

    public void start() throws IOException {
        Thread serverThread = new Thread(worker);
        serverThread.start();
        serverThread.setName("LanguageServer Worker");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        Gson gson = new Gson();

        log("Started language server");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String line = in.readLine();
                RequestPacket req = gson.fromJson(line, RequestPacket.class);
                handleRequest(req);
            } catch (Exception e) {
                WLogger.severe(e);
                System.err.println("Wurst error (see log for more details): " + e);
            }
        }
        log("Stopped language server");
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
                worker.handleInit(req.getSequenceNr(), file);
                break;
            }
            case "reconcile": {
                JsonObject obj = req.getData().getAsJsonObject();
                String file = obj.get("filename").getAsString();
                String content = obj.get("content").getAsString();
                worker.handleReconcile(req.getSequenceNr(), file, content);
                break;
            }
            case "provideDefinition": {
                JsonObject obj = req.getData().getAsJsonObject();
                String filename = obj.get("filename").getAsString();
                String buffer = obj.get("buffer").getAsString();
                int line = obj.get("line").getAsInt();
                int column = obj.get("column").getAsInt();
                worker.handleGetDefinition(req.getSequenceNr(), filename, buffer, line, column);
                break;
            }
            case "hoverInfo": {
                JsonObject obj = req.getData().getAsJsonObject();
                String filename = obj.get("filename").getAsString();
                String buffer = obj.get("buffer").getAsString();
                int line = obj.get("line").getAsInt();
                int column = obj.get("column").getAsInt();
                worker.handleGetHoverInfo(req.getSequenceNr(), filename, buffer, line, column);
                break;
            }
            case "getCompletions": {
                JsonObject obj = req.getData().getAsJsonObject();
                String filename = obj.get("filename").getAsString();
                String buffer = obj.get("buffer").getAsString();
                int line = obj.get("line").getAsInt();
                int column = obj.get("column").getAsInt();
                worker.handleGetCompletions(req.getSequenceNr(), filename, buffer, line, column);
                break;
            }
            case "getUsagesForFile": {
                JsonObject obj = req.getData().getAsJsonObject();
                String filename = obj.get("filename").getAsString();
                String buffer = obj.get("buffer").getAsString();
                int line = obj.get("line").getAsInt();
                int column = obj.get("column").getAsInt();
                boolean global = obj.has("global") && obj.get("global").getAsBoolean();
                worker.handleGetUsages(req.getSequenceNr(), filename, buffer, line, column, global);
                break;
            }
            case "signatureHelp": {
                JsonObject obj = req.getData().getAsJsonObject();
                String filename = obj.get("filename").getAsString();
                int line = obj.get("line").getAsInt();
                int column = obj.get("column").getAsInt();
                worker.handleSignatureHelp(req.getSequenceNr(), filename, line, column);
                break;
            }
            case "clean": {
                worker.handleClean(req.getSequenceNr());
                break;
            }
            case "runmap" : {
            	JsonObject obj = req.getData().getAsJsonObject();
                String mapPath = obj.get("mappath").getAsString();
                String wc3path = obj.get("wc3path").getAsString();
            	worker.handleRunmap(req.getSequenceNr(), mapPath, wc3path);
            	break;
            }
            case "runtests" : {
            	JsonObject obj = req.getData().getAsJsonObject();
            	String filename = obj.has("filename") ? obj.get("filename").getAsString() : null;
                int line = obj.has("line") ? obj.get("line").getAsInt() : -1;
                int column = obj.has("column") ? obj.get("column").getAsInt() : -1;
            	worker.handleRuntests(req.getSequenceNr(), filename, line, column);
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

    public void reply(int requestNr, Object response) {
        responseExecutor.submit(() -> {
            Gson gson = new Gson();
            ReplyPackage p = new ReplyPackage(requestNr, response);
            System.out.println(gson.toJson(p));
        });
    }

    public void sendReplyChunk(Object obj) {
    	responseExecutor.submit(() -> {
            Gson gson = new Gson();
            System.out.println(gson.toJson(obj));
        });
    }
    
	public void sendConsoleOutput(String message) {
		responseExecutor.submit(() -> {
            Gson gson = new Gson();
            Map<String, Object> m = ImmutableMap.<String,Object>builder()
            		.put("consoleOutputMessage", message)
            		.build();
            System.out.println(gson.toJson(m));
        });
		
	}
}
