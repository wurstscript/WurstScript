package tests.wurstscript.tests;

import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;

import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class WurstLanguageServerTest extends WurstScriptTest {
    protected CompletionTestData input(String... lines) {
        return input(true, lines);
    }

    protected CompletionTestData input(boolean newLineAtEnd, String... lines) {
        StringBuilder buffer = new StringBuilder();
        int completionLine = -1;
        int completionColumn = -1;
        int lineNr = 0;
        for (String line : lines) {
            lineNr++;
            int cursorIndex = line.indexOf('|');
            if (cursorIndex >= 0) {
                completionLine = lineNr;
                completionColumn = cursorIndex + 1;
                buffer.append(line.replaceFirst("\\|", ""));
            } else {
                buffer.append(line);
            }
            if (newLineAtEnd || lineNr < lines.length) {
                buffer.append("\n");
            }
        }

        return new CompletionTestData(buffer.toString(), completionLine - 1, completionColumn - 1);
    }

    private WurstModel compile(String... lines) {
        String input = String.join("\n", lines);
        WurstGui gui = new WurstGuiLogger();
        RunArgs runArgs = new RunArgs();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        compiler.getErrorHandler().enableUnitTestMode();
        Map<String, String> inputMap = ImmutableMap.of("test", input);
        return parseFiles(Collections.emptyList(), inputMap, false, compiler);
    }

    static class CompletionTestData {
        String buffer;
        int line;
        int column;

        public CompletionTestData(String buffer, int line, int column) {
            this.buffer = buffer;
            this.line = line;
            this.column = column;
        }
    }
}
