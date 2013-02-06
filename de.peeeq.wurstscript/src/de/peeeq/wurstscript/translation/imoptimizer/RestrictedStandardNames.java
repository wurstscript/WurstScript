package de.peeeq.wurstscript.translation.imoptimizer;

public class RestrictedStandardNames {
	static String names[] = { "InitGlobals",
		"InitCustomPlayerSlots",
		"InitCustomTeams",
		"main",
		"config" };
	
	public static boolean contains(String s) {
		for (int i = 0; i <= 4; i++) {
			if ( names[i].equals(s)) {
				return true;
			}
		}
		return false;
	}
}
