package de.peeeq.wurstscript.types;


public class PScriptTypeArray extends PscriptType {

	private PscriptType baseType;
	private int[] sizes;
	
	public PScriptTypeArray(PscriptType baseType, int[] sizes) {
		this.baseType = baseType;
		this.sizes = sizes;
	}
	
	
	
	public PscriptType getBaseType() {
		return baseType;
	}



	public int getDimensions() {
		return sizes.length;
	}



	@Override
	public boolean isSubtypeOf(PscriptType other) {
		if (other instanceof PScriptTypeArray) {
			PScriptTypeArray otherArray = (PScriptTypeArray) other;
			return baseType.equalsType(otherArray.baseType) && getDimensions() == otherArray.getDimensions();
		}
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return baseType.getName() + " array(dimensions = " + getDimensions() + ")";
	}

	@Override
	public String getFullName() {
		return getName();
	}



	public int getSize(int i) {
		return sizes[i];
	}

}
