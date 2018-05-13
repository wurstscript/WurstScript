package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 *
 */
public class WurstTypeDeferred extends WurstType {
    private final Supplier<WurstType> t;
    private WurstType ty;

    public WurstTypeDeferred(Supplier<WurstType> t) {
        this.t = t;
    }

    @Override
    public boolean isSubtypeOfIntern(WurstType other, @Nullable Element location) {
        return force().isSubtypeOf(other, location);
    }

    @Override
    public String getName() {
        if (ty != null) {
            return ty.getName();
        }
        return "deferred type";
    }

    @Override
    public String getFullName() {
        if (ty != null) {
            return ty.getFullName();
        }
        return "deferred type";
    }

    @Override
    public ImType imTranslateType() {
        return force().imTranslateType();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return force().getDefaultValue();
    }

    public WurstType force() {
        if (ty != null) {
            return ty;
        }
        return t.get();
    }


    @Override
    public WurstType dynamic() {
        return force().dynamic();
    }

    @Override
    public WurstType setTypeArgs(Map<TypeParamDef, WurstTypeBoundTypeParam> typeParamMapping) {
        return new WurstTypeDeferred(() -> force().setTypeArgs(typeParamMapping));
    }

    @Override
    public Map<TypeParamDef, WurstTypeBoundTypeParam> getTypeArgBinding() {
        return force().getTypeArgBinding();
    }

    @Override
    public boolean isVoid() {
        return force().isVoid();
    }

    @Override
    public boolean canBeUsedInInstanceOf() {
        return force().canBeUsedInInstanceOf();
    }

    @Override
    public boolean allowsDynamicDispatch() {
        return force().allowsDynamicDispatch();
    }

    @Override
    public void addMemberMethods(Element node, String name, List<FuncLink> result) {
        force().addMemberMethods(node, name, result);
    }

    @Override
    public Stream<NameLink> getMemberMethods(Element node) {
        return force().getMemberMethods(node);
    }

    @Override
    public boolean isStaticRef() {
        return force().isStaticRef();
    }

    @Override
    public boolean isTranslatedToInt() {
        return force().isTranslatedToInt();
    }

    @Override
    public boolean isCastableToInt() {
        return force().isCastableToInt();
    }

//    @Override
//    public WurstType normalize() {
//        return force().normalize();
//    }

    @Override
    public boolean supportsGenerics() {
        return force().supportsGenerics();
    }

    @Override
    public WurstType typeUnion(WurstType t, Element loc) {
        return force().typeUnion(t, loc);
    }

    @Override
    public boolean isNestedInside(WurstType other) {
        return force().isNestedInside(other);
    }
}
