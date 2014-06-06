package de.peeeq.wurstscript.validation.controlflow;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithLoopVar;
import de.peeeq.wurstscript.ast.CompoundStatement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.HasReadVariables;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.StartFunctionStatement;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.utils.Utils;



public class DataflowAnomalyAnalysis extends ForwardMethod<Set<LocalVarDef>> {

	@Override
	Set<LocalVarDef> calculate(WStatement s, Set<LocalVarDef> incoming) {
		if (s instanceof StartFunctionStatement) {
			// initially all vars are uninitialized
			final Set<LocalVarDef> r = Sets.newLinkedHashSet();
			getFuncDef().accept(new FunctionLike.DefaultVisitor() {
				@Override
				public void visit(LocalVarDef localVarDef) {
					r.add(localVarDef);
				}
				
			});
			return r;
		}
		
		
		if (s instanceof CompoundStatement) {
			// for a compound statement check only the expressions in the statement
			for (int i=0; i<s.size(); i++) {
				if (s.get(i) instanceof Expr) {
					Expr expr = (Expr) s.get(i);
					checkIfVarsInitialized(expr, incoming);
				}
			}
		} else {
			checkIfVarsInitialized(s, incoming);
		}
		
		
		NameDef n = null;
		if (s instanceof StmtSet) {
			StmtSet s2 = (StmtSet) s;
			n = s2.getUpdatedExpr().attrNameDef();
			
		} else if (s instanceof LocalVarDef) {
			LocalVarDef l = (LocalVarDef) s;
			if (l.getInitialExpr() instanceof Expr) {
				n = l;
			}
		} else if (s instanceof AstElementWithLoopVar) {
			AstElementWithLoopVar s2 = (AstElementWithLoopVar) s;
			n = s2.getLoopVar();
		}
		if (n != null && incoming.contains(n)) {
			Set<LocalVarDef> r = Sets.newHashSet(incoming);
			r.remove(n);
			return r;
		}
		return incoming;
	}

	private void checkIfVarsInitialized(HasReadVariables s,
			Set<LocalVarDef> incoming) {
		ImmutableList<NameDef> readVars = s.attrReadVariables();
		for (NameDef v : readVars) {
			if (v.attrTyp() instanceof WurstTypeArray) {
				continue;
			}
			if (incoming.contains(v)) {
				AstElement readingExpr = findRead(s, v);
				if (readingExpr.attrNearestExprClosure() != s.attrNearestExprClosure()) {
					// ignore inside closure
					continue;
				}
				readingExpr.addError("Variable " + v.getName() + " is not initialized ");
			}
		}
	}

	private HasReadVariables findRead(AstElement e, NameDef v) {
		HasReadVariables result = null;
		if (e instanceof HasReadVariables) {
			HasReadVariables r = (HasReadVariables) e;
			if (!r.attrReadVariables().contains(v)) {
				return null;
			}
			result = r;
		}
		for (int i=0; i<e.size(); i++) {
			HasReadVariables r = findRead(e.get(i), v);
			if (r != null) {
				return r;
			}
		}
		return result;
	}

	@Override
	Set<LocalVarDef> merge(Collection<Set<LocalVarDef>> values) {
		if (values.size() == 1) {
			return Utils.getFirst(values);
		}
		Set<LocalVarDef> r = Sets.newLinkedHashSet();
		for (Set<LocalVarDef> v : values) {
			r.addAll(v);
		}
		return r;
	}

	@Override
	String print(Set<LocalVarDef> t) {
		if (t == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder(); 
		sb.append("[");
		for (LocalVarDef v : t) {
			sb.append(v.getName() + ", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	void checkFinal(Set<LocalVarDef> fin) {
	}

	@Override
	public Set<LocalVarDef> startValue() {
		return Collections.emptySet();
	}


	
	
}
