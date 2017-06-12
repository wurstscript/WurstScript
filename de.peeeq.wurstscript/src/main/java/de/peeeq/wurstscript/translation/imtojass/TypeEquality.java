package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImArrayTypeMulti;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVoid;

public class TypeEquality {

	public static boolean isEqualType(ImArrayType a, ImType b) {
		if (b instanceof ImArrayType) {
			ImArrayType at = (ImArrayType) b;
			return at.getTypename().equals(a.getTypename());
		}
		return false;
	}
	
	public static boolean isEqualType(ImArrayTypeMulti a, ImType b) {
		if (b instanceof ImArrayTypeMulti) {
			ImArrayTypeMulti at = (ImArrayTypeMulti) b;
			// TODO check dimensions
			return at.getTypename().equals(a.getTypename());
		}
		return false;
	}
	
	public static boolean isEqualType(ImSimpleType a, ImType b) {
		if (b instanceof ImSimpleType) {
			ImSimpleType at = (ImSimpleType) b;
			// TODO check dimensions
			return at.getTypename().equals(a.getTypename());
		}
		return false;
	}
	
	public static boolean isEqualType(ImTupleArrayType a, ImType b) {
		if (b instanceof ImTupleArrayType) {
			ImTupleArrayType at = (ImTupleArrayType) b;
			if (at.getTypes().size() != a.getTypes().size()) {
				return false;
			}
			for (int i=0; i<a.getTypes().size(); i++) {
				if (!a.getTypes().get(i).equalsType(at.getTypes().get(i))) {
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isEqualType(ImTupleType a, ImType b) {
		if (b instanceof ImTupleType) {
			ImTupleType at = (ImTupleType) b;
			if (at.getTypes().size() != a.getTypes().size()) {
				return false;
			}
			for (int i=0; i<a.getTypes().size(); i++) {
				if (!a.getTypes().get(i).equalsType(at.getTypes().get(i))) {
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isEqualType(ImVoid a, ImType b) {
		return b instanceof ImVoid;
	}
	

}
