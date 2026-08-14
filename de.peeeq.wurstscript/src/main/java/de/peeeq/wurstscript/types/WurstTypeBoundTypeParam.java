package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.ImplicitFuncs;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Either;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.peeeq.wurstscript.types.VariablePosition.NONE;

public class WurstTypeBoundTypeParam extends WurstType {


    private final TypeParamDef typeParamDef;
    private final WurstType baseType;
    // the fromIndex and toIndex functions for old-generics:
    private FuncDef fromIndex;
    private FuncDef toIndex;
    private final @Nullable Map<FuncDef, FuncLink> typeConstraintFunctions;
    private boolean indexInitialized = false;
    private final Element context;

    public WurstTypeBoundTypeParam(TypeParamDef def, WurstType baseType, Element context) {
        if (baseType instanceof WurstTypeIntLiteral) {
            baseType = WurstTypeInt.instance();
        }
        this.typeParamDef = def;
        this.baseType = baseType;
        this.context = context;
        if (def.getTypeParamConstraints() instanceof NoTypeParamConstraints) {
            this.typeConstraintFunctions = null;
        } else {
            this.typeConstraintFunctions = new HashMap<>();
        }
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        return baseType.matchAgainstSupertypeIntern(other, location, mapping, NONE);
    }

    @Override
    public String getName() {
        return baseType.getName();
//		return "[" + typeParamDef.getName() + ": " + baseType + "]";
    }

    @Override
    public String getFullName() {
        return typeParamDef.getName() + "<--" + baseType.getFullName();
    }


    public WurstType getBaseType() {
        return baseType;
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        return baseType.imTranslateType(tr);
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImIntVal(0);
    }


    @Override
    public WurstType dynamic() {
        return baseType.dynamic();
    }

    @Override
    public boolean canBeUsedInInstanceOf() {
        return baseType.canBeUsedInInstanceOf();
    }

    @Override
    public boolean allowsDynamicDispatch() {
        return baseType.allowsDynamicDispatch();
    }

    @Override
    public void addMemberMethods(Element node, String name,
                                 List<FuncLink> result) {
        baseType.addMemberMethods(node, name, result);
    }

    @Override
    public Stream<FuncLink> getMemberMethods(Element node) {
        return baseType.getMemberMethods(node);
    }

    @Override
    public boolean isStaticRef() {
        return baseType.isStaticRef();
    }

    @Override
    public boolean isCastableToInt() {
        return true; // because baseType must always be castable to int
        //return baseType.isCastableToInt();
    }

    @Override
    public WurstType normalize() {
        return baseType.normalize();
    }

    public FuncDef getFromIndex() {
        initIndex();
        return fromIndex;
    }

    public FuncDef getToIndex() {
        initIndex();
        return toIndex;
    }

    private void initIndex() {
        if (indexInitialized) {
            return;
        }
        if (typeConstraintFunctions == null) {
            if (!baseType.supportsGenerics()) {
                // if type does support generics natively, try to find implicit conversion functions
                fromIndex = ImplicitFuncs.findFromIndexFunc(baseType, context);
                toIndex = ImplicitFuncs.findToIndexFunc(baseType, context);
            } else if (baseType instanceof WurstTypeBoundTypeParam) {
                WurstTypeBoundTypeParam bt = (WurstTypeBoundTypeParam) baseType;
                fromIndex = bt.getFromIndex();
                toIndex = bt.getToIndex();
            }
        }
        indexInitialized = true;
    }

    @Override
    public boolean supportsGenerics() {
        return baseType.supportsGenerics()
                || getFromIndex() != null && getToIndex() != null;
    }

    @Override
    protected boolean isNullable() {
        return baseType.isNullable();
    }

    @Override
    public WurstTypeBoundTypeParam setTypeArgs(VariableBinding typeParamMapping) {
        return this.withBaseType(baseType.setTypeArgs(typeParamMapping));
    }

//	public WurstTypeBoundTypeParam applyBinding(Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
//		if (baseType instanceof WurstTypeTypeParam) {
//			WurstTypeTypeParam tp = (WurstTypeTypeParam) baseType;
//			if (binding.containsKey(tp.getDef())) {
//				return this.withBaseType(binding.get(tp.getDef()));
//			}
//		} else if (baseType instanceof WurstTypeBoundTypeParam) {
//			WurstTypeBoundTypeParam bt = (WurstTypeBoundTypeParam) baseType;
//			return this.withBaseType(bt.applyBinding(binding));
//		}
//		return this.baseType.
//		return this;
//	}

    private WurstTypeBoundTypeParam withBaseType(WurstType t) {
        if (t == baseType) {
            return this;
        }
        return new WurstTypeBoundTypeParam(typeParamDef, t, context);
    }

    public TypeParamDef getTypeParamDef() {
        return typeParamDef;
    }

    @Override
    public boolean isTranslatedToInt() {
        return baseType.isTranslatedToInt();
    }

    public @Nullable Map<FuncDef, FuncLink> getTypeConstraintFunctions() {
        return typeConstraintFunctions;
    }

    public boolean isTemplateTypeParameter() {
        return typeParamDef.getTypeParamConstraints() instanceof TypeExprList;
    }

    public ImTypeArgument imTranslateToTypeArgument(ImTranslator tr) {
        return JassIm.ImTypeArgument(imTranslateType(tr), imTypeClassBinding(tr));
    }

    /**
     * Binds every requirement of this type parameter's bounds to the function supplied by the
     * instance chosen for the type it is bound to.
     * <p>
     * Used for both function and class type arguments, so that a bound on a generic class works the
     * same way as one on a generic function. Stays empty while the bound type is itself abstract:
     * the enclosing generic is specialised first, and the binding is inherited at that point.
     */
    public Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> imTypeClassBinding(ImTranslator tr) {
        Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> binding = new HashMap<>();
        List<InterfaceDef> bounds = TypeClassConstraints.boundInterfaces(typeParamDef);
        if (bounds.isEmpty()) {
            return binding;
        }
        WurstType concrete = baseType.normalize();
        if (concrete instanceof WurstTypeTypeParam || concrete instanceof WurstTypeBoundTypeParam) {
            return binding;
        }
        for (InterfaceDef iface : bounds) {
            InstanceDecl instance = TypeClassInstances.find(iface, concrete);
            if (instance == null) {
                // the validator reports the unsatisfied bound; do not fail translation as well
                continue;
            }
            for (FuncDef requirement : iface.getMethods()) {
                FuncDef impl = TypeClassInstances.findImplementation(instance, requirement, concrete);
                if (impl != null) {
                    binding.put(tr.getTypeClassFunc(requirement), Either.right(tr.getFuncFor(impl)));
                }
            }
        }
        return binding;
    }
}
