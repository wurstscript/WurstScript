package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;



/**
 * a JassInt int is a special int for jass-code and not used in pscript-code
 * The only difference is that in jass int is like a subtype of real 
 *
 */
public class PScriptTypeJassInt extends PScriptTypeInt {

	private static final PScriptTypeJassInt instance = new PScriptTypeJassInt();

	// make constructor private as we only need one instance
	private PScriptTypeJassInt() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeInt || other instanceof PScriptTypeReal;
	}

	public static PScriptTypeJassInt instance() {
		return instance;
	}
	
}
