package de.peeeq.wurstscript.translation.imtranslation.purity;

import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.JassImElement;

public class PurityLevels {

	public static PurityLevel calculate(ImStmt s) {
		return mergeWithChildren(s, Pure.instance);
	}

	private static PurityLevel mergeWithChildren(JassImElement e, PurityLevel level) {
		for (int i=0; i<e.size(); i++) {
			JassImElement child = e.get(i);
			if (child instanceof ImStmt) {
				ImStmt imStmt = (ImStmt) child;
				level = level.merge(imStmt.attrPurity());
			} else {
				level = mergeWithChildren(child, level);
			}
		}
		return level;
	}
	
	public static PurityLevel calculate(ImCall s) {
		return ChangesTheWorld.instance;
	}
	
	public static PurityLevel calculate(ImSet s) {
		return mergeWithChildren(s, WritesGlobals.instance);
	}
	
	public static PurityLevel calculate(ImSetArray s) {
		return mergeWithChildren(s, WritesGlobals.instance);
	}
	
	public static PurityLevel calculate(ImSetTuple s) {
		return mergeWithChildren(s, WritesGlobals.instance);
	}
	
	public static PurityLevel calculate(ImSetArrayTuple s) {
		return mergeWithChildren(s, WritesGlobals.instance);
	}
	
	public static PurityLevel calculate(ImVarAccess s) {
		return mergeWithChildren(s, ReadsGlobals.instance);
	}

	public static PurityLevel calculate(ImVarArrayAccess s) {
		return mergeWithChildren(s, ReadsGlobals.instance);
	}
	
}
