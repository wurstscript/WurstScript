package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;

public abstract class UserRequest {
    private int requestNr;

    public UserRequest(int requestNr) {
        this.requestNr = requestNr;
    }

    public abstract Object execute(ModelManager modelManager);

    public int getRequestNr() {
        return requestNr;
    }
}
