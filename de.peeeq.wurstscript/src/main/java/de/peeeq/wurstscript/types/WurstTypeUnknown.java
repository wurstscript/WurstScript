package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;


public class WurstTypeUnknown extends WurstType {

    private static final WurstTypeUnknown instance = new WurstTypeUnknown("unknown");

    private String name;

    public WurstTypeUnknown(String name) {
        this.name = name;
    }


    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        // unknown is a subtype of everything, so that we don't propagate errors
        return mapping;
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
    public ImType imTranslateType(ImTranslator tr) {
        throw new Error("not implemented");
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNoExpr();
    }

    @Override
    protected boolean isNullable() {
        return false;
    }

}
