package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Sets;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStatementExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarWrite;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Debug;
import de.peeeq.wurstscript.jassIm.*;

public class GlobalsInliner {
	
	private ImTranslator translator;
	private ImProg prog;
	private List<ImVar> globals = Lists.newArrayList();
	private List<ImStmt> setExprs = Lists.newArrayList();
	private Set<ImVar> unused = Sets.newHashSet();
	
	
	ImSet s = null;
	ImSetArray sa = null;
	
	boolean notUsed = true;

	public GlobalsInliner(ImTranslator translator) {
		this.translator = translator;
		this.prog = translator.getImProg();
	}
	
	public void inlineGlobals() {
		collectGlobals();
	}

	private void collectGlobals() {
		for ( final ImVar v : prog.getGlobals() ) {
			if (v.attrWrites().size() == 1) {
				System.out.println(">>>>>only 1 write");
				boolean valid = false;
				ImVar theVar = null;
				for ( ImVarWrite v2 : v.attrWrites()) {
					ImFunction func = v2.getNearestFunc();
					System.out.println(">>>>>checking write..");
					if (func.getName().startsWith("init_") || func.getName().equals("main") ) {
						System.out.println(">>>>>in init or main");
						valid = true;
						theVar = v2.getLeft();
						System.out.println(">>>>>set");
						break;
					}
				}
				if( valid ) {
					for ( ImVarRead v3 : v.attrReads()) {
						
						v3.getParent().set(0, theVar);
					}
				}
			}
		}
	}
	
	void replace(JassImElement elem, JassImElement newElem) {
		JassImElement parent = elem.getParent();
		for (int i=0; i<parent.size(); i++) {
			if (parent.get(i) == elem) {
				parent.set(i, ((ImExpr) newElem).copy());
				break;
			}
		}
	}
	
	
}
