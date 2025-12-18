package de.peeeq.wurstscript.parser;

import de.peeeq.wurstscript.WurstParser;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WurstAntlrErrorListener extends BaseErrorListener {

    @FunctionalInterface
    public interface MessageRewriter {
        RewriteResult rewrite(RewriteInput in);
    }

    public static final class RewriteInput {
        public final Object offendingSymbol;
        public final int line;
        public final int charPositionInLine;
        public final String msg;
        public final RecognitionException exception;

        public RewriteInput(Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException exception) {
            this.offendingSymbol = offendingSymbol;
            this.line = line;
            this.charPositionInLine = charPositionInLine;
            this.msg = msg;
            this.exception = exception;
        }
    }

    public static final class RewriteResult {
        public final Object offendingSymbol;
        public final String msg;

        public RewriteResult(Object offendingSymbol, String msg) {
            this.offendingSymbol = offendingSymbol;
            this.msg = msg;
        }

        public static RewriteResult unchanged(Object offendingSymbol, String msg) {
            return new RewriteResult(offendingSymbol, msg);
        }
    }

    private static final Pattern WS = Pattern.compile("\\s*");

    private final int maxErrors;
    private final String sourceFile;
    private final Supplier<LineOffsets> lineOffsets;
    private final Supplier<CharStream> input;
    private final WurstGui gui;
    private final MessageRewriter rewriter;

    private int errorCount = 0;

    public WurstAntlrErrorListener(
        int maxErrors,
        String sourceFile,
        Supplier<LineOffsets> lineOffsets,
        Supplier<CharStream> input,
        WurstGui gui,
        MessageRewriter rewriter
    ) {
        this.maxErrors = maxErrors;
        this.sourceFile = sourceFile;
        this.lineOffsets = lineOffsets;
        this.input = input;
        this.gui = gui;
        this.rewriter = rewriter;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {

        if (rewriter != null) {
            RewriteResult rr = rewriter.rewrite(new RewriteInput(offendingSymbol, line, charPositionInLine, msg, e));
            offendingSymbol = rr.offendingSymbol;
            msg = rr.msg;
        }

        LineOffsets offsets = lineOffsets.get();
        int pos;
        int posStop;

        if (offendingSymbol instanceof Token token) {
            pos = token.getStartIndex();
            posStop = token.getStopIndex() + 1;
        } else {
            pos = offsets.get(line) + charPositionInLine;
            posStop = pos + 1;
        }

        CharStream cs = input.get();
        if (posStop >= cs.size()) {
            posStop = Math.max(0, cs.size() - 1);
        }

        Matcher matcher = WS.matcher(cs.getText(new Interval(pos, posStop)));
        while (pos > 0 && matcher.matches()) {
            pos--;
        }

        gui.sendError(new CompileError(new WPos(sourceFile, offsets, pos, posStop), msg));

        if (++errorCount > maxErrors) {
            throw new WurstParser.TooManyErrorsException();
        }
    }
}
