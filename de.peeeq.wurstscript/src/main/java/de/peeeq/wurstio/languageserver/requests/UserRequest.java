package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;

import java.util.concurrent.CompletableFuture;

public abstract class UserRequest<Res> {

    private CompletableFuture<Res> fut = new CompletableFuture<>();

    public abstract Res execute(ModelManager modelManager);

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

}
