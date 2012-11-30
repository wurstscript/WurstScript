package de.peeeq.wurstscript.translation.imoptimizer;

import java.util.HashMap;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctions;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarRead;
import de.peeeq.wurstscript.jassIm.ImVarWrite;
import de.peeeq.wurstscript.jassoptimizer.NameGenerator;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class ImCompressor {
	
	private ImTranslator translator;
	private ImProg prog;
	private NameGenerator ng;
	final HashMap<String, String> replacements;
	
	public ImCompressor(ImTranslator translator) {
		this.translator = translator;
		this.prog = translator.getImProg();
		ng = new NameGenerator();
		replacements = Maps.newHashMap();
	}
	
	public void compressNames() {
		System.out.println("Compressing Names......");
		compressGlobals();
		//compressFunctions();
		System.out.println("Compressing Names......DONE");
	}
	
	
	public void compressGlobals() {
		for ( final ImVar global : prog.getGlobals() ) {
			String replacement = ng.getUniqueToken();
			global.setName(replacement);
//			for ( ImVarWrite globalWrite : global.attrWrites()) {
//				globalWrite.getLeft().setName(replacement);
//			}
//			for ( ImVarRead globalRead : global.attrReads()) {
//				globalRead.getVar().setName(replacement);
//			}
			
		}
	}
	
	public void compressFunctions() {
		for( ImFunction func : prog.getFunctions() ) {
			if (! func.isNative() ) {
				String rname = ng.getUniqueToken();
				replacements.put(func.getName(), rname);
			}
		}
		
//		for( ImFunction func : prog.getFunctions() ) {
//			for (ImFunction called : translator.getCalledFunctions().get(func)) {
//				if (replacements.containsKey(called.getName()))
//					called.setName(replacements.get(called.getName()));
//			}
//		}
		
		for( ImFunction func : prog.getFunctions() ) {
			if (! func.isNative() ) {
				if (replacements.containsKey(func.getName()))
					func.setName(replacements.get(func.getName()));
			}
		}
		
	}
}
