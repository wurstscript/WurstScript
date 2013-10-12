package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Set;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;

public class UsedVariables {
	
	public static Set<ImVar> calculate(ImFunction f) {
		final Set<ImVar> result = Sets.newLinkedHashSet();
		f.accept(new ImFunction.DefaultVisitor() {
			public void visit(ImSet e) {
				result.add(e.getLeft());
			}
			public void visit(ImSetArrayTuple e) {
				result.add(e.getLeft());
			}
			public void visit(ImSetArray e) {
				result.add(e.getLeft());
			}
			public void visit(ImSetTuple e) {
				result.add(e.getLeft());
			}
			public void visit(ImVarAccess e) {
				result.add(e.getVar());
			}
			public void visit(ImVarArrayAccess e) {
				result.add(e.getVar());
			}
		});
		return result;
	}
}
