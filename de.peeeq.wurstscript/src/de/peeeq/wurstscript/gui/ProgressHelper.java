package de.peeeq.wurstscript.gui;

public class ProgressHelper {

	static public double getValidatorPercent(int done, int total) {
		return 0.1 + 0.8 * ((1.0*done)/total);
	}
	
}
