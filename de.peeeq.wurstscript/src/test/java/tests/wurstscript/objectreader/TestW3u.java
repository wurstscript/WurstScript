package tests.wurstscript.objectreader;

import de.peeeq.wurstio.objectreader.ObjectFile;
import de.peeeq.wurstio.objectreader.ObjectFileType;
import de.peeeq.wurstscript.WLogger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;


public class TestW3u {


    @Test
    public void testLongTooltip() throws IOException {
        File inFile = new File("testscripts/data/longtooltip.w3u");
        File outFile = new File("test-output/longtooltip.w3u");
        testW3u(inFile, outFile);

    }

    @Test
    public void testEbrUnits() throws IOException {
        File inFile = new File("testscripts/data/ebr_crash.w3u");
        File outFile = new File("test-output/ebr_crash_out.w3u");
        testW3u(inFile, outFile);

    }

    private void testW3u(File inFile, File outFile) throws IOException {
        //read
        ObjectFile objFile = new ObjectFile(inFile, ObjectFileType.UNITS);
        // write
        objFile.writeTo(outFile);
        // read input
        ObjectFile objFile2 = new ObjectFile(outFile, ObjectFileType.UNITS);

        // compare objFiles:
        assertEquals(objFile2.toString(), objFile.toString());
        WLogger.info(objFile.toString());
    }


}
