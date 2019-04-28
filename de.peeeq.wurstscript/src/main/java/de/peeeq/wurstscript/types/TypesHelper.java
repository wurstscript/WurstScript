package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.jassIm.*;

public class TypesHelper {

    private static final ImSimpleType intType = WurstTypeInt.instance().imTranslateType();

    private static final ImSimpleType realType = WurstTypeReal.instance().imTranslateType();


    public static ImSimpleType imInt() {
        return intType;
    }

    public static ImSimpleType imReal() {
        return realType;
    }

    public static ImSimpleType imString() {
        return WurstTypeString.instance().imTranslateType();
    }

    public static ImType imVoid() {
        return WurstTypeVoid.instance().imTranslateType();
    }


    public static ImType imBool() {
        return WurstTypeBool.instance().imTranslateType();
    }

    public static ImType imHashTable() {
        return JassIm.ImSimpleType("hashtable");
    }

    public static ImArrayType imIntArray() {
        return JassIm.ImArrayType(imInt());
    }

    public static ImArrayType imStringArray() {
        return JassIm.ImArrayType(imString());
    }

    public static boolean typeContainsTuples(ImType vt) {
        return vt instanceof ImTupleType
                || vt instanceof ImArrayType && typeContainsTuples(((ImArrayType) vt).getEntryType())
                || vt instanceof ImArrayTypeMulti && typeContainsTuples(((ImArrayTypeMulti) vt).getEntryType());
    }

    public static boolean isIntType(ImType t) {
        if (t instanceof ImSimpleType) {
            return ((ImSimpleType) t).getTypename().equals("integer");
        }
        return false;
    }

    public static boolean isRealType(ImType t) {
        if (t instanceof ImSimpleType) {
            return ((ImSimpleType) t).getTypename().equals("real");
        }
        return false;
    }

    public static boolean isBoolType(ImType t) {
        if (t instanceof ImSimpleType) {
            return ((ImSimpleType) t).getTypename().equals("boolean");
        }
        return false;
    }

    public static ImSimpleType imTrigger() {
        return JassIm.ImSimpleType("trigger");
    }


//	public static boolean checkTypeArgs(InstanceDef iDef, List<PscriptType> classParams, List<PscriptType> interfaceParams) {
//		if (classParams.size() == 0 && interfaceParams.size() == 0) {
//			return true;
//		}
//		
//		Map<TypeParamDef, PscriptType> typeParamBinding = Maps.newLinkedHashMap(); 
//		
//		PscriptTypeClass classType = (PscriptTypeClass) iDef.getClassTyp().attrTyp();
//		int i = 0;
//		for (PscriptType classTp : classType.getTypeParameters()) {
//			PscriptTypeTypeParam classTp2 = (PscriptTypeTypeParam) classTp;
//			TypeParamDef tpDef = classTp2.getDef();
//			typeParamBinding.put(tpDef, classParams.get(i));
//			i++;
//		}
//		
//		ArrayList<PscriptType> actualInterfaceTypes = Lists.newArrayList();
//		
//		PscriptTypeInterface interfaceType = (PscriptTypeInterface) iDef.getImplementedTyp().attrTyp();
//		for (PscriptType intTp : interfaceType.getTypeParameters()) {
//			if (intTp instanceof PscriptTypeTypeParam) {
//				PscriptTypeTypeParam intTp2 = (PscriptTypeTypeParam) intTp;
//				actualInterfaceTypes.add(typeParamBinding.get(intTp2.getDef()));
//			} else {
//				actualInterfaceTypes.add(intTp);
//			}
//		}
//		
//		if (actualInterfaceTypes.size() != interfaceParams.size()) {
//			throw new CompileError(iDef.getSource(), "sizes do not match: " + actualInterfaceTypes.size()+ " != " + interfaceParams.size());
//		}
//		
//		i = 0;
//		for (PscriptType iTp : interfaceParams) {
//			if (!iTp.equalsType(actualInterfaceTypes.get(i), iDef)) {
//				return false;
//			}
//			i++;
//		}
//		
//		
//		return true;
//	}


}
