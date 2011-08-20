package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.ast.CompilationUnitPos;

/**
 * translates an AST into the intermediate language 
 */
public class IntermediateLangTranslator {

	private CompilationUnitPos cu;
	private ILprog prog = new ILprog();
	
	public IntermediateLangTranslator(CompilationUnitPos cu) {
		this.cu = cu;
	}
	
	public ILprog translate() {
		
		// TODO translate to intermediate language
		
		
		
		return prog;
		
	}
	
	
	
	
}
