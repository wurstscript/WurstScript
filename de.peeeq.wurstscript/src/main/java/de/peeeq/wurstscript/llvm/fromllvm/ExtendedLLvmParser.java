package de.peeeq.wurstscript.llvm.fromllvm;

import de.peeeq.wurstscript.llvm.antlr.LlvmLexer;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser;
import de.peeeq.wurstscript.llvm.ast.Prog;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.IOException;
import java.io.Reader;

/**
 *
 */
public class ExtendedLLvmParser {

    public static Prog parse(Reader input) throws IOException {
        return parse(CharStreams.fromReader(input));
    }

    public static Prog parse(String input) {
        return parse(CharStreams.fromString(input));
    }

    public static Prog parse(CharStream input) {
        LlvmLexer lexer = new LlvmLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        LlvmParser parser = new LlvmParser(tokens);
        LlvmParser.ModuleContext module = parser.module();
        return new LlvmParseTreeTransformer().transformModule(module);
    }



}
