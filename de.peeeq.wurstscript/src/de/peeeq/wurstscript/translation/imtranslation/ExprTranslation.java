package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.PscriptTypeTuple;

public class ExprTranslation {

	public static ImExpr translate(ExprBinary e, Translator t, ImFunction f) {
		ImExpr left = e.getLeft().imTranslateExpr(t, f);
		ImExpr right = e.getRight().imTranslateExpr(t, f);
		ImFunction func = t.getFuncFor(e.getOp());
		return ImCall(func, ImExprs(left, right));
	}

	public static ImExpr translate(ExprUnary e, Translator t, ImFunction f) {
		ImFunction func = t.getFuncFor(e.getOpU());
		return ImCall(func, ImExprs(e.getRight().imTranslateExpr(t, f)));
	}
	
	public static ImExpr translate(ExprBoolVal e, Translator t, ImFunction f) {
		return JassIm.ImBoolVal(e.getValB());
	}

	public static ImExpr translate(ExprFuncRef e, Translator t, ImFunction f) {
		ImFunction func = t.getFuncFor(e.attrFuncDef());
		return ImFuncRef(func);
	}

	public static ImExpr translate(ExprIntVal e, Translator t, ImFunction f) {
		return ImIntVal(e.getValI());
	}

	public static ImExpr translate(ExprNull e, Translator t, ImFunction f) {
		return ImNull();
	}

	public static ImExpr translate(ExprRealVal e, Translator t, ImFunction f) {
		return ImRealVal(e.getValR());
	}

	public static ImExpr translate(ExprStringVal e, Translator t, ImFunction f) {
		return ImStringVal(e.getValS());
	}

	public static ImExpr translate(ExprThis e, Translator t, ImFunction f) {
		ImVar var = t.getThisVar(e.attrNearestScope());
		return ImVarAccess(var);
	}

	public static ImExpr translate(NameRef e, Translator t, ImFunction f) {
		// TODO deal with nested tuples ...
		NameDef decl = e.attrNameDef();
		if (decl instanceof VarDef) {
			VarDef varDef = (VarDef) decl;

			ImVar v = t.getVarFor(varDef);
			
			
			if (e.attrImplicitParameter() instanceof Expr) {
				// we have implicit parameter
				// e.g. "someObject.someField"
				Expr implicitParam = (Expr) e.attrImplicitParameter();
				
				if (implicitParam.attrTyp() instanceof PscriptTypeTuple) {
					if (e instanceof ExprMemberVar) {
						ExprMemberVar e2 = (ExprMemberVar) e;
						int tupleIndex = t.getTupleIndex(varDef);
						return ImTupleSelection(e2.getLeft().imTranslateExpr(t, f), tupleIndex);
					} else {
						throw new CompileError(e.getSource(), "Cannot create tuple access");
					}
				}
				
				if (e instanceof AstElementWithIndexes) {
					throw new CompileError(e.getSource(), "Member array variables are not supported.");
				} else {
					ImExpr index = implicitParam.imTranslateExpr(t, f);
					return ImVarArrayAccess(v, index);
				}
			} else {
				// direct var access
				if (e instanceof AstElementWithIndexes) {
					// direct access array var
					AstElementWithIndexes withIndexes = (AstElementWithIndexes) e;
					if (withIndexes.getIndexes().size() > 1) {
						throw new CompileError(e.getSource(), "More than one index is not supported.");
					}
					ImExpr index = withIndexes.getIndexes().get(0).imTranslateExpr(t, f);
					return ImVarArrayAccess(v, index);
				} else {
					// not an array var
					return ImVarAccess(v);

				}
			}
		} else {
			throw new CompileError(e.getSource(), "Cannot translate reference to " + decl.getClass().getName());
		}
	}


	public static ImExpr translate(ExprCast e, Translator t, ImFunction f) {
		return e.getExpr().imTranslateExpr(t, f);
	}

	public static ImExpr translate(FunctionCall e, Translator t, ImFunction f) {
		List<Expr> arguments = Lists.newArrayList(e.getArgs());
		if (e.attrImplicitParameter() instanceof Expr) {
			// add implicit parameter to front
			arguments.add(0, (Expr) e.attrImplicitParameter());
		}

		FunctionDefinition calledFunc = e.attrFuncDef();
		if (calledFunc == null) {
			// this must be an ignored function
			return ImNull();
		}
		
		ImExprs imArgs = ImExprs(translateExprs(arguments, t, f));
		
		if (calledFunc instanceof TupleDef) {
			// creating a new tuple...
			return ImTupleExpr(imArgs);
		}
		ImFunction calledImFunc = t.getFuncFor(calledFunc);

		return ImCall(calledImFunc, imArgs);
	}

	private static ImExprs translateExprs(List<Expr> arguments,  Translator t, ImFunction f) {
		ImExprs result = ImExprs();
		for (Expr e : arguments) {
			result.add(e.imTranslateExpr(t, f));
		}
		return result;
	}

	public static ImExpr translate(ExprIncomplete e, Translator t, ImFunction f) {
		throw new CompileError(e.getSource(), "Incomplete expression.");
	}

	public static ImExpr translate(ExprNewObject e, Translator t, ImFunction f) {
		ConstructorDef constructorFunc = e.attrConstructorDef();
		ImFunction constructorImFunc = t.getConstructorFuncFor(constructorFunc);
		return ImCall(constructorImFunc, translateExprs(e.getArgs(), t, f));
	}

	public static ImExprOpt translate(NoExpr e, Translator translator, ImFunction f) {
		return JassIm.ImNoExpr();
	}

	

}
