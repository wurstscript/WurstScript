package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;



/**
 * a JassInt int is a special int for jass-code and not used in pscript-code
 * The only difference is that in jass int is like a subtype of real 
 *
 */
public class WurstTypeJassInt extends WurstTypeInt {

	private static final WurstTypeJassInt instance = new WurstTypeJassInt();

	// make constructor private as we only need one instance
	private WurstTypeJassInt() {}
	
	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		return other instanceof WurstTypeInt || other instanceof WurstTypeReal;
	}

	public static WurstTypeJassInt instance() {
		return instance;
	}
	
}
