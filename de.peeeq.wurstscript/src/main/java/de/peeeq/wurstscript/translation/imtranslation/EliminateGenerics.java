package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * eliminate classes and dynamic method invocations
 */
public class EliminateGenerics {

    private final ImTranslator translator;
    private final ImProg prog;
    private Deque<ImFunctionCall> genericFunctionCalls = new ArrayDeque<>();
    private Deque<ImMethodCall> genericMethodCalls = new ArrayDeque<>();
    private Table<ImFunction, GenericTypes, ImFunction> specializedFunctions = HashBasedTable.create();

    public EliminateGenerics(ImTranslator tr, ImProg prog) {
        translator = tr;
        this.prog = prog;
    }


    public void transform() {
        collectGenericUsages();

        eliminateGenericFunctions();

        removeGenericFunctions();


    }

    private void removeGenericFunctions() {
        prog.getFunctions().removeIf(f -> !f.getTypeVariables().isEmpty());
    }

    private void eliminateGenericFunctions() {
        while (!genericFunctionCalls.isEmpty()) {
            ImFunctionCall fc = genericFunctionCalls.removeFirst();
            ImFunction f = fc.getFunc();

            GenericTypes generics = new GenericTypes(fc.getTypeArguments());
            ImFunction specializedFunc = specializedFunctions.get(f, generics);
            if (specializedFunc == null) {
                specializedFunc = specialize(f, generics);
            }
            fc.setFunc(specializedFunc);
            fc.getTypeArguments().removeAll();
        }
    }

    /**
     * creates a specialized version of this function
     */
    private ImFunction specialize(ImFunction f, GenericTypes generics) {
        ImFunction newF = f.copyWithRefs();
        specializedFunctions.put(f, generics, newF);
        prog.getFunctions().add(newF);
        newF.getTypeVariables().removeAll();
        List<ImTypeVar> typeVars = f.getTypeVariables();

        newF.setName(f.getName() + "_specialized_" + generics.makeName());
        // now adjust all occurrences of type variables in newF
        // this can lead to new generic calls, which we have to add to the queue
        // cases to handle:
        // - ImClassType (types can appear in ImNull, ImFunction(params, locals, return))
        // - ImMethodCall
        // - ImFunctionCall
        newF.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImTypeArgument ta) {
                ta.setType(transformType(ta.getType()));
            }

            @Override
            public void visit(ImMethodCall mc) {
                if (!mc.getTypeArguments().isEmpty()) {
                    genericMethodCalls.add(mc);
                }
                super.visit(mc);
            }

            @Override
            public void visit(ImFunctionCall fc) {
                if (!fc.getTypeArguments().isEmpty()) {
                    genericFunctionCalls.add(fc);
                }
                super.visit(fc);
            }

            @Override
            public void visit(ImNull e) {
                e.setType(transformType(e.getType()));
                super.visit(e);
            }

            @Override
            public void visit(ImFunction e) {
                e.setReturnType(transformType(e.getReturnType()));
                super.visit(e);
            }

            @Override
            public void visit(ImVar e) {
                e.setType(transformType(e.getType()));
                super.visit(e);
            }

            private ImType transformType(ImType type) {
                return type.match(new ImType.Matcher<ImType>() {

                    @Override
                    public ImType case_ImVoid(ImVoid t) {
                        return t;
                    }

                    @Override
                    public ImType case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                        return JassIm.ImArrayTypeMulti(transformType(t.getEntryType()), t.getArraySize());
                    }

                    @Override
                    public ImType case_ImTupleType(ImTupleType t) {
                        return JassIm.ImTupleType(t.getTypes().stream().map(tt -> transformType(tt)).collect(Collectors.toList()), t.getNames());
                    }

                    @Override
                    public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                        int index = typeVars.indexOf(t.getTypeVariable());
                        if (index < 0) {
                            throw new CompileError(t, "Could not find type var " + t + " in " + typeVars);
                        }
                        return generics.typeArguments.get(index).getType();
                    }

                    @Override
                    public ImType case_ImSimpleType(ImSimpleType t) {
                        return t;
                    }

                    @Override
                    public ImType case_ImArrayType(ImArrayType t) {
                        return JassIm.ImArrayType(transformType(t.getEntryType()));
                    }

                    @Override
                    public ImType case_ImClassType(ImClassType t) {
                        ImTypeArguments args = t.getTypeArguments().stream().map(ta -> JassIm.ImTypeArgument(transformType(ta.getType()), ta.getTypeClassBinding())).collect(Collectors.toCollection(JassIm::ImTypeArguments));
                        return JassIm.ImClassType(t.getClassDef(), args);
                    }
                });
            }
        });
        return newF;
    }


    /**
     * Collects all usages from non-generic functions
     */
    private void collectGenericUsages() {
        for (ImFunction func : prog.getFunctions()) {
            if (func.getTypeVariables().isEmpty()) {
                collectGenericUsages(func);
            }
        }
    }

    private void collectGenericUsages(ImFunction func) {
        func.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall f) {
                super.visit(f);
                if (!f.getTypeArguments().isEmpty()) {
                    genericFunctionCalls.add(f);
                }
            }

        });
    }

    /**
     * wraps an imType and adds a hashmap and equals method
     */
    static class GenericTypes {
        private final ImTypeArguments typeArguments;


        public GenericTypes(ImTypeArguments typeArguments) {
            this.typeArguments = typeArguments;
        }

        public ImTypeArguments getTypeArguments() {
            return typeArguments;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof GenericTypes) {
                GenericTypes ot = (GenericTypes) o;
                if (typeArguments.size() != ot.typeArguments.size()) {
                    return false;
                }
                for (int i = 0; i < typeArguments.size(); i++) {
                    ImTypeArgument t1 = typeArguments.get(i);
                    ImTypeArgument t2 = ot.typeArguments.get(i);
                    if (!t1.getType().equalsType(t2.getType())) {
                        return false;
                    }
                    if (!t1.getTypeClassBinding().equals(t2.getTypeClassBinding())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int res = 0;
            for (ImTypeArgument it : typeArguments) {
                res = 131 * res + hashType(it.getType());
            }
            return res;
        }

        private static int hashType(ImType t) {
            return t.match(new ImType.Matcher<Integer>() {
                @Override
                public Integer case_ImTupleType(ImTupleType t) {
                    int res = 172;
                    for (ImType it : t.getTypes()) {
                        res = 73 * res + hashType(it);
                    }
                    return res;
                }

                @Override
                public Integer case_ImVoid(ImVoid imVoid) {
                    return 183;
                }

                @Override
                public Integer case_ImClassType(ImClassType ct) {
                    int res = ct.getClassDef().hashCode();
                    for (ImTypeArgument it : ct.getTypeArguments()) {
                        res = 73 * res + hashType(it.getType());
                    }
                    return res;
                }

                @Override
                public Integer case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                    return 9931532 + hashType(t.getEntryType());
                }

                @Override
                public Integer case_ImSimpleType(ImSimpleType t) {
                    return 234312 + t.getTypename().hashCode();
                }

                @Override
                public Integer case_ImArrayType(ImArrayType t) {
                    return 91532 + hashType(t.getEntryType());
                }

                @Override
                public Integer case_ImTypeVarRef(ImTypeVarRef t) {
                    return t.getTypeVariable().hashCode();
                }
            });
        }

        public String makeName() {
            StringBuilder sb = new StringBuilder();
            for (ImTypeArgument ta : typeArguments) {
                if (sb.length() > 0) {
                    sb.append("_");
                }
                ta.getType().print(sb, 0);
            }
            return sb.toString();
        }
    }

}
