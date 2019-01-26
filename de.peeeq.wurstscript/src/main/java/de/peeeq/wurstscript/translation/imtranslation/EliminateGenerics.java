package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * eliminate classes and dynamic method invocations
 */
public class EliminateGenerics {

    private final ImTranslator translator;
    private final ImProg prog;
    // TODO only use one queue here with the different cases (add: generic class type, member access)
    private Deque<GenericUse> genericsUses = new ArrayDeque<>();
    private Table<ImFunction, GenericTypes, ImFunction> specializedFunctions = HashBasedTable.create();
    private Table<ImClass, GenericTypes, ImClass> specializedClasses = HashBasedTable.create();

    public EliminateGenerics(ImTranslator tr, ImProg prog) {
        translator = tr;
        this.prog = prog;
    }


    public void transform() {
        collectGenericUsages();

        eliminateGenericUses();

        removeGenericConstructs();


    }


    /**
     * When everything is specialized, we can remove generic functions and classes
     */
    private void removeGenericConstructs() {
        prog.getFunctions().removeIf(f -> !f.getTypeVariables().isEmpty());
        prog.getClasses().removeIf(c -> !c.getTypeVariables().isEmpty());
    }

    private void eliminateGenericUses() {
        while (!genericsUses.isEmpty()) {
            GenericUse gu = genericsUses.removeFirst();
            gu.eliminate();
        }
    }

    /**
     * creates a specialized version of this function
     */
    private ImFunction specialize(ImFunction f, GenericTypes generics) {

        ImFunction specialized = specializedFunctions.get(f, generics);
        if (specialized != null) {
            return specialized;
        }
        if (f.getTypeVariables().isEmpty()) {
            return f;
        }
        ImFunction newF = f.copyWithRefs();
        specializedFunctions.put(f, generics, newF);
        prog.getFunctions().add(newF);
        newF.getTypeVariables().removeAll();
        List<ImTypeVar> typeVars = f.getTypeVariables();

        newF.setName(f.getName() + "_specialized_" + generics.makeName());
        rewriteGenerics(newF, generics, typeVars);
        collectGenericUsages(newF);
        return newF;
    }

    private void rewriteGenerics(Element element, GenericTypes generics, List<ImTypeVar> typeVars) {
        element.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImTypeArgument ta) {
                ta.setType(transformType(ta.getType()));
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

            @Override
            public void visit(ImClass c) {
                ListIterator<ImClassType> it = c.getSuperClasses().listIterator();
                while (it.hasNext()) {
                    it.set((ImClassType) transformType(it.next()));
                }
                super.visit(c);
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
                        return generics.getTypeArguments().get(index).getType();
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
    }

    /**
     * creates a specialized version of this class
     */
    private ImClass specializeClass(ImClass c, GenericTypes generics) {
        if (c.getTypeVariables().isEmpty()) {
            return c;
        }
        ImClass specialized = specializedClasses.get(c, generics);
        if (specialized != null) {
            return specialized;
        }
        ImClass newC = c.copyWithRefs();
        specializedClasses.put(c, generics, newC);
        prog.getClasses().add(newC);
        newC.getTypeVariables().removeAll();
        List<ImTypeVar> typeVars = c.getTypeVariables();

        newC.setName(c.getName() + "_specialized_" + generics.makeName());
        rewriteGenerics(newC, generics, typeVars);
        collectGenericUsages(newC);
        return newC;
    }


    /**
     * Collects all usages from non-generic functions
     */
    private void collectGenericUsages() {
        collectGenericUsages(prog);
    }

    private void collectGenericUsages(Element element) {
        element.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall f) {
                super.visit(f);
                if (!f.getTypeArguments().isEmpty()) {
                    genericsUses.add(new GenericImFunctionCall(f));
                }
            }

            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);
                ImType imType = mc.getReceiver().attrTyp();
                System.out.println("visit method call " + mc + " on " + imType);
                if (!(imType instanceof ImClassType)) {
                    throw new CompileError(mc, "Method call " + mc + " not on class type but on " + imType);
                }
                genericsUses.add(new GenericMethodCall(mc));
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                ImType receiverType = ma.getReceiver().attrTyp();
                if (receiverType instanceof ImClassType) {
                    ImClassType ct = (ImClassType) receiverType;
                    if (!ct.getTypeArguments().isEmpty()) {
                        genericsUses.add(new GenericMemberAccess(ma));
                    }
                } else {
                    throw new CompileError(ma, "Cannot handle member access " + ma + " on type " + receiverType);
                }

            }

            @Override
            public void visit(ImVar v) {
                super.visit(v);
                if (isGenericType(v.getType())) {
                    genericsUses.add(new GenericVar(v));
                }
            }

            @Override
            public void visit(ImClass c) {
                if (!c.getTypeVariables().isEmpty()) {
                    // handle generic classes after they are specialized
                    return;
                }
                super.visit(c);
                if (c.getSuperClasses().stream().anyMatch(sc -> !sc.getTypeArguments().isEmpty())) {
                    genericsUses.add(new GenericSuperClasses(c));
                }
            }

            @Override
            public void visit(ImFunction f) {
                if (!f.getTypeVariables().isEmpty()) {
                    // handle generic functions after they are specialized
                    return;
                }

                super.visit(f);
                if (isGenericType(f.getReturnType())) {
                    genericsUses.add(new GenericReturnTypeFunc(f));
                }
            }


        });
    }

    static boolean isGenericType(ImType type) {
        return type.match(new ImType.Matcher<Boolean>() {
            @Override
            public Boolean case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                return isGenericType(t.getEntryType());
            }

            @Override
            public Boolean case_ImArrayType(ImArrayType t) {
                return isGenericType(t.getEntryType());
            }

            @Override
            public Boolean case_ImClassType(ImClassType t) {
                return !t.getTypeArguments().isEmpty();
            }

            @Override
            public Boolean case_ImVoid(ImVoid t) {
                return false;
            }

            @Override
            public Boolean case_ImTupleType(ImTupleType t) {
                for (ImType tt : t.getTypes()) {
                    if (isGenericType(tt)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Boolean case_ImSimpleType(ImSimpleType t) {
                return false;
            }

            @Override
            public Boolean case_ImTypeVarRef(ImTypeVarRef t) {
                return false;
            }
        });
    }

    abstract class GenericUse {

        public abstract void eliminate();
    }

    class GenericImFunctionCall extends GenericUse {
        private final ImFunctionCall fc;

        GenericImFunctionCall(ImFunctionCall fc) {
            this.fc = fc;
        }

        @Override
        public void eliminate() {
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

    class GenericMethodCall extends GenericUse {
        private final ImMethodCall mc;

        GenericMethodCall(ImMethodCall mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            ImType receiverType = mc.getReceiver().attrTyp();


            throw new RuntimeException("TODO");
        }
    }

    class GenericMemberAccess extends GenericUse {
        private final ImMemberAccess ma;

        GenericMemberAccess(ImMemberAccess ma) {
            this.ma = ma;
        }

        @Override
        public void eliminate() {
            ImClassType t = (ImClassType) specializeType(ma.getReceiver().attrTyp());
            ImVar oldVar = ma.getVar();
            ImVars parent = (ImVars) oldVar.getParent();
            int index = parent.indexOf(oldVar);
            ImVar newVar = t.getClassDef().getFields().get(index);
            if (oldVar != newVar) {
                ma.setVar(newVar);
            }
        }
    }


    class GenericClass extends GenericUse {

        private final ImClassType ct;

        public GenericClass(ImClassType ct) {
            this.ct = ct;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

    class GenericVar extends GenericUse {
        private final ImVar mc;

        GenericVar(ImVar mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            mc.setType(specializeType(mc.getType()));
        }
    }

    class GenericSuperClasses extends GenericUse {
        private final ImClass c;

        GenericSuperClasses(ImClass c) {
            this.c = c;
        }

        @Override
        public void eliminate() {
            ListIterator<ImClassType> it = c.getSuperClasses().listIterator();
            while (it.hasNext()) {
                it.set((ImClassType) specializeType(it.next()));
            }
        }
    }



    private ImType specializeType(ImType type) {
        return type.match(new ImType.Matcher<ImType>() {
            @Override
            public ImType case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                return JassIm.ImArrayTypeMulti(specializeType(t.getEntryType()), t.getArraySize());
            }

            @Override
            public ImType case_ImArrayType(ImArrayType t) {
                return JassIm.ImArrayType(specializeType(t.getEntryType()));
            }

            @Override
            public ImType case_ImClassType(ImClassType t) {
                if (t.getTypeArguments().isEmpty()) {
                    return t;
                }
                ImClass specializedClass = specializeClass(t.getClassDef(), new GenericTypes(t.getTypeArguments()));
                return JassIm.ImClassType(specializedClass, JassIm.ImTypeArguments());
            }

            @Override
            public ImType case_ImVoid(ImVoid t) {
                return t;
            }

            @Override
            public ImType case_ImTupleType(ImTupleType t) {
                return null;
            }

            @Override
            public ImType case_ImSimpleType(ImSimpleType t) {
                return t;
            }

            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                return t;
            }
        });
    }

    class GenericReturnTypeFunc extends GenericUse {
        private final ImFunction mc;

        GenericReturnTypeFunc(ImFunction mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

    class GenericReturnTypeMethod extends GenericUse {
        private final ImMethod mc;

        GenericReturnTypeMethod(ImMethod mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

    class GenericNull extends GenericUse {
        private final ImNull mc;

        GenericNull(ImNull mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

    class GenericTypeArgument extends GenericUse {
        private final ImTypeArgument mc;

        GenericTypeArgument(ImTypeArgument mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

}
