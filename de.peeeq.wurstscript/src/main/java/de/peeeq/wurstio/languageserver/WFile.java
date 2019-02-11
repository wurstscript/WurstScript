package de.peeeq.wurstio.languageserver;

import org.eclipse.lsp4j.TextDocumentIdentifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 *
 */
public abstract class WFile {

    private WFile() {

    }

    public static WFile create(File f) {
        return new Ok(f);
    }

    public static WFile create(Path f) {
        return new Ok(f.toFile());
    }

    public static WFile create(URI f) {
        try {
            return new Ok(new File(f));
        } catch (IllegalArgumentException e) {
            return new Unsupported(f.toString());
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

    public abstract File getFile() throws FileNotFoundException;

    public abstract String getUriString();

    public abstract Path getPath() throws FileNotFoundException;

    private static class Unsupported extends WFile {
        private final String uriString;

        public Unsupported(String uriString) {
            this.uriString = uriString;
        }

        @Override
        public File getFile() throws FileNotFoundException {
            throw new FileNotFoundException("URI " + uriString + " is not supported.");
        }

        @Override
        public String getUriString() {
            return uriString;
        }

        @Override
        public Path getPath() throws FileNotFoundException {
            throw new FileNotFoundException("URI " + uriString + " is not supported.");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Unsupported that = (Unsupported) o;
            return Objects.equals(uriString, that.uriString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uriString);
        }

        @Override
        public String toString() {
            return uriString;
        }
    }

    private static class Ok extends WFile {

        private final File file;

        private Ok(File file) {
            try {
                file = file.getAbsoluteFile().getCanonicalFile();
            } catch (Exception e) {
                file = file.getAbsoluteFile();
            }
            this.file = file;
        }

        @Override
        public File getFile() {
            return file;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ok wFile = (Ok) o;
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

        @Override
        public String getUriString() {
            return getFile().toURI().toString();
        }

        public Path getPath() {
            return file.toPath();
        }
    }


}
