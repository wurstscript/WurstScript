package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.*;
import de.peeeq.datastructures.Partitions;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypeClassInstance;
import org.eclipse.xtend.lib.annotations.ToString;
import org.jetbrains.annotations.NotNull;

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
 * When assigned to a class field: That must be in a constructor. Specialize the class with all the assignments used in the constructor.
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
    private final Table<ImFunction, ParameterSpecializationKey, ImFunction> specializedFunctions = HashBasedTable.create();
    private final Table<ImFunction, TypeClassInstanceKey, ImFunction> outOfClassFuncs = HashBasedTable.create();
    private final Map<TypeClassInstanceKey, ImClass> typeClassSpecialization = new HashMap<>();

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

            ImMethod m = findMostConcreteMethod(mc.getMethod(), dictV.getClazz());

            // allow to move
            mc.getTypeArguments().setParent(null);
            mc.getArguments().setParent(null);

            if (!m.getSubMethods().isEmpty()) {
                throw new CompileError(dictV.getTrace(),
                    "Could not specialize method call: " + mc + " to " + m);
            }
            mc.replaceBy(JassIm.ImFunctionCall(
                mc.getTrace(),
                getOutOfClassFunc(m.getImplementation(), key(dictV)),
                mc.getTypeArguments(),
                mc.getArguments(),
                false,
                CallType.NORMAL
            ));

            return;
        } else if (parent instanceof ImMemberAccess) {
            ImMemberAccess ma = (ImMemberAccess) parent;
            ImVar field = ma.getVar();
            int index = ((ImVars) field.getParent()).indexOf(field);
            ImExpr tc = dictV.getArguments().get(index);
            ImExpr copy = tc.copyWithRefs();
            ma.replaceBy(copy);
            if (copy instanceof ImTypeClassDictValue) {
                workList.add((ImTypeClassDictValue) copy);
            }
            return;
        }

        // if used in other places (e.g. class parameters), don't monomorphize and
        // just use the dynamic version here:
        dictV.replaceBy(typeClassAlloc(dictV));
    }

    /**
     * Dynamically allocate an instance of this type-class-instance.
     */
    private Element typeClassAlloc(ImTypeClassDictValue dictV) {
        ImClass clazz = getSpecializedTypeClassDict(key(dictV));
        return JassIm.ImAlloc(dictV.getTrace(), JassIm.ImClassType(clazz, JassIm.ImTypeArguments()));
    }

    private ImClass getSpecializedTypeClassDict(TypeClassInstanceKey key) {

        return typeClassSpecialization.computeIfAbsent(key, k -> {
            if (key.args.isEmpty()) {
                return key.base;
            } else {
                return specializeClass(key);
            }
        });
    }

    private ImClass specializeClass(TypeClassInstanceKey key) {
        ImProg imProg = tr.getImProg();
        ImClass base = key.base;
        // create new subclass
        ImClass sub = JassIm.ImClass(
            base.getTrace(),
            base.getName() + key.args,
            JassIm.ImTypeVars(),
            JassIm.ImVars(),
            JassIm.ImMethods(),
            JassIm.ImFunctions(),
            Collections.singletonList(JassIm.ImClassType(base, JassIm.ImTypeArguments()))
        );
        imProg.getClasses().add(sub);


        // specialize methods using the fields
        final ImVars baseFields = base.getFields();


        List<ImMethod> toSpecialize = imProg.getMethods()
            .stream()
            .filter(m -> m.getMethodClass().getClassDef() == base
                && !collectFieldAccesses(m.getImplementation(), baseFields).isEmpty())
            .collect(Collectors.toList());


        // if one of the fields is used, create a new submethod:
        for (ImMethod m : toSpecialize) {

            ImFunction impl = m.getImplementation();


            ImFunction newImpl = impl.copyWithRefs();
            List<ImMemberAccess> fieldAccesses = collectFieldAccesses(newImpl, baseFields);
            for (ImMemberAccess fa : fieldAccesses) {
                int index = baseFields.indexOf(fa.getVar());
                TypeClassInstanceKey tc = key.args.get(index);
                ImTypeClassDictValue dictValue = tc.makeDictValue(fa.getTrace());
                fa.replaceBy(dictValue);
                workList.add(dictValue);
            }

            ImMethod newM = JassIm.ImMethod(
                m.getTrace(),
                JassIm.ImClassType(sub, JassIm.ImTypeArguments()),
                m.getName(),
                newImpl,
                new ArrayList<>(),
                false
            );

            m.getSubMethods().add(newM);

            imProg.getFunctions().add(newImpl);
            imProg.getMethods().add(newM);
        }

        return sub;
    }

    @NotNull
    private List<ImMemberAccess> collectFieldAccesses(ImFunction impl, ImVars baseFields) {
        List<ImMemberAccess> fieldAccesses = new ArrayList<>();
        impl.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImMemberAccess e) {
                super.visit(e);
                if (baseFields.contains(e.getVar())) {
                    fieldAccesses.add(e);
                }
            }
        });
        return fieldAccesses;
    }

    private ImFunction getOutOfClassFunc(ImFunction impl, TypeClassInstanceKey key) {
        ImFunction res = outOfClassFuncs.get(impl, key);
        if (res != null) {
            return res;
        }
        ImFunction copy = impl.copyWithRefs();
        tr.getImProg().getFunctions().add(copy);
        // remove implicit parameter
        ImVar thisVar = copy.getParameters().remove(0);
        List<ImVarAccess> vars = new ArrayList<>();
        copy.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess v) {

                if (v.getVar() == thisVar) {
                    vars.add(v);
                }
            }
        });

        for (ImVarAccess va : vars) {
            ImTypeClassDictValue newDict = key.makeDictValue(va.attrTrace());
            va.replaceBy(newDict);
            workList.add(newDict);
        }

        outOfClassFuncs.put(impl, key, copy);
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

    private ImFunction getSpecializeFunc(ImFunction func, ParameterSpecializationKey key) {
        ImFunction res = specializedFunctions.get(func, key);
        if (res != null) {
            return res;
        }
        res = specializeFunc(func, key);
        specializedFunctions.put(func, key, res);
        return res;
    }

    private ImFunction specializeFunc(ImFunction func, ParameterSpecializationKey key) {
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
            ImTypeClassDictValue newDict = key.key.makeDictValue(use.attrTrace());
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

    private ParameterSpecializationKey key(ImTypeClassDictValue dictV, int index) {
        return new ParameterSpecializationKey(index,
            new TypeClassInstanceKey(dictV.getClazz().getClassDef(),
                dictV.getArguments().stream()
                    .map(v -> key((ImTypeClassDictValue) v))
                    .collect(Collectors.toList())));
    }

    private TypeClassInstanceKey key(ImTypeClassDictValue dictV) {
        return new TypeClassInstanceKey(dictV.getClazz().getClassDef(),
            dictV.getArguments().stream()
                .map(v -> key((ImTypeClassDictValue) v))
                .collect(Collectors.toList()));
    }

    static class ParameterSpecializationKey {
        private int index;
        private TypeClassInstanceKey key;

        public ParameterSpecializationKey(int index, TypeClassInstanceKey key) {
            this.index = index;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParameterSpecializationKey that = (ParameterSpecializationKey) o;
            return index == that.index &&
                key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, key);
        }

        @Override
        public String toString() {
            return key + "@" + index;
        }
    }

    // TODO remove index here and make another class for parameter index
    static class TypeClassInstanceKey {
        private final ImClass base;

        private final List<TypeClassInstanceKey> args;

        TypeClassInstanceKey(ImClass base, List<TypeClassInstanceKey> args) {
            this.base = base;
            this.args = args;
        }

        @Override
        public String toString() {
            return "#" + base + args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeClassInstanceKey that = (TypeClassInstanceKey) o;
            return Objects.equals(base, that.base) &&
                Objects.equals(args, that.args);
        }

        @Override
        public int hashCode() {
            return Objects.hash(base, args);
        }

        public ImTypeClassDictValue makeDictValue(de.peeeq.wurstscript.ast.Element trace) {
            ImExprs args2 = args.stream()
                .map(x -> x.makeDictValue(trace))
                .collect(Collectors.toCollection(JassIm::ImExprs));
            return JassIm.ImTypeClassDictValue(trace, JassIm.ImClassType(base, JassIm.ImTypeArguments()), args2);
        }
    }

}
