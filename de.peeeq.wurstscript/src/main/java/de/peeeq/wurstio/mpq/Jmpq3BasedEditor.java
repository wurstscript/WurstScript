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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class Jmpq3BasedEditor implements MpqEditor {
    private final File mpqArchive;
    private final boolean readonly;
    private final MpqArchive archive;
    private final List<Consumer<MpqArchiveWriter>> changes = new ArrayList<>();
    private boolean keepHeaderOffset = true;
    private boolean closed;

    private MpqArchiveWriter getStagingWriter() throws IOException {
        MpqArchiveWriter writer = MpqArchiveWriter.from(archive,
            MpqWriteOptions.defaults().withPrefix(keepHeaderOffset));
        for (Consumer<MpqArchiveWriter> change : changes) {
            change.accept(writer);
        }
        return writer;
    }

    public Jmpq3BasedEditor(File mpqArchive, boolean readonly) throws Exception {
        Preconditions.checkNotNull(mpqArchive);
        if (!mpqArchive.exists()) {
            throw new FileNotFoundException("not found: " + mpqArchive);
        }
        this.mpqArchive = mpqArchive;
        this.readonly = readonly;
        this.archive = MpqArchive.open(mpqArchive.toPath(), MpqOpenOptions.warcraft3());
    }

    static void createEmptyArchive(File mpqArchive) throws IOException {
        byte[] image = MpqArchiveWriter.create(MpqWriteOptions.defaults()).toByteArray();
        Files.write(mpqArchive.toPath(), image);
    }

    @Override
    public void insertFile(String filenameInMpq, byte[] contents) {
        if (readonly) throw new IllegalStateException("MPQ archive is read-only");
        byte[] copy = contents.clone();
        changes.add(writer -> writer.put(filenameInMpq, copy));
    }

    @Override
    public void insertFile(String filenameInMpq, File contents) throws Exception {
        if (readonly) throw new IllegalStateException("MPQ archive is read-only");
        Path path = contents.toPath();
        changes.add(writer -> writer.put(filenameInMpq, path));
    }

    @Override
    public boolean canWrite() {
        return !readonly;
    }

    @Override
    public byte[] extractFile(String fileToExtract) throws Exception {
        return archive.read(fileToExtract);
    }

    @Override
    public void deleteFile(String filenameInMpq) {
        if (readonly) throw new IllegalStateException("MPQ archive is read-only");
        changes.add(writer -> writer.remove(filenameInMpq));
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        if (readonly) {
            archive.close();
            closed = true;
            return;
        }
        save(MpqWriteOptions.defaults());
    }

    @Override
    public boolean hasFile(String fileName) {
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
            archive.close();
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
        byte[] image = writer.toByteArray();
        archive.close();
        closed = true;
        Files.write(mpqArchive.toPath(), image);
    }

}
