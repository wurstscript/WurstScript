package de.peeeq.wurstscript.objectreader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

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

	private void testW3u(File inFile, File outFile) {
		//read
		ObjectFile objFile = new ObjectFile(inFile, ObjectFileType.UNITS);
		// write
		objFile.writeTo(outFile);
		// read input
		ObjectFile objFile2 = new ObjectFile(outFile, ObjectFileType.UNITS);
		
		// compare objFiles:
		assertEquals(objFile2.toString(), objFile.toString());
		System.out.println(objFile.toString());
	}


}
