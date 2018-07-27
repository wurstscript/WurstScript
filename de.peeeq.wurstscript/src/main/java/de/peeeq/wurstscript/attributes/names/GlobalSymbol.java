package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class GlobalSymbol<Def extends NameDef> extends Symbol<Def> {
    public static final String GLOBAL_ROOT = "/global";
    private final ImmutableList<String> path;
    private final Class<? extends Def> defClass;
    // parameter types for overloading:
    private final ImmutableList<WurstType> paramTypes;

    private GlobalSymbol(ImmutableList<String> path, Class<? extends Def> defClass, ImmutableList<WurstType> paramTypes) {
        this.path = path;
        this.defClass = defClass;
        this.paramTypes = paramTypes;
    }

    static <D extends NameDef> GlobalSymbol<D> globalSymbolFromDef(D def) {
        if (def instanceof LocalVarDef || def instanceof WParameter) {
            throw new RuntimeException("Should create local symbol.");
        }

        @SuppressWarnings("unchecked")
        Class<? extends D> c = (Class<? extends D>) def.getClass();
        ImmutableList<WurstType> paramTypes = ImmutableList.of();
        if (def instanceof FunctionDefinition) {
            FunctionDefinition f = (FunctionDefinition) def;
            paramTypes = f.getParameters().stream()
                    .map(WParameter::attrTyp)
                    .collect(ImmutableList.toImmutableList());
        }
        GlobalSymbol<D> sym = new GlobalSymbol<>(getPath(def), c, paramTypes);
        if (!sym.getDef(def.getModel()).equals(def)) {
            throw new RuntimeException("Could not find symbol " + Utils.printElementWithSource(def));
        }
        return sym;
    }


    private static ImmutableList<String> getPath(Element def) {
        Deque<String> path = new ArrayDeque<>();
        outerLoop:
        while (def != null) {
            while (!(def instanceof AstElementWithNameId)) {
                def = def.getParent();
                if (def == null) {
                    break outerLoop;
                }
            }
            AstElementWithNameId named = (AstElementWithNameId) def;
            path.addFirst(named.getNameId().getName());
            def = def.getParent();
        }
        return ImmutableList.copyOf(path);
    }

    @Override
    public Def getDef(WurstModel m) {
        Element elem = m;
        for (int i = 0; i < path.size(); i++) {
            elem = findParentWithName(elem, path.get(i), i == path.size() - 1);
        }
        return defClass.cast(elem);
    }

    @Override
    public String getName() {
        return Utils.getLast(path);
    }

    private Element findParentWithName(Element elem, String name, boolean isLast) {
        for (int i = 0; i < elem.size(); i++) {
            Element e = elem.get(i);
            if (e instanceof AstElementWithNameId) {
                AstElementWithNameId en = (AstElementWithNameId) e;
                if (en.getNameId().getName().equals(name)) {
                    if (isLast) {
                        if (!defClass.isInstance(en)) {
                            continue;
                        }
                        if (en instanceof FunctionDefinition) {
                            // TODO add overloading check
                        }
                    } else {
                        if (en instanceof FunctionDefinition
                                || en instanceof VarDef) {
                            continue;
                        }
                    }
                    return en;
                }
            } else {
                // search deeper:
                try {
                    return findParentWithName(e, name, isLast);
                } catch (NotFound nf) {
                    // ignore
                }
            }
        }
        throw new NotFound(path, name);
    }

    public static class NotFound extends RuntimeException {
        public NotFound(List<String> path, String sym) {
            super("Could not find " + sym + " in path " + Utils.join(path, "/"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalSymbol<?> that = (GlobalSymbol<?>) o;
        return Objects.equals(path, that.path)
                && Objects.equals(defClass, that.defClass)
                && paramTypesEq(paramTypes, that.paramTypes);
    }

    private boolean paramTypesEq(ImmutableList<WurstType> pt1, ImmutableList<WurstType> pt2) {
        if (pt1.size() != pt2.size()) {
            return false;
        }
        for (int i = 0; i < pt1.size(); i++) {
            WurstType p1 = pt1.get(i);
            WurstType p2 = pt2.get(i);
            if (!p1.structuralEquals(p2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, defClass);
    }

    @Override
    public String toString() {
        return getName() + "(" + Utils.join(path, "/") + ")";
    }
}
