package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.utils.Utils;


public class WurstTypeArray extends WurstType {

	private WurstType baseType;
	private int[] sizes;
	
	public WurstTypeArray(WurstType baseType, int[] sizes) {
		if (baseType instanceof WurstTypeArray) {
			throw new Error("cannot have array of arrays...");
		}
		this.baseType = baseType;
		this.sizes = Utils.copyArray(sizes);
	}
	
	
	
	public WurstTypeArray(WurstType baseType) {
		this.baseType = baseType;
		this.sizes = new int[1];
	}



	public WurstType getBaseType() {
		return baseType;
	}



	public int getDimensions() {
		return sizes.length;
	}



	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		if (other instanceof WurstTypeArray) {
			WurstTypeArray otherArray = (WurstTypeArray) other;
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
	public String[] jassTranslateType() {
		return baseType.jassTranslateType();
	}



	@Override
	public ImType imTranslateType() {
		ImType bt = baseType.imTranslateType();
		if (bt instanceof ImSimpleType) {
			String typename = ((ImSimpleType) bt).getTypename();
			return JassIm.ImArrayType(typename);
		} else if (bt instanceof ImTupleType) {
			ImTupleType tt = (ImTupleType) bt;
			List<String> types = tt.getTypes();
			return JassIm.ImTupleArrayType(types);
		} else {
			throw new Error("cannot translate array type " + getName() + "  " + bt);
		}
	}

}
