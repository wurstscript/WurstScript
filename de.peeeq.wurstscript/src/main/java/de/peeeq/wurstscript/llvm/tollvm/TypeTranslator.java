package de.peeeq.wurstscript.llvm.tollvm;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.Ast;
import de.peeeq.wurstscript.llvm.ast.Type;
import de.peeeq.wurstscript.llvm.ast.TypeDef;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TypeTranslator implements ImType.Matcher<Type> {
    private LlvmTranslator tr;
    private Map<String, Type> usedTypes = new HashMap<>();

    public TypeTranslator(LlvmTranslator tr) {
        this.tr = tr;

        usedTypes.put("integer", Ast.TypeInt());
    }

    @Override
    public Type case_ImSimpleType(ImSimpleType t) {
        Type type = usedTypes.get(t.getTypename());
        if (type == null) {
            TypeDef td = Ast.TypeDef(t.getTypename(), true, Ast.StructFieldList());
            type = Ast.TypePointer(Ast.TypeRef(td));
            tr.addType(td);
            usedTypes.put(t.getTypename(), type);
        }
        return type;
    }

    @Override
    public Type case_ImArrayType(ImArrayType imArrayType) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Type case_ImArrayTypeMulti(ImArrayTypeMulti imArrayTypeMulti) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Type case_ImVoid(ImVoid imVoid) {
        return Ast.TypeVoid();
    }

    @Override
    public Type case_ImTupleType(ImTupleType imTupleType) {
        throw new RuntimeException("TODO");
    }
}
