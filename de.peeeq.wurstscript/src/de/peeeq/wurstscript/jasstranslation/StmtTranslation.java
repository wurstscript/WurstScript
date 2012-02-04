package de.peeeq.wurstscript.jasstranslation;

import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprNull;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprUnary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarArrayAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprlist;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreater;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMinus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpNot;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpPlus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStatements;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtExitwhen;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtIf;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtLoop;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturn;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturnVoid;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSet;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSetArray;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithIndexes;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.OpAssignment;
import de.peeeq.wurstscript.ast.StmtCall;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.StmtExitwhen;
import de.peeeq.wurstscript.ast.StmtForFrom;
import de.peeeq.wurstscript.ast.StmtForIn;
import de.peeeq.wurstscript.ast.StmtForRange;
import de.peeeq.wurstscript.ast.StmtForRangeDown;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprAtomic;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeInterface;
import de.peeeq.wurstscript.types.PscriptTypeModuleInstanciation;
import de.peeeq.wurstscript.types.PscriptTypeTuple;
import de.peeeq.wurstscript.utils.Utils;

public class StmtTranslation {

	public static List<JassStatement> translate(LocalVarDef localVarDef, JassTranslator translator, JassFunction f) {
		f.getLocals().addAll(translator.manager.getJassVarsFor(localVarDef));

		List<JassStatement> result = Lists.newArrayList();
		if (localVarDef.getInitialExpr() instanceof Expr) {
			Expr initalExpr = (Expr) localVarDef.getInitialExpr();
			ExprTranslationResult newValue = initalExpr.jassTranslateExpr(translator, f);
			// translateAssignmentNoIndex(result, f, localVarDef, null,
			// newValue);
			translateLocalVardefAssignment(translator, result, f, localVarDef, initalExpr);
		}
		return result;
	}

	public static List<JassStatement> translate(StmtCall s, JassTranslator translator, JassFunction f) {
		List<JassStatement> result = Lists.newArrayList();
		if (s instanceof Expr) {
			Expr expr = (Expr) s;
			ExprTranslationResult t = expr.jassTranslateExpr(translator, f);
			result.addAll(t.getStatements());
			for (JassExpr e1 : t.getExpressions()) {
				if (e1 instanceof JassExprFunctionCall) {
					JassExprFunctionCall call = (JassExprFunctionCall) e1;
					result.add(JassStmtCall(call.getFuncName(), call.getArguments().copy()));
				} else {
					// we can ignore any other case because we will not need the
					// result of the expression
				}
			}
		}
		return result;
	}

	public static List<JassStatement> translate(StmtDestroy s, JassTranslator translator, JassFunction f) {
		PscriptType typ = s.getDestroyedObj().attrTyp();
		if (typ instanceof PscriptTypeClass) {
			PscriptTypeClass classType = (PscriptTypeClass) typ;
			ClassDef classDef = classType.getClassDef();
			return callDestroyFunc(translator, f, classDef, s.getDestroyedObj());
		} else if (typ instanceof PscriptTypeModuleInstanciation) {
			ClassDef classDef = translator.getClassDef((PscriptTypeModuleInstanciation) typ);
			return callDestroyFunc(translator, f, classDef, s.getDestroyedObj());
		} else {
			// TODO destroy interfaces?
			throw new Error("cannot destroy object of type " + typ);
		}
	}

	private static List<JassStatement> callDestroyFunc(JassTranslator translator, JassFunction f, ClassDef classDef, Expr e) {
		JassFunction destroyMethod = translator.manager.getJassDestroyFunctionFor(classDef);
		translator.calledFunctions.put(f, destroyMethod);
		ExprTranslationResult toDestroy = e.jassTranslateExpr(translator, f);
		List<JassStatement> result = Lists.newArrayList();
		result.addAll(toDestroy.getStatements());
		result.add(JassStmtCall(destroyMethod.getName(), JassExprlist(toDestroy.getExprSingle())));
		return result;
	}

	public static List<JassStatement> translate(StmtErr s, JassTranslator translator, JassFunction f) {
		throw new CompileError(s.getSource(), "Source contains errors.");
	}

	public static List<JassStatement> translate(StmtExitwhen s, JassTranslator translator, JassFunction f) {
		ExprTranslationResult e = s.getCond().jassTranslateExpr(translator, f);
		List<JassStatement> result = Lists.newArrayList();
		result.addAll(e.getStatements());
		result.add(JassStmtExitwhen(e.getExprSingle()));
		return result;
	}

	public static List<JassStatement> translate(StmtForFrom s, JassTranslator translator, JassFunction f) {
		throw new CompileError(s.getSource(), "Syntactic sugar, for from.");
	}

	public static List<JassStatement> translate(StmtForIn s, JassTranslator translator, JassFunction f) {
		throw new CompileError(s.getSource(), "Syntactic sugar, for in.");
	}

	public static List<JassStatement> translate(StmtForRange s, JassTranslator translator, JassFunction f) {
		return case_StmtForRange(translator, f, s.getLoopVar(), s.getFrom(), s.getTo(), s.getStep(), s.getBody(), JassOpPlus(),
				JassOpGreater());
	}

	public static List<JassStatement> translate(StmtForRangeDown s, JassTranslator translator, JassFunction f) {
		return case_StmtForRange(translator, f, s.getLoopVar(), s.getFrom(), s.getTo(), s.getStep(), s.getBody(),
				JassOpMinus(), JassAst.JassOpLess());
	}

	private static List<JassStatement> case_StmtForRange(JassTranslator translator, JassFunction f, LocalVarDef loopVar,
			Expr from, Expr to, Expr step, WStatements body, JassOpBinary opInc, JassOpBinary op) {
		JassVar jassLoopVar = translator.manager.getJassVarsFor(loopVar).get(0);
		f.getLocals().add(jassLoopVar);

		ExprTranslationResult fromExpr = from.jassTranslateExpr(translator, f);
		List<JassStatement> result = Lists.newArrayList();
		result.add(JassStmtSet(jassLoopVar.getName(), fromExpr.getExprSingle()));

		JassExpr toExpr = addCacheVariableSmart(translator, f, result, to, fromExpr);
		JassExpr stepExpr = addCacheVariableSmart(translator, f, result, step, fromExpr);

		JassStatements jassBody = JassStatements();
		jassBody.add(JassStmtExitwhen(JassExprBinary(JassExprVarAccess(jassLoopVar.getName()), op, toExpr)));
		jassBody.addAll(translator.translateStatements(f, body));
		jassBody.add(JassStmtSet(jassLoopVar.getName(),
				JassExprBinary(JassExprVarAccess(jassLoopVar.getName()), opInc, stepExpr)));
		result.add(JassStmtLoop(jassBody));
		return result;
	}

	public static List<JassStatement> translate(StmtIf stmtIf, JassTranslator translator, JassFunction f) {
		ExprTranslationResult cond = stmtIf.getCond().jassTranslateExpr(translator, f);
		List<JassStatement> thenBlock = translator.translateStatements(f, stmtIf.getThenBlock());
		List<JassStatement> elseBlock = translator.translateStatements(f, stmtIf.getElseBlock());
		List<JassStatement> result = Lists.newArrayList();
		result.addAll(cond.getStatements());
		result.add(JassStmtIf(cond.getExprSingle(), JassStatements(thenBlock), JassStatements(elseBlock)));
		return result;
	}

	public static List<JassStatement> translate(StmtLoop s, JassTranslator translator, JassFunction f) {
		List<JassStatement> body = translator.translateStatements(f, s.getBody());
		return Collections.<JassStatement> singletonList(JassStmtLoop(JassStatements(body)));
	}

	public static List<JassStatement> translate(StmtReturn stmtReturn, JassTranslator translator, JassFunction f) {
		List<JassStatement> result = Lists.newArrayList();
		if (stmtReturn.getReturnedObj() instanceof Expr) {
			Expr expr = (Expr) stmtReturn.getReturnedObj();
			ExprTranslationResult e = expr.jassTranslateExpr(translator, f);
			result.addAll(e.getStatements());

			Set<LocalVarDef> usedLocalVars = translator.getUsedLocalVarsInExpr(expr);
			List<JassVar> usedLocalJassVars = Lists.newArrayList();

			for (LocalVarDef v : usedLocalVars) {
				usedLocalJassVars.addAll(translator.manager.getJassVarsFor(v));
			}
			List<JassStatement> nullSetters = Lists.newArrayList();
			boolean useTempVar = false;
			for (JassVar l : f.getLocals()) {
				if (translator.handleSubTypes.contains(l.getType())) {
					// this is a handle type -> null it
					nullSetters.add(JassStmtSet(l.getName(), JassExprNull()));

					// check if this var is used in the return statement...
					if (usedLocalJassVars.contains(l)) {
						useTempVar = true;
					}
				}
			}

			PscriptType retTyp = stmtReturn.attrNearestFuncDef().getReturnTyp().attrTyp();
			String[] returnedTypes = retTyp.jassTranslateType();

			e = inferMissingTypes(translator, expr, retTyp, expr.attrTyp(), e);

			if (returnedTypes.length != e.exprCount()) {
				throw new CompileError(stmtReturn.getSource(), "length does not match: " + returnedTypes.length + " != "
						+ e.exprCount());
			}

			if (returnedTypes.length > 1) {
				JassVar tempVar = translator.manager.getTempReturnVar(returnedTypes[0], translator.prog);
				result.add(JassStmtSet(tempVar.getName(), e.getExpressions().get(0)));
				for (int i = 1; i < returnedTypes.length; i++) {
					JassVar tempVar2 = translator.manager.getTupleReturnVar(returnedTypes[i], i);
					result.add(JassStmtSet(tempVar2.getName(), e.getExpressions().get(i)));
				}
				result.addAll(nullSetters);
				result.add(JassStmtReturn(JassExprVarAccess(tempVar.getName())));
			} else if (useTempVar) {

				JassVar tempVar = translator.manager.getTempReturnVar(returnedTypes[0], translator.prog);
				result.add(JassStmtSet(tempVar.getName(), e.getExprSingle()));
				result.addAll(nullSetters);
				result.add(JassStmtReturn(JassExprVarAccess(tempVar.getName())));
			} else {
				result.addAll(nullSetters);
				result.add(JassStmtReturn(e.getExprSingle())); // TODO
			}
		} else {
			// set handle variables to null
			for (JassVar l : f.getLocals()) {
				if (translator.handleSubTypes.contains(l.getType())) {
					result.add(JassStmtSet(l.getName(), JassExprNull()));
				}
			}
			result.add(JassStmtReturnVoid());
		}
		return result;
	}

	public static List<JassStatement> translate(StmtSet stmtSet, JassTranslator translator, JassFunction f) {
		Expr newValue = stmtSet.getRight();
		NameRef updatedExpr = stmtSet.getUpdatedExpr();
		OpAssignment opAssign = stmtSet.getOpAssign();
		final JassOpBinary binaryOp = opAssign.jassTranslageAssignGetBinary();
		List<JassStatement> result = Lists.newArrayList();
		translateAssignment(translator, result, f, updatedExpr, binaryOp, newValue);
		return result;
	}

	public static List<JassStatement> translate(StmtWhile s, JassTranslator translator, JassFunction f) {
		ExprTranslationResult cond = s.getCond().jassTranslateExpr(translator, f);

		JassStatements body = JassStatements();
		// ==> exitwhen not cond
		body.addAll(cond.getStatements());
		body.add(JassStmtExitwhen(JassExprUnary(JassOpNot(), cond.getExprSingle())));

		body.addAll(translator.translateStatements(f, s.getBody()));

		return Collections.<JassStatement> singletonList(JassStmtLoop(body));
	}

	/**
	 * stores the value of expression e in a temporary variable if
	 * 
	 * @return the expression e if e was constant or the VarAccess to the
	 *         cacheVariable
	 */
	private static JassExpr addCacheVariableSmart(JassTranslator translator, final JassFunction f,
			final List<JassStatement> result, Expr e, ExprTranslationResult fromExpr) {
		ExprTranslationResult er = e.jassTranslateExpr(translator, f);
		result.addAll(fromExpr.getStatements());
		JassExpr r;
		if (er.getStatements().size() == 0 && er.getExprSingle() instanceof JassExprAtomic) {
			r = er.getExprSingle();
		} else {
			JassVar loopEndVar = translator.getNewTempVar(f, "integer");
			result.addAll(er.getStatements());
			result.add(JassStmtSet(loopEndVar.getName(), er.getExprSingle()));
			r = JassExprVarAccess(loopEndVar.getName());
		}
		return r;
	}

	private static void translateAssignment(JassTranslator translator, final List<JassStatement> result, final JassFunction f,
			NameRef updatedExpr, final JassOpBinary binaryOp, Expr newValue) throws CompileError {

		ExprTranslationResult left = updatedExpr.jassTranslateExpr(translator, f);
		ExprTranslationResult right = newValue.jassTranslateExpr(translator, f);

		result.addAll(left.getStatements());
		result.addAll(right.getStatements());

		if (left.exprCount() != right.exprCount()) {
			throw new CompileError(updatedExpr.getSource(), "Left side has " + left.exprCount()
					+ " jass variables, but right side has " + right.exprCount() + " jass expressions.");
		}

		for (int i = 0; i < left.exprCount(); i++) {
			JassExpr l = left.getExpressions().get(i);
			if (l instanceof JassExprVarAccess) {
				JassExprVarAccess l2 = (JassExprVarAccess) l;
				if (binaryOp == null) {
					result.add(JassStmtSet(l2.getVarName(), right.getExpressions().get(i)));
				} else {
					result.add(JassStmtSet(l2.getVarName(),
							JassExprBinary(JassExprVarAccess(l2.getVarName()), binaryOp, right.getExpressions().get(i))));
				}
			} else if (l instanceof JassExprVarArrayAccess) {
				JassExprVarArrayAccess l2 = (JassExprVarArrayAccess) l;

				if (binaryOp == null) {
					result.add(JassStmtSetArray(l2.getVarName(), (JassExpr) l2.getIndex().copy(), right.getExpressions().get(i)));
				} else {
					JassExpr indexExpr = smartStoreToTemp(translator, result, f, l2.getIndex());
					result.add(JassStmtSetArray(
							l2.getVarName(),
							 (JassExpr) indexExpr.copy(),
							JassExprBinary(JassExprVarArrayAccess(l2.getVarName(), (JassExpr) indexExpr.copy()), binaryOp,
									right.getExpressions().get(i))));
				}

			} else {
				throw new CompileError(updatedExpr.getSource(), "Left side translates to " + l + " ( part " + i + " )");
			}
		}

	}

	private static void translateLocalVardefAssignment(JassTranslator translator,	final List<JassStatement> result, final JassFunction f, VarDef updatedVar
			,Expr newValue) throws CompileError {

		
		
		PscriptType varTyp = updatedVar.attrTyp();
		PscriptType rightTyp = newValue.attrTyp();
		
		
		
		ExprTranslationResult right = newValue.jassTranslateExpr(translator, f);
		
		right = inferMissingTypes(translator, newValue, varTyp, rightTyp, right);
		
		translateAssignment2(translator, result, f, updatedVar, null, right);
		
		
		
		
	}
	
	public static void translateAssignment2(JassTranslator translator, final List<JassStatement> result, final JassFunction f, VarDef updatedVar,
				ExprTranslationResult indexExpr, ExprTranslationResult right) {
		List<JassVar> leftJassVars = translator.manager.getJassVarsFor(updatedVar);
		
		if (right.exprCount() > leftJassVars.size()) {
			// if we have to many expressions on the right we can just remove unnecessary ones...
			right = new ExprTranslationResult(right.getStatements(), Utils.slice(right.getExpressions(), 0, leftJassVars.size()));
		}
		
		if (leftJassVars.size() != right.exprCount()) {
			throw new CompileError(updatedVar.getSource(), "Cannot assig expr with size " + right.exprCount() + " to var with size " + leftJassVars.size());
		}
		result.addAll(right.getStatements());
		
		if (indexExpr != null) {
			result.addAll(indexExpr.getStatements());
		}
		
		for (int i = 0; i<leftJassVars.size(); i++) {
			if (indexExpr == null) {
				result.add(JassStmtSet(leftJassVars.get(i).getName(), right.getExpressions().get(i)));
			} else {
				JassExpr index = indexExpr.getExpressions().get(i);
				index = smartStoreToTemp(translator, result, f, index);
				result.add(JassStmtSetArray(leftJassVars.get(i).getName(), index, right.getExpressions().get(i)));
			}
		}
	}
	

	private static ExprTranslationResult inferMissingTypes(JassTranslator translator, AstElement where, PscriptType neededTyp,
			PscriptType actualTyp, ExprTranslationResult expr) {
		if (neededTyp instanceof PscriptTypeInterface && actualTyp instanceof PscriptTypeClass) {
			PscriptTypeClass actualTyp2 = (PscriptTypeClass) actualTyp;
			// in this special case we have to manually add the type based on
			// the static type information that we have
			int instanceId = translator.manager.getTypeId(actualTyp2.getClassDef());
			expr = expr.plus(JassExprIntVal(instanceId));
		}
		return expr;
	}

	protected static JassExpr smartStoreToTemp(JassTranslator translator, List<JassStatement> result, JassFunction f,
			JassExpr index) {
		if (!(index instanceof JassExprAtomic)) {
			// save index to tempvar
			JassVar tempVar = translator.getNewTempVar(f, "integer"); // TODO type of tempvar could be different to integer
			result.add(JassStmtSet(tempVar.getName(), index));
			index = JassExprVarAccess(tempVar.getName());
		}
		return index;
	}

	

}
