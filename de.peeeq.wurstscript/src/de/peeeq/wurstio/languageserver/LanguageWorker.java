package de.peeeq.wurstio.languageserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Lists;

import de.peeeq.wurstio.languageserver.requests.CleanProject;
import de.peeeq.wurstio.languageserver.requests.GetCompletions;
import de.peeeq.wurstio.languageserver.requests.GetDefinition;
import de.peeeq.wurstio.languageserver.requests.GetUsages;
import de.peeeq.wurstio.languageserver.requests.HoverInfo;
import de.peeeq.wurstio.languageserver.requests.RunMap;
import de.peeeq.wurstio.languageserver.requests.SignatureInfo;
import de.peeeq.wurstio.languageserver.requests.UserRequest;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Utils;

public class LanguageWorker implements Runnable {

    private final Map<String, PendingChange> changes = new LinkedHashMap<>();
    private final AtomicLong currentTime = new AtomicLong();
    private final LanguageServer server;
    private final Queue<UserRequest> userRequests = new LinkedList<>();

    private ModelManager modelManager;
    private String rootPath;

    private final Object lock = new Object();
    private int initRequestSequenceNr = -1;

    public LanguageWorker(LanguageServer server) {
        this.server = server;
    }

    public void handleFileChanged(String filePath) {
        if (!Utils.isWurstFile(filePath) && !filePath.endsWith("wurst.dependencies")) {
            // ignore all files which are not relevant for wurst
            return;
        }

        WLogger.info("handle handleFileChanged " + filePath);
        synchronized (lock) {
            changes.put(filePath, new FileUpdated(filePath));
            lock.notifyAll();
        }
        WLogger.info("handle handleFileChanged END " + filePath);
    }

    public void handleInit(int sequenceNr, String rootPath) {
        WLogger.info("handle init " + rootPath);
        synchronized (lock) {
            this.rootPath = rootPath;
            this.modelManager = null;
            this.initRequestSequenceNr = sequenceNr;
            lock.notifyAll();
        }
        WLogger.info("handle init END " + rootPath);
    }

    public void handleReconcile(int sequenceNr, String file, String content) {
        synchronized (lock) {
            changes.put(file, new FileReconcile(file, content));
            // reply directly
            server.reply(sequenceNr, "request queued");
            lock.notifyAll();
        }
    }

    public void handleGetDefinition(int sequenceNr, String filename, String buffer, int line, int column) {
        synchronized (lock) {
            userRequests.removeIf(req -> req instanceof GetDefinition);
            userRequests.add(new GetDefinition(sequenceNr, filename, buffer, line, column));
            lock.notifyAll();
        }
    }

    public void handleGetHoverInfo(int sequenceNr, String filename, String buffer, int line, int column) {
        synchronized (lock) {
            userRequests.removeIf(req -> req instanceof HoverInfo);
            userRequests.add(new HoverInfo(sequenceNr, filename, buffer, line, column));
            lock.notifyAll();
        }
    }

    public void handleGetCompletions(int sequenceNr, String filename, String buffer, int line, int column) {
        synchronized (lock) {
            userRequests.removeIf(req -> req instanceof GetCompletions);
            userRequests.add(new GetCompletions(sequenceNr, filename, buffer, line, column));
            lock.notifyAll();
        }
    }

    public void handleSignatureHelp(int sequenceNr, String filename, int line, int column) {
        synchronized (lock) {
            userRequests.removeIf(req -> req instanceof SignatureInfo);
            userRequests.add(new SignatureInfo(sequenceNr, filename, line, column));
            lock.notifyAll();
        }
    }

    public void handleClean(int sequenceNr) {
        synchronized (lock) {
            userRequests.removeIf(req -> req instanceof CleanProject);
            userRequests.add(new CleanProject(sequenceNr));
            lock.notifyAll();
        }
    }

    public void handleGetUsages(int sequenceNr, String filename, String buffer, int line, int column, boolean global) {
        synchronized (lock) {
            userRequests.removeIf(req -> req instanceof GetUsages);
            userRequests.add(new GetUsages(sequenceNr, filename, buffer, line, column, global));
            lock.notifyAll();
        }
    }
    
    public void handleRunmap(int requestNr, String mapPath, String wc3path) {
		synchronized (lock) {
			// TODO make args configurable
			List<String> compileArgs = Lists.newArrayList("-stacktraces", "-runcompiletimefunctions", "-injectobjects");
			userRequests.add(new RunMap(requestNr, rootPath, wc3path, new File(mapPath), compileArgs ));
			lock.notifyAll();
		}
	}

    private abstract class PendingChange {
        private long time;
        private String filename;

        public PendingChange(String filename) {
            time = currentTime.incrementAndGet();
            this.filename = filename;
        }

        public long getTime() {
            return time;
        }

        public String getFilename() {
            return filename;
        }
    }

    private class FileUpdated extends PendingChange {

        public FileUpdated(String filename) {
            super(filename);
        }
    }

    private class FileDeleted extends PendingChange {

        public FileDeleted(String filename) {
            super(filename);
        }
    }

    private class FileReconcile extends PendingChange {

        private String contents;

        public FileReconcile(String filename, String contents) {
            super(filename);
            this.contents = contents;
        }

        public String getContents() {
            return contents;
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Runnable work;
                synchronized (lock) {
                    work = getNextWorkItem();
                    if (work == null) {
                        lock.wait(10000);
                        work = getNextWorkItem();
                    }
                }
                if (work != null) {
                    // actual work is not synchronized, so that requests can come in while the work is done
                    try {
                        work.run();
                    } catch (Exception e) {
                        WLogger.severe(e);
                        System.err.println("Error in request " + work + " (see log for details): " + e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            // ignore
        }
        WLogger.info("Language Worker interrupted");
    }

    private Runnable getNextWorkItem() {
        if (modelManager == null) {
            if (rootPath != null) {
                return () -> doInit(rootPath);
            } else {
                // cannot do anything useful at the moment
                WLogger.info("LanguageWorker is waiting for init ... ");
            }
        } else if (!userRequests.isEmpty()) {
            return () -> {
                UserRequest req = userRequests.remove();
                Object response = req.execute(modelManager);
                server.reply(req.getRequestNr(), response);
            };
        } else if (!changes.isEmpty()) {
            // TODO this can be done more efficiently than doing one at a time
            PendingChange change = removeFirst(changes);
            return () -> {
                if (change.getFilename().endsWith("wurst.dependencies")) {
                    if (!(change instanceof FileReconcile)) {
                        modelManager.clean();
                    }
                } else if (change instanceof FileDeleted) {
                    modelManager.removeCompilationUnit(change.getFilename());
                } else if (change instanceof FileUpdated) {
                    modelManager.syncCompilationUnit(change.getFilename());
                } else if (change instanceof FileReconcile) {
                    FileReconcile fr = (FileReconcile) change;
                    modelManager.syncCompilationUnitContent(fr.getFilename(), fr.getContents());
                } else {
                    WLogger.info("unhandled change request: " + change);
                }
            };
        }
        return null;
    }

    private PendingChange removeFirst(Map<String, PendingChange> changes) {
        Iterator<Entry<String, PendingChange>> it = changes.entrySet().iterator();
        Entry<String, PendingChange> e = it.next();
        it.remove();
        return e.getValue();
    }

    private void doInit(String rootPath) {
        try {
            log("Handle init " + rootPath);
            modelManager = new ModelManagerImpl(rootPath);
            modelManager.onCompilationResult(server::onCompilationResult);

            log("Start building " + rootPath);
            modelManager.buildProject();

            log("Finished building " + rootPath);
            server.reply(initRequestSequenceNr, "done");
        } catch (Exception e) {
            WLogger.severe(e);
        }
    }

    private void log(String s) {
        WLogger.info(s);
    }

	

}
