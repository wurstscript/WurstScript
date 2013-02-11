package de.peeeq.wurstscript.translation.imoptimizer;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

/**
 * 
 * sets local handle variables to null at every return statement
 */
public class NullSetter {


	private ImProg prog;
	private ImTranslator translator;
	
	// the types which are not set to null
	private Set<String>	primitiveTypes = Sets.newHashSet("boolean","integer","string","code","real");
	
	public NullSetter(ImTranslator translator) {
		this.translator = translator;
		this.prog = translator.getImProg();
	}
	
	public void optimize() {
		prog.flatten(translator);
		for (ImFunction f : prog.getFunctions()) {
			optimizeFunc(f);			
		}
		
	}

	private void optimizeFunc(final ImFunction f) {
		if (f.isBj() || f.isNative() || f.isCompiletime()) {
			return;
		}
		final List<ImVar> handleVars = Lists.newArrayList();
		for (ImVar local : f.getLocals()) {
			if (isHandleType(local.getType())) {
				handleVars.add(local);
			}
		}
		if (handleVars.isEmpty()) {
			// functions without handle vars can be skipped
			return;
		}
		final List<ImStmt> nullSetStmts = Lists.newArrayList();
		final AstElement trace = f.getTrace();
		for (ImVar local : handleVars) {
			nullSetStmts.add(JassIm.ImSet(trace, local, JassIm.ImNull()));
		}
		boolean returns = optimizeChildren(f, handleVars, nullSetStmts, trace, f.getBody());
		
		if (!returns) {
			// add null set statements if not alreay returned ...
			addNullSetStmts(f.getBody(), f.getBody().size(), nullSetStmts);
		}
	}

	/**
	 * adds nullSetStmts in front of every return statement
	 * 
	 * returns true if "parent" always returns
	 * 
	 * @param f
	 * @param handleVars
	 * @param nullSetStmts
	 * @param trace
	 * @param parent
	 * @return
	 */
	private boolean optimizeChildren(final ImFunction f,
			final List<ImVar> handleVars, final List<ImStmt> nullSetStmts,
			final AstElement trace, JassImElement parent) {
		for (int i=0; i<parent.size(); i++) {
			JassImElement elem = parent.get(i);
			if (elem instanceof ImReturn) {
				handleReturnStmt(f, handleVars, nullSetStmts, trace, elem);
				return true;
			} else if (elem instanceof ImIf) {
				ImIf imIf = (ImIf) elem;
				boolean returnsThen = optimizeChildren(f, handleVars, nullSetStmts, trace, imIf.getThenBlock());
				boolean returnsElse = optimizeChildren(f, handleVars, nullSetStmts, trace, imIf.getThenBlock());
				if (returnsThen && returnsElse) {
					return true;
				}
			} else if (elem instanceof ImLoop) {
				optimizeChildren(f, handleVars, nullSetStmts, trace, elem);
				// simplifying assumption: loops never return always
			} else {
				// visit children
				boolean returns = optimizeChildren(f, handleVars, nullSetStmts, trace, elem);
				if (returns) {
					return true;
				}
			}
		}
		return false;
	}

	private void handleReturnStmt(final ImFunction f,
			final List<ImVar> handleVars, final List<ImStmt> nullSetStmts,
			final AstElement trace, JassImElement elem) {
		ImReturn imReturn = (ImReturn) elem;
		ImStmts parent2 = (ImStmts) imReturn.getParent();
		int parentIndex = parent2.indexOf(imReturn);
		if (imReturn.getReturnValue() instanceof ImExpr) { // returns some value
			ImExpr returnExpr = (ImExpr) imReturn.getReturnValue();
			
			addNullSetStmts(parent2, parentIndex, nullSetStmts);
			
			if (exprContainsVar(returnExpr, handleVars)) {
				// if the returnExpr contains some handleVar, we have to add a temporary var
				
				ImVar tempReturn = JassIm.ImVar(returnExpr.attrTyp(), f.getName() + "tempReturn", false);
				if (isHandleType(returnExpr.attrTyp())) {
					// use global variables for handle types
					prog.getGlobals().add(tempReturn);
				} else {
					// local variables for other types
					f.getLocals().add(tempReturn);
				}
				
				imReturn.setReturnValue(JassIm.ImVarAccess(tempReturn));
				parent2.add(parentIndex, JassIm.ImSet(trace, tempReturn, returnExpr));
			}
			
		} else { // normal return
			
			addNullSetStmts(parent2, parentIndex, nullSetStmts);
		}
	}

	private boolean exprContainsVar(ImExpr returnExpr, final List<ImVar> handleVars) {
		final boolean[] result = { false };
		returnExpr.accept(new ImExpr.DefaultVisitor() {
			public void visit(ImVarAccess e) {
				if (handleVars.contains(e.getVar())) {
					result[0] = true;
				}
			}
			
		});
		return result[0];
	}
	
	private void addNullSetStmts(ImStmts parent, int parentIndex,
			List<ImStmt> nullSetStmts) {
		List<ImStmt> nullSetStmtsCopy = Lists.newArrayListWithCapacity(nullSetStmts.size());
		for (ImStmt s : nullSetStmts) {
			nullSetStmtsCopy.add((ImStmt) s.copy());
		}
		parent.addAll(parentIndex, nullSetStmtsCopy);
	}

	private boolean isHandleType(ImType type) {
		if (type instanceof ImSimpleType) {
			ImSimpleType imSimpleType = (ImSimpleType) type;
			return !primitiveTypes.contains(imSimpleType.getTypename());
		}
		return false;
	}
	
	
	
}
