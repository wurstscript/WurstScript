package de.peeeq.wurstscript.translation.imtojass;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.List;
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
        List<ImType> list = new ArrayList<>();
        for (ImType tt : t.getTypes()) {
            ImType match = tt.match(this);
            list.add(match);
        }
        return JassIm.ImTupleType(list,
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
        ImTypeArguments args = JassIm.ImTypeArguments();
        for (ImTypeArgument ta : t.getTypeArguments()) {
            args.add(rewriteTypeArgument(ta));
        }
        return JassIm.ImClassType(t.getClassDef(), args);
    }

    /**
     * Rewrites one type argument. Only the type is rewritten by default, since a plain rewrite has
     * nothing to say about what is known of the argument. Subclasses which do -- substitution, where
     * the replacing argument carries its own binding -- override this.
     */
    protected ImTypeArgument rewriteTypeArgument(ImTypeArgument ta) {
        return JassIm.ImTypeArgument(ta.getType().match(this), ta.getTypeClassBinding());
    }
}
