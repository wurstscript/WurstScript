package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.ModuleExpander;
import de.peeeq.wurstscript.jassIm.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
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

        }
    }

    /**
     * creates a specialized version of this function
     */
    private ImFunction specialize(ImFunction f, GenericTypes generics) {
        ImFunction newF = ModuleExpander.smartCopy(f, Collections.emptyList());
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
    }

}
