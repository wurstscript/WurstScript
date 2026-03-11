package de.peeeq.wurstscript.parser;

import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.Reader;

public final class AntlrTokenPipeline {

    public static final class Result<TS extends TokenSource, P extends Parser, T> {
        public final CharStream input;
        public final TS tokenSource;
        public final CommonTokenStream tokens;
        public final P parser;
        public final T parseTree;

        Result(CharStream input, TS tokenSource, CommonTokenStream tokens, P parser, T parseTree) {
            this.input = input;
            this.tokenSource = tokenSource;
            this.tokens = tokens;
            this.parser = parser;
            this.parseTree = parseTree;
        }
    }

    @FunctionalInterface public interface TokenSourceFactory<TS extends TokenSource> { TS create(CharStream in); }
    @FunctionalInterface public interface ParserFactory<P extends Parser> { P create(TokenStream ts); }
    @FunctionalInterface public interface EntryRule<P extends Parser, T> { T parse(P parser); }

    public static <TS extends TokenSource, P extends Parser, T>
    Result<TS, P, T> run(
            Reader reader,
            TokenSourceFactory<TS> tokenSourceFactory,
            ParserFactory<P> parserFactory,
            EntryRule<P, T> entryRule,
            ANTLRErrorListener listener,
            java.util.function.BiConsumer<TS, ANTLRErrorListener> installLexerListener
    ) throws IOException {

        CharStream input = CharStreams.fromReader(reader);

        TS tokenSource = tokenSourceFactory.create(input);
        if (installLexerListener != null) {
            installLexerListener.accept(tokenSource, listener);
        }

        CommonTokenStream tokens = new CommonTokenStream(tokenSource);

        P parser = parserFactory.create(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        T tree = entryRule.parse(parser);
        return new Result<>(input, tokenSource, tokens, parser, tree);
    }

    private AntlrTokenPipeline() {}
}
