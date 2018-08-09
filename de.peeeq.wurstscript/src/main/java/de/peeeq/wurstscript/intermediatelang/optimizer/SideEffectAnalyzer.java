package de.peeeq.wurstscript.intermediatelang.optimizer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Analyzes a program for side-effects
 */
public class SideEffectAnalyzer {

    private final ImProg prog;
    // f -> set of functions directly called by f
    private Multimap<ImFunction, ImFunction> callRelation;
    // f -> set of functions directly and transitively called by f
    private Multimap<ImFunction, ImFunction> callRelationTr;
    // f -> global variables directly used in f
    private Multimap<ImFunction, ImVar> usedGlobals;

    public SideEffectAnalyzer(ImProg prog) {
        this.prog = prog;
    }

    /**
     * @return f -> set of functions directly called by f
     */
    public Multimap<ImFunction, ImFunction> getCallRelation() {
        if (callRelation != null) {
            return callRelation;
        }
        callRelation = LinkedListMultimap.create();
        for (ImFunction caller : prog.getFunctions()) {
            callRelation.putAll(caller, directlyCalledFunctions(caller));
        }
        return callRelation;
    }

    /**
     * @return f -> set of functions directly and transitively called by f
     */
    public Multimap<ImFunction, ImFunction> getCallRelationTr() {
        if (callRelationTr != null) {
            return callRelationTr;
        }
        callRelationTr = Utils.transientClosure(getCallRelation());
        return callRelationTr;
    }

    /**
     * @return f -> global variables directly used in f
     */
    public Multimap<ImFunction, ImVar> getUsedGlobals() {
        if (usedGlobals != null) {
            return usedGlobals;
        }
        usedGlobals = LinkedHashMultimap.create();
        for (ImFunction function : prog.getFunctions()) {
            for (ImVar v : directlyUsedVariables(function)) {
                if (v.isGlobal()) {
                    usedGlobals.put(function, v);
                }
            }
        }
        return usedGlobals;
    }


    /**
     * Functions directly or indirectly called in e
     */
    public Set<ImFunction> calledFunctions(Element e) {
        return calledFunctionsStream(e)
                .collect(Collectors.toSet());
    }

    /**
     * Functions directly or indirectly called in e
     */
    private Stream<ImFunction> calledFunctionsStream(Element e) {
        return directlyCalledFunctions(e).stream()
                .flatMap(f -> Stream.concat(Stream.of(f), getCallRelationTr().get(f).stream()));
    }

    /**
     * Natives directly or indirectly called in e
     */
    public Set<ImFunction> calledNatives(Element e) {
        return calledFunctionsStream(e)
                .filter(ImFunction::isNative)
                .collect(Collectors.toSet());
    }

    /**
     * Variables directly or indirectly (via called functions) used in e
     * Does not consider variables used because of natives doing stuff (e.g. ForGroup callback)
     */
    public Set<ImVar> usedVariables(Element e) {
        Stream<ImVar> indirectGlobals = calledFunctionsStream(e)
                .flatMap(f -> getUsedGlobals().get(f).stream());
        return Stream.concat(indirectGlobals, directlyUsedVariables(e).stream())
                .collect(Collectors.toSet());
    }


    /**
     * Functions directly called in e
     */
    public Set<ImFunction> directlyCalledFunctions(Element e) {
        Set<ImFunction> calledFunctions = new LinkedHashSet<>();
        e.accept(new ImFunction.DefaultVisitor() {

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                calledFunctions.add(c.getFunc());
            }
        });
        return calledFunctions;

    }

    /**
     * Variables directly used in e
     */
    public Set<ImVar> directlyUsedVariables(Element e) {
        Set<ImVar> imVars = new LinkedHashSet<>();
        e.accept(new ImStmt.DefaultVisitor() {

            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

            @Override
            public void visit(ImVarArrayAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

            @Override
            public void visit(ImVarArrayMultiAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

            @Override
            public void visit(ImMemberAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

            @Override
            public void visit(ImSet va) {
                super.visit(va);
                imVars.add(va.getLeft());
            }

            @Override
            public void visit(ImSetTuple va) {
                super.visit(va);
                imVars.add(va.getLeft());
            }

            @Override
            public void visit(ImSetArrayTuple va) {
                super.visit(va);
                imVars.add(va.getLeft());
            }

            @Override
            public void visit(ImVarargLoop va) {
                super.visit(va);
                imVars.add(va.getLoopVar());
            }

        });
        return imVars;
    }

    /**
     * Variables directly used in e
     */
    public Set<ImVar> directlyAccessedVariables(Element e) {
        Set<ImVar> imVars = new LinkedHashSet<>();
        e.accept(new ImStmt.DefaultVisitor() {

            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

            @Override
            public void visit(ImVarArrayAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

            @Override
            public void visit(ImVarArrayMultiAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

            @Override
            public void visit(ImMemberAccess va) {
                super.visit(va);
                imVars.add(va.getVar());
            }

        });
        return imVars;
    }

    /**
     * Variables directly used in e
     */
    public Set<ImVar> directlySetVariables(Element e) {
        Set<ImVar> imVars = new LinkedHashSet<>();
        e.accept(new ImStmt.DefaultVisitor() {

            @Override
            public void visit(ImSet va) {
                super.visit(va);
                imVars.add(va.getLeft());
            }

            @Override
            public void visit(ImSetTuple va) {
                super.visit(va);
                imVars.add(va.getLeft());
            }

            @Override
            public void visit(ImSetArrayTuple va) {
                super.visit(va);
                imVars.add(va.getLeft());
            }

            @Override
            public void visit(ImVarargLoop va) {
                super.visit(va);
                imVars.add(va.getLoopVar());
            }

        });
        return imVars;
    }


    /**
     * Checks if two statements might affect each other.
     * When this returns true, it is certain that it does not matter whether stmt1 or stmt2 are called first
     * <p>
     * The only difference between executing stmt1; stmt2 vs stmt2; stmt1 would be if one of the statement
     * crashes and thus the second statement would not be executed.
     * But for optimizations, we assume the program already is correct and thus we can ignore crashes.
     */
    public boolean mightAffect(ImStmt stmt1, ImStmt stmt2) {
        if (!calledNatives(stmt1).isEmpty() || !calledNatives(stmt2).isEmpty()) {
            // there are natives that can affect other natives
            // be safe
            return true;
        }
        Set<ImVar> used1 = usedVariables(stmt1);
        Set<ImVar> used2 = usedVariables(stmt2);

        // check that there are no variables, that both use
        return used1.stream().anyMatch(used2::contains);
    }

    /**
     * Checks if the given statement cannot use the variable v
     */
    public boolean cannotUseVar(ImStmt s, ImVar v) {
        if (v.isGlobal()) {
            Set<ImVar> imVars = usedVariables(s);
            Set<ImFunction> imFunctions = calledNatives(s);
            return !imVars.contains(v) && imFunctions.isEmpty();
        } else {
            // local variables
            Set<ImVar> imVars = directlyUsedVariables(s);
            return !imVars.contains(v);
        }
    }

    /**
     * Checks if the given function calls any functions or modifies and variable
     */
    public boolean hasSideEffects(Element elem) {
        Set<ImFunction> natives = calledNatives(elem);
        Set<ImFunction> directFuncs = calledFunctions(elem);
        Set<ImVar> imVars = directlySetVariables(elem);
        return natives.size() + directFuncs.size() + imVars.size() > 0;
    }
}
