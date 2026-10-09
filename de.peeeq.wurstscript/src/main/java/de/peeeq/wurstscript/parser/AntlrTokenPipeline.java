package de.peeeq.wurstscript.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;
import java.io.Reader;

public final class AntlrTokenPipeline {

    public static final class Result<TS extends TokenSource, P extends Parser, T> {
        public final CharStream input;
        public final TS tokenSource;
        public final CommonTokenStream tokens;
        public final P parser;
        public final T parseTree;
        /** Whether the SLL pass did not accept the input and it was parsed again with the full LL prediction. */
        public final boolean fellBack;

        Result(CharStream input, TS tokenSource, CommonTokenStream tokens, P parser, T parseTree, boolean fellBack) {
            this.input = input;
            this.tokenSource = tokenSource;
            this.tokens = tokens;
            this.parser = parser;
            this.parseTree = parseTree;
            this.fellBack = fellBack;
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
            java.util.function.BiConsumer<TS, ANTLRErrorListener> installLexerListener,
            boolean sllFirst
    ) throws IOException {

        CharStream input = CharStreams.fromReader(reader);
        if (input.LA(1) == '\uFEFF') {
            input.consume();
        }

        TS tokenSource = tokenSourceFactory.create(input);
        if (installLexerListener != null) {
            installLexerListener.accept(tokenSource, listener);
        }

        CommonTokenStream tokens = new CommonTokenStream(tokenSource);

        P parser = parserFactory.create(tokens);
        parser.removeErrorListeners();
        T tree;
        boolean fellBack = false;
        if (sllFirst) {
            // The SLL prediction ignores the context of the rule it is in, which makes it much faster, and the parser
            // either returns the tree the full LL prediction returns or reports a syntax error. So it goes first, with
            // no listener and a strategy which gives up at the first error. An input it does not accept (a broken file,
            // or a file which needs the context) is parsed again as it always was: from the first token (reset rewinds
            // the stream, which has read the tokens already, so nothing is lexed twice and no error of the lexer is
            // reported twice), with the full prediction, the listener and the strategy which recovers.
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.setErrorHandler(new BailErrorStrategy());
            try {
                tree = entryRule.parse(parser);
            } catch (ParseCancellationException e) {
                fellBack = true;
                parser.reset();
                parser.setErrorHandler(new DefaultErrorStrategy());
                parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                parser.addErrorListener(listener);
                tree = entryRule.parse(parser);
            }
        } else {
            parser.addErrorListener(listener);
            tree = entryRule.parse(parser);
        }
        return new Result<>(input, tokenSource, tokens, parser, tree, fellBack);
    }

    private AntlrTokenPipeline() {}
}
