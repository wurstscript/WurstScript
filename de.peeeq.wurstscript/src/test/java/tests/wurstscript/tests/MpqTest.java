package tests.wurstscript.tests;

import com.google.common.io.Files;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MpqTest {
    private static final String TEST_W3X = "./testscripts/mpq/test_temp.w3x";
    private static final String TEST_W3X_ORIG = "./testscripts/mpq/test.w3x";

    @BeforeMethod
    public void before() throws IOException {
        File testMap = new File(TEST_W3X);
        Files.copy(new File(TEST_W3X_ORIG), testMap);
        Assert.assertTrue(testMap.exists());
    }

    @AfterMethod
    public void after() {
        File f = new File(TEST_W3X);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void test_insert() throws Exception {
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            if (edit.hasFile("test.txt")) {
                edit.deleteFile("test.txt");
            }
        }
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            edit.insertFile("test.txt", new File("./testscripts/mpq/test.txt"));
        }
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            Assert.assertTrue(edit.hasFile("test.txt"));
        }

    }

    @Test
    public void test_extract() throws Exception {
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            if (!edit.hasFile("test.txt")) {
                edit.insertFile("test.txt", new File("./testscripts/mpq/test.txt"));
            }
        }
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            byte[] f = edit.extractFile("war3map.j");
            Assert.assertTrue(f.length > 5);
        }

    }

    @Test
    public void test_delete() throws Exception {
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            edit.insertFile("test.txt", new File("./testscripts/mpq/test.txt"));
        }
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            edit.deleteFile("test.txt");
        }
        try (MpqEditor edit = MpqEditorFactory.getEditor(Optional.of(new File(TEST_W3X)))) {
            Assert.assertFalse(edit.hasFile("test.txt"));
        }
    }

}
