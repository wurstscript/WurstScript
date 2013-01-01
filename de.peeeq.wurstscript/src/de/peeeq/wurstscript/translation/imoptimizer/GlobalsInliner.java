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

public class GlobalsInliner {
	
	private ImTranslator translator;
	private ImProg prog;

	
	ImSet s = null;
	ImSetArray sa = null;
	
	boolean notUsed = true;

	public GlobalsInliner(ImTranslator translator) {
		this.translator = translator;
		this.prog = translator.getImProg();
	}
	

	public void inlineGlobals() {
		Set<ImVar> obsoleteVars = Sets.newHashSet();
		for ( final ImVar v : prog.getGlobals() ) {
			if (v.attrWrites().size() == 1) {
				System.out.println(">>>>>only 1 write");
				boolean valid = false;
				ImExpr right = null;
				ImVarWrite obs = null;
				for ( ImVarWrite v2 : v.attrWrites()) {
					ImFunction func = v2.getNearestFunc();
					System.out.println(">>>>>checking write..");
					if (func.getName().startsWith("init_") || func.getName().equals("main") ) {
						System.out.println(">>>>>in init or main");
						valid = true;
						right = v2.getRight();
						obs = v2;
						System.out.println(">>>>>set");
						break;
					}
				}
				if( valid ) {
					for ( ImVarRead v3 : v.attrReads()) {
						if (right instanceof ImIntVal) {
							System.out.println("replaced");
							ImIntVal val = (ImIntVal)right;
							v3.replaceWith(JassIm.ImIntVal(val.getValI()));
							if (obs.getParent() != null)
								obs.replaceWith(JassIm.ImNull());
							obsoleteVars.add(v);
						}else if (right instanceof ImRealVal) {
							System.out.println("replaced");
							ImRealVal val = (ImRealVal)right;
							v3.replaceWith(JassIm.ImRealVal(val.getValR()));
							if (obs.getParent() != null)
								obs.replaceWith(JassIm.ImNull());
							obsoleteVars.add(v);
						}else if (right instanceof ImStringVal) {
							System.out.println("replaced");
							ImStringVal val = (ImStringVal)right;
							v3.replaceWith(JassIm.ImStringVal(val.getValS()));
							if (obs.getParent() != null)
								obs.replaceWith(JassIm.ImNull());
							obsoleteVars.add(v);
						}else if (right instanceof ImBoolVal) {
							System.out.println("replaced");
							ImBoolVal val = (ImBoolVal)right;
							v3.replaceWith(JassIm.ImBoolVal(val.getValB()));
							if (obs.getParent() != null)
								obs.replaceWith(JassIm.ImNull());
							obsoleteVars.add(v);
						}
						
						
					}
				}
			}
		}
		prog.getGlobals().removeAll(obsoleteVars);
	}
	
	
	
}
