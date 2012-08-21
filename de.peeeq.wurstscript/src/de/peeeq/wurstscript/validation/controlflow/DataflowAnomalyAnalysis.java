package de.peeeq.wurstscript.validation.controlflow;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElementWithLoopVar;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.StartFunctionStatement;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.utils.Utils;



public class DataflowAnomalyAnalysis extends ForwardMethod<Set<LocalVarDef>> {

	Map<WStatement, String> errors = Maps.newHashMap();
	
	@Override
	Set<LocalVarDef> calculate(WStatement s, Set<LocalVarDef> incoming) {
		if (s instanceof StartFunctionStatement) {
			// initially all vars are uninitialized
			final Set<LocalVarDef> r = Sets.newHashSet();
			getFuncDef().accept(new FunctionLike.DefaultVisitor() {
				@Override
				public void visit(LocalVarDef localVarDef) {
					r.add(localVarDef);
				}
				
			});
			return r;
		}
		
		
		String err = null;
		for (NameDef v : s.attrReadVariables2()) {
			if (v.attrTyp() instanceof WurstTypeArray) {
				continue;
			}
			if (incoming.contains(v)) {
				err = "Variable " + v.getName() + " is not initialized ";
			}
		}
		errors.put(s, err);
		
		
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

	@Override
	Set<LocalVarDef> merge(Collection<Set<LocalVarDef>> values) {
		if (values.size() == 1) {
			return Utils.getFirst(values);
		}
		Set<LocalVarDef> r = Sets.newHashSet();
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
		for (Entry<WStatement, String> e : errors.entrySet()) {
			if (e.getValue() != null) {
				e.getKey().addError(e.getValue());
			}
		}
	}

	@Override
	public Set<LocalVarDef> startValue() {
		return Collections.emptySet();
	}


	
	
}
