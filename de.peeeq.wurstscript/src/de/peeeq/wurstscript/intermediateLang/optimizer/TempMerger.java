package de.peeeq.wurstscript.intermediateLang.optimizer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.jassIm.ImConst;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarRead;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.AssertProperty;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class TempMerger {

	private final ImProg prog;
	private final ImTranslator trans;
	
	public TempMerger(ImTranslator trans) {
		this.prog = trans.getImProg();
		this.trans = trans;
	}

	public void optimize() {
		trans.assertProperties(AssertProperty.FLAT, AssertProperty.NOTUPLES);
		prog.clearAttributes();
		for (ImFunction f : prog.getFunctions()) {
			optimizeFunc(f);
		}
		// flatten the program because we introduces null-statements
		prog.flatten(trans);
	}

	private void optimizeFunc(ImFunction f) {
		optimizeStatements(f.getBody());
	}

	private void optimizeStatements(ImStmts stmts) {
		Knowledge kn = new Knowledge();

		Replacement replacement = null;
		do { // repeat while there are changes found
			kn.clear();
			 // this terminates, because each replacement eliminates one set-statement
			for (ImStmt s : stmts) {
				replacement = processStatement(s, kn);
				if (replacement != null) {
					// do the replacement
					replacement.apply();
					break;
				}
			}
		} while (replacement != null);
		
		// process nested statements:
		for (ImStmt s : stmts) {
			if (s instanceof ImIf) {
				ImIf imIf = (ImIf) s;
				optimizeStatements(imIf.getThenBlock());
				optimizeStatements(imIf.getElseBlock());
			} else if (s instanceof ImLoop) {
				ImLoop imLoop = (ImLoop) s;
				optimizeStatements(imLoop.getBody());
			}
		}
	}

	private Replacement processStatement(ImStmt s, Knowledge kn) {
		Replacement rep = getPossibleReplacement(s, kn);
		if (rep != null) {
			return rep;
		}
		if (containsFuncCall(s)) {
			kn.invalidateGlobals();
		}	
		if (readsGlobal(s)) {
			kn.invalidateMutatingExpressions();
		}
		if (s instanceof ImSet) {
			ImSet imSet = (ImSet) s;
			// update the knowledge with the new set statement
			kn.update(imSet.getLeft(), imSet);
		} else if (s instanceof ImSetArray) {
			kn.invalidateVar(((ImSetArray) s).getLeft());
		} else if (s instanceof ImExitwhen || s instanceof ImIf || s instanceof ImLoop) {
			kn.clear(); 
			// TODO this could be more precise for local variables, 
			// but for now we just forget everything if we see a loop or if statement
		}
		return null;
	}

	private Replacement getPossibleReplacement(JassImElement elem, Knowledge kn) {
		if (kn.isEmpty()) {
			return null;
		}
		if (elem instanceof ImVarAccess) {
			ImVarAccess va = (ImVarAccess) elem;
			return kn.getReplacementIfPossible(va);
		} else if (elem instanceof ImLoop) {
			return null;
		} else if (elem instanceof ImIf) {
			ImIf imIf = (ImIf) elem;
			return getPossibleReplacement(imIf.getCondition(), kn);
		} else if (elem instanceof ImOperatorCall) {
			ImOperatorCall opCall = (ImOperatorCall) elem;
			if (opCall.getOp().isLazy()) {
				// for lazy operators (and, or) we only search the left expression for possible replacements
				return getPossibleReplacement(opCall.getArguments().get(0), kn);
			}
		}
		// process children
		for (int i=0; i<elem.size(); i++) {
			Replacement r = getPossibleReplacement(elem.get(i), kn);
			if (r != null) {
				return r;
			}
		}
		if (elem instanceof ImFunctionCall) {
			// function call invalidates globals
			kn.invalidateGlobals();
		} else if (elem instanceof ImVarRead) {
			ImVarRead va = (ImVarRead) elem;
			if (va.getVar().isGlobal()) {
				// in case we read a global variable
				kn.invalidateMutatingExpressions();
			}
		}
		return null;
	}

	private boolean containsFuncCall(JassImElement elem) {
		if (elem instanceof ImFunctionCall) {
			return true;
		}
		// process children
		for (int i=0; i<elem.size(); i++) {
			boolean r = containsFuncCall(elem.get(i));
			if (r) {
				return true;
			}
		}
		return false;
	}
	
	public boolean readsVar(JassImElement elem, ImVar left) {
		if (elem instanceof ImVarRead) {
			ImVarRead va = (ImVarRead) elem;
			if (va.getVar() == left) {
				return true;
			}
		}
		// process children
		for (int i=0; i<elem.size(); i++) {
			boolean r = readsVar(elem.get(i), left);
			if (r) {
				return true;
			}
		}
		return false;
	}

	public boolean readsGlobal(JassImElement elem) {
		if (elem instanceof ImVarRead) {
			ImVarRead va = (ImVarRead) elem;
			if (va.getVar().isGlobal()) {
				return true;
			}
		}
		// process children
		for (int i=0; i<elem.size(); i++) {
			boolean r = readsGlobal(elem.get(i));
			if (r) {
				return true;
			}
		}
		return false;
	}

	class Replacement {
		public final ImSet set;
		public final ImVarAccess read;

		public Replacement(ImSet set, ImVarAccess read) {
			this.set = set;
			this.read = read;
		}

		public void apply() {
			ImExpr e = set.getRight();
			if (set.getLeft().attrReads().size() <= 1) {
				// make sure that an impure expression is only evaluated once
				// by removing the assignment
				set.replaceWith(JassIm.ImNull());
			}
			read.replaceWith(e.copy());
			// update attrReads
			set.getLeft().attrReads().remove(read);
		}

	}

	class Knowledge {

		private Map<ImVar, ImSet> currentValues = Maps.newLinkedHashMap();

		public void invalidateGlobals() {
			// invalidate all knowledge which might be based on global state
			// i.e. using a global var or calling a function
			List<ImVar> invalid = Lists.newArrayList();
			for (Entry<ImVar, ImSet> e : currentValues.entrySet()) {
				if (readsGlobal(e.getValue().getRight()) || containsFuncCall(e.getValue())) {
					invalid.add(e.getKey());
				}
			}
			removeKnowledge(invalid);
			
		}

		public void invalidateMutatingExpressions() {
			// invalidate all knowledge which can change global state
			// i.e. calling a function
			List<ImVar> invalid = Lists.newArrayList();
			for (Entry<ImVar, ImSet> e : currentValues.entrySet()) {
				if (containsFuncCall(e.getValue())) {
					invalid.add(e.getKey());
				}
			}
			removeKnowledge(invalid);
			
		}

		public void clear() {
			currentValues.clear();
		}

		public Replacement getReplacementIfPossible(ImVarAccess va) {
			for (Entry<ImVar, ImSet> e : currentValues.entrySet()) {
				if (e.getKey() == va.getVar()) {
					return new Replacement(e.getValue(), va);
				}
			}
			return null;
		}

		public boolean isEmpty() {
			return currentValues.isEmpty();
		}

		public void update(ImVar left, ImSet set) {
			invalidateVar(left);

			if (isMergable(left, set.getRight())) {
				// only store local vars which are read exactly once
				currentValues.put(left, set);
			}
		}

		private boolean isMergable(ImVar left, ImExpr e) {
			if (left.isGlobal()) {
				// never merge globals
				return false;
			}
			if (left.attrReads().size() == 1) {
				// variable read exactly once can be replaced
				return true;
			}
			if (isSimplePureExpr(e)) {
				// simple and pure expressions can always be merged
				return true;
			}
			return false;
		}

		/** invalidates all expression depending on 'left' */
		private void invalidateVar(ImVar left) {
			if (left.isGlobal()) {
				invalidateGlobals();
			} else {
				List<ImVar> invalid = Lists.newArrayList();
				for (Entry<ImVar, ImSet> e : currentValues.entrySet()) {
					if (readsVar(e.getValue().getRight(), left)) {
						invalid.add(e.getKey());
					}
				}
				removeKnowledge(invalid);
			}
		}

		public void removeKnowledge(List<ImVar> invalid) {
			for (ImVar i : invalid) {
				currentValues.remove(i);
			}
		}

	}

	private boolean isSimplePureExpr(ImExpr e) {
		if (e instanceof ImConst) {
			// constants are ok
			return true;
		} else if (e instanceof ImVarAccess) {
			// local variables are ok
			ImVarAccess va = (ImVarAccess) e;
			return !va.getVar().isGlobal(); 
		}
		return false;
	}
	
}
