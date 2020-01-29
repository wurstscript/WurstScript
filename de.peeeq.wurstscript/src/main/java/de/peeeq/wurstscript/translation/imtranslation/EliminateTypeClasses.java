package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Eliminates ImTypeClassDictValue from the program.
 * <p>
 * The algorithm works like this:
 * Initialize a list of all ImTypeClassDictValue with no dynamic values.
 * Maintain this list invariant and while the list is not empty:
 * <p>
 * Look at where the dict is used:
 * When used as a function parameter: specialize the function
 * When used as a method parameter: specialize the method (including submethods and implementation)
 * When used as an alloc parameter: specialize the whole class
 * When used as receiver in a method call: Specialize method (if no submethods, replace by function call)
 * No other uses should be possible --> compilation error.
 * <p>
 * Keep Mappings from Function (etc.) and type to specialized version such that every specialization
 * is only done once.
 * <p>
 * Replace the call to the generic function with a call to the specialized one.
 * <p>
 * When specializing a parameter to a type:
 * - Replace all uses of the parameter with a concrete dict
 * - Add the concrete dicts to the work list
 */
public class EliminateTypeClasses {
    private final ImTranslator tr;
    private final ArrayDeque<ImTypeClassDictValue> workList = new ArrayDeque<>();
    private final Table<ImFunction, TypeClassInstanceKey, ImFunction> specializedFunctions = HashBasedTable.create();
    private final Map<ImFunction, ImFunction> outOfClassFuncs = new LinkedHashMap<>();

    public EliminateTypeClasses(ImTranslator tr) {
        this.tr = tr;
    }

    public static void transform(ImTranslator tr) {
        new EliminateTypeClasses(tr).run();


    }

    private void run() {
        ImProg prog = tr.getImProg();

        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImTypeClassDictValue dv) {
                // no super visit
                if (!dynamicArgsStream(dv).findAny().isPresent()) {
                    workList.add(dv);
                }
            }
        });

        while (!workList.isEmpty()) {
            ImTypeClassDictValue dictV = workList.removeFirst();
            doSpecialize(dictV);

        }
    }

    private void doSpecialize(ImTypeClassDictValue dictV) {
        Element parent = dictV.getParent();
        if (parent instanceof ImExprs) {
            Element parent2 = parent.getParent();
            if (parent2 instanceof ImFunctionCall) {
                ImFunctionCall fc = (ImFunctionCall) parent2;
                int index = fc.getArguments().indexOf(dictV);
                ImFunction specializedFunc = getSpecializeFunc(fc.getFunc(), key(dictV, index));
                fc.getArguments().remove(index);
                fc.setFunc(specializedFunc);
                return;
            }
        } else if (parent instanceof ImMethodCall) {
            ImMethodCall mc = (ImMethodCall) parent;
            assert mc.getReceiver() == dictV;

            ImMethod m = findMostConcreteMethod(mc.getMethod(), dictV.getClassType());

            // allow to move
            mc.getTypeArguments().setParent(null);
            mc.getArguments().setParent(null);

            if (!m.getSubMethods().isEmpty()) {
                throw new CompileError(dictV.getTrace(),
                    "Could not specialize method call: " + mc + " to " + m);
            }
            mc.replaceBy(JassIm.ImFunctionCall(
                mc.getTrace(),
                getOutOfClassFunc(m.getImplementation()),
                mc.getTypeArguments(),
                mc.getArguments(),
                false,
                CallType.NORMAL
            ));

            return;
        }


        throw new CompileError(dictV.getTrace(),
            "Unhandled parent for dict: " + parent + " // " + parent.getClass());
    }

    private ImFunction getOutOfClassFunc(ImFunction impl) {
        ImFunction res = outOfClassFuncs.get(impl);
        if (res != null) {
            return res;
        }
        ImFunction copy = impl.copyWithRefs();
        // remove implicit parameter
        copy.getParameters().remove(0);
        outOfClassFuncs.put(impl, copy);
        return copy;
    }

    private ImMethod findMostConcreteMethod(ImMethod method, ImClassType classType) {
        ImMethod result = method;
        for (ImMethod sub : method.getSubMethods()) {
            if (isSubClass(classType, sub.getMethodClass()) && isSubClass(sub.getMethodClass(), result.getMethodClass())) {
                result = sub;
            }
        }
        return result;
    }

    private boolean isSubClass(ImClassType sub, ImClassType sup) {
        return isSubClass(sub.getClassDef(), sup.getClassDef());
    }

    private boolean isSubClass(ImClass sub, ImClass sup) {
        return sub == sup
            || sub.getSuperClasses().stream()
            .anyMatch(sc -> isSubClass(sc.getClassDef(), sup));
    }

    private ImFunction getSpecializeFunc(ImFunction func, TypeClassInstanceKey key) {
        ImFunction res = specializedFunctions.get(func, key);
        if (res != null) {
            return res;
        }
        res = specializeFunc(func, key);
        specializedFunctions.put(func, key, res);
        return res;
    }

    private ImFunction specializeFunc(ImFunction func, TypeClassInstanceKey key) {
        ImFunction copy = func.copyWithRefs();
        copy.setName(func.getName() + "_" + key);
        ImVar removedParam = copy.getParameters().remove(key.index);
        Set<ImVarAccess> uses = new HashSet<>();
        copy.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                if (va.getVar() == removedParam) {
                    uses.add(va);
                }
            }
        });

        for (ImVarAccess use : uses) {
            ImTypeClassDictValue newDict = key.makeDictValue(use.attrTrace());
            use.replaceBy(newDict);
            workList.add(newDict);
        }
        tr.getImProg().getFunctions().add(copy);
        return copy;
    }


    private static Stream<ImVar> dynamicArgsStream(ImTypeClassDictValue dv) {
        return dv.getArguments().stream().flatMap(EliminateTypeClasses::dynamicArgsStreamE);
    }

    private static Stream<ImVar> dynamicArgsStreamE(ImExpr a) {
        if (a instanceof ImVarAccess) {
            return Stream.of(((ImVarAccess) a).getVar());
        } else if (a instanceof ImTypeClassDictValue) {
            return dynamicArgsStream((ImTypeClassDictValue) a);
        }
        throw new RuntimeException("invalid TypeClassDictValue " + a + " // " + a.getClass());
    }

    private static List<ImVar> dynamicArgs(ImTypeClassDictValue dv) {
        return dynamicArgsStream(dv).collect(Collectors.toList());
    }

    private TypeClassInstanceKey key(ImTypeClassDictValue dictV, int index) {
        return new TypeClassInstanceKey(dictV.getClassType().getClassDef(), index,
            dictV.getArguments().stream()
                .map(v -> key((ImTypeClassDictValue) v, index))
                .collect(Collectors.toList()));
    }

    static class TypeClassInstanceKey {
        private final ImClass base;
        private int index;
        private final List<TypeClassInstanceKey> args;

        TypeClassInstanceKey(ImClass base, int index, List<TypeClassInstanceKey> args) {
            this.base = base;
            this.index = index;
            this.args = args;
        }

        @Override
        public String toString() {
            return "#" + base + "@" + index + args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeClassInstanceKey that = (TypeClassInstanceKey) o;
            return index == that.index &&
                base.equals(that.base) &&
                args.equals(that.args);
        }

        @Override
        public int hashCode() {
            return Objects.hash(base, index, args);
        }

        public ImTypeClassDictValue makeDictValue(de.peeeq.wurstscript.ast.Element trace) {
            ImExprs args2 = args.stream()
                .map(x -> x.makeDictValue(trace))
                .collect(Collectors.toCollection(JassIm::ImExprs));
            return JassIm.ImTypeClassDictValue(trace, JassIm.ImClassType(base, JassIm.ImTypeArguments()), args2);
        }
    }

}
