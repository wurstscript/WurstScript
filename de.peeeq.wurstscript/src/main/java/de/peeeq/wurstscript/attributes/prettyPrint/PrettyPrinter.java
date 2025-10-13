package de.peeeq.wurstscript.attributes.prettyPrint;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassAst.JassExprUnary;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.parser.WPosWithComments;
import de.peeeq.wurstscript.parser.WPosWithComments.Comment;
import de.peeeq.wurstscript.utils.Utils;
import org.apache.commons.lang.StringUtils;

public class PrettyPrinter {

    private static void commaSeparatedList(Element e, Spacer spacer, StringBuilder sb, int indent) {
        for (int i = 0; i < e.size(); i++) {
            if (i > 0) {
                sb.append(",");
                spacer.addSpace(sb);
            }
            e.get(i).prettyPrint(spacer, sb, indent);
        }
    }

    private static void printClassOrModuleDeclaration(ClassOrModule e, Spacer spacer, StringBuilder sb, int indent) {
        e.getModuleUses().prettyPrint(spacer, sb, indent);
        e.getVars().prettyPrint(spacer, sb, indent);
        e.getConstructors().prettyPrint(spacer, sb, indent);
        e.getMethods().prettyPrint(spacer, sb, indent);
        e.getOnDestroy().prettyPrint(spacer, sb, indent);
        e.getInnerClasses().prettyPrint(spacer, sb, indent);
    }

    private static void printFirstNewline(Element e, StringBuilder sb, int indent) {
        // Don't add first new line.
        if (e.getParent().get(0).equals(e)) {
            return;
        }
        if (sb.charAt(sb.length() - 1) == '\n' && sb.charAt(sb.length() - 2) == '\n') {
            return;
        }
        sb.append("\n");
    }

    private static void printNewline(Element e, StringBuilder sb, int indent) {
        // Don't add a newline unless we are directly under statements.
        // Otherwise we would add newlines inside function calls, etc.
        if (!(e.getParent() instanceof WStatements)) {
            return;
        }
        sb.append("\n");
    }

    private static void printStuff(Documentable e, Spacer spacer, StringBuilder sb, int indent) {
        printCommentsBefore(sb, e, indent);
        printHotDoc(e.getModifiers(), spacer, sb, indent);
        printIndent(sb, indent);
        e.getModifiers().prettyPrint(spacer, sb, indent);
    }

    // For some reason comments are a kind of modifier.
    private static void printHotDoc(Modifiers e, Spacer spacer, StringBuilder sb, int indent) {
        for (Modifier modifier : e) {
            if (modifier instanceof WurstDoc) {
                modifier.prettyPrint(spacer, sb, indent);
            }
        }
    }

    private static void printCommentsBefore(StringBuilder sb, Element d, int indent) {
        if (!(d instanceof AstElementWithSource)) {
            return;
        }
        WPos source1 = ((AstElementWithSource) d).getSource();
        if (source1 instanceof WPosWithComments) {
            WPosWithComments source = (WPosWithComments) source1;
            for (Comment comment : source.getCommentsBefore()) {
                printIndent(sb, indent);
                sb.append(comment.getContent());
                if (comment.isSingleLine()) {
                    sb.append("\n");
                }
            }
        }
    }

    private static void printCommentsAfter(StringBuilder sb, Element d, int indent) {
        if (!(d instanceof AstElementWithSource)) {
            return;
        }
        WPos source1 = ((AstElementWithSource) d).getSource();
        if (source1 instanceof WPosWithComments) {
            WPosWithComments source = (WPosWithComments) source1;
            for (Comment comment : source.getCommentsAfter()) {
                printIndent(sb, indent);
                sb.append(comment.getContent());
                if (comment.isSingleLine()) {
                    sb.append("\n");
                }
            }
        }
    }


    private static void printIndent(StringBuilder sb, int indent) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            for (int i = 0; i < indent; i++) {
                sb.append("    ");
            }
        }
    }

    private static String calculateIndent(int indent) {
        String s = "";
        for (int i = 0; i < indent; i++) {
            s += "\t";
        }
        return s;
    }

    public static void prettyPrint(JassToplevelDeclarations e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(JassGlobalBlock e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(Annotation e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getAnnotationType());
        if (e.getAnnotationMessage() != null && e.getAnnotationMessage().length() >= 1) {
            sb.append("(");
            sb.append(Utils.escapeString(e.getAnnotationMessage()));
            sb.append(")");
        }
    }

    public static void prettyPrint(Arguments e, Spacer spacer, StringBuilder sb, int indent) {
        commaSeparatedList(e, spacer, sb, indent);
    }

    public static void prettyPrint(ExprList e, Spacer spacer, StringBuilder sb, int indent) {
        commaSeparatedList(e, spacer, sb, indent);
    }

    public static void prettyPrint(ArraySizes e, Spacer spacer, StringBuilder sb, int indent) {
        commaSeparatedList(e, spacer, sb, indent);
    }

    public static void prettyPrint(ClassDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("class");
        spacer.addSpace(sb);
        sb.append(e.getName());
        e.getTypeParameters().prettyPrint(spacer, sb, indent);
        if (e.getExtendedClass() instanceof TypeExpr) {
            TypeExpr te = (TypeExpr) e.getExtendedClass();
            spacer.addSpace(sb);
            sb.append("extends");
            spacer.addSpace(sb);
            te.prettyPrint(spacer, sb, indent);
        }
        if (!e.getImplementsList().isEmpty()) {
            spacer.addSpace(sb);
            sb.append("implements");
            spacer.addSpace(sb);
            commaSeparatedList(e.getImplementsList(), spacer, sb, indent);
        }
        sb.append("\n");
        printClassOrModuleDeclaration(e, spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(CompilationUnit e, Spacer spacer, StringBuilder sb, int indent) {
        prettyPrint(e.getPackages(), spacer, sb, indent);
        jassPrettyPrint(e.getJassDecls(), spacer, sb, indent);
    }

    public static void prettyPrint(ConstructorDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("construct");
        e.getParameters().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        e.getSuperConstructorCall().prettyPrint(spacer, sb, indent);
        e.getBody().prettyPrint(spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(ConstructorDefs e, Spacer spacer, StringBuilder sb, int indent) {
        for (ConstructorDef constructorDef : e) {
            constructorDef.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(EndFunctionStatement e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(EnumDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("enum");
        spacer.addSpace(sb);
        sb.append(e.getName());
        sb.append("\n");
        e.getMembers().prettyPrint(spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(EnumMember e, Spacer spacer, StringBuilder sb, int indent) {
        printStuff(e, spacer, sb, indent);
        sb.append(e.getName());
        sb.append("\n");
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(EnumMembers e, Spacer spacer, StringBuilder sb, int indent) {
        for (EnumMember enumMember : e) {
            enumMember.prettyPrint(spacer, sb, indent);
        }
    }

    public static int precedence(WurstOperator op) {
        // Higher number = binds tighter
        // 5: unary (not, unary minus)
        // 4: * / % (mod)
        // 3: + -
        // 2: comparisons (<= >= > < == !=)
        // 1: or
        // 0: and
        switch (op) {
            case NOT:
            case UNARY_MINUS:
                return 5;

            case MULT:
            case DIV_INT:
            case DIV_REAL:
            case MOD_INT:
            case MOD_REAL:
                return 4;

            case PLUS:
            case MINUS:
                return 3;

            case LESS:
            case LESS_EQ:
            case GREATER:
            case GREATER_EQ:
            case EQ:
            case NOTEQ:
                return 2;

            case OR:
                return 1;

            case AND:
                return 0;
        }
        // Fallback if new ops appear
        return 0;
    }


    public static void prettyPrint(ExprBinary e, Spacer spacer, StringBuilder sb, int indent) {
        boolean useParanthesesLeft = false;
        boolean useParanthesesRight = false;
        if (e.getLeft() instanceof ExprBinary) {
            ExprBinary left = (ExprBinary) e.getLeft();
            if (precedence(left.getOp()) < precedence(e.getOp())) {
                // if the precedence level on the left is _smaller_ we have to use parentheses
                useParanthesesLeft = true;
            }
            // if the precedence level is equal we can assume left associativity of all operators
            // so they are treated correctly
        } else if (e.getLeft() instanceof ExprUnary) {
            useParanthesesLeft = true;
        }
        if (e.getRight() instanceof ExprBinary) {
            ExprBinary right = (ExprBinary) e.getRight();
            WurstOperator op = right.getOp();
            WurstOperator op2 = e.getOp();
            if (precedence(op) < precedence(op2)) {
                // if the precedence level on the right is smaller we have to use parentheses
                useParanthesesRight = true;
            } else if (precedence(op) == precedence(op2)) {
                // if the precedence level is equal we have to parentheses as operators are
                // left associative but for commutative operators (+, *, and, or) we do not
                // need parentheses

                if (!((op == WurstOperator.PLUS && op2 == WurstOperator.PLUS)
                    || (op == WurstOperator.MULT && op2 == WurstOperator.MULT)
                    || (op == WurstOperator.OR && op2 == WurstOperator.OR)
                    || (op == WurstOperator.AND && op2 == WurstOperator.AND))) {
                    // in other cases use parentheses
                    // for example
                    useParanthesesRight = true;
                }
            }
        } else if (e.getRight() instanceof JassExprUnary) {
            useParanthesesRight = true;
        }

        sb.append(useParanthesesLeft ? "(" : "");
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(useParanthesesLeft ? ")" : "");
        spacer.addSpace(sb);
        sb.append(e.getOp().toString());
        spacer.addSpace(sb);
        sb.append(useParanthesesRight ? "(" : "");
        e.getRight().prettyPrint(spacer, sb, indent);
        sb.append(useParanthesesRight ? ")" : "");
    }

    public static void prettyPrint(ExprBoolVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getValB());
    }

    public static void prettyPrint(ExprCast e, Spacer spacer, StringBuilder sb, int indent) {
        e.getExpr().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("castTo");
        spacer.addSpace(sb);
        e.getTyp().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprClosure e, Spacer spacer, StringBuilder sb, int indent) {
        e.getShortParameters().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("->");
        spacer.addSpace(sb);
        e.getImplementation().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprDestroy e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printIndent(sb, indent);
        sb.append("destroy");
        spacer.addSpace(sb);
        e.getDestroyedObj().prettyPrint(spacer, sb, indent);
        sb.append("\n");
    }

    public static void prettyPrint(ExprEmpty e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ExprFuncRef e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("function");
        spacer.addSpace(sb);
        if (!StringUtils.isBlank(e.getScopeName())) {
            sb.append(e.getScopeName());
            sb.append(".");
        }
        sb.append(e.getFuncName());
    }

    public static void prettyPrint(ExprFunctionCall e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append(e.getFuncName());
        sb.append("(");
        e.getArgs().prettyPrint(spacer, sb, indent);
        sb.append(")");
        printNewline(e, sb, indent);
    }

    public static void prettyPrint(ExprIncomplete e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ExprInstanceOf e, Spacer spacer, StringBuilder sb, int indent) {
        e.getExpr().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("instanceof");
        spacer.addSpace(sb);
        e.getTyp().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprIntVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getValIraw());
    }

    public static void prettyPrint(ExprMemberArrayVarDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(".");
        sb.append(e.getVarName());
        sb.append("[");
        e.getIndexes().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(ExprMemberArrayVarDotDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append("..");
        sb.append(e.getVarName());
        sb.append("[");
        e.getIndexes().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(ExprMemberMethodDot e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        if (e.getLeft() instanceof ExprBinary) {
            sb.append("(");
            e.getLeft().prettyPrint(spacer, sb, indent);
            sb.append(")");
        } else {
            e.getLeft().prettyPrint(spacer, sb, indent);
        }
        sb.append(".");
        sb.append(e.getFuncName());
        sb.append("(");
        e.getArgs().prettyPrint(spacer, sb, indent);
        sb.append(")");
        printNewline(e, sb, indent);
    }

    public static void prettyPrint(ExprMemberMethodDotDot e, Spacer spacer, StringBuilder sb, int indent) {
        if (e.getLeft() instanceof ExprBinary) {
            sb.append("(");
            e.getLeft().prettyPrint(spacer, sb, indent);
            sb.append(")");
        } else {
            e.getLeft().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
        printIndent(sb, indent + 1);
        sb.append("..");
        sb.append(e.getFuncName());
        sb.append("(");
        e.getArgs().prettyPrint(spacer, sb, indent);
        sb.append(")");
        printNewline(e, sb, indent);
    }

    public static void prettyPrint(ExprMemberVarDot e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(".");
        sb.append(e.getVarName());
    }

    public static void prettyPrint(ExprMemberVarDotDot e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append("..");
        sb.append(e.getVarName());
    }

    public static void prettyPrint(ExprNewObject e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("new");
        spacer.addSpace(sb);
        sb.append(e.getTypeName());
        e.getTypeArgs().prettyPrint(spacer, sb, indent);
        if (e.getArgs().size() > 0) {
            sb.append("(");
            e.getArgs().prettyPrint(spacer, sb, indent);
            sb.append(")");
        }
        printNewline(e, sb, indent);
    }

    public static void prettyPrint(ExprNull e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("null");
    }

    public static void prettyPrint(ExprRealVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getValR());
    }

    public static void prettyPrint(ExprStatementsBlock e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("begin");
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
        printIndent(sb, indent);
        sb.append("end");
    }

    public static void prettyPrint(ExprStringVal e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(Utils.escapeString(e.getValS()));
    }

    public static void prettyPrint(ExprSuper e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("super");
    }

    public static void prettyPrint(ExprThis e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("this");
    }

    public static void prettyPrint(ExprTypeId e, Spacer spacer, StringBuilder sb, int indent) {
        e.getLeft().prettyPrint(spacer, sb, indent);
        sb.append(".typeId");
    }

    public static void prettyPrint(ExprUnary e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getOpU().toString());
        // Don't add space for unary minus, e.g -1.
        if (e.getOpU().toString() == "not") {
            spacer.addSpace(sb);
        }
        e.getRight().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(ExprVarAccess e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append(e.getVarName());
    }

    public static void prettyPrint(ExprVarArrayAccess e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append(e.getVarName());
        sb.append("[");
        e.getIndexes().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(ExtensionFuncDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("function");
        spacer.addSpace(sb);
        e.getExtendedType().prettyPrint(spacer, sb, indent);
        sb.append(".");
        printFunctionSignature(e, spacer, sb, indent);
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
    }


    private static void printFunctionSignature(FunctionImplementation e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getName());
        e.getTypeParameters().prettyPrint(spacer, sb, indent);
        e.getParameters().prettyPrint(spacer, sb, indent);
        if (!(e.getReturnTyp() instanceof NoTypeExpr)) {
            spacer.addSpace(sb);
            sb.append("returns");
            spacer.addSpace(sb);
            e.getReturnTyp().prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(FuncDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("function");
        spacer.addSpace(sb);
        printFunctionSignature(e, spacer, sb, indent);
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(FuncDefs e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        for (FuncDef funcDef : e) {
            funcDef.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(GlobalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
        printStuff(e, spacer, sb, indent);
        if ((e.getOptTyp() instanceof NoTypeExpr)) {
            if (!e.attrIsConstant()) {
                sb.append("var");
            }
        } else {
            e.getOptTyp().prettyPrint(spacer, sb, indent);
        }
        spacer.addSpace(sb);
        sb.append(e.getName());
        if (!(e.getInitialExpr() instanceof NoExpr)) {
            spacer.addSpace(sb);
            sb.append("=");
            spacer.addSpace(sb);
            e.getInitialExpr().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(GlobalVarDefs e, Spacer spacer, StringBuilder sb, int indent) {
        if (e.size() <= 0) {
            return;
        }
        for (GlobalVarDef varDef : e) {
            varDef.prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
    }

    public static void prettyPrint(IdentifierWithTypeArgs e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(IdentifierWithTypeParamDefs e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(Indexes e, Spacer spacer, StringBuilder sb, int indent) {
        for (Expr expr : e) {
            expr.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(InitBlock e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printIndent(sb, indent);
        sb.append("init");
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(InterfaceDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("interface");
        spacer.addSpace(sb);
        sb.append(e.getName());
        e.getTypeParameters().prettyPrint(spacer, sb, indent);
        if (e.getExtendsList().size() >= 1) {
            spacer.addSpace(sb);
            sb.append("extends");
            spacer.addSpace(sb);
            e.getExtendsList().prettyPrint(spacer, sb, indent);
        }
        e.getModuleUses().prettyPrint(spacer, sb, indent + 1);
        e.getVars().prettyPrint(spacer, sb, indent + 1);
        e.getConstructors().prettyPrint(spacer, sb, indent + 1);
        e.getMethods().prettyPrint(spacer, sb, indent + 1);
        e.getOnDestroy().prettyPrint(spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
    }


    public static void jassPrettyPrint(GlobalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
        printStuff(e, spacer, sb, indent);
        jassPrettyPrint(e.getOptTyp(), spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(e.getName());
        if (!(e.getInitialExpr() instanceof NoExpr)) {
            spacer.addSpace(sb);
            sb.append("=");
            spacer.addSpace(sb);
            e.getInitialExpr().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
        printCommentsAfter(sb, e, indent);
    }

    public static void jassPrettyPrint(OptTypeExpr e, Spacer spacer, StringBuilder sb, int indent) {
        if (e instanceof NoTypeExpr) {
            sb.append("nothing");
        } else if (e instanceof TypeExpr) {
            jassPrettyPrint((TypeExpr) e, spacer, sb, indent);
        }
    }

    public static void jassPrettyPrint(TypeExpr e, Spacer spacer, StringBuilder sb, int indent) {
        if (e instanceof NoTypeExpr) {
            sb.append("nothing");
        } else if (e instanceof TypeExprSimple) {
            sb.append(((TypeExprSimple) e).getTypeName());
        } else if (e instanceof TypeExprArray) {
            jassPrettyPrint(((TypeExprArray) e).getBase(), spacer, sb, indent);
            spacer.addSpace(sb);
            sb.append("array");
        }
    }

    public static void jassPrettyPrint(JassToplevelDeclarations e, Spacer spacer, StringBuilder sb, int indent) {
        for (int i = 0; i < e.size(); i++) {
            jassPrettyPrint(e.get(i), spacer, sb, indent);
        }
    }

    private static void jassPrettyPrint(JassToplevelDeclaration e, Spacer spacer, StringBuilder sb, int indent) {
        if (e instanceof JassGlobalBlock) {
            jassPrettyPrint((JassGlobalBlock) e, spacer, sb, indent);
        } else if (e instanceof NativeFunc) {
            jassPrettyPrint((NativeFunc) e, spacer, sb, indent);
        } else if (e instanceof FuncDef) {
            jassPrettyPrint((FuncDef) e, spacer, sb, indent);
        }
    }

    public static void jassPrettyPrint(FuncDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("function");
        spacer.addSpace(sb);
        sb.append(e.getName());
        spacer.addSpace(sb);
        sb.append("takes");
        spacer.addSpace(sb);
        jassPrettyPrint(e.getParameters(), spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("returns");
        spacer.addSpace(sb);
        if (e.getReturnTyp() instanceof NoTypeExpr) {
            sb.append("nothing");
        } else {
            e.getReturnTyp().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
        jassPrettyPrint(e.getBody(), spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
        sb.append("endfunction\n");
    }


    public static void jassPrettyPrint(WStatements e, Spacer spacer, StringBuilder sb, int indent) {
        for (int i = 0; i < e.size(); i++) {
            jassPrettyPrint(e.get(i), spacer, sb, indent);
        }
    }

    public static void jassPrettyPrint(WStatement e, Spacer spacer, StringBuilder sb, int indent) {
        if (e instanceof LocalVarDef) {
            jassPrettyPrint((LocalVarDef) e, spacer, sb, indent);
        } else if (e instanceof StmtSet) {
            jassPrettyPrint((StmtSet) e, spacer, sb, indent);
        } else if (e instanceof StmtCall) {
            jassPrettyPrint((StmtCall) e, spacer, sb, indent);
        } else if (e instanceof StmtIf) {
            jassPrettyPrint((StmtIf) e, spacer, sb, indent);
        } else if (e instanceof StmtReturn) {
            jassPrettyPrint((StmtReturn) e, spacer, sb, indent);
        } else if (e instanceof StmtLoop) {
            jassPrettyPrint((StmtLoop) e, spacer, sb, indent);
        } else if (e instanceof StmtExitwhen) {
            jassPrettyPrint((StmtExitwhen) e, spacer, sb, indent);
        }
    }

    public static void jassPrettyPrint(StmtExitwhen e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("exitwhen");
        spacer.addSpace(sb);
        e.getCond().prettyPrint(spacer, sb, indent);
        sb.append("\n");
    }

    public static void jassPrettyPrint(StmtLoop e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("loop\n");
        jassPrettyPrint(e.getBody(), spacer, sb, indent + 1);
        printIndent(sb, indent);
        sb.append("endloop\n");
    }

    public static void jassPrettyPrint(StmtReturn e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("return");
        spacer.addSpace(sb);
        e.getReturnedObj().prettyPrint(spacer, sb, indent);
        sb.append("\n");
    }

    public static void jassPrettyPrint(StmtIf e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("if");
        spacer.addSpace(sb);
        e.getCond().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("then\n");
        jassPrettyPrint(e.getThenBlock(), spacer, sb, indent + 1);
        if (e.getElseBlock().size() > 0) {
            printIndent(sb, indent);
            sb.append("else");
            if (e.getElseBlock().size() == 1 && e.getElseBlock().get(0) instanceof StmtIf) {
                jassPrettyPrint(e.getElseBlock().get(0), spacer, sb, indent);
            } else {
                sb.append("\n");
                jassPrettyPrint(e.getElseBlock(), spacer, sb, indent + 1);
                printIndent(sb, indent);
                sb.append("endif\n");
            }
        } else {
            printIndent(sb, indent);
            sb.append("endif\n");
        }

    }

    public static void jassPrettyPrint(StmtCall e, Spacer spacer, StringBuilder sb, int indent) {
        if (e instanceof FunctionCall) {
            jassPrettyPrint((FunctionCall) e, spacer, sb, indent);
        }
    }

    public static void jassPrettyPrint(FunctionCall e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("call");
        spacer.addSpace(sb);
        sb.append(e.getFuncName());
        sb.append("(");
        jassPrettyPrint(e.getArgs(), spacer, sb, indent);
        sb.append(")");
        sb.append("\n");
    }

    public static void jassPrettyPrint(Arguments e, Spacer spacer, StringBuilder sb, int indent) {
        commaSeparatedList(e, spacer, sb, indent);
    }

    public static void jassPrettyPrint(StmtSet e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("set");
        spacer.addSpace(sb);
        e.getUpdatedExpr().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("=");
        spacer.addSpace(sb);
        e.getRight().prettyPrint(spacer, sb, indent);
        sb.append("\n");
    }


    public static void jassPrettyPrint(LocalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
        printCommentsBefore(sb, e, indent);
        printIndent(sb, indent);
        sb.append("local");
        spacer.addSpace(sb);
        e.getOptTyp().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(e.getName());
        if (!(e.getInitialExpr() instanceof NoExpr)) {
            spacer.addSpace(sb);
            sb.append("=");
            spacer.addSpace(sb);
            e.getInitialExpr().prettyPrint(spacer, sb, indent);
        }
        printNewline(e, sb, indent);
        printCommentsAfter(sb, e, indent);
    }

    public static void jassPrettyPrint(JassGlobalBlock e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("globals\n");
        for (int i = 0; i < e.size(); i++) {
            jassPrettyPrint(e.get(i), spacer, sb, indent + 1);
        }
        sb.append("endglobals\n");
    }

    public static void jassPrettyPrint(WParameters wParameters, Spacer spacer, StringBuilder sb, int indent) {
        if (wParameters.size() == 0) {
            sb.append("nothing");
            return;
        }
        String prefix = "";
        for (WParameter wParameter : wParameters) {
            sb.append(prefix);
            prefix = ",";
            spacer.addSpace(sb);
            wParameter.prettyPrint(spacer, sb, indent);
        }
    }

    public static void jassPrettyPrint(NativeFunc e, Spacer spacer, StringBuilder sb, int indent) {
        printStuff(e, spacer, sb, indent);
        sb.append("native");
        spacer.addSpace(sb);
        e.getNameId().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("takes");
        spacer.addSpace(sb);
        jassPrettyPrint(e.getParameters(), spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("returns");
        spacer.addSpace(sb);
        e.getReturnTyp().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(LocalVarDef e, Spacer spacer, StringBuilder sb, int indent) {
        printCommentsBefore(sb, e, indent);
        printIndent(sb, indent);
        if ((e.getOptTyp() instanceof NoTypeExpr)) {
            if (e.attrIsConstant()) {
                sb.append("let");
            } else if (!(e.getParent() instanceof StmtForRange)) {
                sb.append("var");
            }
        } else {
            e.getOptTyp().prettyPrint(spacer, sb, indent);
        }
        spacer.addSpace(sb);
        sb.append(e.getName());
        if (!(e.getInitialExpr() instanceof NoExpr)) {
            spacer.addSpace(sb);
            sb.append("=");
            spacer.addSpace(sb);
            e.getInitialExpr().prettyPrint(spacer, sb, indent);
        }
        printNewline(e, sb, indent);
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(ModAbstract e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("abstract");
    }

    public static void prettyPrint(ModConstant e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("constant");
    }

    public static void prettyPrint(ModVararg e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("vararg");
    }

    public static void prettyPrint(Modifiers e, Spacer spacer, StringBuilder sb, int indent) {
        for (Modifier modifier : e) {
            // We handle WurstDoc separately.
            if (!(modifier instanceof WurstDoc)) {
                modifier.prettyPrint(spacer, sb, indent);
                spacer.addSpace(sb);
            }
        }
    }

    public static void prettyPrint(ModOverride e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("override");
    }

    public static void prettyPrint(ModStatic e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("static");
    }

    public static void prettyPrint(ModuleDef e, Spacer spacer, StringBuilder sb, int indent) {
        printFirstNewline(e, sb, indent);
        printStuff(e, spacer, sb, indent);
        sb.append("module");
        spacer.addSpace(sb);
        e.getNameId().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        printClassOrModuleDeclaration(e, spacer, sb, indent + 1);
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(ModuleInstanciation e, Spacer spacer, StringBuilder sb, int indent) {
        printCommentsBefore(sb, e, indent);
    }

    public static void prettyPrint(ModuleInstanciations e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(ModuleUse e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("use");
        spacer.addSpace(sb);
        sb.append(e.getModuleName());
        sb.append("\n");
    }

    public static void prettyPrint(ModuleUses e, Spacer spacer, StringBuilder sb, int indent) {
        for (ModuleUse moduleUse : e) {
            moduleUse.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(NativeFunc e, Spacer spacer, StringBuilder sb, int indent) {
        printStuff(e, spacer, sb, indent);
        sb.append("native");
        spacer.addSpace(sb);
        e.getNameId().prettyPrint(spacer, sb, indent);
        e.getParameters().prettyPrint(spacer, sb, indent);
        if (!(e.getReturnTyp() instanceof NoTypeExpr)) {
            spacer.addSpace(sb);
            sb.append("returns");
            spacer.addSpace(sb);
            e.getReturnTyp().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(NativeType e, Spacer spacer, StringBuilder sb, int indent) {
        printStuff(e, spacer, sb, indent);
        sb.append("nativetype");
        spacer.addSpace(sb);
        e.getNameId().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(NoDefaultCase e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(NoExpr e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(NoTypeExpr e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(OnDestroyDef e, Spacer spacer, StringBuilder sb, int indent) {
        if (e.getBody().size() <= 0) {
            return;
        }
        printFirstNewline(e, sb, indent);
        printIndent(sb, indent);
        sb.append("ondestroy");
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(StartFunctionStatement e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtErr e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtExitwhen e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("break");
        sb.append("\n");
    }

    public static void prettyPrint(StmtForFrom e, Spacer spacer, StringBuilder sb, int indent) {
        forIteratorLoop("from", e, e.getIn(), spacer, sb, indent);
    }

    public static void prettyPrint(StmtForIn e, Spacer spacer, StringBuilder sb, int indent) {
        forIteratorLoop("in", e, e.getIn(), spacer, sb, indent);
    }

    private static void forIteratorLoop(String method, LoopStatementWithVarDef e, Expr target, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("for");
        spacer.addSpace(sb);
        e.getLoopVar().getNameId().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(method);
        spacer.addSpace(sb);
        target.prettyPrint(spacer, sb, indent);
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    private static void forRangeLoop(String direction, StmtForRange e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("for");
        spacer.addSpace(sb);
        e.getLoopVar().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(direction);
        spacer.addSpace(sb);
        e.getTo().prettyPrint(spacer, sb, indent);
        if (e.getStep() instanceof ExprIntVal && ((ExprIntVal) e.getStep()).getValI() != 1) {
            spacer.addSpace(sb);
            sb.append("step");
            spacer.addSpace(sb);
            e.getStep().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(StmtForRangeDown e, Spacer spacer, StringBuilder sb, int indent) {
        forRangeLoop("downto", e, spacer, sb, indent);
    }

    public static void prettyPrint(StmtForRangeUp e, Spacer spacer, StringBuilder sb, int indent) {
        forRangeLoop("to", e, spacer, sb, indent);
    }

    public static void prettyPrint(StmtIf e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("if");
        spacer.addSpace(sb);
        e.getCond().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        e.getThenBlock().prettyPrint(spacer, sb, indent + 1);
        if (e.getElseBlock().size() > 0) {
            printIndent(sb, indent);
            sb.append("else");
            if (e.getElseBlock().size() > 0 && e.getElseBlock().get(0) instanceof StmtIf) {
                spacer.addSpace(sb);
                e.getElseBlock().get(0).prettyPrint(spacer, sb, indent);
            } else {
                sb.append("\n");
                e.getElseBlock().prettyPrint(spacer, sb, indent + 1);
            }
        }
    }

    public static void prettyPrint(StmtLoop e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(StmtReturn e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("return");
        spacer.addSpace(sb);
        e.getReturnedObj().prettyPrint(spacer, sb, indent);
        sb.append("\n");
    }

    private static boolean printAssignmentShorthands(Expr left, Expr right, Spacer spacer, StringBuilder sb, int indent) {
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
            sb.append(operator);
            sb.append(operator);
            return true;
        }

        // i +=, ...
        spacer.addSpace(sb);
        sb.append(operator);
        sb.append("=");
        spacer.addSpace(sb);
        val.prettyPrint(spacer, sb, indent);
        return true;
    }

    public static void prettyPrint(StmtSet e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        e.getUpdatedExpr().prettyPrint(spacer, sb, indent);

        // Special cases for assignment shorthands i++ and i += variants.
        boolean shortened = printAssignmentShorthands(e.getUpdatedExpr(), e.getRight(), spacer, sb, indent);

        if (!shortened) {
            spacer.addSpace(sb);
            sb.append("=");
            spacer.addSpace(sb);
            e.getRight().prettyPrint(spacer, sb, indent);
        }
        sb.append("\n");
    }

    public static void prettyPrint(StmtSkip e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("skip");
        sb.append("\n");
    }

    public static void prettyPrint(StmtWhile e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("while");
        spacer.addSpace(sb);
        e.getCond().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        e.getBody().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(SwitchCase e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("case");
        spacer.addSpace(sb);
        e.getExpressions().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        e.getStmts().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(SwitchCases sw, Spacer spacer, StringBuilder sb, int indent) {
        for (SwitchCase c : sw) {
            c.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(SwitchDefaultCaseStatements e, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("default");
        sb.append("\n");
        e.getStmts().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(SwitchStmt sw, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("switch");
        spacer.addSpace(sb);
        sw.getExpr().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        sw.getCases().prettyPrint(spacer, sb, indent + 1);
        sw.getSwitchDefault().prettyPrint(spacer, sb, indent + 1);
    }

    public static void prettyPrint(TopLevelDeclarations e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TupleDef e, Spacer spacer, StringBuilder sb, int indent) {
        printStuff(e, spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("tuple");
        spacer.addSpace(sb);
        e.getNameId().prettyPrint(spacer, sb, indent);
        e.getParameters().prettyPrint(spacer, sb, indent);
        sb.append("\n");
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(TypeExprArray e, Spacer spacer, StringBuilder sb, int indent) {
        e.getBase().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("array");
    }

    public static void prettyPrint(TypeExprList typeExprList, Spacer spacer, StringBuilder sb, int indent) {
        if (typeExprList.size() == 0) {
            return;
        }
        sb.append("<");
        commaSeparatedList(typeExprList, spacer, sb, indent);
        sb.append(">");
    }

    public static void prettyPrint(TypeExprResolved e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(TypeExprSimple e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(e.getTypeName());
        e.getTypeArgs().prettyPrint(spacer, sb, indent);
    }

    public static void prettyPrint(TypeExprThis e, Spacer spacer, StringBuilder sb, int indent) {
        if (!(e.getScopeType() instanceof NoTypeExpr)) {
            e.getScopeType().prettyPrint(spacer, sb, indent);
            sb.append(".");
        }
        sb.append("thistype");
    }

    public static void prettyPrint(TypeParamDef e, Spacer spacer, StringBuilder sb, int indent) {
        printCommentsBefore(sb, e, indent);
        e.getNameId().prettyPrint(spacer, sb, indent);
        printCommentsAfter(sb, e, indent);
    }

    public static void prettyPrint(TypeParamDefs e, Spacer spacer, StringBuilder sb, int indent) {
        if (e.size() >= 1) {
            sb.append("<");
            commaSeparatedList(e, spacer, sb, indent);
            sb.append(">");
        }
    }

    public static void prettyPrint(VisibilityDefault e, Spacer spacer, StringBuilder sb, int indent) {
    }

    public static void prettyPrint(VisibilityPrivate e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("private");
    }

    public static void prettyPrint(VisibilityProtected e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("protected");
    }

    public static void prettyPrint(VisibilityPublic e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("public");
    }

    public static void prettyPrint(VisibilityPublicread e, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("publicread");
    }

    public static void prettyPrint(WBlock wBlock, Spacer spacer, StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
        throw new Error("not implemented");
    }

    public static void prettyPrint(WEntities wEntities, Spacer spacer, StringBuilder sb, int indent) {
        for (WEntity entity : wEntities) {
            entity.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(WImport wImport, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("import");
        spacer.addSpace(sb);
        sb.append(wImport.getIsInitLater() ? "initlater" : "");
        spacer.addSpace(sb);
        sb.append(wImport.getIsPublic() ? "public" : "");
        spacer.addSpace(sb);
        sb.append(wImport.getPackagename());
        sb.append("\n");
    }

    public static void prettyPrint(WurstModel wurstModel, Spacer spacer, StringBuilder sb, int indent) {
        for (CompilationUnit compilationUnit : wurstModel) {
            compilationUnit.prettyPrint(spacer, sb, indent);
        }
    }

    private static String spaces(int indentation) {
        return " " + "{" + indentation + "}";
    }

    public static void prettyPrint(WurstDoc wurstDoc, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append(wurstDoc.getRawComment());
        sb.append("\n");
    }

    public static void prettyPrint(WStatements wStatements, Spacer spacer, StringBuilder sb, int indent) {
        for (WStatement wStatement : wStatements) {
            wStatement.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(WParameters wParameters, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("(");
        String prefix = "";
        for (WParameter wParameter : wParameters) {
            sb.append(prefix);
            prefix = ",";
            spacer.addSpace(sb);
            wParameter.prettyPrint(spacer, sb, indent);
        }
        sb.append(")");
    }

    public static void prettyPrint(WShortParameters wParameters, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("(");
        String prefix = "";
        for (WShortParameter wParameter : wParameters) {
            sb.append(prefix);
            prefix = ",";
            spacer.addSpace(sb);
            wParameter.prettyPrint(spacer, sb, indent);
        }
        sb.append(")");
    }

    public static void prettyPrint(WParameter wParameter, Spacer spacer, StringBuilder sb, int indent) {
        wParameter.getTyp().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(wParameter.getName());
    }

    public static void prettyPrint(WShortParameter wParameter, Spacer spacer, StringBuilder sb, int indent) {
        OptTypeExpr t = wParameter.getTypOpt();
        if (t instanceof TypeExpr) {
            t.prettyPrint(spacer, sb, indent);
            spacer.addSpace(sb);
        }
        sb.append(wParameter.getName());
    }

    public static void prettyPrint(WPackages wPackages, Spacer spacer, StringBuilder sb, int indent) {
        for (WPackage wPackage : wPackages) {
            wPackage.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(WPackage wPackage, Spacer spacer, StringBuilder sb, int indent) {
        printCommentsBefore(sb, wPackage, indent);
        sb.append("package");
        spacer.addSpace(sb);
        sb.append(wPackage.getName());
        sb.append("\n\n");
        wPackage.getImports().prettyPrint(spacer, sb, indent);
        wPackage.getElements().prettyPrint(spacer, sb, indent);
        printCommentsAfter(sb, wPackage, indent);
    }

    public static void prettyPrint(WImports wImports, Spacer spacer, StringBuilder sb, int indent) {
        if (wImports.size() <= 0) {
            return;
        }
        for (WImport wImport : wImports) {
            if (!wImport.getPackagename().equals("Wurst")) {
                wImport.prettyPrint(spacer, sb, indent);
            }
        }
        sb.append("\n");
    }

    public static void prettyPrint(ClassDefs classDefs, Spacer spacer, StringBuilder sb, int indent) {
        for (ClassDef classDef : classDefs) {
            classDef.prettyPrint(spacer, sb, indent);
        }
    }

    public static void prettyPrint(Identifier identifier, Spacer spacer, StringBuilder sb, int indent) {
        sb.append(identifier.getName());
    }

    public static void prettyPrint(ExprIfElse e, Spacer spacer, StringBuilder sb, int indent) {
        boolean shouldBracket = e.getParent() instanceof ExprBinary;
        if (shouldBracket) {
            sb.append("(");
        }
        e.getCond().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append("?");
        spacer.addSpace(sb);
        e.getIfTrue().prettyPrint(spacer, sb, indent);
        spacer.addSpace(sb);
        sb.append(":");
        spacer.addSpace(sb);
        e.getIfFalse().prettyPrint(spacer, sb, indent);
        if (shouldBracket) {
            sb.append(")");
        }
    }

    public static void prettyPrint(ArrayInitializer arrayInitializer, Spacer spacer, StringBuilder sb, int indent) {
        sb.append("[");
        arrayInitializer.getValues().prettyPrint(spacer, sb, indent);
        sb.append("]");
    }

    public static void prettyPrint(NoSuperConstructorCall noSuperConstructorCall, Spacer spacer, StringBuilder sb, int indent) {
        // nothing
    }

    public static void prettyPrint(SomeSuperConstructorCall c, Spacer spacer, StringBuilder sb, int indent) {
        printIndent(sb, indent + 1);
        sb.append("super(");
        c.getSuperArgs().prettyPrint(spacer, sb, indent);
        sb.append(")\n");

    }

    public static void prettyPrint(NoTypeParamConstraints noTypeParamConstraints, Spacer spacer, StringBuilder sb, int indent) {
        // nothing
    }

    public static void prettyPrint(ExprArrayLength exprArrayLength, Spacer spacer, StringBuilder sb, int indent) {
        exprArrayLength.getArray().prettyPrint(spacer, sb, indent);
        sb.append(".length");
    }
}
