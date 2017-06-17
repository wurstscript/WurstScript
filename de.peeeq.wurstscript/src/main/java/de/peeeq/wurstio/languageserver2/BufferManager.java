package de.peeeq.wurstio.languageserver2;

import com.google.common.io.Files;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class BufferManager {
    private Map<WFile, StringBuilder> currentBuffer = new HashMap<>();


    public BufferManager() {

    }

    public String getBuffer(TextDocumentIdentifier textDocument) {
        WFile uri = WFile.create(textDocument.getUri());
        return getBuffer(uri);
    }

    private String getBuffer(WFile uri) {
        StringBuilder sb = currentBuffer.get(uri);
        if (sb == null) {
            return "";
        }
        return sb.toString();
    }

    private StringBuilder buffer(WFile uri) {
        StringBuilder res = currentBuffer.get(uri);
        if (res == null) {
            res = new StringBuilder();
            currentBuffer.put(uri, res);
        }
        return res;
    }

    public void handleFileChange(FileEvent fileEvent) {
        String uri = fileEvent.getUri();
        switch (fileEvent.getType()) {
            case Created:
            case Changed:
                try {
                    String str = Files.toString(new File(uri), StandardCharsets.UTF_8);
                    StringBuilder sb = buffer(WFile.create(uri));
                    sb.replace(0, sb.length(), str);
                } catch (IOException e) {
                    WLogger.severe("Could not read file " + uri);
                    WLogger.severe(e);
                }
                break;
            case Deleted:
                currentBuffer.remove(uri);
                break;
        }
    }

    public void handleChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        StringBuilder sb = buffer(WFile.create(uri));
        for (TextDocumentContentChangeEvent contentChange : params.getContentChanges()) {
            if (contentChange.getRange() == null) {
                // replace whole buffer
                sb.replace(0, sb.length(), contentChange.getText());
            } else {
                int start = getOffset(sb, contentChange.getRange().getStart());
                int end = getOffset(sb, contentChange.getRange().getEnd());
                sb.replace(start, end - start, contentChange.getText());
            }
        }
    }

    private int getOffset(StringBuilder sb, Position position) {
        int pos = 0;
        int line = 1;
        while (pos < sb.length() && line < position.getLine()) {
            if (sb.charAt(pos) == '\n') {
                line++;
            }
            pos++;
        }
        pos += position.getCharacter();
        return Math.min(pos, sb.length() - 1);
    }

    public void updateFile(WFile wFile, String contents) {
        StringBuilder sb = buffer(wFile);
        sb.replace(0, sb.length(), contents);
    }
}
