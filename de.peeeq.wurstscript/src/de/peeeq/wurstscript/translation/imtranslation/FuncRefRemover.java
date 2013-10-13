package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.WurstTypeCode;


/**
 * removes function references with variables
 * in order to avoid cyclic dependencies 
 */
public class FuncRefRemover {

	private ImProg prog;
	private ImTranslator tr;

	public FuncRefRemover(ImProg imProg, ImTranslator tr) {
		this.prog = imProg;
		this.tr = tr;
	}
	
	
	public void run() {
		final List<ImFuncRef> funcRefs = Lists.newArrayList();
		prog.accept(new ImProg.DefaultVisitor() {
			@Override
			public void visit(ImFuncRef imFuncRef) {
				funcRefs.add(imFuncRef);
			}
		});
		
		for (ImFuncRef fr : funcRefs) {
			ImVar g = JassIm.ImVar(WurstTypeCode.instance().imTranslateType(), 
					"ref_function_" + fr.getFunc().getName(), false);
			tr.addGlobalWithInitalizer(g, fr.copy());
			fr.replaceWith(JassIm.ImVarAccess(g));
		}
		
	}
}
