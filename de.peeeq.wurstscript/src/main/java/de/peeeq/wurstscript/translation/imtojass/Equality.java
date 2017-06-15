package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.jassIm.*;

public class Equality {

    public static boolean equalValue(ImBoolVal v, ImConst other) {
        if (other instanceof ImBoolVal) {
            ImBoolVal ov = (ImBoolVal) other;
            return v.getValB() == ov.getValB();
        }
        return false;
    }

    public static boolean equalValue(ImFuncRef v, ImConst other) {
        if (other instanceof ImFuncRef) {
            ImFuncRef ov = (ImFuncRef) other;
            return v.getFunc() == ov.getFunc();
        }
        return false;
    }

    public static boolean equalValue(ImIntVal v, ImConst other) {
        if (other instanceof ImIntVal) {
            ImIntVal ov = (ImIntVal) other;
            return v.getValI() == ov.getValI();
        }
        return false;
    }

    public static boolean equalValue(ImNull v, ImConst other) {
        return other instanceof ImNull;
    }

    public static boolean equalValue(ImRealVal v, ImConst other) {
        if (other instanceof ImRealVal) {
            ImRealVal ov = (ImRealVal) other;
            return v.getValR().equals(ov.getValR());
        }
        return false;
    }

    public static boolean equalValue(ImStringVal v, ImConst other) {
        if (other instanceof ImStringVal) {
            ImStringVal ov = (ImStringVal) other;
            return v.getValS().equals(ov.getValS());
        }
        return false;
    }

}
