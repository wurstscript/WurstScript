package de.peeeq.wurstscript.translation.imtranslation.purity;

public class ReadsGlobals extends PurityLevel {
  static ReadsGlobals instance = new ReadsGlobals();

  private ReadsGlobals() {}

  @Override
  public PurityLevel merge(PurityLevel other) {
    if (other instanceof ChangesTheWorld) {
      return other;
    } else if (other instanceof WritesGlobals) {
      return other;
    }
    return this;
  }
}
