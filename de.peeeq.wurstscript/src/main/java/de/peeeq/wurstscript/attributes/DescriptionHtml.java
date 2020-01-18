package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.WurstKeywords;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

public class DescriptionHtml {

    public static @Nullable String description(WurstModel wurstModel) {
        return null;
    }

    public static String description(VarDef v) {
        return "Variable " + v.getName() + " of type " + htmlType(v.attrTyp());
    }

    public static String description(FunctionDefinition f) {
        String comment = f.attrComment();
        comment = comment.replaceAll("\n", "<br />");
        String params = getParameterString(f);
        String returnTypeHtml = htmlType(f.attrReturnTyp());
        String functionDescription;
        if (f.attrComment().length() > 1) {
            functionDescription = comment;
        } else {
            functionDescription = "";
        }

        String funcName = f.getName();
        if (f instanceof ExtensionFuncDef) {
            ExtensionFuncDef exf = (ExtensionFuncDef) f;
            funcName = htmlType(exf.getExtendedType().attrTyp()) + "." + funcName;
        }
        functionDescription += "<pre><hr /><b><font color=\"rgb(127,0,85)\">" + "function</font></b> " + funcName + "(" + params + ") ";
        if (!f.attrTyp().isVoid()) {
            functionDescription += "<br /><b><font color=\"rgb(127,0,85)\">returns</font></b> " + returnTypeHtml;
        }
        functionDescription += "<br /></pre>" + "defined in " + nearestScopeName(f);
        return functionDescription;
    }

    public static String getParameterString(AstElementWithParameters f) {
        StringBuilder descrhtml = new StringBuilder();
        boolean first = true;
        for (WParameter p : f.getParameters()) {
            if (!first) {
                descrhtml.append(", ");
            }
            descrhtml.append(htmlType(p.attrTyp()) + " " + p.getName());
            first = false;
        }
        String params = descrhtml.toString();
        return params;
    }

    public static String description(ConstructorDef constr) {
        ClassDef c = constr.attrNearestClassDef();
        String comment = constr.attrComment();
        comment = comment.replaceAll("\n", "<br />");
        String descr;
        if (constr.attrComment().length() > 1) {
            descr = comment;
        } else {
            descr = "";
        }
        descr += "<pre><hr /><b><font color=\"rgb(127,0,85)\">" +
                "construct</font></b>(" + getParameterString(constr) + ") "
                + "<br /></pre>" + "defined in class " + c.getName();
        return descr;
    }

    public static String htmlType(WurstType attrTyp) {
        String typ = Utils.escapeHtml(attrTyp.getName());
        for (String s : WurstKeywords.JASSTYPES) {
            if (s.equals(typ)) {
                return "<font color=\"rgb(34,136,143)\">" + typ + "</font>";
            }
        }
        return typ;
    }

    private static String nearestScopeName(NameDef n) {
        if (n.attrNearestNamedScope() != null) {
            return Utils.printElement(n.attrNearestNamedScope());
        } else {
            return "Global";
        }
    }

    public static String description(NameDef n) {
        String comment = n.attrComment();
        comment = comment.replaceAll("\n", "<br />");

        String additionalProposalInfo;
        if (n.attrComment().length() > 1) {
            additionalProposalInfo = comment;
        } else {
            additionalProposalInfo = "";
        }
        additionalProposalInfo += "<pre><hr />" + htmlType(n.attrTyp()) + " " + n.getName()
                + "<br /></pre>" + "defined in " + nearestScopeName(n);
        return additionalProposalInfo;
    }

    public static String description(Annotation annotation) {
        // TODO different annotations
        return "This is an annotation ";
    }

    public static @Nullable String description(List<?> l) {
        // just a list of strings
        return null;
    }

    public static @Nullable String description(CompilationUnit compilationUnit) {
        return null;
    }

    public static @Nullable String description(EndFunctionStatement endFunctionStatement) {
        return null;
    }

    public static @Nullable String description(ExprBinary e) {
        FuncLink f = e.attrFuncLink();
        if (f != null) {
            return "This is an overloaded operator:<br/>" +
                    f.getDef().descriptionHtml();
        }
        return null;
    }

    public static @Nullable String description(ExprBoolVal exprBoolVal) {
        return null;
    }

    public static String description(ExprCast e) {
        return "Change the type of the left expression to " + htmlType(e.getTyp().attrTyp());
    }

    public static String description(ExprClosure e) {
        return "This is a closure of type " + htmlType(e.attrExpectedTypAfterOverloading());
    }

    public static String description(FuncRef fr) {
        FuncLink def = fr.attrFuncLink();
        if (def != null) {
            return def.getDef().descriptionHtml();
        }
        return "";
    }

    public static String description(NameRef nr) {
        NameLink nameDef = nr.attrNameLink();
        if (nameDef == null) {
            return nr.getVarName() + " is not defined yet.";
        }
        return nameDef.getDef().descriptionHtml();
    }

    public static @Nullable String description(ExprIncomplete exprIncomplete) {
        return null;
    }

    public static String description(ExprInstanceOf e) {
        return "instanceof: Check if an object has a type which is a subtype of " + htmlType(e.getTyp().attrTyp());
    }

    public static @Nullable String description(ExprIntVal exprIntVal) {
        return null;
    }

    public static String description(ExprNewObject e) {
        return "new: Create a new object of class " + e.getTypeName();
    }

    public static String description(ExprNull e) {
        return "'null' of type " + e.attrExpectedTyp();
    }

    public static @Nullable String description(ExprRealVal exprRealVal) {
        return null;
    }

    public static String description(ExprStatementsBlock exprStatementsBlock) {
        return "begin ... end: This is an expression which consists of a list of statements.";
    }

    public static @Nullable String description(ExprStringVal exprStringVal) {
        return null;
    }

    public static String description(ExprSuper exprSuper) {
        return "super: refers to the super class (extends ...)  of this class";
    }

    public static String description(ExprThis e) {
        return "this has type " + htmlType(e.attrTyp());
    }

    public static String description(ExprTypeId exprTypeId) {
        return "typeId: returns the typeId of an object or class. The typeId is "
                + "a unique number for each class in the same type hierarchy.";
    }

    public static @Nullable String description(ExprUnary exprUnary) {
        return null;
    }


    public static @Nullable String description(
            IdentifierWithTypeArgs identifierWithTypeArgs) {
        return null;
    }

    public static String description(InitBlock initBlock) {
        return "An init block: This block is executed at map start";
    }

    public static @Nullable String description(
            IdentifierWithTypeParamDefs identifierWithTypeParamDefs) {
        return null;
    }

    public static String description(ModAbstract modAbstract) {
        return "abstract: This function provides no implementation. Other classes have to provide "
                + "an implementation for this method.";
    }

    public static String description(ModConstant modConstant) {
        return "constant: This variable can never be changed";
    }

    public static String description(ModOverride m) {
        // TODO add info about which function is overridden
        return "override: This function overrides an other function from a module or superclass";
    }

    public static String description(ModStatic modStatic) {
        return "static: This function or variable is just like a function outside of a class. "
                + "It is not bound to an instance. No dynamic dispatch is used.";
    }

    public static String description(ModuleUse m) {
        return m.attrModuleDef().descriptionHtml();
    }

    public static @Nullable String description(NoDefaultCase noDefaultCase) {
        return null;
    }

    public static @Nullable String description(NoExpr noExpr) {
        return null;
    }

    public static @Nullable String description(NoTypeExpr noTypeExpr) {
        return null;
    }

    public static String description(OnDestroyDef s) {
        return "ondestroy block: These statements are executed when an object of this class "
                + "is destroyed." + s.getSource().getLeftPos() + " - " + s.getSource().getRightPos();
    }

    public static @Nullable String description(StartFunctionStatement s) {
        return null;
    }

    public static String description(ExprDestroy s) {
        return "Destroys an object.";
    }

    public static @Nullable String description(StmtErr s) {
        return null;
    }

    public static String description(StmtExitwhen stmtExitwhen) {
        return "extiwhen: Exits the current loop when the condition is true";
    }

    public static String description(StmtForFrom stmtForFrom) {
        return "Iterate using an iterator. Remember to close the iterator.";
    }

    public static String description(StmtForIn stmtForIn) {
        return "Iterate over something";
    }

    public static String description(StmtForRangeDown s) {
        return "Do something for all " + s.getLoopVar().getName() + " counting from _ down to _";
    }

    public static String description(StmtForRangeUp s) {
        return "Do something for all " + s.getLoopVar().getName() + " counting from _ up to _";
    }

    public static String description(StmtIf stmtIf) {
        return "If statement";
    }

    public static String description(StmtLoop stmtLoop) {
        return "Repeat something forever";
    }

    public static String description(StmtReturn r) {
        if (r.attrNearestExprClosure() != null) {
            return "Returns a value from a closure";
        }
        FunctionImplementation f = r.attrNearestFuncDef();
        if (f != null) {
            return "Returns a value from function " + f.getName();
        }
        return "A return statement";
    }

    public static @Nullable String description(StmtSet s) {
        return null;
    }

    public static String description(StmtSkip stmtSkip) {
        return "The skip statement does nothing. Just skip this line.";
    }

    public static String description(StmtWhile stmtWhile) {
        return "While Statement: Repeat while the condition is true.";
    }

    public static String description(SwitchCase switchCase) {
        return "A case of a switch statement";
    }

    public static String description(
            SwitchDefaultCaseStatements switchDefaultCaseStatements) {
        return "The default case for this switch statement";
    }

    public static String description(SwitchStmt switchStmt) {
        return "A switch statement does different things depending on the value of an epxression.";
    }

    public static String description(TypeExpr t) {
        return "" + htmlType(t.attrTyp());
    }

    public static String description(TypeExprThis t) {
        return "thistype = " + htmlType(t.attrTyp());
    }

    public static @Nullable String description(VisibilityDefault visibilityDefault) {
        return null;
    }

    public static String description(VisibilityPrivate visibilityPrivate) {
        return "private: can only be used inside this class";
    }

    public static String description(VisibilityProtected visibilityProtected) {
        return "protected: can be used in subclasses and in the same package";
    }

    public static String description(VisibilityPublic visibilityPublic) {
        return "public: can be used in other packages";
    }

    public static @Nullable String description(VisibilityPublicread visibilityPublicread) {
        return null;
    }

    public static @Nullable String description(WBlock wBlock) {
        return null;
    }

    public static String description(WImport imp) {
        WPackage imported = imp.attrImportedPackage();
        if (imported != null)
            return imported.attrComment();
        return "import ...";
    }

    public static String description(WurstDoc wurstDoc) {
        return wurstDoc.getRawComment();
    }

    public static @Nullable String description(ExprEmpty exprEmpty) {
        return null;
    }

    public static String description(Identifier identifier) {
        Element parent = identifier.getParent();
        if (parent != null) {
            return parent.descriptionHtml();
        }
        return "";
    }

    public static String description(ExprIfElse exprIfElse) {
        return "A conditional expression.";
    }

    public static String description(ArrayInitializer arrayInitializer) {
        return "An array initializer";
    }

    public static String description(ModVararg modVararg) {
        return "A varargs modifier";
    }

    public static String description(NoSuperConstructorCall noSuperConstructorCall) {
        return "No super constructor called";
    }

    public static String description(SomeSuperConstructorCall s) {
        return "Calling the super constructor";
    }

    public static String description(NoTypeParamConstraints noTypeParamConstraints) {
        return "no type parameter constraints";
    }
}
