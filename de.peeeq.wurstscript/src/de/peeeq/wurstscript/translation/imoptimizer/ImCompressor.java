package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassoptimizer.NameGenerator;
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
		System.out.println("Compressing Names......");
		compressGlobals();
		//compressFunctions();
		System.out.println("Compressing Names......DONE");
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
			if (func.isNative() || func.isBj() || func.isCompiletime()) {
				// do not rename builtin an bj functions
				continue;
			}
			String rname = ng.getUniqueToken();
			func.setName(rname);
		}
		
	}
}
