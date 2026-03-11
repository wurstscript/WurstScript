package de.peeeq.wurstio.languageserver;

import com.google.common.io.Files;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class BufferManager {
    private final Map<WFile, StringBuilder> currentBuffer = new HashMap<>();
    private final Map<WFile, Integer> latestVersion = new HashMap<>();

    public synchronized String getBuffer(TextDocumentIdentifier textDocument) {
        WFile uri = WFile.create(textDocument.getUri());
        return getBuffer(uri);
    }

    public String getBuffer(WFile uri) {
        StringBuilder sb = currentBuffer.get(uri);
        if (sb == null) {
            return readFileFromDisk(uri);
        }
        return sb.toString();
    }

    private StringBuilder buffer(WFile uri) {
        return currentBuffer.computeIfAbsent(uri, k -> new StringBuilder());
    }

    synchronized void handleFileChange(FileEvent fileEvent) {
        WFile uri = WFile.create(fileEvent.getUri());

        switch (fileEvent.getType()) {
            case Created:
            case Changed:
                readFileFromDisk(uri);
                break;
            case Deleted:
                currentBuffer.remove(uri);
                latestVersion.remove(uri);
                break;
        }
    }

    private String readFileFromDisk(WFile uri) {
        try {
            File file;
            try {
                file = uri.getFile();
            } catch (FileNotFoundException e) {
                WLogger.info("URI " + uri + " cannot be opened by Wurst: " + e);
                return "";
            }
            String str = Files.toString(file, StandardCharsets.UTF_8);
            StringBuilder sb = buffer(uri);
            sb.replace(0, sb.length(), str);
            return sb.toString();
        } catch (IOException e) {
            WLogger.severe("Could not read file " + uri);
            WLogger.severe(e);
            throw new RuntimeException(e);
        }
    }

    synchronized void handleChange(DidChangeTextDocumentParams params) {
        WFile uri = WFile.create(params.getTextDocument().getUri());
        Integer versionObj = params.getTextDocument().getVersion();
        int version = versionObj != null ? versionObj : getTextDocumentVersion(uri) + 1;
        if (version < getTextDocumentVersion(uri)) {
            // ignore old versions
            return;
        }
        latestVersion.put(uri, version);

        StringBuilder sb = buffer(uri);
        for (TextDocumentContentChangeEvent contentChange : params.getContentChanges()) {
            if (contentChange.getRange() == null) {
                // replace whole buffer
                sb.replace(0, sb.length(), contentChange.getText());
            } else {
                int start = getOffset(sb, contentChange.getRange().getStart());
                int end = getOffset(sb, contentChange.getRange().getEnd());
                if (end < start) {
                    int tmp = start;
                    start = end;
                    end = tmp;
                }
                sb.replace(start, end, contentChange.getText());
            }
        }
    }

    synchronized void handleOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem item = params.getTextDocument();
        WFile uri = WFile.create(item.getUri());
        latestVersion.put(uri, item.getVersion());
        StringBuilder sb = buffer(uri);
        sb.replace(0, sb.length(), item.getText());
    }

    synchronized void handleClose(DidCloseTextDocumentParams params) {
        WFile uri = WFile.create(params.getTextDocument().getUri());
        currentBuffer.remove(uri);
        latestVersion.remove(uri);
    }

    public synchronized int getTextDocumentVersion(WFile uri) {
        return latestVersion.getOrDefault(uri, -1);
    }

    private int getOffset(StringBuilder sb, Position position) {
        int pos = 0;
        int line = 0;
        while (pos < sb.length() && line < position.getLine()) {
            if (sb.charAt(pos) == '\n') {
                line++;
            }
            pos++;
        }
        pos += Math.max(0, position.getCharacter());
        return Math.min(Math.max(0, pos), sb.length());
    }

    synchronized public void updateFile(WFile wFile, String contents) {
        StringBuilder sb = buffer(wFile);
        sb.replace(0, sb.length(), contents);
    }
}
