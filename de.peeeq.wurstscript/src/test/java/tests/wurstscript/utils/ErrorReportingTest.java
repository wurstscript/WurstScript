package tests.wurstscript.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.ErrorReportingIO;
import de.peeeq.wurstscript.ErrorReporting;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class ErrorReportingTest {
    ErrorReporting instance = new ErrorReportingIO();

    @Ignore // would have side effects on server
    @Test
    public void testSendErrorReport() {
        boolean result = instance.sendErrorReport(new Error("bla"), "source");
        assertTrue(result);
    }

    @Ignore // would have side effects on server
    @Test
    public void testBigSource() throws IOException {
        String source = Files.toString(new File("/home/peter/kram/errorreport_source.wurst"), Charsets.UTF_8);

        boolean result = instance.sendErrorReport(new Error("bla"), source);
        assertTrue(result);
    }


}
