package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.gui.WurstGui;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressNotification;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.List;

/** Reports map-build diagnostics and progress through the editor's language client. */
public class WurstGuiLsp extends WurstGui {
    private final ModelManager modelManager;
    private final LanguageClient client;
    private final Either<String, Integer> token;
    private boolean finished;

    public WurstGuiLsp(ModelManager modelManager, LanguageClient client) {
        this(modelManager, client, null, "Building Wurst map");
    }

    public WurstGuiLsp(ModelManager modelManager, LanguageClient client, Either<String, Integer> token, String title) {
        this.modelManager = modelManager;
        this.client = client;
        this.token = token;
        modelManager.reportBuildDiagnostics(List.of());
        WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
        begin.setTitle(title);
        begin.setCancellable(false);
        notifyProgress(begin);
    }

    private void notifyProgress(WorkDoneProgressNotification notification) {
        if (token != null) {
            client.notifyProgress(new ProgressParams(token, Either.forLeft(notification)));
        }
    }

    @Override
    public void sendProgress(String message) {
        if (finished) {
            return;
        }
        client.logMessage(new MessageParams(MessageType.Log, message));
        WorkDoneProgressReport report = new WorkDoneProgressReport();
        report.setMessage(message);
        report.setCancellable(false);
        notifyProgress(report);
    }

    @Override
    public void sendFinished() {
        if (!finished) {
            finished = true;
            try {
                modelManager.reportBuildDiagnostics(getErrorsAndWarnings());
            } finally {
                notifyProgress(new WorkDoneProgressEnd());
            }
        }
    }

    @Override
    public void showInfoMessage(String message) {
        client.showMessage(new MessageParams(MessageType.Info, message));
    }
}
