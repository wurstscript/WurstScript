package de.peeeq.wurstscript.translation.imtojass;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.jassIm.*;

import java.util.stream.Collectors;

/**
 *
 */
public class TypeRewriteMatcher implements ImType.Matcher<ImType> {

    @Override
    public ImType case_ImVoid(ImVoid t) {
        return t;
    }

    @Override
    public ImType case_ImArrayTypeMulti(ImArrayTypeMulti t) {
        return JassIm.ImArrayTypeMulti(t.getEntryType().match(this), ImmutableList.copyOf(t.getArraySize()));
    }

    @Override
    public ImType case_ImAnyType(ImAnyType t) {
        return t;
    }

    @Override
    public ImType case_ImTupleType(ImTupleType t) {
        return JassIm.ImTupleType(t.getTypes()
                        .stream()
                        .map(tt -> tt.match(this))
                        .collect(Collectors.toList()),
                ImmutableList.copyOf(t.getNames()));
    }

    @Override
    public ImType case_ImTypeVarRef(ImTypeVarRef t) {
        return t;
    }

    @Override
    public ImType case_ImSimpleType(ImSimpleType t) {
        return t;
    }

    @Override
    public ImType case_ImArrayType(ImArrayType t) {
        return JassIm.ImArrayType(t.getEntryType().match(this));
    }

    @Override
    public ImType case_ImClassType(ImClassType t) {
        ImTypeArguments args = t.getTypeArguments()
                .stream()
                .map(ta -> JassIm.ImTypeArgument(ta.getType().match(this), ta.getTypeClassBinding()))
                .collect(Collectors.toCollection(JassIm::ImTypeArguments));
        return JassIm.ImClassType(t.getClassDef(), args);
    }
}
