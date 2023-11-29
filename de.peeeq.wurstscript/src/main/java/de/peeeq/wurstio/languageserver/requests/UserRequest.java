package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public abstract class UserRequest<Res> {

    private final CompletableFuture<Res> fut = new CompletableFuture<>();

    public abstract Res execute(ModelManager modelManager) throws IOException;

    public boolean keepDuplicateRequests() {
        return false;
    }

    public void cancel() {
        fut.cancel(true);
    }

    public CompletableFuture<Res> getFuture() {
        return fut;
    }

    public void run(ModelManager modelManager) {
        try {
            Res res = execute(modelManager);
            fut.complete(res);
        } catch (Exception e) {
            fut.completeExceptionally(e);
        }
    }

    public void handleException(LanguageClient languageClient, Throwable err, CompletableFuture<Res> resFut) {
        languageClient.showMessage(new MessageParams(MessageType.Error, err.getMessage()));
        resFut.completeExceptionally(err);
    }


}
