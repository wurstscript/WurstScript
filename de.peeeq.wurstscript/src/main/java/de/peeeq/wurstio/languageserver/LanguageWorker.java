package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.HoverInfo;
import de.peeeq.wurstio.languageserver.requests.UserRequest;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class LanguageWorker implements Runnable {

    private class Workitem {
        private String description;
        private Runnable runnable;

        public Workitem(String description, Runnable runnable) {
            this.description = description;
            this.runnable = runnable;
        }

        void run() {
            runnable.run();
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private final Map<WFile, PendingChange> changes = new LinkedHashMap<>();
    private final AtomicLong currentTime = new AtomicLong();
    private final Queue<UserRequest<?>> userRequests = new LinkedList<>();
    private final Thread thread;

    private ModelManager modelManager;

    public void setRootPath(WFile rootPath) {
        this.rootPath = rootPath;
    }

    private WFile rootPath;

    private final Object lock = new Object();
    private BufferManager bufferManager = new BufferManager();
    private LanguageClient languageClient;

    public LanguageWorker() {
        // start working
        thread = new Thread(this);
        thread.setName("Wurst LanguageWorker");
        thread.start();
    }

    public BufferManager getBufferManager() {
        return bufferManager;
    }

    public void setLanguageClient(LanguageClient languageClient) {
        this.languageClient = languageClient;
    }

    public void stop() {
        thread.interrupt();
    }

    abstract class PendingChange {
        private long time;
        private WFile filename;

        public PendingChange(WFile filename) {
            time = currentTime.incrementAndGet();
            this.filename = filename;
        }

        public long getTime() {
            return time;
        }

        public WFile getFilename() {
            return filename;
        }
    }

    class FileUpdated extends PendingChange {

        public FileUpdated(WFile filename) {
            super(filename);
        }
    }

    class FileDeleted extends PendingChange {

        public FileDeleted(WFile filename) {
            super(filename);
        }
    }

    class FileReconcile extends PendingChange {

        private String contents;

        public FileReconcile(WFile filename, String contents) {
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
                Workitem work;
                synchronized (lock) {
                    work = getNextWorkItem();
                    if (work == null) {
                        lock.wait(10000);
                        work = getNextWorkItem();
                    }
                }
                if (work != null) {
                    // actual work is not synchronized, so that requests can
                    // come in while the work is done
                    try {
                        work.run();
                    } catch (Throwable e) {
                        languageClient.showMessage(new MessageParams(MessageType.Error, "Request '" + work + "' could not be processed (see log for details): " + e.toString()));
                        WLogger.severe(e);
                        System.err.println("Error in request '" + work + "' (see log for details): " + e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            // ignore
        }
        WLogger.info("Language Worker interrupted");
    }

    private Workitem getNextWorkItem() {
        if (modelManager == null) {
            if (rootPath != null) {
                WLogger.info("LanguageWorker start init");
                return new Workitem("init", () -> doInit(rootPath));
            } else {
                // cannot do anything useful at the moment
                WLogger.info("LanguageWorker is waiting for init ... ");
            }
        } else if (!userRequests.isEmpty()) {
            UserRequest<?> req = userRequests.remove();
            return new Workitem(req.toString(), () -> req.run(modelManager));
        } else if (!changes.isEmpty()) {
            // TODO this can be done more efficiently than doing one at a time
            PendingChange change = removeFirst(changes);
            return new Workitem(change.toString(), () -> {
                if (isWurstDependencyFile(change)) {
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
            });
        }
        return null;
    }

    private boolean isWurstDependencyFile(PendingChange change) {
        return change.getFilename().getUriString().endsWith("wurst.dependencies");
    }

    private PendingChange removeFirst(Map<WFile, PendingChange> changes) {
        Iterator<Map.Entry<WFile, PendingChange>> it = changes.entrySet().iterator();
        Map.Entry<WFile, PendingChange> e = it.next();
        it.remove();
        return e.getValue();
    }

    private void doInit(WFile rootPath) {
        try {
            log("Handle init " + rootPath);
            modelManager = new ModelManagerImpl(rootPath.getFile(), bufferManager);
            modelManager.onCompilationResult(this::onCompilationResult);

            log("Start building " + rootPath);
            modelManager.buildProject();

            log("Finished building " + rootPath);
        } catch (Exception e) {
            WLogger.severe(e);
        }
    }


    private void onCompilationResult(PublishDiagnosticsParams compilationResult) {
        languageClient.publishDiagnostics(compilationResult);
    }

    private void log(String s) {
        WLogger.info(s);
    }


    public void handleFileChanged(DidChangeWatchedFilesParams params) {
        synchronized (lock) {
            for (FileEvent fileEvent : params.getChanges()) {
                bufferManager.handleFileChange(fileEvent);

                WFile file = WFile.create(fileEvent.getUri());
                if (fileEvent.getType() == FileChangeType.Deleted) {
                    changes.put(file, new FileDeleted(file));
                } else {
                    changes.put(file, new FileUpdated(file));
                }
            }
            lock.notifyAll();
        }
    }

    public void handleChange(DidChangeTextDocumentParams params) {
        synchronized (lock) {
            bufferManager.handleChange(params);
            WFile file = WFile.create(params.getTextDocument().getUri());

            changes.put(file, new FileReconcile(file, bufferManager.getBuffer(params.getTextDocument())));
            lock.notifyAll();
        }
    }

    public <Res> CompletableFuture<Res> handle(UserRequest<Res> request) {
        synchronized (lock) {
            if (!request.keepDuplicateRequests()) {
                Iterator<UserRequest<?>> it = userRequests.iterator();
                while (it.hasNext()) {
                    UserRequest<?> o = it.next();
                    if (o.getClass().equals(request.getClass())) {
                        o.cancel();
                        it.remove();
                    }
                }
            }
            userRequests.add(request);
            lock.notifyAll();
            CompletableFuture<Res> fut = request.getFuture();
            CompletableFuture<Res> resFut = new CompletableFuture<>();
            fut.whenComplete((res, err) -> {
                if (err != null) {
                    request.handleException(languageClient, err, resFut);
                } else if (res == null) {
                    System.err.println("Request returned null: " + request);
                    if(!(request instanceof HoverInfo)) {
                        languageClient.showMessage(new MessageParams(MessageType.Error, "Request returned null: " + request));
                    }
                    resFut.completeExceptionally(new RuntimeException("Request returned null: " + request));
                } else {
                    resFut.complete(res);
                }

            });
            return resFut;
        }
    }
}
