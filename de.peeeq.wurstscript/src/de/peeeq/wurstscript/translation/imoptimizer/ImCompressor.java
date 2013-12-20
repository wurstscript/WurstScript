package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class ImCompressor {
	
	private ImTranslator translator;
	private ImProg prog;
	private NameGenerator ng;
	
	public ImCompressor(ImTranslator translator) {
		this.translator = translator;
		this.prog = translator.getImProg();
		ng = new NameGenerator();
	}
	
	public void compressNames() {
		compressGlobals();
		compressFunctions();
	}
	
	
	public void compressGlobals() {
		for ( final ImVar global : prog.getGlobals() ) {
			if (global.getIsBJ()) {
				// no not rename bj constants
				
				continue;
			}
			
			String replacement = ng.getUniqueToken();
			
			global.setName(replacement);
		}
	}
	
	public void compressFunctions() {
		for( ImFunction func : prog.getFunctions() ) {
			if (func.isNative() || func.isBj() || func.isCompiletime() || func.isExtern()) {
				// do not rename builtin an bj functions
				continue;
			}
			compressLocals(func);			
			if (func.getName().equals("main") || func.getName().equals("config")) {
				// do not rename main and config functions
				continue;
			}
			String rname = ng.getUniqueToken();
			func.setName(rname);
		}
		
	}

	private void compressLocals(ImFunction func) {
		// TODO compressing locals should not use the global name pool but use a own pool
		for (ImVar local : func.getParameters()) {
			local.setName(ng.getUniqueToken());
		}
		for (ImVar local : func.getLocals()) {
			local.setName(ng.getUniqueToken());
		}
	}
}
