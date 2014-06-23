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

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.AstElementWithIndexes;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.EnumMember;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprDestroy;
import de.peeeq.wurstscript.ast.ExprEmpty;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprIncomplete;
import de.peeeq.wurstscript.ast.ExprInstanceOf;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberMethodDotDot;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStatementsBlock;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprSuper;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExprTypeId;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NoExpr;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TranslatedToImFunction;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ImplicitFuncs;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeModuleInstanciation;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.types.WurstTypeTuple;
import de.peeeq.wurstscript.utils.Utils;

public class ExprTranslation {

	public static ImExpr translate(ExprBinary e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprUnary e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprBoolVal e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprFuncRef e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprIntVal e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprNull e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprRealVal e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprStringVal e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprThis e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprSuper e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(NameRef e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprCast e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(FunctionCall e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprIncomplete e, ImTranslator t,
			ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprNewObject e, ImTranslator t, ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}

	public static ImExpr translate(ExprInstanceOf e, ImTranslator t,
			ImFunction f) {
		return wrapTranslation(e, t, f, translateIntern(e, t, f));
	}
	
	
	private static ImExpr wrapTranslation(Expr e, ImTranslator t,
			ImFunction f, ImExpr translated) {
		WurstType actualType = e.attrTypRaw();
		ImFunction toIndex = null;
		ImFunction fromIndex = null;
		if (actualType instanceof WurstTypeBoundTypeParam) {
			WurstTypeBoundTypeParam wtb = (WurstTypeBoundTypeParam) actualType;
			if (!wtb.getBaseType().supportsGenerics()) {
				// if we have a generic type, convert it to the original type using the fromIndex func
				fromIndex = t.getFuncFor(ImplicitFuncs.findFromIndexFunc(wtb.getBaseType(), e));
			}
		} 
		if (e.attrExpectedTypRaw() instanceof WurstTypeBoundTypeParam) {
			if (!actualType.supportsGenerics()) {
				// if we expect a generic type but have something different, use the toIndex func
				toIndex =  t.getFuncFor(ImplicitFuncs.findToIndexFunc(actualType, e));
			}
		}
		
		if (toIndex != null && fromIndex != null) {
			// the two conversions cancel each other out
			return translated;
		} else if (fromIndex != null) {
			return JassIm.ImFunctionCall(e, fromIndex, JassIm.ImExprs(translated), false, CallType.NORMAL);
		} else if (toIndex != null) {
			return JassIm.ImFunctionCall(e, toIndex, JassIm.ImExprs(translated), false, CallType.NORMAL);
		}
		return translated;
	}

	public static ImExpr translateIntern(ExprBinary e, ImTranslator t, ImFunction f) {
		ImExpr left = e.getLeft().imTranslateExpr(t, f);
		ImExpr right = e.getRight().imTranslateExpr(t, f);
		WurstOperator op = e.getOp();
		if (e.attrFuncDef() != null) {
			// overloaded operator
			ImFunction calledFunc = t.getFuncFor(e.attrFuncDef());
			return JassIm.ImFunctionCall(e, calledFunc, ImExprs(left, right), false, CallType.NORMAL);
		} 
		if (op == WurstOperator.DIV_REAL) {
			if (Utils.isJassCode(e)) {
				if (e.getLeft().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)
						&& e.getRight().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)) {
					// in jass when we have int1 / int2 this actually means int1 div int2
					op = WurstOperator.DIV_INT;
				}
			} else {
				if (e.getLeft().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)
						&& e.getRight().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)) {
					// we want a real division but have 2 ints so we need to multiply with 1.0
					// TODO is this really needed or handled in IM->Jass translation?
					left = ImOperatorCall(WurstOperator.MULT, ImExprs(left, ImRealVal("1.")));
				}
			}
		}
		return ImOperatorCall(op, ImExprs(left, right));
	}

	public static ImExpr translateIntern(ExprUnary e, ImTranslator t, ImFunction f) {
		return ImOperatorCall(e.getOpU(), ImExprs(e.getRight().imTranslateExpr(t, f)));
	}
	
	public static ImExpr translateIntern(ExprBoolVal e, ImTranslator t, ImFunction f) {
		return JassIm.ImBoolVal(e.getValB());
	}

	public static ImExpr translateIntern(ExprFuncRef e, ImTranslator t, ImFunction f) {
		ImFunction func = t.getFuncFor(e.attrFuncDef());
		return ImFuncRef(func);
	}

	public static ImExpr translateIntern(ExprIntVal e, ImTranslator t, ImFunction f) {
		if (e.attrExpectedTyp() instanceof WurstTypeReal) {
			// translate differently when real is expected
			return ImRealVal(e.getValI()+".");
		}
		
		return ImIntVal(e.getValI());
	}

	public static ImExpr translateIntern(ExprNull e, ImTranslator t, ImFunction f) {
		WurstType expectedTypeRaw = e.attrExpectedTypRaw();
		if (expectedTypeRaw.isTranslatedToInt()) {
			return ImIntVal(0);
		}
		return ImNull();
	}

	public static ImExpr translateIntern(ExprRealVal e, ImTranslator t, ImFunction f) {
		return ImRealVal(e.getValR());
	}

	public static ImExpr translateIntern(ExprStringVal e, ImTranslator t, ImFunction f) {
		return ImStringVal(e.getValS());
	}

	public static ImExpr translateIntern(ExprThis e, ImTranslator t, ImFunction f) {
		ImVar var = t.getThisVar(f, e);
		return ImVarAccess(var);
	}
	
	public static ImExpr translateIntern(ExprSuper e, ImTranslator t, ImFunction f) {
		ImVar var = t.getThisVar(f, e);
		return ImVarAccess(var);
	}

	public static ImExpr translateIntern(NameRef e, ImTranslator t, ImFunction f) {
		return translateNameDef(e, t, f);
	}

	private static ImExpr translateNameDef(NameRef e, ImTranslator t, ImFunction f) throws CompileError {
		NameDef decl = e.attrNameDef();
		if (decl == null) {
			// should only happen with gg_ variables
			if (!t.isEclipseMode()) {
				e.addError("Translation Error: Could not find definition of " + e.getVarName() + ".");
			}
			return ImNull();
		} if (decl instanceof VarDef) {
			VarDef varDef = (VarDef) decl;

			ImVar v = t.getVarFor(varDef);
			
			
			if (e.attrImplicitParameter() instanceof Expr) {
				// we have implicit parameter
				// e.g. "someObject.someField"
				Expr implicitParam = (Expr) e.attrImplicitParameter();
				
				if (implicitParam.attrTyp() instanceof WurstTypeTuple) {
					WurstTypeTuple tupleType = (WurstTypeTuple) implicitParam.attrTyp();
					if (e instanceof ExprMemberVar) {
						ExprMemberVar e2 = (ExprMemberVar) e;
						int tupleIndex = t.getTupleIndex(tupleType.getTupleDef(), varDef);
						return ImTupleSelection(e2.getLeft().imTranslateExpr(t, f), tupleIndex);
					} else {
						throw new CompileError(e.getSource(), "Cannot create tuple access");
					}
				}
				
				if (e instanceof AstElementWithIndexes) {
					ImExpr index1 = implicitParam.imTranslateExpr(t, f);
					ImExpr index2 = ((AstElementWithIndexes) e).getIndexes().get(0).imTranslateExpr(t, f);
					return JassIm.ImVarArrayMultiAccess(v, index1, index2);

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
			throw new CompileError(e.getSource(), "Cannot translate reference to " + Utils.printElement(decl));
		}
	}


	public static ImExpr translateIntern(ExprCast e, ImTranslator t, ImFunction f) {
		return e.getExpr().imTranslateExpr(t, f);
	}

	public static ImExpr translateIntern(FunctionCall e, ImTranslator t, ImFunction f) {
		if (e instanceof ExprMemberMethodDotDot) {
			return translateFunctionCall(e, t, f, true);
		} else {
			return translateFunctionCall(e, t, f, false);
		}
	}

	private static ImExpr translateFunctionCall(FunctionCall e, ImTranslator t, ImFunction f, boolean returnReveiver) {
		
		
		if (e.getFuncName().equals("error") 
				&& e.attrImplicitParameter() instanceof NoExpr
				&& e.getArgs().size() == 1
				&& e.getArgs().get(0).attrTyp().isSubtypeOf(WurstTypeString.instance(), e)) {
			// special built-in error function
			return JassIm.ImStatementExpr(JassIm.ImStmts(
					JassIm.ImError(e.getArgs().get(0).imTranslateExpr(t, f))),
					JassIm.ImNull());
		}
		
		if (e.getFuncName().equals("ExecuteFunc")) {
			ExprStringVal s = (ExprStringVal) e.getArgs().get(0);
			String exFunc = s.getValS();
			NameLink func = Utils.getFirst(e.lookupFuncs(exFunc));
			ImFunction executedFunc = t.getFuncFor((TranslatedToImFunction) func.getNameDef());
			return JassIm.ImFunctionCall(e, executedFunc, JassIm.ImExprs(), true, CallType.EXECUTE);
		}
		
		List<Expr> arguments = Lists.newArrayList(e.getArgs());
		Expr leftExpr = null;
		boolean dynamicDispatch = false;

		FunctionDefinition calledFunc = e.attrFuncDef();
		
		if (e.attrImplicitParameter() instanceof Expr) {
			if (isCalledOnDynamicRef(e) && calledFunc instanceof FuncDef) {
				dynamicDispatch = true;	
			}
			// add implicit parameter to front
			// TODO why would I add the implicit parameter here, if it is
			// not a dynamic dispatch?
			leftExpr = (Expr) e.attrImplicitParameter();
		}

		
		
		
		
		
		// get real func def (override of module function)
		boolean useRealFuncDef = true;
		if (e instanceof ExprMemberMethod) {
			ExprMemberMethod exprMemberMethod = (ExprMemberMethod) e;
			WurstType left = exprMemberMethod.getLeft().attrTyp();
			if (left instanceof WurstTypeModuleInstanciation) {
				// if we have a call like A.foo() and A is a module,
				// use this function
				useRealFuncDef = false;
			}
		}
		
		if (calledFunc == null) {
			// this must be an ignored function
			return ImNull();
		}
		
		if (useRealFuncDef) {
			calledFunc = calledFunc.attrRealFuncDef();
		}
		
		if (calledFunc == e.attrNearestFuncDef()) {
			// recursive self calls are bound statically
			// this is different to other objectoriented languages but it is necessary 
			// because jass does not allow mutually recursive calls
			// The only situation where this would make a difference is with super-calls 
			// (or other statically bound calls)
			dynamicDispatch = false;
		}
		
		ImExpr receiver = leftExpr == null ? null : leftExpr.imTranslateExpr(t, f);
		ImExprs imArgs = translateExprs(arguments, t, f);
		
		if (calledFunc instanceof TupleDef) {
			// creating a new tuple...
			return ImTupleExpr(imArgs);
		}
		
		ImStmts stmts = null;
		ImVar tempVar = null;
		if (returnReveiver) {
			if (leftExpr == null) throw new Error("impossible");
			tempVar = JassIm.ImVar(leftExpr, leftExpr.attrTyp().imTranslateType(), "receiver", false);
			f.getLocals().add(tempVar);
			stmts = JassIm.ImStmts(
					JassIm.ImSet(e, tempVar, receiver)
					);
			receiver = JassIm.ImVarAccess(tempVar);
		}
		
		
		ImExpr call;
		if (dynamicDispatch) {
			ImMethod method = t.getMethodFor((FuncDef) calledFunc);
			call = JassIm.ImMethodCall(e, method, receiver, imArgs, false);
		} else {
			ImFunction calledImFunc = t.getFuncFor(calledFunc);
			if (receiver != null) {
				imArgs.add(0, receiver);
			}
			call = ImFunctionCall(e, calledImFunc, imArgs, false, CallType.NORMAL);
		}
		
		if (returnReveiver) {
			if (stmts == null) throw new Error("impossible");
			stmts.add(call);
			return JassIm.ImStatementExpr(stmts, JassIm.ImVarAccess(tempVar));
		} else {
			return call;
		}
	}

	
	private static boolean isCalledOnDynamicRef(FunctionCall e) {
		if (e instanceof ExprMemberMethod) {
			ExprMemberMethod mm = (ExprMemberMethod) e;
			return mm.getLeft().attrTyp().allowsDynamicDispatch();
		} else if (e.attrIsDynamicContext()) {
			return true;
		}
		return false;
	}

	

	private static ImExprs translateExprs(List<Expr> arguments,  ImTranslator t, ImFunction f) {
		ImExprs result = ImExprs();
		for (Expr e : arguments) {
			result.add(e.imTranslateExpr(t, f));
		}
		return result;
	}

	public static ImExpr translateIntern(ExprIncomplete e, ImTranslator t, ImFunction f) {
		throw new CompileError(e.getSource(), "Incomplete expression.");
	}

	public static ImExpr translateIntern(ExprNewObject e, ImTranslator t, ImFunction f) {
		ConstructorDef constructorFunc = e.attrConstructorDef();
		ImFunction constructorImFunc = t.getConstructNewFunc(constructorFunc);
		return ImFunctionCall(e, constructorImFunc, translateExprs(e.getArgs(), t, f), false, CallType.NORMAL);
	}

	public static ImExprOpt translate(NoExpr e, ImTranslator translator, ImFunction f) {
		return JassIm.ImNoExpr();
	}

	public static ImExpr translateIntern(ExprInstanceOf e, ImTranslator translator, ImFunction f) {
		WurstType targetType = e.getTyp().attrTyp();
		if (targetType instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope t = (WurstTypeNamedScope) targetType;
			ImClass clazz = translator.getClassFor((StructureDef) t.getDef());
			return JassIm.ImInstanceof(e.getExpr().imTranslateExpr(translator, f), clazz);
		}
		throw new Error("Cannot compile instanceof " + targetType);
	}

	public static ImExpr translate(ExprTypeId e, ImTranslator translator, ImFunction f) {
		WurstType leftType = e.getLeft().attrTyp();
		if (leftType instanceof WurstTypeClassOrInterface) {
			WurstTypeClassOrInterface wtc = (WurstTypeClassOrInterface) leftType;
			
			ImClass c = translator.getClassFor(wtc.getDef());
			if (wtc.isStaticRef()) {
				return JassIm.ImTypeIdOfClass(c);
			} else {
				return JassIm.ImTypeIdOfObj(e.getLeft().imTranslateExpr(translator, f), c);
			}
		} else {
			throw new Error("not implemented for " + leftType);
		}
	}

	public static ImExpr translate(ExprClosure e, ImTranslator tr, ImFunction f) {
		return new ClosureTranslator(e, tr, f).translate();
	}

	public static ImExpr translate(ExprStatementsBlock e,
			ImTranslator translator, ImFunction f) {
		
		ImStmts statements = JassIm.ImStmts(); 
		for (WStatement s : e.getBody()) {
			if (s instanceof StmtReturn) {
				continue;
			}
			ImStmt translated = s.imTranslateStmt(translator, f);
			statements.add(translated);
		}
		
		ImExprOpt expr = null;
		StmtReturn r = e.getReturnStmt();
		if (r != null && r.getReturnedObj() instanceof Expr) {
			expr = ((Expr) r.getReturnedObj()).imTranslateExpr(translator, f);
		}
		if (expr instanceof ImExpr) {
			return JassIm.ImStatementExpr(statements, (ImExpr) expr);
		} else {
			return JassIm.ImStatementExpr(statements, JassIm.ImNull());
		}
	}

	public static ImExpr translate(ExprDestroy s, ImTranslator t, ImFunction f) {
		WurstType typ = s.getDestroyedObj().attrTyp();
		if (typ instanceof WurstTypeClass) {
			WurstTypeClass classType = (WurstTypeClass) typ;
			return destroyClass(s, t, f, classType.getClassDef());
		} else if (typ instanceof WurstTypeInterface) {
			WurstTypeInterface wti = (WurstTypeInterface) typ;
			return destroyClass(s, t, f, wti.getDef());
		} else if (typ instanceof WurstTypeModuleInstanciation) {
			WurstTypeModuleInstanciation minsType = (WurstTypeModuleInstanciation) typ;
			ClassDef classDef = minsType.getDef().attrNearestClassDef();
			return destroyClass(s, t, f, classDef);
		}
		// TODO destroy interfaces?
		throw new CompileError(s.getSource(), "cannot destroy object of type " + typ);
	}

	
	public static ImExpr destroyClass(ExprDestroy s, ImTranslator t,
			ImFunction f, StructureDef classDef) {
		ImMethod destroyFunc = t.destroyMethod.getFor(classDef);
		return JassIm.ImMethodCall(s, destroyFunc, s.getDestroyedObj().imTranslateExpr(t, f), ImExprs(), false);
	}

	public static ImExpr translate(ExprEmpty s,	ImTranslator translator, ImFunction f) {
		throw new CompileError(s.getSource(), "cannot translate empty expression");
	}

	

}
