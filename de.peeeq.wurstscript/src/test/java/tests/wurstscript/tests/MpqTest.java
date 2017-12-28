package tests.wurstscript.tests;

import com.google.common.io.Files;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MpqTest {

    private static final String TEST_W3X = "./testscripts/mpq/test_temp.w3x";
    private static final String TEST_W3X_ORIG = "./testscripts/mpq/test.w3x";
    private static final String TEST_OUTPUT_PATH = "./test-output/";

    @Before
    public void before() throws IOException {
        File testMap = new File(TEST_W3X);
        Files.copy(new File(TEST_W3X_ORIG), testMap);
        Assert.assertTrue(testMap.exists());
    }

    @After
    public void after() {
        File f = new File(TEST_W3X);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void test_extract() throws Exception {
        try (MpqEditor edit = MpqEditorFactory.getEditor(new File(TEST_W3X))) {
            byte[] f = edit.extractFile("war3map.j");
            // edit.insertFile(new File("./testscripts/mpq/test.w3x"), "war3map.j",
            // f);
            Assert.assertTrue(f.length > 5);
            // bnlub
        }

    }

    // @Test
    // public void test_extract_w3u() {
    // try {
    // MpqEditorFactory.setFilepath("./lib/mpqedit/mpqeditor.exe");
    // MpqEditorFactory.setTempfolder(TEST_OUTPUT_PATH);
    // LadikMpq edit = MpqEditorFactory.getEditor();
    // File f = edit.extractFile(new File("./testscripts/mpq/objtest.w3x"),
    // "war3map.w3u");
    // Assert.assertTrue(f.exists());
    // f.delete();
    // //bnlub
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // }

    @Test
    public void test_insert() throws Exception {
        try (MpqEditor edit = MpqEditorFactory.getEditor(new File(TEST_W3X))) {
            edit.insertFile("test.txt", Files.toByteArray(new File(
                    "./testscripts/mpq/test.txt")));
        }
        Assert.assertTrue(true);

    }

    @Test
    public void test_delete() throws Exception {
        try (MpqEditor edit = MpqEditorFactory.getEditor(new File(TEST_W3X))) {
            edit.deleteFile("test.txt");
        }
        Assert.assertTrue(true);
    }

}
