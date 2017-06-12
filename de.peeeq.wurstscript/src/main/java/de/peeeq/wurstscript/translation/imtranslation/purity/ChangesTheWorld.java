package de.peeeq.wurstscript.translation.imtranslation.purity;

public class ChangesTheWorld extends PurityLevel {
	static ChangesTheWorld instance = new ChangesTheWorld();
	private ChangesTheWorld() {}
	
	@Override
	public PurityLevel merge(PurityLevel other) {
		return this;
	}
}
