package tests.wurstscript.utils;

import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstscript.attributes.CompileError;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class PjassTests {

    @Test
    @Ignore
    public void test() {
        Result result = Pjass.runPjass(new File("./testscripts/invalid/fail.j"));
        System.out.println(result.getMessage());


        WurstGuiImpl gui = new WurstGuiImpl();

        for (CompileError err : result.getErrors()) {
            System.out.println(err);
            System.out.println(err.getSource().getLeftPos());
            gui.sendError(err);
        }

    }

    private static File script(Path dir, String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, content);
        return file.toFile();
    }

    private static String longStringScript(String extraStatement) {
        return "function main takes nothing returns nothing\n"
            + "    local string s = \"" + "x".repeat(1024) + "\"\n"
            + "    local string t = \"" + "y".repeat(2048) + "\"\n"
            + extraStatement + "endfunction\n";
    }

    @Test
    public void longStringsAreNonBlockingWarnings() throws IOException {
        Path dir = Files.createTempDirectory("pjass-strings");
        File file = script(dir, "long.j", longStringScript(""));

        Result result = Pjass.runPjass(file);

        Assert.assertTrue(result.isOk(), result.getMessage());
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertTrue(result.getLogMessage().contains("String literals over 1023"), result.getLogMessage());
    }

    @Test
    public void longStringsDoNotHideOtherErrors() throws IOException {
        Path dir = Files.createTempDirectory("pjass-strings");
        File file = script(dir, "invalid.j", longStringScript("    call Nothing()\n"));

        Result result = Pjass.runPjass(file);

        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertTrue(result.getErrors().get(0).getMessage().contains("Nothing"));
    }

    @Test
    public void eachTreatsLongStringsAsWarningsWithoutHidingOtherErrors() throws IOException {
        Path dir = Files.createTempDirectory("pjass-strings");
        File good = script(dir, "long.j", longStringScript(""));
        File bad = script(dir, "invalid.j", longStringScript("    call Nothing()\n"));

        List<Result> results = Pjass.runPjassEach(Arrays.asList(good, bad, good));

        Assert.assertTrue(results.get(0).isOk(), results.get(0).getMessage());
        Assert.assertTrue(results.get(0).getLogMessage().contains("String literals over 1023"));
        Assert.assertFalse(results.get(1).isOk());
        Assert.assertEquals(results.get(1).getErrors().size(), 1);
        Assert.assertTrue(results.get(2).isOk(), results.get(2).getMessage());
    }

    @Test
    public void eachChecksScriptsWhichDefineTheSameNames() throws IOException {
        Path dir = Files.createTempDirectory("pjass-each");
        String main = "function main takes nothing returns nothing\nendfunction\n";
        File first = script(dir, "first.j", main);
        File second = script(dir, "second.j", main);

        List<Result> results = Pjass.runPjassEach(Arrays.asList(first, second));

        Assert.assertEquals(results.size(), 2);
        Assert.assertTrue(results.get(0).isOk(), results.get(0).getMessage());
        Assert.assertTrue(results.get(1).isOk(), results.get(1).getMessage());
    }

    @Test
    public void eachReportsErrorsForTheFileWhichHasThem() throws IOException {
        Path dir = Files.createTempDirectory("pjass-each");
        File good = script(dir, "good.j", "function a takes nothing returns nothing\nendfunction\n");
        File bad = script(dir, "bad.j", "function b takes nothing returns nothing\n    call Nothing()\nendfunction\n");
        File after = script(dir, "after.j", "function a takes nothing returns nothing\nendfunction\n");

        List<Result> results = Pjass.runPjassEach(Arrays.asList(good, bad, after));

        Assert.assertTrue(results.get(0).isOk(), results.get(0).getMessage());
        Assert.assertFalse(results.get(1).isOk());
        Assert.assertTrue(results.get(1).getMessage().contains("bad.j:2"), results.get(1).getMessage());
        Assert.assertFalse(results.get(1).getMessage().contains("good.j"), results.get(1).getMessage());
        Assert.assertEquals(results.get(1).getErrors().size(), 1);
        Assert.assertTrue(results.get(2).isOk(), results.get(2).getMessage());
    }

    @Test
    public void eachFailsEveryFileWhenABaseFileDoesNotParse() throws IOException {
        Path dir = Files.createTempDirectory("pjass-each");
        File common = script(dir, "common.j", "this is not jass\n");
        File blizzard = script(dir, "blizzard.j", "");
        File good = script(dir, "good.j", "function a takes nothing returns nothing\nendfunction\n");

        List<Result> results = Pjass.runPjassEach(common.getPath(), blizzard.getPath(), Arrays.asList(good, good));

        Assert.assertEquals(results.size(), 2);
        for (Result result : results) {
            Assert.assertFalse(result.isOk(), result.getMessage());
            Assert.assertTrue(result.getMessage().contains("common.j:1"), result.getMessage());
        }
    }

    @Test
    public void eachFailsEveryFileWhenABaseFileIsMissing() throws IOException {
        Path dir = Files.createTempDirectory("pjass-each");
        File common = script(dir, "common.j", "");
        File good = script(dir, "good.j", "function a takes nothing returns nothing\nendfunction\n");

        List<Result> results = Pjass.runPjassEach(common.getPath(), dir.resolve("blizzard.j").toString(), Arrays.asList(good));

        Assert.assertFalse(results.get(0).isOk(), results.get(0).getMessage());
        Assert.assertTrue(results.get(0).getMessage().contains("blizzard.j"), results.get(0).getMessage());
    }

    public static void main(String[] args) {

        WurstGuiImpl gui = new WurstGuiImpl();
        Result result = Pjass.runPjass(new File("./testscripts/invalid/fail.j"));
        System.out.println(result.getMessage());


        for (CompileError err : result.getErrors()) {
            System.out.println(err);
            System.out.println(err.getSource().getLeftPos());
            gui.sendError(err);
        }
        gui.sendFinished();
    }

}
