package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.NotNullList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorHandler {

    private final List<CompileError> errors   = new NotNullList<>();
    private final List<CompileError> warnings = new NotNullList<>();

    // Per-file buckets to avoid O(all) scans
    private final Map<String, List<CompileError>> errorsByFile   = new HashMap<>();
    private final Map<String, List<CompileError>> warningsByFile = new HashMap<>();

    private final WurstGui gui;
    private boolean unitTestMode = false;
    public static boolean outputTestSource = true;

    public ErrorHandler(WurstGui gui) {
        this.gui = gui;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public List<CompileError> getWarnings() {
        return warnings;
    }

    public List<CompileError> getErrors() {
        return errors;
    }

    public void setProgress(String message, double percent) {
        gui.sendProgress(message);
    }

    public WurstGui getGui() {
        return gui;
    }

    /** Called after makeCompileError() decides to keep it. */
    public void sendError(CompileError err) {
        if (err.getErrorType() == ErrorType.ERROR) {
            errors.add(err);
            addToBucket(errorsByFile, err);
        } else {
            warnings.add(err);
            addToBucket(warningsByFile, err);
        }
        gui.sendError(err);
    }

    public void enableUnitTestMode() {
        unitTestMode = true;
    }

    public boolean isUnitTestMode() {
        return unitTestMode;
    }

    public boolean isOutputTestSource() {
        return outputTestSource;
    }

    List<CompileError> getBucketForFile(String file, ErrorType type) {
        return (type == ErrorType.ERROR) ? errorsByFile.get(file) : warningsByFile.get(file);
    }

    void removeFromGlobal(CompileError err) {
        final String file = err.getSource().getFile();
        if (err.getErrorType() == ErrorType.ERROR) {
            errors.remove(err);
            removeFromBucket(errorsByFile, file, err);
        } else {
            warnings.remove(err);
            removeFromBucket(warningsByFile, file, err);
        }
    }

    private static void addToBucket(Map<String, List<CompileError>> byFile, CompileError err) {
        final String file = err.getSource().getFile();
        byFile.computeIfAbsent(file, f -> new NotNullList<>()).add(err);
    }

    private static void removeFromBucket(Map<String, List<CompileError>> byFile, String file, CompileError err) {
        List<CompileError> bucket = byFile.get(file);
        if (bucket != null) {
            bucket.remove(err);
            if (bucket.isEmpty()) {
                byFile.remove(file);
            }
        }
    }
}
