package de.peeeq.wurstscript.jass;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.antlr.JassParser;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompilationUnitInfo;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;


public class AntlrJassParseTreeTransformer {
    private String file;
    private ErrorHandler cuErrorHandler;
    private LineOffsets lineOffsets;

    public AntlrJassParseTreeTransformer(String file,
                                         ErrorHandler cuErrorHandler, LineOffsets lineOffsets) {
        this.file = file;
        this.cuErrorHandler = cuErrorHandler;
        this.lineOffsets = lineOffsets;
    }

    public CompilationUnit transform(JassParser.CompilationUnitContext cu) {
        JassToplevelDeclarations jassDecls = Ast.JassToplevelDeclarations();
        try {
            for (JassParser.JassTopLevelDeclarationContext decl : cu.decls) {
                jassDecls.add(transformJassToplevelDecl(decl));
            }

        } catch (CompileError e) {
            cuErrorHandler.sendError(e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            // ignore
        }

        return Ast.CompilationUnit(new CompilationUnitInfo(this.cuErrorHandler), jassDecls, Ast.WPackages());
    }

    private JassToplevelDeclaration transformJassToplevelDecl(
            JassParser.JassTopLevelDeclarationContext d) {
        if (d.jassFuncDef() != null) {
            return transformJassFuncDef(d.jassFuncDef());
        } else if (d.jassGlobalsBlock() != null) {
            return transformJassGlobalsBlock(d.jassGlobalsBlock());
        } else if (d.jassNativeDecl() != null) {
            return transformJassNativeDecl(d.jassNativeDecl());
        } else if (d.jassTypeDecl() != null) {
            return transformJassTypeDecl(d.jassTypeDecl());
        }
        throw error(d, "unhandled case: " + text(d));
    }

    private String text(@Nullable Token t) {
        if (t == null) {
            return "";
        }
        return t.getText();
    }

    private Identifier text(@Nullable ParserRuleContext c) {
        if (c == null) {
            return Ast.Identifier(new WPos(file, lineOffsets, 1, 0), "");
        }
        return Ast.Identifier(source(c), c.getText());
    }

    private JassToplevelDeclaration transformJassTypeDecl(JassParser.JassTypeDeclContext t) {
        Modifiers modifiers = Ast.Modifiers();
        Identifier name = Ast.Identifier(source(t.name), t.name.getText());
        OptTypeExpr optTyp = transformOptionalType(t.jassTypeExpr());
        return Ast.NativeType(source(t), modifiers, name, optTyp);
    }

    private JassToplevelDeclaration transformJassNativeDecl(
            JassParser.JassNativeDeclContext n) {
        Modifiers modifiers = Ast.Modifiers();
        FuncSig sig = transformFuncSig(n.jassFuncSignature());
        return Ast.NativeFunc(source(n), modifiers, sig.name,
                sig.formalParameters, sig.returnType);
    }

    private JassToplevelDeclaration transformJassGlobalsBlock(
            JassParser.JassGlobalsBlockContext g) {
        JassGlobalBlock result = Ast.JassGlobalBlock();
        for (JassParser.JassGlobalDeclContext v : g.jassGlobalDecl()) {
            Modifiers modifiers = Ast.Modifiers();
            if (v.constant != null) {
                modifiers.add(Ast.ModConstant(source(v.constant)));
            }
            OptTypeExpr optTyp = transformOptionalType(v.jassTypeExpr());
            Identifier name = Ast.Identifier(source(v.name), v.name.getText());
            OptExpr initialExpr = transformOptionalExpr(v.initial);
            result.add(Ast.GlobalVarDef(source(v), modifiers, optTyp, name,
                    initialExpr));
        }
        return result;
    }

    private JassToplevelDeclaration transformJassFuncDef(JassParser.JassFuncDefContext f) {
        Modifiers modifiers = Ast.Modifiers();
        FuncSig sig = transformFuncSig(f.jassFuncSignature());
        WStatements body = transformJassLocals(f.jassLocals);
        body.addAll(transformJassStatements(f.jassStatements()).removeAll());
        return Ast.FuncDef(source(f), modifiers, sig.name, sig.typeParams,
                sig.formalParameters, sig.returnType, body);
    }

    private WStatements transformJassLocals(List<JassParser.JassLocalContext> jassLocals) {
        WStatements result = Ast.WStatements();
        for (JassParser.JassLocalContext l : jassLocals) {
            Modifiers modifiers = Ast.Modifiers();
            OptTypeExpr optTyp = transformOptionalType(l.jassTypeExpr());
            Identifier name = Ast.Identifier(source(l.name), l.name.getText());
            OptExpr initialExpr = transformOptionalExpr(l.initial);
            result.add(Ast.LocalVarDef(source(l), modifiers, optTyp, name,
                    initialExpr));
        }
        return result;
    }

    private WStatements transformJassStatements(JassParser.JassStatementsContext stmts) {
        WStatements result = Ast.WStatements();
        for (JassParser.JassStatementContext s : stmts.jassStatement()) {
            result.add(transformJassStatement(s));
        }
        return result;
    }

    private WStatement transformJassStatement(JassParser.JassStatementContext s) {
        if (s.jassStatementCall() != null) {
            return transformJassStatementCall(s.jassStatementCall());
        } else if (s.jassStatementExithwhen() != null) {
            return transformJassStatementExitwhen(s.jassStatementExithwhen());
        } else if (s.jassStatementIf() != null) {
            return transformJassStatementIf(s.jassStatementIf());
        } else if (s.jassStatementLoop() != null) {
            return transformJassStatementLoop(s.jassStatementLoop());
        } else if (s.jassStatementReturn() != null) {
            return transformJassStatementReturn(s.jassStatementReturn());
        } else if (s.jassStatementSet() != null) {
            return transformJassStatementSet(s.jassStatementSet());
        }
        throw error(s, "unhandled case: " + text(s));
    }

    private WStatement transformJassStatementSet(JassParser.JassStatementSetContext s) {
        return Ast.StmtSet(source(s), transformExprVarAccess(s.left), transformExpr(s.right));
    }

    private WStatement transformJassStatementReturn(JassParser.JassStatementReturnContext s) {
        return Ast.StmtReturn(source(s), transformOptionalExpr(s.jassExpr()));
    }

    private WStatement transformJassStatementLoop(JassParser.JassStatementLoopContext s) {
        return Ast.StmtLoop(source(s), transformJassStatements(s.jassStatements()));
    }

    private WStatement transformJassStatementIf(JassParser.JassStatementIfContext s) {
        WStatements thenBlock = transformJassStatements(s.thenStatements);
        WStatements elseBlock = transformJassElseIfs(s.jassElseIfs());
        return Ast.StmtIf(source(s), transformExpr(s.cond), thenBlock, elseBlock);
    }

    private WStatements transformJassElseIfs(JassParser.JassElseIfsContext s) {
        if (s == null) {
            return Ast.WStatements();
        }
        if (s.cond != null) {
            return Ast.WStatements(Ast.StmtIf(source(s),
                    transformExpr(s.cond),
                    transformJassStatements(s.thenStatements),
                    transformJassElseIfs(s.jassElseIfs())));
        } else if (s.elseStmts != null) {
            return transformJassStatements(s.elseStmts);
        } else {
            return Ast.WStatements();
        }
    }

    private WStatement transformJassStatementExitwhen(JassParser.JassStatementExithwhenContext s) {
        return Ast.StmtExitwhen(source(s), transformExpr(s.cond));
    }

    private WStatement transformJassStatementCall(JassParser.JassStatementCallContext s) {
        return transformFunctionCall(s.exprFunctionCall());
    }

    private Indexes transformIndexes(JassParser.IndexesContext i) {
        Indexes result = Ast.Indexes();
        result.add(transformExpr(i.jassExpr()));
        return result;
    }

    private NameRef transformExprVarAccess(JassParser.ExprVarAccessContext e) {
        if (e.indexes() == null || e.indexes().isEmpty()) {
            return Ast.ExprVarAccess(source(e), Ast.Identifier(source(e.varname), e.varname.getText()));
        } else {
            return Ast.ExprVarArrayAccess(source(e), Ast.Identifier(source(e.varname), e.varname.getText()),
                    transformIndexes(e.indexes()));
        }
    }

    private ExprFunctionCall transformFunctionCall(JassParser.ExprFunctionCallContext c) {
        return Ast.ExprFunctionCall(source(c), Ast.Identifier(source(c.funcName), c.funcName.getText()),
                Ast.TypeExprList(), transformExprs(c.argumentList().exprList()));
    }

    private OptExpr transformOptionalExpr(JassParser.JassExprContext e) {
        if (e == null) {
            return Ast.NoExpr();
        }
        Expr r = transformExpr(e);
        if (r instanceof ExprEmpty) {
            return Ast.NoExpr();
        }
        return r;
    }

    private Arguments transformExprs(JassParser.ExprListContext es) {
        Arguments result = Ast.Arguments();
        if (es != null) {
            for (JassParser.JassExprContext e : es.exprs) {
                result.add(transformExpr(e));
            }
        }
        if (result.size() == 1 && result.get(0) instanceof ExprEmpty) {
            result.clear();
        }
        return result;
    }

    private Expr transformExpr(JassParser.JassExprContext e) {
        WPos source = source(e);
        if (e.jassExprPrimary() != null) {
            return transformExprPrimary(e.jassExprPrimary());
        } else if (e.left != null && e.right != null && e.op != null) {
            return Ast.ExprBinary(source, transformExpr(e.left),
                    transformOp(e.op), transformExpr(e.right));
        } else if (e.op != null && e.op.getType() == JassParser.NOT) {
            return Ast.ExprUnary(source, WurstOperator.NOT,
                    transformExpr(e.right));
        } else if (e.op != null && e.op.getType() == JassParser.MINUS) {
            return Ast.ExprUnary(source, WurstOperator.UNARY_MINUS,
                    transformExpr(e.right));
        }

        ParseTree left = getLeftParseTree(e);
        if (left != null) {
            source = source.withLeftPos(1 + stopPos(left));
        }
        ParseTree right = getRightParseTree(e);
        if (right != null) {
            source = source.withRightPos(beginPos(right));
        }
        return Ast.ExprEmpty(source);
    }


    private int beginPos(ParseTree left) {
        if (left instanceof ParserRuleContext) {
            ParserRuleContext left2 = (ParserRuleContext) left;
            return left2.getStart().getStartIndex();
        } else if (left instanceof TerminalNode) {
            TerminalNode left2 = (TerminalNode) left;
            return left2.getSymbol().getStartIndex();
        }
        throw new Error("unhandled case: " + left.getClass() + "  // " + left);
    }

    private int stopPos(ParseTree left) {
        if (left instanceof ParserRuleContext) {
            ParserRuleContext left2 = (ParserRuleContext) left;
            return left2.getStop().getStopIndex();
        } else if (left instanceof TerminalNode) {
            TerminalNode left2 = (TerminalNode) left;
            return left2.getSymbol().getStopIndex();
        }
        throw new Error("unhandled case: " + left.getClass() + "  // " + left);
    }

    private @Nullable ParseTree getLeftParseTree(@Nullable ParserRuleContext e) {
        if (e == null || e.getParent() == null) {
            return null;
        }
        ParserRuleContext parent = e.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) == e) {
                if (i > 0) {
                    return parent.getChild(i - 1);
                } else {
                    return getLeftParseTree(parent);
                }
            }
        }
        return null;
    }

    private @Nullable ParseTree getRightParseTree(@Nullable ParserRuleContext e) {
        if (e == null || e.getParent() == null) {
            return null;
        }
        ParserRuleContext parent = e.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) == e) {
                if (i < parent.getChildCount() - 1) {
                    return parent.getChild(i + 1);
                } else {
                    return getRightParseTree(parent);
                }
            }
        }
        return null;
    }

    private WurstOperator transformOp(Token op) {
        switch (op.getType()) {
            case JassParser.OR:
                return WurstOperator.OR;
            case JassParser.AND:
                return WurstOperator.AND;
            case JassParser.EQEQ:
                return WurstOperator.EQ;
            case JassParser.NOT_EQ:
                return WurstOperator.NOTEQ;
            case JassParser.LESS_EQ:
                return WurstOperator.LESS_EQ;
            case JassParser.LESS:
                return WurstOperator.LESS;
            case JassParser.GREATER_EQ:
                return WurstOperator.GREATER_EQ;
            case JassParser.GREATER:
                return WurstOperator.GREATER;
            case JassParser.PLUS:
                return WurstOperator.PLUS;
            case JassParser.MINUS:
                return WurstOperator.MINUS;
            case JassParser.MULT:
                return WurstOperator.MULT;
            case JassParser.DIV_REAL:
                return WurstOperator.DIV_REAL;
            case JassParser.MOD_REAL:
                return WurstOperator.MOD_REAL;
            case JassParser.NOT:
                return WurstOperator.NOT;
        }
        throw error(source(op), "operator not implemented: " + text(op));
    }

    private Expr transformExprPrimary(JassParser.JassExprPrimaryContext e) {
        if (e.atom != null) {
            return transformAtom(e.atom);
        } else if (e.varname != null) {
            if (e.indexes() != null && !e.indexes().isEmpty()) {
                Indexes index = transformIndexes(e.indexes());
                return Ast.ExprVarArrayAccess(source(e), Ast.Identifier(source(e.varname), e.varname.getText()),
                        index);
            } else {
                return Ast.ExprVarAccess(source(e), Ast.Identifier(source(e.varname), e.varname.getText()));
            }
        } else if (e.jassExpr() != null) {
            return transformExpr(e.jassExpr());
        } else if (e.exprFunctionCall() != null) {
            return transformFunctionCall(e.exprFunctionCall());
        } else if (e.exprFuncRef() != null) {
            return transformExprFuncRef(e.exprFuncRef());
        }
        // TODO Auto-generated method stub
        throw error(e, "primary expr not implemented " + text(e));
    }

    private ExprFuncRef transformExprFuncRef(JassParser.ExprFuncRefContext e) {
        String scopeName = e.scopeName == null ? "" : e.scopeName.getText();
        Identifier funcName = Ast.Identifier(source(e.funcName), e.funcName.getText());
        return Ast.ExprFuncRef(source(e), scopeName, funcName);
    }

    private Expr transformAtom(Token a) {
        WPos source = source(a);
        switch (a.getType()) {
            case JassParser.INT:
                return Ast.ExprIntVal(source, text(a));
            case JassParser.REAL:
                return Ast.ExprRealVal(source, text(a));
            case JassParser.STRING:
                return Ast.ExprStringVal(source, getStringVal(source, text(a)));
            case JassParser.NULL:
                return Ast.ExprNull(source);
            case JassParser.TRUE:
                return Ast.ExprBoolVal(source, true);
            case JassParser.FALSE:
                return Ast.ExprBoolVal(source, false);
        }
        throw error(source(a), "atom not implemented: " + text(a));
    }

    private String getStringVal(WPos source, String text) {
        StringBuilder res = new StringBuilder();
        buildStringVal(source, text, res);
        return res.toString();
    }

    public static void buildStringVal(WPos source, String text, StringBuilder res) {
        for (int i = 1; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                i++;
                switch (text.charAt(i)) {
                    case '\\':
                        res.append('\\');
                        break;
                    case 'n':
                        res.append('\n');
                        break;
                    case 'r':
                        res.append('\r');
                        break;
                    case 't':
                        res.append('\t');
                        break;
                    case '"':
                        res.append('"');
                        break;
                    case '\'':
                        res.append('\'');
                        break;
                    default:
                        throw new CompileError(source, "Invalid escape sequence: "
                                + text.charAt(i));
                }
            } else {
                res.append(c);
            }
        }
    }

    private FuncSig transformFuncSig(JassParser.JassFuncSignatureContext s) {
        TypeParamDefs typeParams = Ast.TypeParamDefs();
        WParameters formalParameters = Ast.WParameters();
        for (JassParser.FormalParamContext p : s.args) {
            formalParameters.add(transformFormalParameter(p, false));
        }
        OptTypeExpr returnType = transformOptionalType(s.returnType);
        return new FuncSig(Ast.Identifier(source(s.name), s.name.getText()), typeParams, formalParameters, returnType);
    }

    private OptTypeExpr transformOptionalType(JassParser.JassTypeExprContext t) {
        if (t == null) {
            return Ast.NoTypeExpr();
        }
        return transformTypeExpr(t);
    }

    private TypeExpr transformTypeExpr(JassParser.JassTypeExprContext t) throws Error {
        OptTypeExpr scopeType;
        if (t.jassTypeExpr() != null) {
            scopeType = transformTypeExpr(t.jassTypeExpr());
        } else {
            scopeType = Ast.NoTypeExpr();
        }
        if (t.jassTypeExpr() != null) {
            JassParser.JassExprContext arrSize = null;
            return Ast.TypeExprArray(source(t), (TypeExpr) scopeType, transformOptionalExpr(arrSize));
        } else {
            return Ast.TypeExprSimple(source(t), scopeType, t.typeName.getText(), Ast.TypeExprList());
        }
    }


    private CompileError error(WPos source, String msg) {
        return new CompileError(source, msg);
    }

    private CompileError error(ParserRuleContext source, String msg) {
        return new CompileError(source(source), msg);
    }

    private WParameter transformFormalParameter(JassParser.FormalParamContext p,
                                                boolean makeConstant) {
        Modifiers modifiers = Ast.Modifiers();
        if (makeConstant) {
            modifiers.add(Ast.ModConstant(source(p).artificial()));
        }
        return Ast.WParameter(source(p), modifiers,
                transformTypeExpr(p.jassTypeExpr()), Ast.Identifier(source(p.name), p.name.getText()));
    }

    private WPos source(ParserRuleContext p) {
        return new WPos(file, lineOffsets, p.start.getStartIndex(),
                p.stop.getStopIndex() + 1);
    }

    private WPos source(Token p) {
        return new WPos(file, lineOffsets, p.getStartIndex(), p.getStopIndex() + 1);
    }

    class FuncSig {
        Identifier name;
        TypeParamDefs typeParams;
        WParameters formalParameters;
        OptTypeExpr returnType;

        public FuncSig(Identifier name, TypeParamDefs typeParams,
                       WParameters formalParameters, OptTypeExpr returnType) {
            this.name = name;
            this.typeParams = typeParams;
            this.formalParameters = formalParameters;
            this.returnType = returnType;
        }

    }

}
