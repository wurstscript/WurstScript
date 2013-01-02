package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Preconditions;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Op;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OpUnequals;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImConst;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.jassIm.JassImElementWithTypes;

/**
 * a rewrite would return a combination of
 *  - List of statements
 *  - list of expressions 
 *  for expressions, returned expressions would never have a parent
 *
 */
public class EliminateTuples {

	public static void eliminateTuplesProg(ImProg imProg, ImTranslator translator) {
		transformVars(imProg.getGlobals(), translator);
		for (ImFunction f : imProg.getFunctions()) {
			f.eliminateTuples(translator);
		}
		
		for (ImFunction f : imProg.getFunctions()) {
			transformStatements(f, f.getBody(), translator);
		}
		
		translator.assertProperties(AssertProperty.NOTUPLES);
	}

	

	public static void eliminateTuplesFunc(ImFunction f, final ImTranslator translator) {
		// transform parameters
		transformVars(f.getParameters(), translator);
		transformVars(f.getLocals(), translator);

		translator.setOriginalReturnValue(f, f.getReturnType());
		
		f.setReturnType(getFirstType(f.getReturnType()));
		f.getBody().accept(new ImStmts.DefaultVisitor() {
			@Override
			public void visit(ImFunctionCall e) {
				// use temp return valus instead of tuples
				List<ImVar> tempVars = translator.getTempReturnVarsFor(e.getFunc());
				if (tempVars.size() > 1) {
					JassImElement parent = e.getParent();
					int parentIndex = -1;
					for (int i=0; i<parent.size(); i++) {
						if (parent.get(i) == e) {
							parentIndex = i;
							parent.set(i, JassIm.ImNull()); // dummy
							break;
						}
					}
					
					ImExprs exprs = JassIm.ImExprs(e);
					for (int i=1; i<tempVars.size(); i++) {
						exprs.add(JassIm.ImVarAccess(tempVars.get(i)));
					}
					ImExpr newExpr = JassIm.ImTupleExpr(exprs);
					parent.set(parentIndex, newExpr);
				}
			}
			
		});
	}


	private static ImType getFirstType(ImType t) {
		if (t instanceof ImTupleType) {
			ImTupleType tt = (ImTupleType) t;
			return getFirstType(tt.getTypes().get(0));
		}
		return t;
	}

	private static void transformStatements(ImFunction f, ImStmts stmts,
			ImTranslator translator) {
		ListIterator<ImStmt> it = stmts.listIterator();
		while (it.hasNext()) {
			ImStmt s = it.next();
			ImStmt newS = s.eliminateTuples(translator, f);
			if (newS != s) {
				// element changed, replace it
				it.set(newS);
			}
		}
	}

	private static void transformVars(ImVars vars, ImTranslator translator) {
		ListIterator<ImVar> it = vars.listIterator();
		while (it.hasNext()) {
			ImVar v = it.next();
			Preconditions.checkNotNull(v.getParent(),  "null parent: " + v);
			if (v.getType() instanceof ImTupleType || v.getType() instanceof ImTupleArrayType) {
				List<ImVar> varsForTuple = translator.getVarsForTuple(v);
				it.remove();
				for (ImVar nv : varsForTuple) {
					it.add(nv);
				}
			}
		}
		for (ImVar v : vars) {
			if (v.getType() instanceof ImTupleType || v.getType() instanceof ImTupleArrayType) {
				throw new Error("still contains a bad var: " + v + " in: \n" + v.getParent().getParent());
			}
		}
	}

	public static JassImElement eliminateTuples2(JassImElement e, ImTranslator translator, ImFunction f) {
		for (int i=0; i<e.size(); i++) {
			JassImElement c = e.get(i);
			JassImElement newC = eliminateTuplesDispatch(c, translator, f);
			if (newC != c) {
				e.set(i, newC);
			}
		}
		return e;
	}
	
	private static JassImElement eliminateTuplesDispatch(JassImElement e,
			ImTranslator translator, ImFunction f) {
		if (e instanceof ImExprOpt) {
			ImExprOpt imExprOpt = (ImExprOpt) e;
			return imExprOpt.eliminateTuplesExprOpt(translator, f);
		} else if (e instanceof ImStmt) {
			ImStmt stmt = (ImStmt) e;
			return stmt.eliminateTuples(translator, f);
		}
		return eliminateTuples2(e, translator, f);
	}

	
	
	public static ImStmt eliminateTuples(ImExitwhen e, ImTranslator translator, ImFunction f) {
		return (ImStmt) eliminateTuples2(e, translator, f);
	}
	
	public static ImStmt eliminateTuples(ImIf e, ImTranslator translator, ImFunction f) {
		return (ImStmt) eliminateTuples2(e, translator, f);
	}
	
	public static ImStmt eliminateTuples(ImLoop e, ImTranslator translator, ImFunction f) {
		return (ImStmt) eliminateTuples2(e, translator, f);
	}
	
	
	
	public static ImStmt eliminateTuples(ImExpr e, ImTranslator translator, ImFunction f) {
		return e.eliminateTuplesExpr(translator, f);
	}
	
	public static ImStmt eliminateTuples(ImSet e, ImTranslator translator, ImFunction f) {
		ImStmts statements = JassIm.ImStmts();
		List<ImVar> vars = translator.getVarsForTuple(e.getLeft());
		if (vars.size() == 1) {
			ImExpr newExpr = copyExpr(e.getRight().eliminateTuplesExpr(translator, f));
			newExpr = elimStatementExpr(statements, newExpr, translator, f);
			if (newExpr instanceof ImTupleExpr) {
				ImTupleExpr te = (ImTupleExpr) newExpr;
				if (te.getExprs().size() > 1) {
					throw new Error();
				}
				newExpr = te.getExprs().remove(0);
			}
			
			if (statements.size() > 0) {
				statements.add(JassIm.ImSet(e.getTrace(), vars.get(0), copyExpr(newExpr)));
				return JassIm.ImStatementExpr(statements, JassIm.ImNull());
			} else {
				e.setLeft(vars.get(0));
				e.setRight(newExpr);
				return e;
			}
		}
		
		ImExpr right1 = e.getRight().eliminateTuplesExpr(translator, f);
		right1 = elimStatementExpr(statements, right1, translator, f);
		if (right1 instanceof ImTupleExpr) {
			ImTupleExpr right = (ImTupleExpr) right1;
			ImExprs exprs = right.getExprs();
			int exprsSize = exprs.size();
			int varsSize = vars.size();
			if (exprsSize != varsSize) {
				throw new Error(exprsSize + " != " + varsSize + " in " + e);
			}
			for (int i=0; i<varsSize; i++) {
				statements.add(JassIm.ImSet(e.getTrace(), vars.get(i), copyExpr(exprs.get(i))));
			}	
		} else {
			throw new Error("unhandled case: " + right1);
		}
		return JassIm.ImStatementExpr(statements, JassIm.ImNull());
	}
	
	private static ImExpr copyExpr(ImExpr e) {
		if (e.getParent() == null) {
			return e;
		}
		return (ImExpr) e.copy();
	}



	public static ImStmt eliminateTuples(ImSetArray e, ImTranslator translator, ImFunction f) {
		ImStmts statements = JassIm.ImStmts();
		
		List<ImVar> vars = translator.getVarsForTuple(e.getLeft());
		if (vars.size() == 1) {
			ImExpr newExpr = copyExpr(e.getRight().eliminateTuplesExpr(translator, f));
			newExpr = elimStatementExpr(statements, newExpr, translator, f);
			if (newExpr instanceof ImTupleExpr) {
				ImTupleExpr te = (ImTupleExpr) newExpr;
				if (te.getExprs().size() > 1) {
					throw new Error();
				}
				newExpr = te.getExprs().remove(0);
			}
			
			ImExpr indexExpr = copyExpr(e.getIndex().eliminateTuplesExpr(translator, f));
			if (statements.size() > 0) {
				statements.add(JassIm.ImSetArray(e.getTrace(), 
						vars.get(0), 
						indexExpr,
						copyExpr(newExpr)));
				return JassIm.ImStatementExpr(statements, JassIm.ImNull());
			} else {
				e.setLeft(vars.get(0));
				e.setRight(newExpr);
				e.setIndex(indexExpr);
				return e;
			}
		}
		// assign index to temporary variable
		ImVar tempIndex = JassIm.ImVar(JassIm.ImSimpleType("integer"), "tempIndex", false);
		f.getLocals().add(tempIndex);
		statements.add(JassIm.ImSet(e.getTrace(), tempIndex, copyExpr(e.getIndex().eliminateTuplesExpr(translator, f))));
		
		
		ImExpr right1 = e.getRight().eliminateTuplesExpr(translator, f);
		right1 = elimStatementExpr(statements, right1, translator, f);
		if (right1 instanceof ImTupleExpr) {
			ImTupleExpr right = (ImTupleExpr) right1;
			ImExprs exprs = right.getExprs();
			int exprsSize = exprs.size();
			int varsSize = vars.size();
			if (exprsSize != varsSize) {
				throw new Error(exprsSize + " != " + varsSize);
			}
			for (int i=0; i<varsSize; i++) {
				statements.add(JassIm.ImSetArray(e.getTrace(), vars.get(i), 
						JassIm.ImVarAccess(tempIndex),
						copyExpr(exprs.get(i))));
			}	
		} else {
			throw new Error("unhandled case: " + right1);
		}
		return JassIm.ImStatementExpr(statements, JassIm.ImNull());
	}

	private static ImExpr elimStatementExpr(ImStmts statements, ImExpr expr,
			ImTranslator translator, ImFunction f) {
		if (expr instanceof ImStatementExpr) {
			ImStatementExpr right = (ImStatementExpr) expr;
			List<ImStmt> ss = right.getStatements().removeAll();
			for (ImStmt s : ss) {
				statements.add(s.eliminateTuples(translator, f));
			}
			expr = right.getExpr();
		}
		return expr;
	}
	
	public static ImStmt eliminateTuples(ImSetTuple e, ImTranslator translator, ImFunction f) {
		ImVar left = e.getLeft();
		IntRange range = getTupleIndexRange((ImTupleType) left.getType(), e.getTupleIndex());
		List<ImVar> vars = translator.getVarsForTuple(left);
		
		ImExpr expr = e.getRight().eliminateTuplesExpr(translator, f);
		ImStmts statements = JassIm.ImStmts();
		expr = elimStatementExpr(statements, expr, translator, f);
		int rangeSize = range.size();
		if (expr instanceof ImTupleExpr) {
			ImTupleExpr tupleExpr = (ImTupleExpr) expr;
			ImExprs exprs = tupleExpr.getExprs();
			int exprsSize = exprs.size();
			if (exprsSize != rangeSize) {
				throw new Error(exprsSize + " != " + rangeSize);
			}
			for (int i : range) {
				statements.add(JassIm.ImSet(e.attrTrace(), vars.get(i), exprs.get(i-range.start)));
			}
		} else {
			if (rangeSize > 1) {
				throw new Error("range size was " + rangeSize);
			}
			statements.add(JassIm.ImSet(e.attrTrace(), vars.get(range.start), copyExpr(expr)));
		}
		return JassIm.ImStatementExpr(statements, JassIm.ImNull());
	}
	
	public static ImStmt eliminateTuples(ImSetArrayTuple e, ImTranslator translator, ImFunction f) {
		// TODO use tuple index
		ImStmts statements = JassIm.ImStmts();
		// assign index to temporary variable
		ImVar tempIndex = JassIm.ImVar(JassIm.ImSimpleType("integer"), "tempIndex", false);
		f.getLocals().add(tempIndex);
		statements.add(JassIm.ImSet(e.getTrace(), tempIndex, copyExpr(e.getIndex().eliminateTuplesExpr(translator, f))));
		
		ImVar left = e.getLeft();
		IntRange range = getTupleIndexRange((ImTupleArrayType) left.getType(), e.getTupleIndex());
		List<ImVar> vars = translator.getVarsForTuple(left);
		
		ImExpr expr = e.getRight().eliminateTuplesExpr(translator, f);
		expr = elimStatementExpr(statements, expr, translator, f);
		int rangeSize = range.size();
		if (expr instanceof ImTupleExpr) {
			ImTupleExpr tupleExpr = (ImTupleExpr) expr;
			ImExprs exprs = tupleExpr.getExprs();
			int exprsSize = exprs.size();
			if (exprsSize != rangeSize) {
				throw new Error(exprsSize + " != " + rangeSize);
			}
			for (int i : range) {
				statements.add(JassIm.ImSetArray(e.attrTrace(), 
						vars.get(i), 
						JassIm.ImVarAccess(tempIndex),
						exprs.get(i-range.start)));
			}
		} else {
			if (rangeSize > 1) {
				throw new Error("range size was " + rangeSize);
			}
			statements.add(JassIm.ImSetArray(e.attrTrace(), 
					vars.get(range.start),
					JassIm.ImVarAccess(tempIndex), 
					copyExpr(expr)));
		}
		return JassIm.ImStatementExpr(statements, JassIm.ImNull());
	}

	public static ImExprOpt eliminateTuplesExpr(ImNoExpr e, ImTranslator translator, ImFunction f) {
		return e;
	}

	
	
	public static ImExpr eliminateTuplesExpr(ImTupleExpr e, ImTranslator translator, ImFunction f) {
		ListIterator<ImExpr> it = e.getExprs().listIterator();
		while (it.hasNext()) {
			ImExpr expr = it.next();
			ImExpr newExpr = expr.eliminateTuplesExpr(translator, f);
			if (newExpr instanceof ImTupleExpr) {
				ImTupleExpr te = (ImTupleExpr) newExpr;
				it.remove();
				for (ImExpr child : te.getExprs()) {
					it.add(copyExpr(child));
				}
			} else if (newExpr != expr) {
				it.set(newExpr);
			}
		}
		return e;
	}
	
	public static ImExpr eliminateTuplesExpr(ImTupleSelection e, ImTranslator translator, ImFunction f) {
		IntRange range;
		
		System.out.println("tuple selection = " + e);
		if (e.getTupleExpr() instanceof ImVarAccess) {
			ImVarAccess varAccess = (ImVarAccess) e.getTupleExpr();
			if (varAccess.attrTyp() instanceof ImTupleType) {
				ImTupleType tt = (ImTupleType) varAccess.attrTyp();
				range = getTupleIndexRange(tt, e.getTupleIndex());
			} else {
				throw new Error("problem with " + varAccess + "\n" +
						"has type " + varAccess.attrTyp());
			}
			ImVar v = varAccess.getVar();
			List<ImVar> vars = translator.getVarsForTuple(v);
			System.out.println("is a var, selecting range " + range + " from vars " + vars);
			if (range.size() == 1) {		
				return JassIm.ImVarAccess(vars.get(range.start));
			} else {
				ImExprs exprs = JassIm.ImExprs();
				for (int i : range) {
					exprs.add(JassIm.ImVarAccess(vars.get(i)));
				}
				return JassIm.ImTupleExpr(exprs);
			}
		}
		
		ImExpr tupleExpr = e.getTupleExpr().eliminateTuplesExpr(translator, f);
		
		if (tupleExpr.attrTyp() instanceof ImTupleType) {
			ImTupleType tt = (ImTupleType) tupleExpr.attrTyp();
			range = getTupleIndexRange(tt, e.getTupleIndex());
		} else {
			throw new Error("problem with " + tupleExpr + "\n" +
					"has type " + tupleExpr.attrTyp());
		}
		
		
		
		
		ImVar tempVar = JassIm.ImVar(tupleExpr.attrTyp(), "tempTupleSelectionResult", false);
		f.getLocals().addAll(translator.getVarsForTuple(tempVar));
		
		ImStmts statements = JassIm.ImStmts();
		statements.add(
				JassIm.ImSet(tupleExpr.attrTrace(), tempVar, copyExpr(tupleExpr))
				.eliminateTuples(translator, f)
				);
		
		ImExpr result;
		if (range.size() == 1) {
			result = JassIm.ImVarAccess(translator.getVarsForTuple(tempVar).get(range.start));
		} else {
			ImExprs exprs = JassIm.ImExprs();
			for (int i : range) {
				exprs.add(JassIm.ImVarAccess(translator.getVarsForTuple(tempVar).get(i)));
			}
			result = JassIm.ImTupleExpr(exprs);
		}
		
		
		return JassIm.ImStatementExpr(statements, result);
	}
	
	private static IntRange getTupleIndexRange(JassImElementWithTypes tt, int index) {
		int start = 0;
		for (int i=0; i<index; i++) {
			ImType t = tt.getTypes().get(i);
			start += getTypeSize(t);
		}
		int end = start + getTypeSize(tt.getTypes().get(index));
		return new IntRange(start, end);
	}

	private static int getTypeSize(ImType imType) {
		return imType.match(new ImType.Matcher<Integer>() {

			@Override
			public Integer case_ImTupleArrayType(
					ImTupleArrayType tt) {
				int sum = 0;
				for (ImType t : tt.getTypes()) {
					sum += getTypeSize(t);
				}
				return sum;
			}

			@Override
			public Integer case_ImSimpleType(ImSimpleType t) {
				return 1;
			}

			@Override
			public Integer case_ImVoid(ImVoid t) {
				return 0;
			}

			@Override
			public Integer case_ImArrayType(ImArrayType t) {
				return 1;
			}

			@Override
			public Integer case_ImTupleType(ImTupleType tt) {
				int sum = 0;
				for (ImType t : tt.getTypes()) {
					sum += getTypeSize(t);
				}
				return sum;
			}
			
			
		});
	}

	public static ImExpr eliminateTuplesExpr(ImVarAccess e, ImTranslator translator, ImFunction f) {
		ImVar v = e.getVar();
		List<ImVar> varsForTuple = translator.getVarsForTuple(v);
		if (varsForTuple.size() > 1 || varsForTuple.get(0) != v) {
			ImExprs exprs = JassIm.ImExprs();
			for (ImVar nv : varsForTuple) {
				exprs.add(JassIm.ImVarAccess(nv));
			}
			return JassIm.ImTupleExpr(exprs );
		}
		return (ImExpr) eliminateTuples2(e, translator, f);
	}
	
	public static ImExpr eliminateTuplesExpr(ImVarArrayAccess e, ImTranslator translator, ImFunction f) {
		ImVar v = e.getVar();
		List<ImVar> varsForTuple = translator.getVarsForTuple(v);
		if (varsForTuple.size() > 1 || varsForTuple.get(0) != v) {
			ImVar tempIndex = JassIm.ImVar(e.getIndex().attrTyp(), "tempIndex", false);
			f.getLocals().add(tempIndex);
			
			ImStmts statements = JassIm.ImStmts(JassIm.ImSet(e.attrTrace(), tempIndex, copyExpr(e.getIndex().eliminateTuplesExpr(translator, f))));
			ImExprs exprs = JassIm.ImExprs();
			for (ImVar nv : varsForTuple) {
				exprs.add(JassIm.ImVarArrayAccess(nv, JassIm.ImVarAccess(tempIndex)));
			}
			return JassIm.ImStatementExpr(statements, JassIm.ImTupleExpr(exprs));
		}
		return (ImExpr) eliminateTuples2(e, translator, f);
	}
	
	public static ImExpr eliminateTuplesExpr(ImConst e, ImTranslator translator, ImFunction f) {
		// constants cannot contain tuples and thus do not change
		return e;
	}

	public static ImExpr eliminateTuplesExpr(ImStatementExpr imStatementExpr,
			ImTranslator translator, ImFunction f) {
		return (ImExpr) eliminateTuples2(imStatementExpr, translator, f);
	}
	
	
	public static ImExpr eliminateTuplesExpr(ImFunctionCall e, ImTranslator translator, ImFunction f) {
		// eliminate tuple expressions in arguments
		eliminateTuplesInArgs(e, translator, f);
		return e;
	}

	public static ImExpr eliminateTuplesExpr(ImOperatorCall e, ImTranslator translator, ImFunction f) {
		// eliminate tuple expressions in arguments
		eliminateTuplesInArgs(e, translator, f);
		
		if (e.getArguments().size() > 2) {
			List<ImExpr> arguments = e.getArguments().removeAll();
			int size = arguments.size() / 2;
			Op logicOp;
			Op compareOp = e.getOp();
			if (compareOp instanceof OpEquals) {
				logicOp = Ast.OpAnd();
			} else if (compareOp instanceof OpUnequals) {
				logicOp = Ast.OpOr();
			} else {
				throw new Error("unsupported tuple operator " + e);
			}
			ImExpr result = null;
			for (int i=0; i<size;i++) {
				ImExpr left = arguments.get(i);
				ImExpr right = arguments.get(size + i);
				ImOperatorCall comparison = JassIm.ImOperatorCall(compareOp, JassIm.ImExprs(left, right));
				if (result == null) {
					result = comparison;
				} else {
					result = JassIm.ImOperatorCall(logicOp, JassIm.ImExprs(result, comparison));
				}
			}
			return result;
		}
		
		return e;
	}

	private static void eliminateTuplesInArgs(ImCall e,
			ImTranslator translator, ImFunction f) {
		ListIterator<ImExpr> it = e.getArguments().listIterator();
		while (it.hasNext()) {
			ImExpr arg = it.next();
			ImStmts stmts = JassIm.ImStmts();
			ImExpr newArg = arg.eliminateTuplesExpr(translator, f);
			newArg = elimStatementExpr(stmts, newArg, translator, f);
			if (newArg instanceof ImTupleExpr) {
				ImTupleExpr te = (ImTupleExpr) newArg;
				it.remove();
				int i = 0;
				for (ImExpr child : te.getExprs()) {
					ImExpr newArg2 = copyExpr(child);
					if (i == 0 && !stmts.isEmpty()) {
						newArg2 = JassIm.ImStatementExpr(stmts, newArg2);
					}
					it.add(newArg2);
					i++;
				}
			} else if (newArg != arg) {
				if (!stmts.isEmpty()) {
					newArg = JassIm.ImStatementExpr(stmts, copyExpr(newArg));
				}
				it.set(newArg);
			}
		}
	}
	
	public static ImStmt eliminateTuples(ImReturn e, ImTranslator translator, ImFunction f) {
		ImExprOpt ret1 = e.getReturnValue().eliminateTuplesExprOpt(translator, f);
		if (ret1 instanceof ImNoExpr) {
			// returns nothing
			return e;			
		}
		ImExpr ret = (ImExpr) ret1;
		ImStmts statements = JassIm.ImStmts();
		ImExpr retExpr = elimStatementExpr(statements, (ImExpr) ret, translator, f);
		ImExpr result;
		if (retExpr instanceof ImTupleExpr) {
			ImTupleExpr te = (ImTupleExpr) retExpr;
			List<ImVar> tempReturnVars = translator.getTempReturnVarsFor(f);
			for (int i=0; i<tempReturnVars.size(); i++) {
				statements.add(JassIm.ImSet(e.getTrace(), 
						tempReturnVars.get(i), 
						copyExpr(te.getExprs().get(i))));
			}
			result = JassIm.ImVarAccess(tempReturnVars.get(0));
		} else {
			result = copyExpr(retExpr);
		}
		statements.add(JassIm.ImReturn(e.attrTrace(), result));
		return JassIm.ImStatementExpr(statements, JassIm.ImNull());
	}

}
