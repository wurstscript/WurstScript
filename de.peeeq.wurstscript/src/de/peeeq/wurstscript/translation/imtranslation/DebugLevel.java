package de.peeeq.wurstscript.translation.imtranslation;

public class DebugLevel {
	public boolean methodDispatchChecks;
	public boolean useStackTraces;
	public boolean checkedCasts;
	public boolean checkedDivision;
	public boolean checkedArrays;

	public static final DebugLevel DEFAULT = new DebugLevel() {{
		methodDispatchChecks = true;
	}};
	
	
	

}
