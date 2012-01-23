package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeArray extends PscriptType {

	private PscriptType baseType;
	private int[] sizes;
	
	public PScriptTypeArray(PscriptType baseType, int[] sizes) {
		if (baseType instanceof PScriptTypeArray) {
			throw new Error("cannot have array of arrays...");
		}
		this.baseType = baseType;
		this.sizes = sizes;
	}
	
	
	
	public PScriptTypeArray(PscriptType baseType) {
		this.baseType = baseType;
		this.sizes = new int[1];
	}



	public PscriptType getBaseType() {
		return baseType;
	}



	public int getDimensions() {
		return sizes.length;
	}



	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		if (other instanceof PScriptTypeArray) {
			PScriptTypeArray otherArray = (PScriptTypeArray) other;
			return baseType.equalsType(otherArray.baseType, location) && getDimensions() == otherArray.getDimensions();
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



	@Override
	public String printJass() {
		return baseType.getName() + " array";
	}

}
