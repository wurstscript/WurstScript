package de.peeeq.wurstscript.gui;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class WurstGui {

    private final List<CompileError> errors = Lists.newArrayList();


    public abstract void sendProgress(String whatsRunningNow);

    public abstract void sendFinished();

    public abstract void showInfoMessage(String message);

    public void sendError(CompileError err) {
        if (shouldSuppressWarning(err)) {
            return;
        }
        errors.add(err);
    }

    private boolean shouldSuppressWarning(CompileError err) {
        if (err.getErrorType() != ErrorType.WARNING) {
            return false;
        }
        String file = err.getSource().getFile();
        String normalized = file.replace('\\', '/').toLowerCase();
        return normalized.contains("/_build/dependencies/")
            || normalized.startsWith("_build/dependencies/");
    }

    public void clearErrors() {
        errors.clear();
    }

    public final int getErrorCount() {
        return getErrorList().size();
    }

    public final String getErrors() {
        return Utils.join(errors, "\n");
    }

    public final List<CompileError> getErrorList() {
        return errors.stream()
                .filter(e -> e.getErrorType() == ErrorType.ERROR)
                .collect(Collectors.toList());
    }

    public final List<CompileError> getWarningList() {
        return errors.stream()
                .filter(e -> e.getErrorType() == ErrorType.WARNING)
                .collect(Collectors.toList());
    }

    public final List<CompileError> getErrorsAndWarnings() {
        return Collections.unmodifiableList(errors);
    }

}
