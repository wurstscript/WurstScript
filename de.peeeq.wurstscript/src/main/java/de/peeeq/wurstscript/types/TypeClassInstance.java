package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Either;

import java.util.Map;

public abstract class TypeClassInstance {


    public static TypeClassInstance asSubtype(WurstTypeInterface interfaceType) {
        return new SubtypeTypeClassInstance(interfaceType);
    }


    static class SubtypeTypeClassInstance extends TypeClassInstance {
        private WurstTypeInterface interfaceType;

        public SubtypeTypeClassInstance(WurstTypeInterface interfaceType) {
            this.interfaceType = interfaceType;
        }


        @Override
        public String toString() {
            return "SubtypeTypeClassInstance<" + interfaceType + ">";
        }
    }

}
