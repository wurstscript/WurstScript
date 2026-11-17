package de.peeeq.wurstio.languageserver.requests;

import net.moonlightflower.wc3libs.port.Orient;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class OutsideStartTest {
    @Test
    public void quotesOnlyWhatACommandLineWouldSplit() {
        assertEquals(OutsideStart.quoteWindowsArg("-editor"), "-editor");
        assertEquals(OutsideStart.quoteWindowsArg(""), "\"\"");
        assertEquals(OutsideStart.quoteWindowsArg("C:\\Program Files\\Warcraft III\\Warcraft III.exe"),
            "\"C:\\Program Files\\Warcraft III\\Warcraft III.exe\"");
        // Backslashes before a quote, or before the closing one, are doubled; others are left alone.
        assertEquals(OutsideStart.quoteWindowsArg("say \"hi\""), "\"say \\\"hi\\\"\"");
        assertEquals(OutsideStart.quoteWindowsArg("C:\\dir with space\\"), "\"C:\\dir with space\\\\\"");
        assertEquals(OutsideStart.quoteWindowsArg("C:\\dir\\"), "C:\\dir\\", "nothing to split, nothing to quote");
        assertEquals(OutsideStart.quoteWindowsArg("a\\\"b"), "\"a\\\\\\\"b\"");
        assertEquals(OutsideStart.commandLine(List.of("game.exe", "-loadfile", "C:\\my map.w3x")),
            "game.exe -loadfile \"C:\\my map.w3x\"");
    }

    @Test
    public void aProcessStartedThroughWmiGetsItsArgumentsAndDirectoryAsGiven() throws Exception {
        if (!Orient.isWindowsSystem()) throw new SkipException("WMI is Windows only");
        Path dir = Files.createTempDirectory("wurst outside start");
        Path program = dir.resolve("PrintArguments.java");
        Path result = dir.resolve("arguments.txt");
        Files.writeString(program, """
            import java.nio.file.*;
            import java.util.*;
            public class PrintArguments {
                public static void main(String[] args) throws Exception {
                    List<String> lines = new ArrayList<>(List.of(System.getProperty("user.dir")));
                    lines.addAll(Arrays.asList(args).subList(1, args.length));
                    Path partial = Path.of(args[0] + ".tmp");
                    Files.write(partial, lines);
                    Files.move(partial, Path.of(args[0]));
                }
            }
            """);
        Path java = Path.of(System.getProperty("java.home"), "bin", "java.exe");
        List<String> arguments = List.of("with space", "quote\"inside", "trailing\\", "C:\\dir with space\\", "",
            "both \\\"q\\\" and \\");
        List<String> command = new ArrayList<>(List.of(java.toString(), program.toString(), result.toString()));
        command.addAll(arguments);

        long pid = OutsideStart.start(command, dir, OutsideStart.Show.HIDDEN);

        assertTrue(pid > 0);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
        while (!Files.exists(result) && System.nanoTime() < deadline) Thread.sleep(100);
        assertTrue(Files.exists(result), "the program started through WMI wrote " + result);
        List<String> lines = Files.readAllLines(result, StandardCharsets.UTF_8);
        assertEquals(Path.of(lines.get(0)).toRealPath(), dir.toRealPath(), "the working directory");
        assertEquals(lines.subList(1, lines.size()), arguments);
    }

    @Test
    public void aProcessWmiCannotCreateIsAnError() {
        if (!Orient.isWindowsSystem()) throw new SkipException("WMI is Windows only");
        Path missing = Path.of(System.getProperty("java.io.tmpdir"), "wurst-no-such-program-" + System.nanoTime() + ".exe");
        java.io.IOException failure = org.testng.Assert.expectThrows(java.io.IOException.class,
            () -> OutsideStart.start(List.of(missing.toString()), null, OutsideStart.Show.HIDDEN));
        assertTrue(failure.getMessage().contains("WMI could not start " + missing.getFileName()), failure.getMessage());
    }
}
