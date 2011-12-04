package de.peeeq.wurstscript.jassoptimizer;

import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprBinary;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprStringVal;

public class StringExpressionPattern {

	private String regexp;

	public StringExpressionPattern(JassExpr e) {
		regexp = buildRegexp(e);
	}
	
	/**
	 * check if a given name matches this pattern
	 * @param name the name to check
	 * @return true, if the name matches this pattern
	 */
	public boolean check(String name) {
		return name.matches(regexp);
	}

	/**
	 * check if the pattern is constant
	 * @param name the name to check
	 * @return true, if the name matches this pattern
	 */
	public boolean isConst() {
		return regexp.matches("[a-zA-Z0-9_]+");
	}
	
	private String buildRegexp(JassExpr e) {
		if (e instanceof JassExprStringVal) {
			JassExprStringVal jassExprStringVal = (JassExprStringVal) e;
			// constant string-> match the string
			return jassExprStringVal.getVal();
		} else if (e instanceof JassExprFunctionCall) {
			JassExprFunctionCall call = (JassExprFunctionCall) e;
			if (call.getFuncName().equals("I2S")) {
				// if we have a I2S call we only match numbers:
				return "[0-9]+";
			}
		} else if (e instanceof JassExprBinary) {
			// if we have a combination of two strings, then combine the regexps
			JassExprBinary jassExprBinary = (JassExprBinary) e;
			return buildRegexp(((JassExprBinary) e).getLeft()) + buildRegexp(((JassExprBinary) e).getRight());			
		}
		// in all other cases ignore everything:
		return ".*";
	}
}
