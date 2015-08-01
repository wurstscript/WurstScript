package de.peeeq.wurstscript.translation.imoptimizer;

import java.util.Set;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarRead;
import de.peeeq.wurstscript.jassIm.ImVarWrite;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;

public class GlobalsInliner {
	
	private ImProg prog;

	
	
	
	
	

	public GlobalsInliner(ImTranslator translator) {
		this.prog = translator.getImProg();
	}
	

	public void inlineGlobals() {
		prog.clearAttributes(); // TODO only clear read/write attributes
		
		Set<ImVar> obsoleteVars = Sets.newLinkedHashSet();
		for ( final ImVar v : prog.getGlobals() ) {
//			WLogger.info("### " + v.getName() + " has " + v.attrWrites().size() + " writes");
			if (v.attrWrites().size() == 1) {
//				WLogger.info(">>>>>only 1 write");
				ImExpr right = null;
				ImVarWrite obs = null;
				for ( ImVarWrite v2 : v.attrWrites()) {
					ImFunction func = v2.getNearestFunc();
//					WLogger.info(">>>>>checking write..");
					if (func.getName().startsWith("init_") || func.getName().equals("main") ) {
//						WLogger.info(">>>>>in init or main");
						right = v2.getRight();
						obs = v2;
//						WLogger.info(">>>>>set");
						break;
					}
				}
				if( obs == null ) {
					continue;
				}
				ImExpr replacement;
				if (right instanceof ImIntVal) {
					ImIntVal val = (ImIntVal)right;
					replacement = (JassIm.ImIntVal(val.getValI()));
					if (obs.getParent() != null)
						obs.replaceWith(JassIm.ImNull());
				}else if (right instanceof ImRealVal) {
					ImRealVal val = (ImRealVal)right;
					replacement = (JassIm.ImRealVal(val.getValR()));
					if (obs.getParent() != null)
						obs.replaceWith(JassIm.ImNull());
				}else if (right instanceof ImStringVal) {
					ImStringVal val = (ImStringVal)right;
					replacement = (JassIm.ImStringVal(val.getValS()));
					if (obs.getParent() != null)
						obs.replaceWith(JassIm.ImNull());
				}else if (right instanceof ImBoolVal) {
					ImBoolVal val = (ImBoolVal)right;
					replacement = (JassIm.ImBoolVal(val.getValB()));
					if (obs.getParent() != null)
						obs.replaceWith(JassIm.ImNull());
				} else {
					replacement = null;
				}
				if (replacement != null) {
					for ( ImVarRead v3 : v.attrReads()) {
						v3.replaceWith(replacement.copy());
					}
				}
				if (replacement != null || v.attrReads().size() == 0) {
					obsoleteVars.add(v);
				}
			}
		}
		for (ImVar i : obsoleteVars) { 
			// remove the write
			ImVarWrite write = Utils.getFirstAndOnly(i.attrWrites());
//			WLogger.info("obsolete var: " + i + " written in " + write);
//			WLogger.info("parent" + write.getParent());
			if (write.getParent() != null) {
				write.replaceWith(write.getRight().copy());
			}
//			if (write.getParent() instanceof ImStmts) {
//				ImStmts stmts = (ImStmts) write.getParent();
//				WLogger.info("removing write " + write);
//				stmts.remove(write);
//			} else {
//				if (write.getParent() != null) {
//					throw new Error("unexpected parent: " + write.getParent());
//				}
//			}
		}
		prog.getGlobals().removeAll(obsoleteVars);
	}
	
	
	
}
