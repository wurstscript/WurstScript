package tests.wurstscript.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.ErrorReporting;

public class ErrorReportingTest {

	@Test
	public void testSendErrorReport() {
		boolean result = ErrorReporting.instance.sendErrorReport(new Error("bla"), "source");
		assertEquals(true, result);
	}
	
	@Test
	public void testBigSource() throws IOException {
//		StringBuilder source = new StringBuilder();
//		appendFileContentsOf(new File("../Wurstpack/wurstscript/lib"), source);
//		
		String source = Files.toString(new File("/home/peter/kram/errorreport_source.wurst"), Charsets.UTF_8);
		
		boolean result = ErrorReporting.instance.sendErrorReport(new Error("bla"), source);
		assertEquals(true, result);
	}

	private void appendFileContentsOf(File f, StringBuilder sb) throws IOException {
		assertTrue("file  does not exist:" + f, f.exists());
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				appendFileContentsOf(child, sb);
			}
		} else {
			if (f.getName().endsWith(".wurst")) {
				sb.append("\n\n//################################\n\n");
				sb.append(Files.toString(f, Charsets.UTF_8));
			}
		}
	}


}
