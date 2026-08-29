package tests.wurstscript.tests;

import de.peeeq.wurstio.Checksums;
import de.peeeq.wurstio.utils.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class ChecksumsTests {
    private Path tempDir;

    @AfterMethod(alwaysRun = true)
    public void cleanup() throws IOException {
        if (tempDir != null) {
            FileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    public void bytesToHexUsesLowercaseAndPreservesLeadingZeroes() {
        assertEquals(Checksums.bytesToHex(new byte[]{0, 15, 16, -1}), "000f10ff");
    }

    @Test
    public void mainWritesChecksumsForNestedFiles() throws IOException {
        tempDir = Files.createTempDirectory("wurst-checksums");
        Path input = Files.createDirectory(tempDir.resolve("input"));
        Path nested = Files.createDirectory(input.resolve("nested"));
        Files.write(nested.resolve("empty.bin"), new byte[0]);
        Files.writeString(input.resolve("a.txt"), "abc", StandardCharsets.UTF_8);
        Path output = tempDir.resolve("checksums.md5");

        Checksums.main(new String[]{input.toString(), output.toString()});

        String result = Files.readString(output, StandardCharsets.UTF_8);
        assertEquals(result,
            "/a.txt\n"
                + "900150983cd24fb0d6963f7d28e17f72\n"
                + "/nested/empty.bin\n"
                + "d41d8cd98f00b204e9800998ecf8427e\n");
    }

    @Test
    public void mainRejectsWrongArgumentCount() {
        RuntimeException error = expectThrows(RuntimeException.class,
            () -> Checksums.main(new String[]{"only-one-argument"}));
        assertEquals(error.getMessage(), "usage: 1. folder, 2. output file");
    }
}
