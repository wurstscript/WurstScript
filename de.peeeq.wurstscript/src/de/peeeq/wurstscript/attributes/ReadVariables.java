package de.peeeq.wurstscript.attributes;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprIncomplete;
import de.peeeq.wurstscript.ast.ExprInstanceOf;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStatementsBlock;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprSuper;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExprTypeId;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.WStatement;

public class ReadVariables {

	public static ImmutableList<NameDef> calculate(WStatement e) {
		return generic(e); 
	}

	public static ImmutableList<NameDef> calculate(StmtSet e) {
		return e.getRight().attrReadVariables().cons(generic(e.getUpdatedExpr()));
		
	}
	
	public static ImmutableList<NameDef> calculate(LocalVarDef e) {
		return generic(e.getInitialExpr());
	}


	private static ImmutableList<NameDef> generic(AstElement e) {
		ImmutableList<NameDef> r = ImmutableList.emptyList();
		for (int i=0; i<e.size(); i++) {
			if (e.get(i) instanceof Expr) {
				Expr child = (Expr) e.get(i);
				r = r.cons(child.attrReadVariables());
			} else if (e.get(i) instanceof Arguments) {
				Arguments args = (Arguments) e.get(i);
				for (Expr a : args) {
					r = r.cons(a.attrReadVariables());
				}
			}
		}
		return r;
	}
	
	
	
	public static ImmutableList<NameDef> calculate(ExprVarAccess e) {
		if (e.attrNameDef() != null) {
			return ImmutableList.of(e.attrNameDef());
		} else {
			return ImmutableList.emptyList();
		}
	}

	public static ImmutableList<NameDef> calculate(ExprVarArrayAccess e) {
		if (e.attrNameDef() != null) {
			return ImmutableList.of(e.attrNameDef());
		} else {
			return ImmutableList.emptyList();
		}
	}
	
	public static ImmutableList<NameDef> calculate(ExprMemberArrayVar e) {
		if (e.attrNameDef() != null) {
			return ImmutableList.of(e.attrNameDef());
		} else {
			return ImmutableList.emptyList();
		}
	}
	
	public static ImmutableList<NameDef> calculate(ExprBinary e) {
		return e.getLeft().attrReadVariables().cons(e.getRight().attrReadVariables());
	}

	public static ImmutableList<NameDef> calculate(ExprCast e) {
		return e.getExpr().attrReadVariables();
	}

	public static ImmutableList<NameDef> calculate(ExprIncomplete e) {
		return ImmutableList.emptyList();
	}

	public static ImmutableList<NameDef> calculate(ExprInstanceOf e) {
		return e.getExpr().attrReadVariables();
	}

	public static ImmutableList<NameDef> calculate(ExprUnary e) {
		return e.getRight().attrReadVariables();
	}

	public static ImmutableList<NameDef> calculate(ExprBoolVal e) {
		return ImmutableList.emptyList();
	}

	public static ImmutableList<NameDef> calculate(ExprIntVal e) {
		return ImmutableList.emptyList();
	}

	public static ImmutableList<NameDef> calculate(ExprNull e) {
		return ImmutableList.emptyList();
	}

	public static ImmutableList<NameDef> calculate(ExprRealVal e) {
		return ImmutableList.emptyList();
	}

	public static ImmutableList<NameDef> calculate(ExprStringVal e) {
		return ImmutableList.emptyList();
	}

	public static ImmutableList<NameDef> calculate(ExprSuper e) {
		return generic(e);
	}

	public static ImmutableList<NameDef> calculate(ExprThis e) {
		return ImmutableList.emptyList();
	}



	public static ImmutableList<NameDef> calculate(ExprFuncRef exprFuncRef) {
		return ImmutableList.emptyList();
	}

	public static ImmutableList<NameDef> calculate(ExprTypeId e) {
		return e.getLeft().attrReadVariables();
	}

	public static ImmutableList<NameDef> calculate(ExprClosure e) {
		return e.getImplementation().attrReadVariables();
	}

	public static ImmutableList<NameDef> calculate(
			ExprStatementsBlock e) {
		// TODO not sure what to do here
		return ImmutableList.emptyList();
	}


	
	
}
