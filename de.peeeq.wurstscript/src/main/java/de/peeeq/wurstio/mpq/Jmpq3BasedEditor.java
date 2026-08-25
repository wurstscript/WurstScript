package de.peeeq.wurstio.mpq;

import com.google.common.base.Preconditions;
import org.inwc3.jmpq.MpqArchive;
import org.inwc3.jmpq.MpqArchiveWriter;
import org.inwc3.jmpq.MpqOpenOptions;
import org.inwc3.jmpq.MpqWriteOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

class Jmpq3BasedEditor implements MpqEditor {
    private final File mpqArchive;
    private final boolean readonly;
    private final MpqArchive archive;
    private final List<Consumer<MpqArchiveWriter>> changes = new ArrayList<>();
    private final Map<String, StagedFile> stagedFiles = new HashMap<>();
    private final Set<String> deletedFiles = new HashSet<>();
    private MpqArchiveWriter stagingWriter;
    private boolean stagingWriterAvailable;
    private boolean keepHeaderOffset = true;
    private boolean archiveClosed;
    private boolean closed;

    private MpqArchiveWriter getStagingWriter() throws IOException {
        if (stagingWriter == null) {
            stagingWriter = MpqArchiveWriter.from(archive,
                MpqWriteOptions.defaults().withPrefix(keepHeaderOffset));
        }
        return stagingWriter;
    }

    public Jmpq3BasedEditor(File mpqArchive, boolean readonly) throws Exception {
        Preconditions.checkNotNull(mpqArchive);
        if (!mpqArchive.exists()) {
            throw new FileNotFoundException("not found: " + mpqArchive);
        }
        this.mpqArchive = mpqArchive;
        this.readonly = readonly;
        this.archive = MpqArchive.open(mpqArchive.toPath(), MpqOpenOptions.warcraft3());
        if (!readonly) {
            try {
                getStagingWriter();
                stagingWriterAvailable = true;
            } catch (IOException e) {
                stagingWriterAvailable = false;
            }
        }
    }

    static void createEmptyArchive(File mpqArchive) throws IOException {
        MpqArchiveWriter.create(MpqWriteOptions.defaults()).save(mpqArchive.toPath());
    }

    @Override
    public void insertFile(String filenameInMpq, byte[] contents) {
        ensureWritable();
        byte[] copy = contents.clone();
        stage(filenameInMpq, new StagedFile(copy, null), writer -> writer.put(filenameInMpq, copy));
    }

    @Override
    public void insertFile(String filenameInMpq, File contents) throws Exception {
        ensureWritable();
        Path path = contents.toPath();
        stage(filenameInMpq, new StagedFile(null, path), writer -> writer.put(filenameInMpq, path));
    }

    @Override
    public boolean canWrite() {
        Path archivePath = mpqArchive.toPath().toAbsolutePath();
        Path parent = archivePath.getParent();
        return !readonly
            && stagingWriterAvailable
            && Files.isWritable(archivePath)
            && parent != null
            && Files.isWritable(parent);
    }

    @Override
    public byte[] extractFile(String fileToExtract) throws Exception {
        String key = stagedKey(fileToExtract);
        StagedFile staged = stagedFiles.get(key);
        if (staged != null) {
            return staged.read();
        }
        if (deletedFiles.contains(key)) {
            throw new FileNotFoundException("not found in staged MPQ: " + fileToExtract);
        }
        return archive.read(fileToExtract);
    }

    @Override
    public void deleteFile(String filenameInMpq) {
        ensureWritable();
        String key = stagedKey(filenameInMpq);
        stagedFiles.remove(key);
        deletedFiles.add(key);
        addChange(writer -> writer.remove(filenameInMpq));
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        if (readonly) {
            closeArchive();
            closed = true;
            return;
        }
        if (changes.isEmpty()) {
            closeArchive();
            closed = true;
            return;
        }
        save(MpqWriteOptions.defaults());
    }

    @Override
    public boolean hasFile(String fileName) {
        String key = stagedKey(fileName);
        if (deletedFiles.contains(key)) {
            return false;
        }
        if (stagedFiles.containsKey(key)) {
            return true;
        }
        try {
            return getStagingWriter().contains(fileName);
        } catch (IOException e) {
            throw new RuntimeException("Could not inspect MPQ archive", e);
        }
    }

    @Override
    public void setKeepHeaderOffset(boolean flag) {
        keepHeaderOffset = flag;
    }

    @Override
    public void closeWithCompression() throws IOException {
        if (closed) return;
        if (readonly) {
            closeArchive();
            closed = true;
            return;
        }
        if (changes.isEmpty()) {
            closeArchive();
            closed = true;
            return;
        }
        save(MpqWriteOptions.recompressed().withPrefix(keepHeaderOffset));
    }

    private void save(MpqWriteOptions options) throws IOException {
        MpqArchiveWriter writer = MpqArchiveWriter.from(archive, options.withPrefix(keepHeaderOffset));
        for (Consumer<MpqArchiveWriter> change : changes) {
            change.accept(writer);
        }
        Path temporaryArchive = null;
        boolean installed = false;
        try {
            Path parent = mpqArchive.toPath().toAbsolutePath().getParent();
            temporaryArchive = Files.createTempFile(parent, ".wurst-mpq-", ".tmp");
            writer.save(temporaryArchive);
            closeArchive();
            Files.move(temporaryArchive, mpqArchive.toPath(), StandardCopyOption.REPLACE_EXISTING);
            installed = true;
        } finally {
            closeArchive();
            closed = true;
            if (!installed && temporaryArchive != null) {
                Files.deleteIfExists(temporaryArchive);
            }
        }
    }

    private void ensureWritable() {
        if (!canWrite()) {
            throw new IllegalStateException("MPQ archive is not writable: " + mpqArchive);
        }
    }

    private void closeArchive() {
        if (!archiveClosed) {
            archive.close();
            archiveClosed = true;
        }
    }

    private void stage(String filename, StagedFile staged, Consumer<MpqArchiveWriter> change) {
        String key = stagedKey(filename);
        stagedFiles.put(key, staged);
        deletedFiles.remove(key);
        addChange(change);
    }

    private void addChange(Consumer<MpqArchiveWriter> change) {
        changes.add(change);
        try {
            getStagingWriter();
            change.accept(stagingWriter);
        } catch (IOException e) {
            throw new IllegalStateException("Could not stage MPQ change", e);
        }
    }

    private static String stagedKey(String filename) {
        return filename.replace('/', '\\').toLowerCase(Locale.ROOT);
    }

    private record StagedFile(byte[] bytes, Path path) {
        private byte[] read() throws IOException {
            return path == null ? bytes.clone() : Files.readAllBytes(path);
        }
    }

}
