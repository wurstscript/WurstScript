package de.peeeq.wurstio.languageserver;

import org.eclipse.lsp4j.TextDocumentIdentifier;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 *
 */
public class WFile {
    private final File file;

    private WFile(File file) {
        try {
            file = file.getAbsoluteFile().getCanonicalFile();
        } catch (Exception e) {
            file = file.getAbsoluteFile();
        }
        this.file = file;
    }

    public static WFile create(File f) {
        return new WFile(f);
    }

    public static WFile create(Path f) {
        return new WFile(f.toFile());
    }

    public static WFile create(URI f) {
        try {
            return new WFile(new File(f));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("URI " + f + " is not a vald file", e);
        }
    }

    public static WFile create(String uri) {
        try {
            URI u = new URI(uri);
            if (u.isAbsolute()) {
                return create(u);
            }
        } catch (URISyntaxException e) {
            // ignore
        }
        // if it is not a valid absolute URI, maybe it is a valid path?
        try {
            return create(Paths.get(uri));
        } catch (InvalidPathException e2) {
            throw new RuntimeException("URI string '" + uri + "' is neither a correct URI nor a correct path.", e2);
        }
    }

    public static WFile create(TextDocumentIdentifier textDocument) {
        return create(textDocument.getUri());
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WFile wFile = (WFile) o;
        return Objects.equals(file, wFile.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String toString() {
        return file.toString();
    }

    public String getUriString() {
        return getFile().toURI().toString();
    }

    public Path getPath() {
        return file.toPath();
    }
}
