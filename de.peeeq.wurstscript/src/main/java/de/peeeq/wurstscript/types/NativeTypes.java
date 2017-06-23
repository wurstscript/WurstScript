package de.peeeq.wurstscript.types;

import org.eclipse.jdt.annotation.Nullable;

public class NativeTypes {
    /**
     * returns the WurstScriptType for a given nativetype definition
     *
     * @param b
     */
    public static @Nullable WurstType nativeType(String typeName, boolean isJassCode) {
        if (typeName.equals("int") || typeName.equals("integer")) {
            return WurstTypeInt.instance();
        }
        if (typeName.equals("bool") || typeName.equals("boolean")) {
            return WurstTypeBool.instance();
        }
        if (typeName.equals("real")) {
            return WurstTypeReal.instance();
        }
        if (typeName.equals("string")) {
            return WurstTypeString.instance();
        }
        if (typeName.equals("code")) {
            return WurstTypeCode.instance();
        }
        if (typeName.equals("handle")) {
            return WurstTypeHandle.instance();
        }
        return null;
    }


}
