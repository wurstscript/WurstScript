package de.peeeq.wurstio.gui;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.gui.WurstGui;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WurstGuiImpl extends WurstGui {


    private final Queue<CompileError> errorQueue = new ConcurrentLinkedQueue<>();
    private volatile double progress = 0.0;
    private volatile boolean finished = false;
    private volatile @Nullable String currentlyWorkingOn = "";
    private final GuiUpdater guiUpdater;
    private final Object progressLock = new Object();
    private String workspaceRoot;

    private static final ConcurrentHashMap<String, Long> staticLastTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> lastTimes = new HashMap<>(staticLastTimes);


    public WurstGuiImpl() {
        // this constructor is called from the main thread, so we should not create the gui
        // here. This would block the main compiler thread until the gui is created.
        guiUpdater = new GuiUpdater();
        guiUpdater.start();
    }

    public WurstGuiImpl(String workspaceRoot) {
        this();
        this.workspaceRoot = workspaceRoot;
    }

    /**
     * this is a thread which creates and updates the status window and the error window
     * <p>
     * this is all done asynchronously, so the main compiler thread is not blocked
     */
    class GuiUpdater extends Thread {
        private @Nullable WurstStatusWindow statusWindow = null;
        private @Nullable WurstErrorWindow errorWindow = null;


        public GuiUpdater() {
        }


        @Override
        public void run() {
            try {
                // init the windows:
                SwingUtilities.invokeAndWait(() -> {
                    statusWindow = new WurstStatusWindow();
                    errorWindow = new WurstErrorWindow(workspaceRoot);
                    errorWindow.repaint();
                    statusWindow.repaint();
                    errorWindow.toFront();
                    statusWindow.toFront();
                    errorWindow.setAlwaysOnTop(true);
                    statusWindow.setAlwaysOnTop(true);
                    statusWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    errorWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                });

                WurstStatusWindow statusWindow = this.statusWindow;
                WurstErrorWindow errorWindow = this.errorWindow;
                Preconditions.checkNotNull(statusWindow);
                Preconditions.checkNotNull(errorWindow);

                // main loop: wait until finished and send the errors in the queue to the actual gui
                while (!finished || !errorQueue.isEmpty()) {

                    // Update the UI:
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            for (CompileError elem = pollErrorQueue(); elem != null; elem = pollErrorQueue()) {
                                errorWindow.sendError(elem);
                            }
                            statusWindow.sendProgress(currentlyWorkingOn, progress);
                        }

                        private @Nullable CompileError pollErrorQueue() {
                            return errorQueue.poll();
                        }
                    });
                    synchronized (progressLock) {
                        progressLock.wait(300);
                    }
                }
                SwingUtilities.invokeAndWait(() -> {
                    if (getErrorCount() == 0) {
                        errorWindow.sendFinished();
                    }
                    statusWindow.sendFinished();
                });
            } catch (Throwable e) {
                WLogger.severe(e);
                throw new Error(e);
            }
        }

    }


    @Override
    public void sendError(CompileError err) {
        super.sendError(err);
        if (err.getErrorType() == ErrorType.ERROR) {
            errorQueue.add(err);
        }
    }

    boolean show = true;
    private final long startTime = System.currentTimeMillis();
    private final Set<String> done = new HashSet<>();
    private long taskStartTime = startTime;

    @Override
    public void sendProgress(String whatsRunningNow) {
        if (whatsRunningNow != null) {
            WLogger.debug("progress: " + whatsRunningNow);
        }
        if (whatsRunningNow == null || done.contains(whatsRunningNow)) {
            return;
        }

        long overAllTime = 0;
        long doneTime = 0;

        for (Entry<String, Long> e : staticLastTimes.entrySet()) {
            if (done.contains(e.getKey())) {
                doneTime += e.getValue();
            }
            overAllTime += e.getValue();
        }

        long currentTime = System.currentTimeMillis();
        lastTimes.put(whatsRunningNow, currentTime - taskStartTime);
        taskStartTime = currentTime;
        this.currentlyWorkingOn = whatsRunningNow;
        done.add(whatsRunningNow);
        if (overAllTime > 0) {
            progress = doneTime * 1. / overAllTime;
        } else {
            progress = (System.currentTimeMillis() - startTime) / 30000.;
        }

        synchronized (progressLock) {
            progressLock.notifyAll();
        }
    }


    @Override
    public void sendFinished() {
        finished = true;
        staticLastTimes.putAll(lastTimes);
    }


    @Override
    public void showInfoMessage(final String message) {
        try {
            SwingUtilities.invokeAndWait(() -> JOptionPane.showMessageDialog(null, message));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
