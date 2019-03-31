package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstscript.attributes.CompileError;
import org.eclipse.lsp4j.MessageType;

/**
 *
 */
public class RequestFailedException extends RuntimeException {
    private MessageType messageType;

    public RequestFailedException(MessageType messageType, String s) {
        super(s);
        this.messageType = messageType;
    }

    public RequestFailedException(MessageType messageType, String s, Throwable ex) {
        super(s + "\n" + ex, ex);
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
