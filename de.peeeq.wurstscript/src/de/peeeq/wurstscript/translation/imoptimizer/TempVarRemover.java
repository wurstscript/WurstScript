package de.peeeq.wurstscript.translation.imoptimizer;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class TempVarRemover {
	
	
	private ImTranslator trans;
	private ImProg prog;

	public TempVarRemover(ImTranslator trans) {
		this.trans = trans;
	}
	
	public void optimize() {
		prog = trans.getImProg();
		for (ImFunction f : prog.getFunctions()) {
			optimizeFunc(f);
		}
	}

	private void optimizeFunc(ImFunction f) {
		Knowledge knowledge = new Knowledge();
		optimizeStatements(f.getBody(), knowledge);
	}
	
	private void optimizeStatements(ImStmts stmts, Knowledge knowledge) {
		ListIterator<ImStmt> it = stmts.listIterator();
		while (it.hasNext()) {
			ImStmt s = it.next();
			if (s instanceof ImSet) {
				ImSet set = (ImSet) s;
				set.setRight(knowledge.optimize(set.getRight()));
				ImVar v = set.getLeft();
				knowledge.invalidateAllExprsUsingVar(v);
				if (!v.isGlobal()) {
					knowledge.add(v, set.getRight());
				}
			} else if (s instanceof ImSetArray) {
				ImSetArray set = (ImSetArray) s;
				knowledge.optimize(set.getIndex());
				knowledge.optimize(set.getRight());
				knowledge.invalidateAllExprsUsingVar(set.getLeft());
			} else if (s instanceof ImExpr) {
				ImExpr expr = (ImExpr) s;
				it.set(knowledge.optimize(expr));			
			} else if (s instanceof ImIf) {
				ImIf imIf = (ImIf) s;
				knowledge.optimize(imIf.getCondition());
				knowledge.clear();
				optimizeStatements(imIf.getThenBlock(), knowledge);
				optimizeStatements(imIf.getElseBlock(), knowledge);
			} else if (s instanceof ImLoop) {
				ImLoop loop = (ImLoop) s;
				knowledge.clear();
				optimizeStatements(loop.getBody(), knowledge);
			} else if (s instanceof ImReturn) {
				ImReturn imReturn = (ImReturn) s;
				if (imReturn.getReturnValue() instanceof ImExpr) {
					knowledge.optimize((ImExpr) imReturn.getReturnValue());
				}
			} else if (s instanceof ImExitwhen) {
				ImExitwhen exitwhen = (ImExitwhen) s;
				knowledge.optimize(exitwhen.getCondition());
			} else {
				throw new Error("unhandled statement " + s);
			}
		}
		knowledge.clear();
	}

	private class Knowledge {
		private List<VarKnowledge> known = Lists.newArrayList();

		private class VarKnowledge {
			ImVar var;
			ImExpr value;
			Set<ImVar> usedVars = Sets.newHashSet();
			private boolean callsFunc;
			public VarKnowledge(ImVar var, ImExpr value) {
				this.var = var;
				this.value = value;
				final boolean[] callsFunc = new boolean[] { false };
				value.accept(new ImExpr.DefaultVisitor() {
					public void visit(ImVarAccess e) {
						usedVars.add(e.getVar());
					}
					
					public void visit(ImVarArrayAccess e) {
						usedVars.add(e.getVar());
					}
					
					public void visit(ImFunctionCall e) {
						callsFunc[0] = true;
					}
				});
				this.callsFunc = callsFunc[0];
			}
			
		}
		
		public void invalidateAllExprsUsingVar(ImVar v) {
			ListIterator<VarKnowledge> it = known.listIterator();
			while (it.hasNext()) {
				VarKnowledge k = it.next();
				if (k.usedVars.contains(v)) {
					it.remove();
					continue;
				}
				if (v.isGlobal() && k.callsFunc) {
					// a global variable could change the value of 
					// the called function, hence we have to invalidate 
					// this knowledge
					it.remove();
					continue;
				}
			}
			
		}

		public void clear() {
			known.clear();
		}

		public ImExpr optimize(ImExpr right) {
			// TODO Auto-generated method stub
			return null;
		}

		public void add(ImVar v, ImExpr right) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
