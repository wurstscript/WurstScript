package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;

public abstract class OtherLink extends NameLink {
    private final String name;
    private final WurstType type;
    private final @Nullable WPos source; // optional

    public OtherLink(Visibility visibility, String name, WurstType type) {
        this(visibility, name, type, null);
    }

    public OtherLink(Visibility visibility, String name, WurstType type, @Nullable WPos source) {
        super(visibility, /* def = */ null, Collections.emptyList());
        this.name = name;
        this.type = type;
        this.source = source;
    }

    @Override public String getName() { return name; }
    @Override public WurstType getTyp() { return type; }

    /** Synthetic links have no backing NameDef. */
    @Override public NameDef getDef() {
        throw new UnsupportedOperationException("Synthetic link has no backing NameDef");
    }

    @Override public NameLink withVisibility(Visibility newVis) { return this; }
    @Override public boolean receiverCompatibleWith(WurstType receiverType, Element location) { return false; }
    @Override public NameLink withTypeArgBinding(Element context, VariableBinding binding) { return this; }
    @Override public NameLink withDef(NameDef actual) { return this; }

    public @Nullable WPos getSyntheticSource() { return source; }

    /** Implement the behavior (e.g., array length) in subclasses/anonymous classes. */
    public abstract ImExpr translate(NameRef e, ImTranslator t, ImFunction f);
}
