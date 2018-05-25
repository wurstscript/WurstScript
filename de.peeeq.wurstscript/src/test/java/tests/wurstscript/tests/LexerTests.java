package tests.wurstscript.tests;

import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.parser.antlr.ExtendedWurstLexer;
import de.peeeq.wurstscript.utils.Utils;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class LexerTests extends WurstScriptTest {


    @Test
    public void dotNewline() {
        List<String> tokens = getTokenNames(
                "package test",
                "init",
                "    new A",
                "        .",
                "init"
        );
        assertEquals(Arrays.asList(
                "'package'", "ID", "NL",
                "'init'", "NL",
                "STARTBLOCK", "'new'", "ID", "'.'", "NL", "ENDBLOCK",
                "'init'", "NL",
                "'endpackage'", "NL"
        ), tokens);
    }

    @Test
    public void dotNewline2() {
        List<String> tokens = getTokenNames(
                "package test",
                "class A",
                "function A.foo() returns int",
                "function A.fuu() returns bool",
                "function test()",
                "	new A()",
                "		.",
                "",
                "function a()"
        );
        assertEquals(Arrays.asList(
                "'package'", "ID", "NL",
                "'class'", "ID", "NL",
                "'function'", "ID", "'.'", "ID", "'('", "')'", "'returns'", "ID", "NL",
                "'function'", "ID", "'.'", "ID", "'('", "')'", "'returns'", "ID", "NL",
                "'function'", "ID", "'('", "')'", "NL",
                "STARTBLOCK", "'new'", "ID", "'('", "')'", "'.'", "NL", "ENDBLOCK",
                "'function'", "ID", "'('", "')'", "NL",
                "'endpackage'", "NL"),
                tokens);
    }


    private List<String> getTokenNames(String... input) {
        return getTokens(input).stream().map(t -> WurstParser.VOCABULARY.getDisplayName(t.getType())).collect(Collectors.toList());

    }


    private List<Token> getTokens(String... input) {
        try {
            CharStream inputStream = new ANTLRInputStream(new StringReader(Utils.join(input, "\n")));

            ExtendedWurstLexer lexer = new ExtendedWurstLexer(inputStream);


            List<Token> result = new ArrayList<>();
            while (true) {
                Token token = lexer.nextToken();
                if (token.getType() == WurstParser.EOF) {
                    return result;
                }
                result.add(token);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
