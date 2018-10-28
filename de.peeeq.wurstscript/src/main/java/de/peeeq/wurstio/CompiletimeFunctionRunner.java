package de.peeeq.wurstio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.ReflectionNativeProvider;
import de.peeeq.wurstio.jassinterpreter.providers.HashtableProvider;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILStackFrame;
import de.peeeq.wurstscript.intermediatelang.interpreter.LocalState;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagCompiletime;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class CompiletimeFunctionRunner {

    private final ImProg imProg;
    private final ILInterpreter interpreter;
    private final WurstGui gui;
    private final FunctionFlagToRun functionFlag;
    private final List<ImFunction> successTests = Lists.newArrayList();
    private final Map<ImFunction, Pair<de.peeeq.wurstscript.jassIm.Element, String>> failTests = Maps.newLinkedHashMap();
    private final ProgramStateIO globalState;
    private boolean injectObjects;
    private final List<Runnable> delayedActions = new ArrayList<>();

    public ILInterpreter getInterpreter() {
        return interpreter;
    }

    public ProgramStateIO getGlobalState() {
        return globalState;
    }


    public enum FunctionFlagToRun {
        Tests {
            @Override
            public boolean matches(ImFunction f) {
                return f.hasFlag(FunctionFlagEnum.IS_TEST) || f.isCompiletime();
            }
        },
        CompiletimeFunctions {
            @Override
            public boolean matches(ImFunction f) {
                return f.isCompiletime();
            }
        };

        public abstract boolean matches(ImFunction f);
    }


    public CompiletimeFunctionRunner(ImProg imProg, File mapFile, MpqEditor mpqEditor, WurstGui gui, FunctionFlagToRun flag) {
        Preconditions.checkNotNull(imProg);
        this.imProg = imProg;
        globalState = new ProgramStateIO(mapFile, mpqEditor, gui, imProg, true);
        this.interpreter = new ILInterpreter(imProg, gui, mapFile, globalState);

        interpreter.addNativeProvider(new CompiletimeNatives(globalState));
        interpreter.addNativeProvider(new ReflectionNativeProvider(interpreter));
        this.gui = gui;
        this.functionFlag = flag;
    }


    public void run() {
//		interpreter.executeFunction("main");
//		interpreter.executeFunction("initGlobals");
        try {
            List<Either<ImCompiletimeExpr, ImFunction>> toExecute = new ArrayList<>();
            collectCompiletimeExpressions(toExecute);
            collectCompiletimeFunctions(toExecute);

            toExecute.sort(Comparator.comparing(this::getOrderIndex));

            execute(toExecute);


            if (functionFlag == FunctionFlagToRun.CompiletimeFunctions) {
                interpreter.writebackGlobalState(isInjectObjects());
            }
            runDelayedActions();

        } catch (InterpreterException e) {
            Element origin = e.getTrace();
            sendErrors(origin, e.getMessage());
            if (isUnitTestMode()) {
                throw e;
            }
        } catch (Throwable e) {
            WLogger.severe(e);
            de.peeeq.wurstscript.jassIm.Element s = interpreter.getLastStatement();
            Element origin = s == null ? null : s.attrTrace();
            if (origin != null) {
                String msg = e.getMessage();
                sendErrors(origin, msg);
            } else {
                throw new Error("could not get origin", e);
            }
            if (isUnitTestMode()) {
                throw e;
            }
        }

    }

    private boolean isUnitTestMode() {
        return Optional.ofNullable(imProg)
                .map(ImProg::attrTrace)
                .map(Element::getErrorHandler)
                .map(ErrorHandler::isUnitTestMode)
                .orElse(false);
    }

    private void sendErrors(Element origin, String msg) {
        gui.sendError(new CompileError(origin.attrSource(), msg));

        // stackframe messages ...
        for (ILStackFrame sf : Utils.iterateReverse(interpreter.getStackFrames().getStackFrames())) {
            gui.sendError(sf.makeCompileError());
        }
    }

    /**
     * Run actions that must be run after all other code
     */
    private void runDelayedActions() {
        for (Runnable delayedAction : delayedActions) {
            delayedAction.run();
        }
    }

    private void execute(List<Either<ImCompiletimeExpr, ImFunction>> es) {
        for (Either<ImCompiletimeExpr, ImFunction> e : es) {
            if (e.isLeft()) {
                ImCompiletimeExpr cte = e.getLeft();
                executeCompiletimeExpr(cte);
            } else {
                ImFunction f = e.getRight();
                executeCompiletimeFunction(f);
            }
        }
        interpreter.completeTimers();
    }

    private int getOrderIndex(Either<ImCompiletimeExpr, ImFunction> e) {
        if (e.isLeft()) {
            ImCompiletimeExpr cte = e.getLeft();
            return cte.getExecutionOrderIndex();
        } else {
            ImFunction f = e.getRight();
            for (FunctionFlag flag : f.getFlags()) {
                if (flag instanceof FunctionFlagCompiletime) {
                    FunctionFlagCompiletime cflag = (FunctionFlagCompiletime) flag;
                    return cflag.getOrderIndex();

                }
            }
            return 0;
        }
    }

    private void collectCompiletimeFunctions(List<Either<ImCompiletimeExpr, ImFunction>> toExecute) {
        for (ImFunction f : imProg.getFunctions()) {
            if (functionFlag.matches(f)) {
                toExecute.add(Either.forRight(f));
            }
        }
    }

    private void collectCompiletimeExpressions(List<Either<ImCompiletimeExpr, ImFunction>> toExecute) {
        imProg.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
            @Override
            public void visit(ImCompiletimeExpr e) {
                super.visit(e);
                toExecute.add(Either.forLeft(e));
            }
        });
    }


    private void executeCompiletimeExpr(ImCompiletimeExpr cte) {
        try {
            ProgramState globalState = interpreter.getGlobalState();
            globalState.setLastStatement(cte);
            globalState.resetStackframes();
            globalState.pushStackframe(cte, cte.attrTrace().attrErrorPos());
            LocalState localState = new LocalState();
            ILconst value = cte.evaluate(globalState, localState);
            ImExpr newExpr = constantToExpr(cte, value);
            cte.replaceBy(newExpr);
        } catch (InterpreterException e) {
            String msg = ILInterpreter.buildStacktrace(globalState, e);
            e.setStacktrace(msg);
            e.setTrace(cte.attrTrace());
            throw e;
        }
    }

    private ImExpr constantToExpr(ImCompiletimeExpr cte, ILconst value) {
        Element trace = cte.attrTrace();
        if (value instanceof ILconstBool) {
            return JassIm.ImBoolVal(((ILconstBool) value).getVal());
        } else if (value instanceof ILconstInt) {
            return JassIm.ImIntVal(((ILconstInt) value).getVal());
        } else if (value instanceof ILconstString) {
            return JassIm.ImStringVal(((ILconstString) value).getVal());
        } else if (value instanceof ILconstTuple) {
            return JassIm.ImTupleExpr(
                    JassIm.ImExprs(
                            ((ILconstTuple) value).values().stream()
                                    .map(e -> constantToExpr(cte, e))
                                    .collect(Collectors.toList())
                    )
            );
        } else if (value instanceof IlConstHandle) {
            IlConstHandle h = (IlConstHandle) value;
            Object obj = h.getObj();
            if (obj instanceof ArrayListMultimap) {
                // a hashtable
                @SuppressWarnings("unchecked")
                ArrayListMultimap<HashtableProvider.KeyPair, Object> map = (ArrayListMultimap<HashtableProvider.KeyPair, Object>) obj;
                return constantToExprHashtable(cte, trace, map);
            }
        }
        throw new InterpreterException(trace, "Compiletime expression returned unsupported value " + value);

    }

    /**
     * Stores a hashtable value in a compiletime expression
     * by generating the respective native calls
     */
    private ImExpr constantToExprHashtable(ImCompiletimeExpr cte, Element trace, ArrayListMultimap<HashtableProvider.KeyPair, Object> map) {
        ImFunction f = cte.getNearestFunc();
        ImVar htVar = JassIm.ImVar(trace, cte.attrTyp(), "ht", false);
        f.getLocals().add(htVar);


        WPos errorPos = trace.attrErrorPos();
        ImFunction initHashtable = findNative("InitHashtable", errorPos);
        ImStmts stmts = JassIm.ImStmts(
                JassIm.ImSet(trace, JassIm.ImVarAccess(htVar), JassIm.ImFunctionCall(trace, initHashtable, JassIm.ImExprs(), false, CallType.NORMAL))
        );

        // we have to collect all values after all compiletime functions have run, so use delayedActions
        delayedActions.add(() -> {
            for (Map.Entry<HashtableProvider.KeyPair, Object> entry : map.entries()) {
                HashtableProvider.KeyPair key = entry.getKey();
                Object v = entry.getValue();
                if (v instanceof ILconstInt) {
                    ILconstInt iv = (ILconstInt) v;
                    ImFunction SaveInteger = findNative("SaveInteger", errorPos);
                    stmts.add(JassIm.ImFunctionCall(trace, SaveInteger, JassIm.ImExprs(
                            JassIm.ImVarAccess(htVar),
                            JassIm.ImIntVal(key.getParentkey()),
                            JassIm.ImIntVal(key.getChildkey()),
                            JassIm.ImIntVal(iv.getVal())
                    ), false, CallType.NORMAL));
                } else {
                    throw new CompileError(errorPos, "Unsupported value stored in HashMap: " + v + " // " + v.getClass().getSimpleName());
                }


            }
        });

        // we already return the expr and fill out stmts in delayedActions (see above)
        return JassIm.ImStatementExpr(
                stmts,
                JassIm.ImVarAccess(htVar)
        );
    }

    @NotNull
    private ImFunction findNative(String funcName, WPos trace) {
        return imProg.getFunctions()
                .stream()
                .filter(ImFunction::isNative)
                .filter(func -> func.getName().equals(funcName))
                .findFirst()
                .orElseGet(() -> {
                    throw new CompileError(trace, "Could not find native 'InitHashtable'");
                });
    }


    private void executeCompiletimeFunction(ImFunction f) {
        if (functionFlag.matches(f)) {
            try {
                if (!f.getBody().isEmpty()) {
                    interpreter.getGlobalState().setLastStatement(f.getBody().get(0));
                }
                WLogger.info("running " + functionFlag + " function " + f.getName());
                interpreter.runVoidFunc(f, null);
                successTests.add(f);
            } catch (TestSuccessException e) {
                successTests.add(f);
            } catch (TestFailException e) {
                failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
            } catch (Throwable e) {
                failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
                throw e;
            }
        }
    }

    public List<ImFunction> getSuccessTests() {
        return successTests;
    }


    public Map<ImFunction, Pair<de.peeeq.wurstscript.jassIm.Element, String>> getFailTests() {
        return failTests;
    }


    public boolean isInjectObjects() {
        return injectObjects;
    }


    public void setInjectObjects(boolean injectObjects) {
        this.injectObjects = injectObjects;
    }


    public void setOutputStream(PrintStream printStream) {
        interpreter.getGlobalState().setOutStream(printStream);
    }

}
