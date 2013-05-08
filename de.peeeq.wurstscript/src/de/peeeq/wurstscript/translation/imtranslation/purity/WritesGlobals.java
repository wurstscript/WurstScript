package de.peeeq.wurstscript.translation.imtranslation.purity;

public class WritesGlobals extends PurityLevel {
	static WritesGlobals instance = new WritesGlobals();
	private WritesGlobals() {}
	
	@Override
	public PurityLevel merge(PurityLevel other) {
		if (other instanceof ChangesTheWorld) {
			return other;
		}
		return this;
	}
}
