package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImTypeClassFunc;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Either;

import java.util.Map;

public abstract class TypeClassInstance {


    public static TypeClassInstance asSubtype(WurstTypeInterface interfaceType) {
        return new SubtypeTypeClassInstance(interfaceType);
    }

    public abstract void addTypeClassBinding(ImTranslator tr, Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> typeClassBinding);

    static class SubtypeTypeClassInstance extends TypeClassInstance {
        private WurstTypeInterface interfaceType;

        public SubtypeTypeClassInstance(WurstTypeInterface interfaceType) {
            this.interfaceType = interfaceType;
        }

        @Override
        public void addTypeClassBinding(ImTranslator tr, Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> typeClassBinding) {
            interfaceType.getMemberMethods(interfaceType.getDef()).forEach(fl -> {
                if (fl.getDef() instanceof FuncDef) {
                    FuncDef def = (FuncDef) fl.getDef();
                    ImTypeClassFunc tcf = tr.getTypeClassFuncFor(def);
                    typeClassBinding.put(tcf, Either.left(tr.getMethodFor(def)));
                }
            });
        }

        @Override
        public String toString() {
            return "SubtypeTypeClassInstance<" + interfaceType + ">";
        }
    }

}
