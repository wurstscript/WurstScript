package tests.wurstscript.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstio.ErrorReportingIO;
import de.peeeq.wurstscript.ErrorReporting;

public class ErrorReportingTest {
	ErrorReporting instance = new ErrorReportingIO();

	@Test
	public void testSendErrorReport() {
		boolean result = instance.sendErrorReport(new Error("bla"), "source");
		assertEquals(true, result);
	}
	
	@Test
	public void testBigSource() throws IOException {
//		StringBuilder source = new StringBuilder();
//		appendFileContentsOf(new File("../Wurstpack/wurstscript/lib"), source);
//		
		String source = Files.toString(new File("/home/peter/kram/errorreport_source.wurst"), Charsets.UTF_8);
		
		boolean result = instance.sendErrorReport(new Error("bla"), source);
		assertEquals(true, result);
	}


}
