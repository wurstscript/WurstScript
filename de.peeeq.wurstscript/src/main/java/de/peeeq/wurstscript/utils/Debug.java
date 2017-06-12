package de.peeeq.wurstscript.utils;

public class Debug {

	private static final boolean DEBUG = false;

	public static void println(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

}
