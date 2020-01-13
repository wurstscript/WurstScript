package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImTypeClassFunc;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Either;

import java.util.Map;

public abstract class TypeClassFuncInstance {

    abstract TypeClassFuncInstance subst(Map<TypeClassFuncInstance, TypeClassFuncInstance> replacements);


    public abstract void addTypeClassBinding(ImTranslator tr, Map<ImTypeClassFunc, TypeClassFuncInstance> typeClassBinding);
}
