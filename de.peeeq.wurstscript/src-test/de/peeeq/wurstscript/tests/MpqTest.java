package de.peeeq.wurstscript.tests;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import de.peeeq.wurstscript.mpq.LadikMpq;
import de.peeeq.wurstscript.mpq.MpqEditorFactory;

public class MpqTest {

	private static final String TEST_OUTPUT_PATH = "./test-output/";

	@Test
	public void test_extract() {
		try {
			MpqEditorFactory.setFilepath("./lib/mpqedit/mpqeditor.exe");
			MpqEditorFactory.setTempfolder(TEST_OUTPUT_PATH);
			LadikMpq edit = MpqEditorFactory.getEditor();
			File f = edit.extractFile(new File("./testscripts/mpq/test.w3x"), "war3map.j");
			//edit.insertFile(new File("./testscripts/mpq/test.w3x"), "war3map.j", f);
			Assert.assertTrue(f.exists());
			f.delete();
			//bnlub
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
//	@Test
//	public void test_extract_w3u() {
//		try {
//			MpqEditorFactory.setFilepath("./lib/mpqedit/mpqeditor.exe");
//			MpqEditorFactory.setTempfolder(TEST_OUTPUT_PATH);
//			LadikMpq edit = MpqEditorFactory.getEditor();
//			File f = edit.extractFile(new File("./testscripts/mpq/objtest.w3x"), "war3map.w3u");
//			Assert.assertTrue(f.exists());
//			f.delete();
//			//bnlub
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
	@Test
	public void test_insert() {
		try {
			MpqEditorFactory.setFilepath("./lib/mpqedit/mpqeditor.exe");
			MpqEditorFactory.setTempfolder(TEST_OUTPUT_PATH);
			LadikMpq edit = MpqEditorFactory.getEditor();
			edit.insertFile(new File("./testscripts/mpq/test.w3x"), "test.txt", new File("./testscripts/mpq/test.txt"));
			Assert.assertTrue(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void test_delete() {
		try {
			MpqEditorFactory.setFilepath("./lib/mpqedit/mpqeditor.exe");
			MpqEditorFactory.setTempfolder(TEST_OUTPUT_PATH);
			LadikMpq edit = MpqEditorFactory.getEditor();
			edit.deleteFile(new File("./testscripts/mpq/test.w3x"), "test.txt");
			Assert.assertTrue(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void test_compact() {
		try {
			MpqEditorFactory.setFilepath("./lib/mpqedit/mpqeditor.exe");
			MpqEditorFactory.setTempfolder(TEST_OUTPUT_PATH);
			LadikMpq edit = MpqEditorFactory.getEditor();
			edit.compact(new File("./testscripts/mpq/test.w3x"));
			Assert.assertTrue(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	


	
}
