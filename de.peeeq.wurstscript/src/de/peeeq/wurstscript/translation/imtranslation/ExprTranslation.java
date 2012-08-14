package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassIm.JassIm.ImExprs;
import static de.peeeq.wurstscript.jassIm.JassIm.ImFuncRef;
import static de.peeeq.wurstscript.jassIm.JassIm.ImFunctionCall;
import static de.peeeq.wurstscript.jassIm.JassIm.ImIntVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImNull;
import static de.peeeq.wurstscript.jassIm.JassIm.ImOperatorCall;
import static de.peeeq.wurstscript.jassIm.JassIm.ImRealVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStringVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImTupleExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImTupleSelection;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarAccess;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarArrayAccess;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElementWithIndexes;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.EnumMember;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprIncomplete;
import de.peeeq.wurstscript.ast.ExprInstanceOf;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NoExpr;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeInterface;
import de.peeeq.wurstscript.types.PscriptTypeTuple;
import de.peeeq.wurstscript.types.PscriptTypeTypeParam;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;

public class ExprTranslation {

	public static ImExpr translate(ExprBinary e, ImTranslator t, ImFunction f) {
		ImExpr left = e.getLeft().imTranslateExpr(t, f);
		ImExpr right = e.getRight().imTranslateExpr(t, f);
		OpBinary op = e.getOp();
		if (e.attrFuncDef() != null) {
			// overloaded operator
			ImFunction calledFunc = t.getFuncFor(e.attrFuncDef());
			t.addCallRelation(f, calledFunc);
			return JassIm.ImFunctionCall(e, calledFunc, ImExprs(left, right));
		} 
		if (op instanceof OpDivReal && !Utils.isJassCode(op)) {
			if (e.getLeft().attrTyp() instanceof PScriptTypeInt
					&& e.getRight().attrTyp() instanceof PScriptTypeInt) {
				// we want a real division but have 2 ints so we need to multiply with 1.0
				left = ImOperatorCall(Ast.OpMult(), ImExprs(left, ImRealVal("1.")));
			}
		}
		return ImOperatorCall(op, ImExprs(left, right));
	}

	public static ImExpr translate(ExprUnary e, ImTranslator t, ImFunction f) {
		return ImOperatorCall(e.getOpU(), ImExprs(e.getRight().imTranslateExpr(t, f)));
	}
	
	public static ImExpr translate(ExprBoolVal e, ImTranslator t, ImFunction f) {
		return JassIm.ImBoolVal(e.getValB());
	}

	public static ImExpr translate(ExprFuncRef e, ImTranslator t, ImFunction f) {
		ImFunction func = t.getFuncFor(e.attrFuncDef());
		t.addCallRelation(f, func);
		return ImFuncRef(func);
	}

	public static ImExpr translate(ExprIntVal e, ImTranslator t, ImFunction f) {
		if (e.attrExpectedTyp() instanceof PScriptTypeReal) {
			return ImRealVal(e.getValI() + ".");
		}
		return ImIntVal(e.getValI());
	}

	public static ImExpr translate(ExprNull e, ImTranslator t, ImFunction f) {
		ImType imType = e.attrTyp().imTranslateType();
		if (imType instanceof ImSimpleType) {
			ImSimpleType st = (ImSimpleType) imType;
			if (st.getTypename().equals(TypesHelper.imInt().getTypename())) {
				return ImIntVal(0);
			} else {
				return ImNull();
			}
		} else if (imType instanceof ImTupleType) {
			ImTupleType tt = (ImTupleType) imType;
			ImExprs exprs = JassIm.ImExprs();
			for (String $ : tt.getTypes()) {
				exprs.add(ImIntVal(0));
			}
			return ImTupleExpr(exprs);
		}
		throw new Error("unhandled case");
	}

	public static ImExpr translate(ExprRealVal e, ImTranslator t, ImFunction f) {
		return ImRealVal(e.getValR());
	}

	public static ImExpr translate(ExprStringVal e, ImTranslator t, ImFunction f) {
		return ImStringVal(e.getValS());
	}

	public static ImExpr translate(ExprThis e, ImTranslator t, ImFunction f) {
		ImVar var = t.getThisVar(e);
		return ImVarAccess(var);
	}

	public static ImExpr translate(NameRef e, ImTranslator t, ImFunction f) {
		return translateNameDef(e, t, f);
	}

	/** checks whether a interface type is expected for e */
	private static boolean expectedInterfaceType(Expr e) {
		return e.attrExpectedTyp() instanceof PscriptTypeInterface || e.attrExpectedTyp() instanceof PscriptTypeTypeParam;
	}

	private static ImExpr translateNameDef(NameRef e, ImTranslator t, ImFunction f) throws CompileError {
		NameDef decl = e.attrNameDef();
		if (decl instanceof VarDef) {
			VarDef varDef = (VarDef) decl;

			ImVar v = t.getVarFor(varDef);
			
			
			if (e.attrImplicitParameter() instanceof Expr) {
				// we have implicit parameter
				// e.g. "someObject.someField"
				Expr implicitParam = (Expr) e.attrImplicitParameter();
				
				if (implicitParam.attrTyp() instanceof PscriptTypeTuple) {
					PscriptTypeTuple tupleType = (PscriptTypeTuple) implicitParam.attrTyp();
					if (e instanceof ExprMemberVar) {
						ExprMemberVar e2 = (ExprMemberVar) e;
						int tupleIndex = t.getTupleIndex(tupleType.getTupleDef(), varDef);
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
		} else if (decl instanceof EnumMember) {
			EnumMember enumMember = (EnumMember) decl;
			int id = t.getEnumMemberId(enumMember);
			return ImIntVal(id);
		} else {
			throw new CompileError(e.getSource(), "Cannot translate reference to " + decl.getClass().getName());
		}
	}


	public static ImExpr translate(ExprCast e, ImTranslator t, ImFunction f) {
		return e.getExpr().imTranslateExpr(t, f);
	}

	public static ImExpr translate(FunctionCall e, ImTranslator t, ImFunction f) {
		return translateFunctionCall(e, t, f);
	}

	private static ImExpr translateFunctionCall(FunctionCall e, ImTranslator t, ImFunction f) {
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
		
		ImExprs imArgs = translateExprs(arguments, t, f);
		
		if (calledFunc instanceof TupleDef) {
			// creating a new tuple...
			return ImTupleExpr(imArgs);
		}
		ImFunction calledImFunc = t.getFuncFor(calledFunc);
		t.addCallRelation(f, calledImFunc);
		
		
		ImFunctionCall fc = ImFunctionCall(e, calledImFunc, imArgs);
		return fc;
	}

	

	private static ImExprs translateExprs(List<Expr> arguments,  ImTranslator t, ImFunction f) {
		ImExprs result = ImExprs();
		for (Expr e : arguments) {
			result.add(e.imTranslateExpr(t, f));
		}
		return result;
	}

	public static ImExpr translate(ExprIncomplete e, ImTranslator t, ImFunction f) {
		throw new CompileError(e.getSource(), "Incomplete expression.");
	}

	public static ImExpr translate(ExprNewObject e, ImTranslator t, ImFunction f) {
		ConstructorDef constructorFunc = e.attrConstructorDef();
		ImFunction constructorImFunc = t.getConstructNewFunc(constructorFunc);
		t.addCallRelation(f, constructorImFunc);
		return ImFunctionCall(e, constructorImFunc, translateExprs(e.getArgs(), t, f));
	}

	public static ImExprOpt translate(NoExpr e, ImTranslator translator, ImFunction f) {
		return JassIm.ImNoExpr();
	}

	public static ImExpr translate(ExprInstanceOf e, ImTranslator translator, ImFunction f) {
		PscriptType targetType = e.getTyp().attrTyp();
		
		Collection<ClassDef> subTypes = translator.getConcreteSubtypes(targetType);
		if (subTypes.size() == 1) {
			for (ClassDef st : subTypes) {
				int id = translator.getTypeId(st);
				ClassManagementVars cmv = translator.getClassManagementVarsFor(st);
				return JassIm.ImOperatorCall(Ast.OpEquals(), JassIm.ImExprs(
						JassIm.ImVarArrayAccess(cmv.typeId, e.getExpr().imTranslateExpr(translator, f)),
						JassIm.ImIntVal(id)));
			}
			throw new Error();
		} else {
			ImVar tempVar = JassIm.ImVar(TypesHelper.imInt(), "tempInstanceOfExpr", false);
			f.getLocals().add(tempVar);
			
			ImExpr condition = JassIm.ImBoolVal(false);
			for (ClassDef st : subTypes) {
				int id = translator.getTypeId(st);
				ClassManagementVars cmv = translator.getClassManagementVarsFor(st);
				
				ImOperatorCall check = JassIm.ImOperatorCall(Ast.OpEquals(), JassIm.ImExprs(
						JassIm.ImVarArrayAccess(cmv.typeId, JassIm.ImVarAccess(tempVar)),
						JassIm.ImIntVal(id)));
				if (condition instanceof ImBoolVal) {
					condition = check;
				} else {
					condition = JassIm.ImOperatorCall(Ast.OpOr(), JassIm.ImExprs(condition, check));
				}
				
			}
			ImStmt evalExpr = JassIm.ImSet(e, tempVar, e.getExpr().imTranslateExpr(translator, f));
			return JassIm.ImStatementExpr(JassIm.ImStmts(evalExpr), condition);
		}
	}

	

}
