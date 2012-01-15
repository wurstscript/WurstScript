package de.peeeq.wurstscript.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import de.peeeq.wurstscript.ErrorReporting;

public class ErrorReportingTest {

	@Test
	public void testSendErrorReport() {
		boolean result = ErrorReporting.sendErrorReport(new Error("bla"));
		assertEquals(true, result);
	}

}
