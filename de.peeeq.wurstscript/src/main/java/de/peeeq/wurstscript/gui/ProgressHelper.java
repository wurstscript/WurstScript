package de.peeeq.wurstscript.gui;

public class ProgressHelper {

	static public double getValidatorPercent(int done, int total) {
		return 0.2 + 0.3 * ((1.0*done)/total);
	}
	
}
