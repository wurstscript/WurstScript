package de.peeeq.wurstscript.intermediatelang.optimizer;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.UsedVariables;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Splits a long function into several smaller functions which are
 * executed using TriggerEvaluate
 */
public class FunctionSplitter {

    private final int op_limit;
    private final ImTranslator tr;
    private final ImFunction func;
    private final Map<ImFunction, Integer> fuelVisited;

    private FunctionSplitter(int op_limit, ImTranslator tr, ImFunction func) {
        this.op_limit = op_limit;
        this.tr = tr;
        this.func = func;
        this.fuelVisited = new LinkedHashMap<>();
        fuelVisited.put(func, null);
    }

    public static void splitFunc(ImTranslator tr, ImFunction func) {
        new FunctionSplitter(tr.getRunArgs().getFunctionSplitLimit(), tr, func).optimize();

    }

    private void optimize() {
        Preconditions.checkArgument(func.getTypeVariables().isEmpty(), "func must not be generic");
        Preconditions.checkArgument(func.getParameters().isEmpty(), "func parameters must be empty");
        Preconditions.checkArgument(func.getReturnType() instanceof ImVoid, "func must return void");
        // run some basic optimizations first:
        func.flatten(tr);
        new ConstantAndCopyPropagation().optimizeFunc(func);
        new TempMerger().optimizeFunc(func);
        new LocalMerger().optimizeFunc(func);
        Set<ImVar> usedVars = UsedVariables.calculate(func);
        func.getLocals().removeIf(v -> !usedVars.contains(v));
        func.flatten(tr);
        List<List<ImStmt>> splitResult = split(func.getBody().removeAll());

        ImProg prog = tr.getImProg();
        // make all local variables global
        prog.getGlobals().addAll(func.getLocals().removeAll());

        // create helper functions
        List<ImFunction> helperFuncs = new ArrayList<>();
        for (int i = 0; i < splitResult.size(); i++) {
            List<ImStmt> stmts = splitResult.get(i);
            ImFunction helperFunc = JassIm.ImFunction(
                func.getTrace(),
                func.getName() + "_" + i,
                JassIm.ImTypeVars(),
                JassIm.ImVars(),
                JassIm.ImVoid(),
                JassIm.ImVars(),
                JassIm.ImStmts(stmts),
                Collections.emptyList()
            );
            helperFuncs.add(helperFunc);
        }
        prog.getFunctions().addAll(helperFuncs);

        // call helper functions with ExecuteFunc
        for (ImFunction helperFunc : helperFuncs) {
            func.getBody().add(JassIm.ImFunctionCall(
                func.getTrace(),
                helperFunc,
                JassIm.ImTypeArguments(),
                JassIm.ImExprs(),
                false,
                CallType.EXECUTE
            ));
        }
    }

    private List<List<ImStmt>> split(List<ImStmt> body) {
        List<List<ImStmt>> result = new ArrayList<>();
        int fuel = 0;
        for (ImStmt s : body) {
            fuel += estimateFuel(s);
            if (result.isEmpty() || fuel > op_limit) {
                result.add(new ArrayList<>());
                fuel = 0;
            }
            result.get(result.size() - 1).add(s);
        }
        return result;
    }


    private int estimateFuel(ImStmt s) {
        return s.match(new ImStmt.Matcher<Integer>() {
            @Override
            public Integer case_ImTypeVarDispatch(ImTypeVarDispatch s) {
                return estimateFuel(s.getArguments()) + 100;
            }

            @Override
            public Integer case_ImDealloc(ImDealloc s) {
                return 10 + estimateFuel(s.getObj());
            }

            @Override
            public Integer case_ImBoolVal(ImBoolVal s) {
                return 1;
            }

            @Override
            public Integer case_ImTypeIdOfClass(ImTypeIdOfClass s) {
                return 1;
            }

            @Override
            public Integer case_ImVarAccess(ImVarAccess s) {
                return 1;
            }

            @Override
            public Integer case_ImStringVal(ImStringVal s) {
                return 1;
            }

            @Override
            public Integer case_ImMethodCall(ImMethodCall s) {
                return estimateFuel(s.getArguments())
                    + estimateFuelMethod(s.getMethod())
                    + 10;
            }

            @Override
            public Integer case_ImRealVal(ImRealVal s) {
                return 1;
            }

            @Override
            public Integer case_ImFunctionCall(ImFunctionCall s) {
                return estimateFuel(s.getArguments()) + 10 + estimateFuelFunc(s.getFunc());
            }

            @Override
            public Integer case_ImReturn(ImReturn s) {
                return 3 + estimateFuelOpt(s.getReturnValue());
            }

            @Override
            public Integer case_ImTupleSelection(ImTupleSelection s) {
                return estimateFuel(s.getTupleExpr()) + 10;
            }

            @Override
            public Integer case_ImOperatorCall(ImOperatorCall s) {
                return 5 + estimateFuel(s.getArguments());
            }

            @Override
            public Integer case_ImVarArrayAccess(ImVarArrayAccess s) {
                return 3 + estimateFuel(s.getIndexes());
            }

            @Override
            public Integer case_ImAlloc(ImAlloc s) {
                return 30;
            }

            @Override
            public Integer case_ImIntVal(ImIntVal s) {
                return 1;
            }

            @Override
            public Integer case_ImExitwhen(ImExitwhen s) {
                return 5 + estimateFuel(s.getCondition());
            }

            @Override
            public Integer case_ImVarargLoop(ImVarargLoop s) {
                throw new CompileError(s, "Cannot estimate size of function " + func.getBody() + " as it contains looops.");
            }

            @Override
            public Integer case_ImNull(ImNull s) {
                return 1;
            }

            @Override
            public Integer case_ImLoop(ImLoop s) {
                throw new CompileError(s, "Cannot estimate size of function " + func.getBody() + " as it contains looops.");
            }

            @Override
            public Integer case_ImMemberAccess(ImMemberAccess s) {
                return 1 + estimateFuel(s.getReceiver())
                    + estimateFuel(s.getIndexes());
            }

            @Override
            public Integer case_ImGetStackTrace(ImGetStackTrace s) {
                return 50;
            }

            @Override
            public Integer case_ImTupleExpr(ImTupleExpr s) {
                return 1 + estimateFuel(s.getExprs());
            }

            @Override
            public Integer case_ImTypeIdOfObj(ImTypeIdOfObj s) {
                return 1 + estimateFuel(s.getObj());
            }

            @Override
            public Integer case_ImSet(ImSet s) {
                return 3 + estimateFuel(s.getLeft()) + estimateFuel(s.getRight());
            }

            @Override
            public Integer case_ImStatementExpr(ImStatementExpr s) {
                return 1 + estimateFuel(s.getStatements()) + estimateFuel(s.getExpr());
            }

            @Override
            public Integer case_ImCompiletimeExpr(ImCompiletimeExpr s) {
                return 1;
            }

            @Override
            public Integer case_ImIf(ImIf s) {
                return 1 + estimateFuel(s.getCondition()) + Math.max(estimateFuel(s.getThenBlock()), estimateFuel(s.getElseBlock()));
            }

            @Override
            public Integer case_ImCast(ImCast s) {
                return estimateFuel(s.getExpr());
            }

            @Override
            public Integer case_ImFuncRef(ImFuncRef s) {
                return 1;
            }

            @Override
            public Integer case_ImInstanceof(ImInstanceof s) {
                return 1 + estimateFuel(s.getObj()) + 10 * tr.getImProg().getClasses().size();
            }
        });
    }

    private int estimateFuelMethod(ImMethod method) {
        return Math.max(
            estimateFuelFunc(method.getImplementation()),
            method.getSubMethods().stream()
                .mapToInt(m -> estimateFuelMethod(method))
                .sum());
    }

    private int estimateFuelFunc(ImFunction f) {
        if (f.isNative()) {
            return 10;
        }
        if (fuelVisited.containsKey(f)) {
            Integer v = fuelVisited.get(f);
            if (v == null) {
                throw new CompileError(func, "Cannot split recursive method " + func.getName() + " calling funcs: " +
                    fuelVisited.entrySet().stream()
                        .filter(e -> e.getValue() == null)
                        .map(e -> e.getKey().getName())
                        .collect(Collectors.joining(", ")));
            }
            return v;
        } else {
            // mark f as being calculated
            fuelVisited.put(f, null);
            int v = estimateFuel(f.getBody());
            fuelVisited.put(f, v);
            return v;
        }
    }

    private int estimateFuelOpt(ImExprOpt returnValue) {
        if (returnValue instanceof ImExpr) {
            return estimateFuel((ImExpr) returnValue);
        }
        return 0;
    }

    private int estimateFuel(List<? extends ImStmt> stmts) {
        return stmts.stream().mapToInt(this::estimateFuel).sum();
    }
}
