package de.peeeq.wurstscript.attributes.prettyPrint;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.antlr.CommentToken;
import org.apache.commons.lang.StringUtils;

import static de.peeeq.wurstscript.utils.Utils.escapeString;

public class PrettyPrinter {

    private static void commaSeparatedList(Element e, Printer printer) {
        for (int i = 0; i < e.size(); i++) {
            if (i > 0) {
                printer.appendStr(",");
                printer.addSpace();
            }
            e.get(i).prettyPrint(printer);
        }
    }

    private static void printClassOrModuleDeclaration(ClassOrModule e, Printer printer) {
        e.getModuleUses().prettyPrint(printer);
        e.getVars().prettyPrint(printer);
        e.getConstructors().prettyPrint(printer);
        e.getMethods().prettyPrint(printer);
        e.getOnDestroy().prettyPrint(printer);
        e.getInnerClasses().prettyPrint(printer);
    }

    private static void printFirstNewline(Element e, Printer printer) {
        // Don't add first new line.
        if (e.getParent().get(0).equals(e)) {
            return;
        }
        if (printer.charAt(printer.length()-1) == '\n' && printer.charAt(printer.length()-2) == '\n') {
            return;
        }
        printer.newline();
    }

    private static void printNewline(Element e, Printer printer) {
        // Don't add a newline unless we are directly under statements.
        // Otherwise we would add newlines inside function calls, etc.
        if (!(e.getParent() instanceof WStatements)) {
            return;
        }
        printer.newline();
    }

    private static void printStuff(Documentable e, Printer printer) {
        printComment(e, printer);
        printComment(e.getModifiers(), printer);
        printer.indent();
        e.getModifiers().prettyPrint(printer);
    }

    // For some reason comments are a kind of modifier.
    private static void printComment(Modifiers e, Printer printer) {
        for (Modifier modifier : e) {
            if (modifier instanceof WurstDoc) {
                modifier.prettyPrint(printer);
            }
        }
    }

    private static void printComment(Documentable d, Printer printer) {
        printer.addComments(d.getSource());
        // Comments doesn't exist in the wurst parser atm.
    }




    public static void prettyPrint(Annotation e, Printer printer) {
        printer.append(e.getAnnotationType(), e);
        if (e.getAnnotationMessage().length() >= 1) {
            printer.appendStr("(");
            printer.appendStr(e.getAnnotationMessage());
            printer.appendStr(")");
        }
    }

    public static void prettyPrint(Arguments e, Printer printer) {
        commaSeparatedList(e, printer);
    }

    public static void prettyPrint(ExprList e, Printer printer) {
        commaSeparatedList(e, printer);
    }

    public static void prettyPrint(ArraySizes e, Printer printer) {
        commaSeparatedList(e, printer);
    }

    public static void prettyPrint(ClassDef e, Printer printer) {
        printFirstNewline(e, printer);
        printStuff(e, printer);
        printer.append("class", e);
        printer.addSpace();
        printer.append(e.getName(), e.getNameId());
        e.getTypeParameters().prettyPrint(printer);
        if (e.getExtendedClass() instanceof TypeExpr) {
            TypeExpr te = (TypeExpr) e.getExtendedClass();
            printer.addSpace();
            printer.appendStr("extends");
            printer.addSpace();
            te.prettyPrint(printer);
        }
        if (!e.getImplementsList().isEmpty()) {
            printer.addSpace();
            printer.appendStr("implements");
            printer.addSpace();
            commaSeparatedList(e.getImplementsList(), printer);
        }
        printer.newline();
        printer.startIndent();
        printClassOrModuleDeclaration(e, printer);
        printer.endIndent();
    }

    public static void prettyPrint(CompilationUnit e, Printer printer) {
        printer.setCommentTokens(e.getCommentTokens());
        prettyPrint(e.getPackages(), printer);
        printer.printRemainingComments();
    }

    public static void prettyPrint(ConstructorDef e, Printer printer) {
        printFirstNewline(e, printer);
        printStuff(e, printer);
        printer.append("construct", e);
        e.getParameters().prettyPrint(printer);
        printer.newline();
        printer.startIndent();
        if (!e.getSuperArgs().isEmpty()) {
            printer.indent();
            printer.appendStr("super(");
            e.getSuperArgs().prettyPrint(printer);
            printer.appendStr(")");
            printer.appendStr("\n");
        }
        e.getBody().prettyPrint(printer);
        printer.endIndent();;
    }

    public static void prettyPrint(ConstructorDefs e, Printer printer) {
        for (ConstructorDef constructorDef : e) {
            // Remove empty constructors.
            if (constructorDef.getBody().size() < 1 && constructorDef.getSuperArgs().size() <= 0) {
                continue;
            }
            constructorDef.prettyPrint(printer);
        }
    }

    public static void prettyPrint(EndFunctionStatement e, Printer printer) {
    }

    public static void prettyPrint(EnumDef e, Printer printer) {
        printFirstNewline(e, printer);
        printStuff(e, printer);
        printer.append("enum", e);
        printer.addSpace();
        printer.append(e.getName(), e.getNameId());
        printer.appendStr("\n");
        printer.startIndent();
        e.getMembers().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(EnumMember e, Printer printer) {
        printStuff(e, printer);
        printer.append(e.getName(), e);
        printer.newline();
    }

    public static void prettyPrint(EnumMembers e, Printer printer) {
        for (EnumMember enumMember : e) {
            enumMember.prettyPrint(printer);
        }
    }

    public static void prettyPrint(ExprBinary e, Printer printer) {
        e.getLeft().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr(e.getOp().toString());
        printer.addSpace();
        e.getRight().prettyPrint(printer);
    }

    public static void prettyPrint(ExprBoolVal e, Printer printer) {
        printer.append(String.valueOf(e.getValB()), e);
    }

    public static void prettyPrint(ExprCast e, Printer printer) {
        e.getExpr().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr("castTo");
        printer.addSpace();
        e.getTyp().prettyPrint(printer);
    }

    public static void prettyPrint(ExprClosure e, Printer printer) {
        e.getParameters().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr("->");
        printer.addSpace();
        e.getImplementation().prettyPrint(printer);
    }

    public static void prettyPrint(ExprDestroy e, Printer printer) {
        printFirstNewline(e, printer);
        printer.indent();
        printer.append("destroy", e);
        printer.addSpace();
        e.getDestroyedObj().prettyPrint(printer);
        printer.newline();
    }

    public static void prettyPrint(ExprEmpty e, Printer printer) {
    }

    public static void prettyPrint(ExprFuncRef e, Printer printer) {
        printer.append("function", e);
        printer.addSpace();
        if (!StringUtils.isBlank(e.getScopeName())) {
            printer.appendStr(e.getScopeName());
            printer.appendStr(".");
        }
        printer.appendStr(e.getFuncName());
    }

    public static void prettyPrint(ExprFunctionCall e, Printer printer) {
        printer.indent();
        printer.append(e.getFuncName(), e);
        printer.appendStr("(");
        e.getArgs().prettyPrint(printer);
        printer.appendStr(")");
        printNewline(e, printer);
    }

    public static void prettyPrint(ExprIncomplete e, Printer printer) {
    }

    public static void prettyPrint(ExprInstanceOf e, Printer printer) {
        e.getExpr().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr("instanceof");
        printer.addSpace();
        e.getTyp().prettyPrint(printer);
    }

    public static void prettyPrint(ExprIntVal e, Printer printer) {
        printer.append(e.getValIraw(), e);
    }

    public static void prettyPrint(ExprMemberArrayVarDot e, Printer printer) {
        e.getLeft().prettyPrint(printer);
        printer.appendStr(".");
        printer.appendStr(e.getVarName());
        printer.appendStr("[");
        e.getIndexes().prettyPrint(printer);
        printer.appendStr("]");
    }

    public static void prettyPrint(ExprMemberArrayVarDotDot e, Printer printer) {
        e.getLeft().prettyPrint(printer);
        printer.appendStr("..");
        printer.appendStr(e.getVarName());
        printer.appendStr("[");
        e.getIndexes().prettyPrint(printer);
        printer.appendStr("]");
    }

    public static void prettyPrint(ExprMemberMethodDot e, Printer printer) {
        printer.indent();
        printer.addComments(e.getSource());
        if (e.getLeft() instanceof ExprBinary) {
            printer.appendStr("(");
            e.getLeft().prettyPrint(printer);
            printer.appendStr(")");
        } else {
            e.getLeft().prettyPrint(printer);
        }
        printer.appendStr(".");
        printer.appendStr(e.getFuncName());
        printer.appendStr("(");
        e.getArgs().prettyPrint(printer);
        printer.appendStr(")");
        printNewline(e, printer);
    }

    public static void prettyPrint(ExprMemberMethodDotDot e, Printer printer) {
        printer.addComments(e.getSource());
        if (e.getLeft() instanceof ExprBinary) {
            printer.appendStr("(");
            e.getLeft().prettyPrint(printer);
            printer.appendStr(")");
        } else {
            e.getLeft().prettyPrint(printer);
        }
        printer.newline();
        printer.startIndent();
        printer.indent();
        printer.appendStr("..");
        printer.appendStr(e.getFuncName());
        printer.appendStr("(");
        e.getArgs().prettyPrint(printer);
        printer.appendStr(")");
        printer.endIndent();
        printNewline(e, printer);
    }

    public static void prettyPrint(ExprMemberVarDot e, Printer printer) {
        e.getLeft().prettyPrint(printer);
        printer.appendStr(".");
        printer.appendStr(e.getVarName());
    }

    public static void prettyPrint(ExprMemberVarDotDot e, Printer printer) {
        printer.indent();
        e.getLeft().prettyPrint(printer);
        printer.appendStr("..");
        printer.appendStr(e.getVarName());
    }

    public static void prettyPrint(ExprNewObject e, Printer printer) {
        printer.indent();
        printer.append("new", e);
        printer.addSpace();
        printer.appendStr(e.getTypeName());
        e.getTypeArgs().prettyPrint(printer);
        if (e.getArgs().size() > 0) {
            printer.appendStr("(");
            e.getArgs().prettyPrint(printer);
            printer.appendStr(")");
        }
        printNewline(e, printer);
    }

    public static void prettyPrint(ExprNull e, Printer printer) {
        printer.appendStr("null");
    }

    public static void prettyPrint(ExprRealVal e, Printer printer) {
        printer.appendStr(e.getValR());
    }

    public static void prettyPrint(ExprStatementsBlock e, Printer printer) {
        printer.append("begin", e);
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
        printer.indent();
        printer.appendStr("end");
    }

    public static void prettyPrint(ExprStringVal e, Printer printer) {
        printer.append(escapeString(e.getValS()), e);
    }

    public static void prettyPrint(ExprSuper e, Printer printer) {
        printer.indent();
        printer.append("super", e);
    }

    public static void prettyPrint(ExprThis e, Printer printer) {
        printer.indent();
        printer.append("this", e);
    }

    public static void prettyPrint(ExprTypeId e, Printer printer) {
        e.getLeft().prettyPrint(printer);
        printer.appendStr(".typeId");
    }

    public static void prettyPrint(ExprUnary e, Printer printer) {
        printer.append(e.getOpU().toString(), e);
        // Don't add space for unary minus, e.g -1.
        if (e.getOpU().toString().equals("not")) {
            printer.addSpace();
        }
        e.getRight().prettyPrint(printer);
    }

    public static void prettyPrint(ExprVarAccess e, Printer printer) {
        printer.indent();
        printer.append(e.getVarName(), e);
    }

    public static void prettyPrint(ExprVarArrayAccess e, Printer printer) {
        printer.indent();
        printer.append(e.getVarName(), e);
        printer.appendStr("[");
        e.getIndexes().prettyPrint(printer);
        printer.appendStr("]");
    }

    public static void prettyPrint(ExtensionFuncDef e, Printer printer) {
        printFirstNewline(e, printer);
        printStuff(e, printer);
        printer.append("function", e);
        printer.addSpace();
        e.getExtendedType().prettyPrint(printer);
        printer.appendStr(".");
        printFunctionSignature(e, printer);
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
    }


    private static void printFunctionSignature(FunctionImplementation e, Printer printer) {
        printer.append(e.getName(), e);
        e.getTypeParameters().prettyPrint(printer);
        e.getParameters().prettyPrint(printer);
        if (!(e.getReturnTyp() instanceof NoTypeExpr)) {
            printer.addSpace();
            printer.appendStr("returns");
            printer.addSpace();
            e.getReturnTyp().prettyPrint(printer);
        }
    }

    public static void prettyPrint(FuncDef e, Printer printer) {
        printFirstNewline(e, printer);
        printStuff(e, printer);
        printer.append("function", e);
        printer.addSpace();
        printFunctionSignature(e, printer);
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(FuncDefs e, Printer printer) {
        printFirstNewline(e, printer);
        for (FuncDef funcDef : e) {
            funcDef.prettyPrint(printer);
        }
    }

    public static void prettyPrint(GlobalVarDef e, Printer printer) {
        printStuff(e, printer);
        if ((e.getOptTyp() instanceof NoTypeExpr)) {
            if (!e.attrIsConstant()) {
                printer.appendStr("var");
            }
        } else {
            e.getOptTyp().prettyPrint(printer);
        }
        printer.addSpace();
        printer.appendStr(e.getName());
        if (!(e.getInitialExpr() instanceof NoExpr)) {
            printer.addSpace();
            printer.appendStr("=");
            printer.addSpace();
            e.getInitialExpr().prettyPrint(printer);
        }
        printer.newline();
    }

    public static void prettyPrint(GlobalVarDefs e, Printer printer) {
        if (e.size() <= 0) {
            return;
        }
        for (GlobalVarDef varDef : e) {
            varDef.prettyPrint(printer);
        }
        printer.newline();
    }

    public static void prettyPrint(IdentifierWithTypeArgs e, Printer printer) {
    }

    public static void prettyPrint(IdentifierWithTypeParamDefs e, Printer printer) {
    }

    public static void prettyPrint(Indexes e, Printer printer) {
        for (Expr expr : e) {
            expr.prettyPrint(printer);
        }
    }

    public static void prettyPrint(InitBlock e, Printer printer) {
        printFirstNewline(e, printer);
        printer.indent();
        printer.append("init", e);
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(InterfaceDef e, Printer printer) {
        printFirstNewline(e, printer);
        printStuff(e, printer);
        printer.append("interface", e);
        printer.addSpace();
        printer.appendStr(e.getName());
        e.getTypeParameters().prettyPrint(printer);
        if (e.getExtendsList().size() >= 1) {
            printer.addSpace();
            printer.appendStr("extends");
            printer.addSpace();
            e.getExtendsList().prettyPrint(printer);
        }
        printer.startIndent();
        e.getModuleUses().prettyPrint(printer);
        e.getVars().prettyPrint(printer);
        e.getConstructors().prettyPrint(printer);
        e.getMethods().prettyPrint(printer);
        e.getOnDestroy().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(JassGlobalBlock e, Printer printer) {
    }

    public static void prettyPrint(JassToplevelDeclarations e, Printer printer) {
    }

    public static void prettyPrint(LocalVarDef e, Printer printer) {
        printComment(e, printer);
        printer.indent();
        if ((e.getOptTyp() instanceof NoTypeExpr)) {
            if (e.attrIsConstant()) {
                printer.appendStr("let");
            } else if (!(e.getParent() instanceof StmtForRange)) {
                printer.appendStr("var");
            }
        } else {
            e.getOptTyp().prettyPrint(printer);
        }
        printer.addSpace();
        printer.appendStr(e.getName());
        if (!(e.getInitialExpr() instanceof NoExpr)) {
            printer.addSpace();
            printer.appendStr("=");
            printer.addSpace();
            e.getInitialExpr().prettyPrint(printer);
        }
        printNewline(e, printer);
    }

    public static void prettyPrint(ModAbstract e, Printer printer) {
        printer.append("abstract", e);
    }

    public static void prettyPrint(ModConstant e, Printer printer) {
        printer.append("constant", e);
    }

    public static void prettyPrint(Modifiers e, Printer printer) {
        for (Modifier modifier : e) {
            // We handle WurstDoc separately.
            if (!(modifier instanceof WurstDoc)) {
                modifier.prettyPrint(printer);
                printer.addSpace();
            }
        }
    }

    public static void prettyPrint(ModOverride e, Printer printer) {
        printer.append("override", e);
    }

    public static void prettyPrint(ModStatic e, Printer printer) {
        printer.append("static", e);
    }

    public static void prettyPrint(ModuleDef e, Printer printer) {
        printFirstNewline(e, printer);
        printStuff(e, printer);
        printer.append("module", e);
        printer.addSpace();
        e.getNameId().prettyPrint(printer);
        printer.newline();
        printer.startIndent();
        printClassOrModuleDeclaration(e, printer);
        printer.endIndent();
    }

    public static void prettyPrint(ModuleInstanciation e, Printer printer) {
    }

    public static void prettyPrint(ModuleInstanciations e, Printer printer) {
    }

    public static void prettyPrint(ModuleUse e, Printer printer) {
        printer.indent();
        printer.append("use", e);
        printer.addSpace();
        printer.appendStr(e.getModuleName());
        printer.newline();
    }

    public static void prettyPrint(ModuleUses e, Printer printer) {
        for (ModuleUse moduleUse : e) {
            moduleUse.prettyPrint(printer);
        }
    }

    public static void prettyPrint(NativeFunc e, Printer printer) {
        printStuff(e, printer);
        printer.append("native", e);
        printer.addSpace();
        e.getNameId().prettyPrint(printer);
        e.getParameters().prettyPrint(printer);
        if (!(e.getReturnTyp() instanceof NoTypeExpr)) {
            printer.addSpace();
            printer.appendStr("returns");
            printer.addSpace();
            e.getReturnTyp().prettyPrint(printer);
        }
        printer.newline();
    }

    public static void prettyPrint(NativeType e, Printer printer) {
        printStuff(e, printer);
        printer.append("nativetype", e);
        printer.addSpace();
        e.getNameId().prettyPrint(printer);
        printer.newline();
    }

    public static void prettyPrint(NoDefaultCase e, Printer printer) {
    }

    public static void prettyPrint(NoExpr e, Printer printer) {
    }

    public static void prettyPrint(NoTypeExpr e, Printer printer) {
    }

    public static void prettyPrint(OnDestroyDef e, Printer printer) {
        if (e.getBody().size() <= 0) {
            return;
        }
        printFirstNewline(e, printer);
        printer.indent();
        printer.append("ondestroy", e);
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(StartFunctionStatement e, Printer printer) {
    }

    public static void prettyPrint(StmtErr e, Printer printer) {
    }

    public static void prettyPrint(StmtExitwhen e, Printer printer) {
        printer.indent();
        printer.append("break", e);
        printer.newline();
    }

    public static void prettyPrint(StmtForFrom e, Printer printer) {
        forIteratorLoop("from", e, e.getIn(), printer);
    }

    public static void prettyPrint(StmtForIn e, Printer printer) {
        forIteratorLoop("in", e, e.getIn(), printer);
    }

    private static void forIteratorLoop(String method, LoopStatementWithVarDef e, Expr target, Printer printer) {
        printer.indent();
        printer.append("for", e);
        printer.addSpace();
        e.getLoopVar().getNameId().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr(method);
        printer.addSpace();
        target.prettyPrint(printer);
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
    }

    private static void forRangeLoop(String direction, StmtForRange e, Printer printer) {
        printer.indent();
        printer.append("for", e);
        printer.addSpace();
        e.getLoopVar().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr(direction);
        printer.addSpace();
        e.getTo().prettyPrint(printer);
        if (e.getStep() instanceof ExprIntVal && ((ExprIntVal) e.getStep()).getValI() != 1) {
            printer.addSpace();
            printer.appendStr("step");
            printer.addSpace();
            e.getStep().prettyPrint(printer);
        }
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(StmtForRangeDown e, Printer printer) {
        forRangeLoop("downto", e, printer);
    }

    public static void prettyPrint(StmtForRangeUp e, Printer printer) {
        forRangeLoop("to", e, printer);
    }

    public static void prettyPrint(StmtIf e, Printer printer) {
        printer.indent();
        printer.append("if", e);
        printer.addSpace();
        e.getCond().prettyPrint(printer);
        printer.newline();
        printer.startIndent();
        e.getThenBlock().prettyPrint(printer);
        printer.endIndent();
        if (e.getElseBlock().size() > 0) {
            printer.indent();
            printer.appendStr("else");
            if (e.getElseBlock().size() > 0 && e.getElseBlock().get(0) instanceof StmtIf) {
                printer.addSpace();
                e.getElseBlock().get(0).prettyPrint(printer);
            } else {
                printer.newline();
                printer.startIndent();
                e.getElseBlock().prettyPrint(printer);
                printer.endIndent();
            }
        }
    }

    public static void prettyPrint(StmtLoop e, Printer printer) {
    }

    public static void prettyPrint(StmtReturn e, Printer printer) {
        printer.indent();
        printer.append("return", e);
        printer.addSpace();
        e.getReturnedObj().prettyPrint(printer);
        printer.newline();
    }

    private static boolean printAssignmentShorthands(Expr left, Expr right, Printer printer) {
        if (!(right instanceof ExprBinary)) {
            return false;
        }

        if (!(left.toString().equals(((ExprBinary) right).getLeft().toString()))) {
            return false;
        }

        String operator = ((ExprBinary) right).getOp().toString();
        Expr val = ((ExprBinary) right).getRight();

        // i++ and i--
        if (val instanceof ExprIntVal
                && ((ExprIntVal) val).getValI() == 1
                && (operator.equals("+") || operator.equals("-"))) {
            printer.appendStr(operator);
            printer.appendStr(operator);
            return true;
        }

        // i +=, ...
        printer.addSpace();
        printer.appendStr(operator);
        printer.appendStr("=");
        printer.addSpace();
        val.prettyPrint(printer);
        return true;
    }

    public static void prettyPrint(StmtSet e, Printer printer) {
        printer.indent();
        e.getUpdatedExpr().prettyPrint(printer);

        // Special cases for assignment shorthands i++ and i += variants.
        boolean shortened = printAssignmentShorthands(e.getUpdatedExpr(), e.getRight(), printer);

        if (!shortened) {
            printer.addSpace();
            printer.appendStr("=");
            printer.addSpace();
            e.getRight().prettyPrint(printer);
        }
        printer.newline();
    }

    public static void prettyPrint(StmtSkip e, Printer printer) {
        printer.indent();
        printer.append("skip", e);
        printer.newline();
    }

    public static void prettyPrint(StmtWhile e, Printer printer) {
        printer.indent();
        printer.append("while", e);
        printer.addSpace();
        e.getCond().prettyPrint(printer);
        printer.newline();
        printer.startIndent();
        e.getBody().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(SwitchCase e, Printer printer) {
        printer.indent();
        printer.append("case", e);
        printer.addSpace();
        e.getExpr().prettyPrint(printer);
        printer.newline();
        printer.startIndent();
        e.getStmts().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(SwitchCases sw, Printer printer) {
        for (SwitchCase c : sw) {
            c.prettyPrint(printer);
        }
    }

    public static void prettyPrint(SwitchDefaultCaseStatements e, Printer printer) {
        printer.indent();
        printer.append("default", e);
        printer.newline();
        printer.startIndent();
        e.getStmts().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(SwitchStmt sw, Printer printer) {
        printer.indent();
        printer.append("switch", sw);
        printer.addSpace();
        sw.getExpr().prettyPrint(printer);
        printer.newline();
        printer.startIndent();
        sw.getCases().prettyPrint(printer);
        sw.getSwitchDefault().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(TopLevelDeclarations e, Printer printer) {
    }

    public static void prettyPrint(TupleDef e, Printer printer) {
        printStuff(e, printer);
        printer.addSpace();
        printer.append("tuple", e);
        printer.addSpace();
        e.getNameId().prettyPrint(printer);
        e.getParameters().prettyPrint(printer);
        printer.newline();
    }

    public static void prettyPrint(TypeExprArray e, Printer printer) {
        e.getBase().prettyPrint(printer);
        printer.addSpace();
        printer.append("array", e);
    }

    public static void prettyPrint(TypeExprList typeExprList, Printer printer) {
        if (typeExprList.size() == 0) {
            return;
        }
        printer.appendStr("<");
        commaSeparatedList(typeExprList, printer);
        printer.appendStr(">");
    }

    public static void prettyPrint(TypeExprResolved e, Printer printer) {
    }

    public static void prettyPrint(TypeExprSimple e, Printer printer) {
        printer.append(e.getTypeName(), e);
        e.getTypeArgs().prettyPrint(printer);
    }

    public static void prettyPrint(TypeExprThis e, Printer printer) {
        printer.addComments(e.getSource());
        if (!(e.getScopeType() instanceof NoTypeExpr)) {
            e.getScopeType().prettyPrint(printer);
            printer.appendStr(".");
        }
        printer.appendStr("thistype");
    }

    public static void prettyPrint(TypeParamDef e, Printer printer) {
        printComment(e, printer);
        e.getNameId().prettyPrint(printer);
    }

    public static void prettyPrint(TypeParamDefs e, Printer printer) {
        if (e.size() >= 1) {
            printer.appendStr("<");
            commaSeparatedList(e, printer);
            printer.appendStr(">");
        }
    }

    public static void prettyPrint(VisibilityDefault e, Printer printer) {
    }

    public static void prettyPrint(VisibilityPrivate e, Printer printer) {
        printer.append("private", e);
    }

    public static void prettyPrint(VisibilityProtected e, Printer printer) {
        printer.append("protected", e);
    }

    public static void prettyPrint(VisibilityPublic e, Printer printer) {
        printer.append("public", e);
    }

    public static void prettyPrint(VisibilityPublicread e, Printer printer) {
        printer.append("publicread", e);
    }

    public static void prettyPrint(WBlock wBlock, Printer printer) {
        // TODO Auto-generated method stub
        throw new Error("not implemented");
    }

    public static void prettyPrint(WEntities wEntities, Printer printer) {
        for (WEntity entity : wEntities) {
            entity.prettyPrint(printer);
        }
    }

    public static void prettyPrint(WImport wImport, Printer printer) {
        printer.append("import", wImport);
        printer.addSpace();
        printer.appendStr(wImport.getIsInitLater() ? "initlater" : "");
        printer.addSpace();
        printer.appendStr(wImport.getIsPublic() ? "public" : "");
        printer.addSpace();
        printer.appendStr(wImport.getPackagename());
        printer.newline();
    }

    public static void prettyPrint(WurstModel wurstModel, Printer printer) {
        for (CompilationUnit compilationUnit : wurstModel) {
            compilationUnit.prettyPrint(printer);
        }
    }

    private static String spaces(int indentation) {
        return " "+"{"+indentation+"}";
    }

    public static void prettyPrint(WurstDoc wurstDoc, Printer printer) {
        printer.indent();
        printer.append(wurstDoc.getRawComment(), wurstDoc);
        printer.newline();
    }

    public static void prettyPrint(WStatements wStatements, Printer printer) {
        for (WStatement wStatement : wStatements) {
            wStatement.prettyPrint(printer);
        }
    }

    public static void prettyPrint(WParameters wParameters, Printer printer) {
        printer.appendStr("(");
        String prefix = "";
        for (WParameter wParameter : wParameters) {
            printer.appendStr(prefix);
            prefix = ",";
            printer.addSpace();
            wParameter.prettyPrint(printer);
        }
        printer.appendStr(")");
    }

    public static void prettyPrint(WParameter wParameter, Printer printer) {
        wParameter.getTyp().prettyPrint(printer);
        printer.addSpace();
        printer.append(wParameter.getName(), wParameter.getNameId());
    }

    public static void prettyPrint(WPackages wPackages, Printer printer) {
        for (WPackage wPackage : wPackages) {
            wPackage.prettyPrint(printer);
        }
    }

    public static void prettyPrint(WPackage wPackage, Printer printer) {
        printComment(wPackage, printer);
        printer.append("package", wPackage);
        printer.addSpace();
        printer.append(wPackage.getName(), wPackage.getNameId());
        printer.newline();
        printer.newline();
        wPackage.getImports().prettyPrint(printer);
        wPackage.getElements().prettyPrint(printer);
    }

    public static void prettyPrint(WImports wImports, Printer printer) {
        if (wImports.size() <= 0) {
            return;
        }
        for (WImport wImport : wImports) {
            if (!wImport.getPackagename().equals("Wurst")) {
                wImport.prettyPrint(printer);
            }
        }
        printer.newline();
    }

    public static void prettyPrint(ClassDefs classDefs, Printer printer) {
        for (ClassDef classDef : classDefs) {
            classDef.prettyPrint(printer);
        }
    }

    public static void prettyPrint(Identifier identifier, Printer printer) {
        printer.append(identifier.getName(), identifier);
    }

    public static void prettyPrint(ExprIfElse e, Printer printer) {
        printer.startIndent();
        e.getCond().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr("?");
        printer.addSpace();
        e.getIfTrue().prettyPrint(printer);
        printer.addSpace();
        printer.appendStr(":");
        printer.addSpace();
        e.getIfFalse().prettyPrint(printer);
        printer.endIndent();
    }

    public static void prettyPrint(ArrayInitializer arrayInitializer, Printer printer) {
        printer.append("[", arrayInitializer);
        arrayInitializer.getValues().prettyPrint(printer);
        printer.appendStr("]");
    }
}
