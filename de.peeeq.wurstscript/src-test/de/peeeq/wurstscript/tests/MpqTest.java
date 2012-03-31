package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import junit.framework.Assert;

import com.google.common.base.Charsets;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import de.peeeq.wurstscript.Pjass;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.JassInterpreter;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizer;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizerImpl;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.mpq.LadikMpq;
import de.peeeq.wurstscript.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.utils.Utils;

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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
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
	
	


	
}
