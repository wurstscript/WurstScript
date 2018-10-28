package tests.wurstscript.tests;

import com.google.common.io.Files;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class MpqTest {
    private static final String TEST_W3X = "./testscripts/mpq/test_temp.w3x";
    private static final String TEST_W3X_ORIG = "./testscripts/mpq/test.w3x";

    @BeforeClass
    public void before() throws IOException {
        File testMap = new File(TEST_W3X);
        Files.copy(new File(TEST_W3X_ORIG), testMap);
        Assert.assertTrue(testMap.exists());
    }

    @AfterClass
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
            Assert.assertTrue(f.length > 5);
        }

    }

    @Test
    public void test_insert() throws Exception {
        try (MpqEditor edit = MpqEditorFactory.getEditor(new File(TEST_W3X))) {
            edit.insertFile("test.txt", new File("./testscripts/mpq/test.txt"));
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
