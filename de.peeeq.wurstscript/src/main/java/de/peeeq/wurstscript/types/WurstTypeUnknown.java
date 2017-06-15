package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeUnknown extends WurstType {

    private static final WurstTypeUnknown instance = new WurstTypeUnknown("unknown");

    private String name = "unknown";

    public WurstTypeUnknown(String name) {
        this.name = name;
    }

    @Override
    public boolean isSubtypeOfIntern(WurstType other, Element location) {
        // unknown is a subtype of everything, so that we don't propagate errors
        return true;
    }


    @Override
    public String getName() {
        if (name.equals("empty")) {
            return "missing expression";
        } else if (name.equals("unknown")) {
            return "unknown type";
        }
        return "'unknown type'\n(the type " + name +
                " could not be found, the containing package might not be imported)";
    }

    @Override
    public String getFullName() {
        return getName();
    }

    public static WurstTypeUnknown instance() {
        return instance;
    }


    @Override
    public ImType imTranslateType() {
        throw new Error("not implemented");
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNoExpr();
    }

}
