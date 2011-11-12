package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.util.Vector;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstAbstract;
import de.peeeq.wurstscript.types.NativeTypes;
import de.peeeq.wurstscript.types.PscriptType;

public class ILarray extends ILconstAbstract {

	private static final int maxsize = 8191;
	Vector<ILconst> values = new Vector<ILconst>(16);
	PscriptType type;
	
	public ILarray(PscriptType pscriptType) {
		this.type = pscriptType;
	}
	
	

	public void set(int index, ILconst value) {
		if (index < 0 || index >= maxsize) {
			throw new Error("Array index out of bounds: " + index);
		}
		if (index >= values.size()) {
			values.setSize(index+1);
		}
		values.set(index, value);
	}
	
	public ILconst get(int index) {
		if (index < 0 || index >= maxsize) {
			throw new Error("Array index out of bounds: " + index);
		}
		ILconst result = null;
		if (index < values.size()) {
			result = values.get(index);
		} 
		if (result == null) {
			// TODO return default value for arraytype
			return NativeTypes.getDefaultValue(type);
		}		
		return result;
	}
	
	
	@Override
	public String print() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ILconst c : values) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(c);
			first = false;
		}
		return "array(" + sb + ")";
	}


	@Override
	public void printJass(StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}


	@Override
	public PscriptType getType() {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}



	@Override
	public boolean isEqualTo(ILconst other) {
		throw new Error("Cannot compare arrays.");
	}

}
