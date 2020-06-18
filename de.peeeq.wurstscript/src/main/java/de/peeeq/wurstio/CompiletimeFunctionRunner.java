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
import de.peeeq.wurstscript.intermediatelang.optimizer.FunctionSplitter;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.*;
import de.peeeq.wurstscript.types.TypesHelper;
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
    private final ImTranslator translator;
    private boolean injectObjects;
    private final Deque<Runnable> delayedActions = new ArrayDeque<>();

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


    public CompiletimeFunctionRunner(
            ImTranslator tr, ImProg imProg, Optional<File> mapFile, MpqEditor mpqEditor, WurstGui gui,
            FunctionFlagToRun flag) {
        Preconditions.checkNotNull(imProg);
        this.translator = tr;
        this.imProg = imProg;
        globalState = new ProgramStateIO(mapFile, mpqEditor, gui, imProg, true);
        this.interpreter = new ILInterpreter(imProg, gui, mapFile, globalState);

        interpreter.addNativeProvider(new CompiletimeNatives(globalState));
        interpreter.addNativeProvider(new ReflectionNativeProvider(interpreter));
        this.gui = gui;
        this.functionFlag = flag;
    }


    public void run() {
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

            partitionCompiletimeStateInitFunction();

        } catch (InterpreterException e) {
            Element origin = e.getTrace();
            sendErrors(origin, e.getMessage(), e);
            if (isUnitTestMode()) {
                throw e;
            }
        } catch (Throwable e) {
            WLogger.severe(e);
            de.peeeq.wurstscript.jassIm.Element s = interpreter.getLastStatement();
            Element origin = s == null ? null : s.attrTrace();
            if (origin != null) {
                String msg = e.getMessage();
                sendErrors(origin, msg, e);
            } else {
                throw new Error("could not get origin", e);
            }
            if (isUnitTestMode()) {
                throw e;
            }
        }

    }

    private void partitionCompiletimeStateInitFunction() {
        if (compiletimeStateInitFunction == null) {
            return;
        }

        FunctionSplitter.splitFunc(translator, compiletimeStateInitFunction);
    }

    private boolean isUnitTestMode() {
        return Optional.ofNullable(imProg)
                .map(ImProg::attrTrace)
                .map(Element::getErrorHandler)
                .map(ErrorHandler::isUnitTestMode)
                .orElse(false);
    }

    private void sendErrors(Element origin, String msg, Throwable ex) {
        gui.sendError(new CompileError(origin.attrSource(), msg, CompileError.ErrorType.ERROR, ex));

        // stackframe messages ...
        for (ILStackFrame sf : Utils.iterateReverse(interpreter.getStackFrames().getStackFrames())) {
            gui.sendError(sf.makeCompileError());
        }
    }

    /**
     * Run actions that must be run after all other code
     */
    private void runDelayedActions() {
        while (!delayedActions.isEmpty()) {
            delayedActions.removeFirst().run();
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
            ImExpr newExpr = constantToExpr(cte.getTrace(), value);
            cte.replaceBy(newExpr);
        } catch (InterpreterException e) {
            String msg = ILInterpreter.buildStacktrace(globalState, e);
            e.setStacktrace(msg);
            e.setTrace(cte.attrTrace());
            throw e;
        }
    }


    private GetAForB<ILconstObject, ImVar> globalForObject = new GetAForB<ILconstObject, ImVar>() {
        @Override
        public ImVar initFor(ILconstObject obj) {


            ImVar res = JassIm.ImVar(obj.getTrace(), obj.getType(), obj.getType() + "_compiletime", false);
            imProg.getGlobals().add(res);
            ImAlloc alloc = JassIm.ImAlloc(obj.getTrace(), obj.getType());
            addCompiletimeStateInitAlloc(alloc.getTrace(), res, alloc);


            Element trace = obj.getTrace();

            delayedActions.add(() -> {
                for (Map.Entry<ImVar, Map<List<Integer>, ILconst>> entry : obj.getAttributes().rowMap().entrySet()) {
                    ImVar var = entry.getKey();
                    Map<List<Integer>, ILconst> value1 = entry.getValue();
                    for (Map.Entry<List<Integer>, ILconst> entry2 : value1.entrySet()) {
                        List<Integer> indexes = entry2.getKey();
                        ILconst attrValue = entry2.getValue();
                        ImExprs indexesT = indexes.stream()
                                .map(i -> constantToExpr(trace, ILconstInt.create(i)))
                                .collect(Collectors.toCollection(JassIm::ImExprs));
                        addCompiletimeStateInit(JassIm.ImSet(trace, JassIm.ImMemberAccess(trace, JassIm.ImVarAccess(res), JassIm.ImTypeArguments(), var, indexesT), constantToExpr(trace, attrValue)));
                    }
                }
            });

            return res;
        }
    };

    private GetAForB<IlConstHandle, ImVar> globalForHandle = new GetAForB<IlConstHandle, ImVar>() {
        @Override
        public ImVar initFor(IlConstHandle a) {

            Element trace = imProg.getTrace();

            ImExpr init;

            Object obj = a.getObj();
            if (obj instanceof ArrayListMultimap) {
                @SuppressWarnings("unchecked")
                ArrayListMultimap<HashtableProvider.KeyPair, Object> map = (ArrayListMultimap<HashtableProvider.KeyPair, Object>) obj;
                ImType type = TypesHelper.imHashTable();
                ImVar res = JassIm.ImVar(trace, type, type + "_compiletime", false);
                imProg.getGlobals().add(res);

                init = constantToExprHashtable(trace, res, a, map);
                addCompiletimeStateInitAlloc(trace, res, init);

                return res;
            } else {
                throw new RuntimeException("Handle value " + obj + " (" + obj.getClass() + ") can not be persistet at compiletime");
            }
        }
    };

    private ImExpr constantToExpr(Element trace, ILconst value) {
        if (value instanceof ILconstBool) {
            return JassIm.ImBoolVal(((ILconstBool) value).getVal());
        } else if (value instanceof ILconstInt) {
            return JassIm.ImIntVal(((ILconstInt) value).getVal());
        } else if (value instanceof ILconstReal) {
            return JassIm.ImRealVal("" + ((ILconstReal) value).getVal());
        } else if (value instanceof ILconstString) {
            return JassIm.ImStringVal(((ILconstString) value).getVal());
        } else if (value instanceof ILconstTuple) {
            return JassIm.ImTupleExpr(
                    JassIm.ImExprs(
                            ((ILconstTuple) value).values().stream()
                                    .map(e -> constantToExpr(trace, e))
                                    .collect(Collectors.toList())
                    )
            );
        } else if (value instanceof IlConstHandle) {
            IlConstHandle h = (IlConstHandle) value;
            ImVar hVar = globalForHandle.getFor(h);
            return JassIm.ImVarAccess(hVar);
        } else if (value instanceof ILconstObject) {
            ILconstObject obj = globalState.toObject(value);
            ImVar v = globalForObject.getFor(obj);
            return JassIm.ImVarAccess(v);
        }
        throw new InterpreterException(trace, "Compiletime expression returned unsupported value " + value);

    }

    private ImFunction compiletimeStateInitFunction = null;

    private ImFunction getCompiletimeStateInitFunction() {
        ImFunction res = this.compiletimeStateInitFunction;
        if (res == null) {
            Element trace = imProg.getTrace();
            res = JassIm.ImFunction(trace, "initCompiletimeState", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
            imProg.getFunctions().add(res);
            compiletimeStateInitFunction = res;
            ImFunction mainFunc = translator.getMainFunc();
            ImFunction globalInitFunc = translator.getGlobalInitFunc();
            Preconditions.checkNotNull(mainFunc);
            ListIterator<ImStmt> iterator = mainFunc.getBody().listIterator();
            ImFunctionCall call = JassIm.ImFunctionCall(trace, res, JassIm.ImTypeArguments(), JassIm.ImExprs(), true, CallType.NORMAL);
            while (iterator.hasNext()) {
                ImStmt stmt = iterator.next();
                if (stmt instanceof ImFunctionCall) {
                    ImFunctionCall fc = (ImFunctionCall) stmt;
                    if (fc.getFunc() == globalInitFunc) {
                        // call initCompiletimeState right after globalInitFunc
                        iterator.add(call);
                        return res;
                    }
                }
            }
            iterator.add(call);
        }
        return res;
    }

    // insert at the beginning
    private void addCompiletimeStateInitAlloc(Element trace, ImVar v, ImExpr init) {
        imProg.getGlobalInits().put(v, Collections.singletonList(init));
        getCompiletimeStateInitFunction().getBody().add(0, JassIm.ImSet(trace, JassIm.ImVarAccess(v), init.copy()));
    }

    // insert at the end
    private void addCompiletimeStateInit(ImStmt stmt) {
        getCompiletimeStateInitFunction().getBody().add(stmt);
    }

    /**
     * Stores a hashtable value in a compiletime expression
     * by generating the respective native calls
     */
    private ImExpr constantToExprHashtable(Element trace, ImVar htVar, IlConstHandle handle, ArrayListMultimap<HashtableProvider.KeyPair, Object> map) {
        WPos errorPos = trace.attrErrorPos();
        // we have to collect all values after all compiletime functions have run, so use delayedActions
        delayedActions.add(() -> {
            for (Map.Entry<HashtableProvider.KeyPair, Object> entry : map.entries()) {
                HashtableProvider.KeyPair key = entry.getKey();
                Object v = entry.getValue();
                if (v instanceof ILconstInt) {
                    ILconstInt iv = (ILconstInt) v;
                    ImFunction SaveInteger = findNative("SaveInteger", errorPos);
                    addCompiletimeStateInit(JassIm.ImFunctionCall(trace, SaveInteger, JassIm.ImTypeArguments(), JassIm.ImExprs(
                            JassIm.ImVarAccess(htVar),
                            JassIm.ImIntVal(key.getParentkey()),
                            JassIm.ImIntVal(key.getChildkey()),
                            JassIm.ImIntVal(iv.getVal())
                    ), false, CallType.NORMAL));
                } else if (v instanceof ILconstReal) {
                    ILconstReal iv = (ILconstReal) v;
                    ImFunction SaveReal = findNative("SaveReal", errorPos);
                    addCompiletimeStateInit(JassIm.ImFunctionCall(trace, SaveReal, JassIm.ImTypeArguments(), JassIm.ImExprs(
                            JassIm.ImVarAccess(htVar),
                            JassIm.ImIntVal(key.getParentkey()),
                            JassIm.ImIntVal(key.getChildkey()),
                            JassIm.ImRealVal("" + iv.getVal())
                    ), false, CallType.NORMAL));
                } else if (v instanceof ILconstString) {
                    ILconstString iv = (ILconstString) v;
                    ImFunction SaveStr = findNative("SaveStr", errorPos);
                    addCompiletimeStateInit(JassIm.ImFunctionCall(trace, SaveStr, JassIm.ImTypeArguments(), JassIm.ImExprs(
                            JassIm.ImVarAccess(htVar),
                            JassIm.ImIntVal(key.getParentkey()),
                            JassIm.ImIntVal(key.getChildkey()),
                            JassIm.ImStringVal(iv.getVal())
                    ), false, CallType.NORMAL));
                } else if (v instanceof ILconstBool) {
                    ILconstBool iv = (ILconstBool) v;
                    ImFunction SaveBoolean = findNative("SaveBoolean", errorPos);
                    addCompiletimeStateInit(JassIm.ImFunctionCall(trace, SaveBoolean, JassIm.ImTypeArguments(), JassIm.ImExprs(
                        JassIm.ImVarAccess(htVar),
                        JassIm.ImIntVal(key.getParentkey()),
                        JassIm.ImIntVal(key.getChildkey()),
                        JassIm.ImBoolVal(iv.getVal())
                    ), false, CallType.NORMAL));
                } else if (v instanceof ILconstNull) {
                    // treat null like no entry
                } else {
                    throw new CompileError(errorPos, "Unsupported value stored in HashMap: " + v + " // " + v.getClass().getSimpleName());
                }
            }
        });

        // we already return the expr and fill out stmts in delayedActions (see above)
        ImFunction initHashtable = findNative("InitHashtable", errorPos);
        return JassIm.ImFunctionCall(trace, initHashtable, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL);
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
