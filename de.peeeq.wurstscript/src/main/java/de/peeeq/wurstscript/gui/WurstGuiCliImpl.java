package de.peeeq.wurstscript.gui;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;

/**
 * implementation for use with cli interfaces
 */
public class WurstGuiCliImpl extends WurstGui {

    private final boolean compactOutput;

    public WurstGuiCliImpl() {
        this(false);
    }

    public WurstGuiCliImpl(boolean compactOutput) {
        this.compactOutput = compactOutput;
    }

    @Override
    public void sendError(CompileError err) {
        if (compactOutput && isGeneratedJassNameResolutionWarning(err)) {
            return;
        }
        super.sendError(err);
    }

    private boolean isGeneratedJassNameResolutionWarning(CompileError err) {
        if (err.getErrorType() != ErrorType.WARNING) {
            return false;
        }
        String message = err.getMessage();
        if (!message.contains("Could not find variable") && !message.contains("Could not find a function")) {
            return false;
        }
        String source = err.getSource().getFile().replace('\\', '/').toLowerCase();
        return source.endsWith("war3map.j")
            || source.endsWith("output.j")
            || source.contains("/_build/")
            || source.startsWith("_build/");
    }

    @Override
    public void sendProgress(String msg) {
    }

    @Override
    public void sendFinished() {
        if (compactOutput) {
            return;
        }
        System.out.println("compilation finished (errors: " + getErrorCount() + ", warnings: " + getWarningList().size() + ")");
    }

    @Override
    public void showInfoMessage(String message) {
        System.out.println(message);

    }


}
