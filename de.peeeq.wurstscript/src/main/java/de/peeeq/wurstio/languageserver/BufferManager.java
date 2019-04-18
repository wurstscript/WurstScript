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
    private Map<WFile, StringBuilder> currentBuffer = new HashMap<>();
    private Map<WFile, Integer> latestVersion = new HashMap<>();

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
            case Deleted:
                currentBuffer.remove(uri);
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
        int version = params.getTextDocument().getVersion();
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
                sb.replace(start, end - start, contentChange.getText());
            }
        }
    }

    public synchronized int getTextDocumentVersion(WFile uri) {
        return latestVersion.getOrDefault(uri, -1);
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

    synchronized public void updateFile(WFile wFile, String contents) {
        StringBuilder sb = buffer(wFile);
        sb.replace(0, sb.length(), contents);
    }
}
