package de.peeeq.wurstio.languageserver2.requests;

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
        fut.complete(execute(modelManager));
    }

}
