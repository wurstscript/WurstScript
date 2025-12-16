package de.peeeq.wurstscript;

import de.peeeq.wurstscript.antlr.JassParser;
import de.peeeq.wurstscript.antlr.WurstParser.CompilationUnitContext;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.CompilationUnitInfo;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jass.AntlrJassParseTreeTransformer;
import de.peeeq.wurstscript.jass.ExtendedJassLexer;
import de.peeeq.wurstscript.jurst.AntlrJurstParseTreeTransformer;
import de.peeeq.wurstscript.jurst.ExtendedJurstLexer;
import de.peeeq.wurstscript.jurst.antlr.JurstParser;
import de.peeeq.wurstscript.parser.AntlrTokenPipeline;
import de.peeeq.wurstscript.parser.WurstAntlrErrorListener;
import de.peeeq.wurstscript.parser.antlr.AntlrWurstParseTreeTransformer;
import de.peeeq.wurstscript.parser.antlr.ExtendedWurstLexer;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

public class WurstParser {
    private static final int MAX_SYNTAX_ERRORS = 15;
    private final ErrorHandler errorHandler;
    private final WurstGui gui;
    private boolean removeSugar = true;

    public WurstParser(ErrorHandler errorHandler, WurstGui gui) {
        this.errorHandler = errorHandler;
        this.gui = gui;
    }

    public void setRemoveSugar(boolean removeSugar) {
        this.removeSugar = removeSugar;
    }

    public CompilationUnit parse(Reader reader, String source, boolean hasCommonJ) {
        return parseWithAntlr(reader, source, hasCommonJ);
    }

    private CompilationUnit parseWithAntlr(Reader reader, final String source, boolean hasCommonJ) {
        try {
            final ExtendedWurstLexer[] lexerRef = new ExtendedWurstLexer[1];
            final CharStream[] inputRef = new CharStream[1];

            WurstAntlrErrorListener.MessageRewriter rewriter = in -> {
                if (in.exception instanceof NoViableAltException) {
                    NoViableAltException ne = (NoViableAltException) in.exception;
                    if (ne.getStartToken().getType() == de.peeeq.wurstscript.antlr.WurstLexer.HOTDOC_COMMENT) {
                        return new WurstAntlrErrorListener.RewriteResult(
                            ne.getStartToken(),
                            "Hotdoc comment is in invalid position, it can " +
                                "only appear before function definitions, classes, and " +
                                "other elements that can be documented."
                        );
                    }
                }
                return WurstAntlrErrorListener.RewriteResult.unchanged(in.offendingSymbol, in.msg);
            };

            WurstAntlrErrorListener listener = new WurstAntlrErrorListener(
                MAX_SYNTAX_ERRORS,
                source,
                () -> lexerRef[0].getLineOffsets(),
                () -> inputRef[0],
                gui,
                rewriter
            );

            AntlrTokenPipeline.Result<ExtendedWurstLexer, de.peeeq.wurstscript.antlr.WurstParser, CompilationUnitContext> res =
                AntlrTokenPipeline.run(
                    reader,
                    in -> {
                        inputRef[0] = in;
                        ExtendedWurstLexer lx = new ExtendedWurstLexer(in);
                        lexerRef[0] = lx;
                        return lx;
                    },
                    de.peeeq.wurstscript.antlr.WurstParser::new,
                    de.peeeq.wurstscript.antlr.WurstParser::compilationUnit,
                    listener,
                    (lx, l) -> lx.setErrorListener(l) // <-- keep your existing API
                );

            if (lexerRef[0].getTabWarning() != null) {
                CompileError warning = lexerRef[0].getTabWarning();
                warning = new CompileError(warning.getSource().withFile(source), warning.getMessage(), CompileError.ErrorType.WARNING);
                gui.sendError(warning);
            }

            CompilationUnit root = new AntlrWurstParseTreeTransformer(
                source,
                errorHandler,
                lexerRef[0].getLineOffsets(),
                lexerRef[0].getCommentTokens(),
                true
            ).transform(res.parseTree);

            if (this.removeSugar) {
                removeSyntacticSugar(root, hasCommonJ);
            }
            root.getCuInfo().setIndentationMode(lexerRef[0].getIndentationMode());
            return root;

        } catch (IOException e) {
            WLogger.severe(e);
            throw new Error(e);
        } catch (TooManyErrorsException e) {
            WLogger.info("Stopped parsing file " + source + ", too many errors");
            return emptyCompilationUnit();
        }
    }


    public CompilationUnit parseJurst(Reader reader, String source, boolean hasCommonJ) {
        return parseJurstWithAntlr(reader, source, hasCommonJ);
    }

    private CompilationUnit parseJurstWithAntlr(Reader reader, final String source, boolean hasCommonJ) {
        try {
            final ExtendedJurstLexer[] lexerRef = new ExtendedJurstLexer[1];
            final CharStream[] inputRef = new CharStream[1];

            WurstAntlrErrorListener.MessageRewriter rewriter =
                in -> new WurstAntlrErrorListener.RewriteResult(in.offendingSymbol, "line " + in.line + ": " + in.msg);

            WurstAntlrErrorListener listener = new WurstAntlrErrorListener(
                MAX_SYNTAX_ERRORS,
                source,
                () -> lexerRef[0].getLineOffsets(),
                () -> inputRef[0],
                gui,
                rewriter
            );

            AntlrTokenPipeline.Result<ExtendedJurstLexer, JurstParser, de.peeeq.wurstscript.jurst.antlr.JurstParser.CompilationUnitContext> res =
                AntlrTokenPipeline.run(
                    reader,
                    in -> {
                        inputRef[0] = in;
                        ExtendedJurstLexer lx = new ExtendedJurstLexer(in);
                        lexerRef[0] = lx;
                        return lx;
                    },
                    JurstParser::new,
                    JurstParser::compilationUnit,
                    listener,
                    (lx, l) -> lx.addErrorListener(l)
                );

            CompilationUnit root = new AntlrJurstParseTreeTransformer(
                source,
                errorHandler,
                lexerRef[0].getLineOffsets()
            ).transform(res.parseTree);

            if (this.removeSugar) {
                removeSyntacticSugar(root, hasCommonJ);
            }
            return root;

        } catch (IOException e) {
            WLogger.severe(e);
            throw new Error(e);
        } catch (TooManyErrorsException e) {
            WLogger.info("Stopped parsing file " + source + ", too many errors");
            return emptyCompilationUnit();
        }
    }

    public CompilationUnit parseJass(Reader reader, String source, boolean hasCommonJ) {
        return parseJassAntlr(reader, source, hasCommonJ);
    }

    private CompilationUnit parseJassAntlr(Reader reader, final String source, boolean hasCommonJ) {
        try {
            final ExtendedJassLexer[] lexerRef = new ExtendedJassLexer[1];
            final CharStream[] inputRef = new CharStream[1];

            WurstAntlrErrorListener.MessageRewriter rewriter =
                in -> new WurstAntlrErrorListener.RewriteResult(in.offendingSymbol, "line " + in.line + ": " + in.msg);

            WurstAntlrErrorListener listener = new WurstAntlrErrorListener(
                MAX_SYNTAX_ERRORS,
                source,
                () -> lexerRef[0].getLineOffsets(),
                () -> inputRef[0],
                gui,
                rewriter
            );

            AntlrTokenPipeline.Result<ExtendedJassLexer, JassParser, JassParser.CompilationUnitContext> res =
                AntlrTokenPipeline.run(
                    reader,
                    in -> {
                        inputRef[0] = in;
                        ExtendedJassLexer lx = new ExtendedJassLexer(in);
                        lexerRef[0] = lx;
                        return lx;
                    },
                    JassParser::new,
                    JassParser::compilationUnit,
                    listener,
                    (lx, l) -> lx.addErrorListener(l)
                );

            CompilationUnit root = new AntlrJassParseTreeTransformer(
                source,
                errorHandler,
                lexerRef[0].getLineOffsets()
            ).transform(res.parseTree);

            removeSyntacticSugar(root, hasCommonJ);
            return root;

        } catch (IOException e) {
            WLogger.severe(e);
            throw new Error(e);
        } catch (TooManyErrorsException e) {
            WLogger.info("Stopped parsing file " + source + ", too many errors");
            return emptyCompilationUnit();
        }
    }


    public CompilationUnit emptyCompilationUnit() {
        return Ast.CompilationUnit(new CompilationUnitInfo(errorHandler), Ast.JassToplevelDeclarations(), Ast.WPackages());
    }

    private void removeSyntacticSugar(CompilationUnit root, boolean hasCommonJ) {
        new SyntacticSugar().removeSyntacticSugar(root, hasCommonJ);
    }

    public static class TooManyErrorsException extends RuntimeException {
    }
}
