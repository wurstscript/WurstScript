package de.peeeq.wurstio;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.wurstscript.projectconfig.WurstProjectConfigData;
import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.ReflectionNativeProvider;
import de.peeeq.wurstio.jassinterpreter.providers.HashtableProvider;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.GlobalVarDef;
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
import de.peeeq.wurstscript.translation.imtranslation.ClassManagementVars;
import de.peeeq.wurstscript.translation.imtranslation.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class CompiletimeFunctionRunner implements AutoCloseable {

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
    private final Map<ClassManagementVars, List<CompiletimeObjectInit>> compiletimeObjects = new LinkedHashMap<>();
    private final Map<String, Long> compiletimeFunctionNanos = new LinkedHashMap<>();
    private long compiletimeExprNanos = 0L;

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
        FunctionFlagToRun flag, WurstProjectConfigData projectConfigData, boolean isProd, boolean cache) {
        Preconditions.checkNotNull(imProg);
        this.translator = tr;
        this.imProg = imProg;
        globalState = new ProgramStateIO(mapFile, mpqEditor, gui, imProg, true);
        // The interpreter is handed a program; this hands over the one thing it cannot work out from
        // the program alone, which is what a specialised node was copied from.
        globalState.setSpecialisations(tr);
        initializeBackendConstants();
        this.interpreter = new ILInterpreter(imProg, gui, mapFile, globalState);

        interpreter.addNativeProvider(new CompiletimeNatives(globalState, projectConfigData, isProd));
        interpreter.addNativeProvider(new ReflectionNativeProvider(interpreter));
        this.gui = gui;
        this.functionFlag = flag;
    }

    private void initializeBackendConstants() {
        for (ImVar global : imProg.getGlobals()) {
            if (global.getName().equals("MagicFunctions_isLua")) {
                globalState.setValUntracked(global, ILconstBool.instance(translator.isLuaTarget()));
                return;
            }
        }
    }

    public void run() {
        try {
            long t0 = System.nanoTime();
            List<Either<ImCompiletimeExpr, ImFunction>> toExecute = new ArrayList<>();
            collectCompiletimeExpressions(toExecute);
            collectCompiletimeFunctions(toExecute);
            long tCollected = System.nanoTime();

            toExecute.sort(Comparator.comparingInt(this::getOrderIndex));
            long tSorted = System.nanoTime();

            execute(toExecute);
            long tExecuted = System.nanoTime();

            if (functionFlag == FunctionFlagToRun.CompiletimeFunctions) {
                emitCompiletimeState();
            }

            if (functionFlag == FunctionFlagToRun.CompiletimeFunctions) {
                interpreter.writebackGlobalState(isInjectObjects());
            }
            long tWriteback = System.nanoTime();
            runDelayedActions();
            emitCompiletimeObjectAllocs();
            if (functionFlag == FunctionFlagToRun.CompiletimeFunctions) {
                insertCompiletimeScalarStateInitCalls();
                insertCompiletimeArrayStateInitCalls();
            }
            long tDelayed = System.nanoTime();

            partitionCompiletimeStateInitFunction();
            long tPartitioned = System.nanoTime();
            logCompiletimeTiming(toExecute, t0, tCollected, tSorted, tExecuted, tWriteback, tDelayed, tPartitioned);

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
                sendErrors(origin, describeFailure(e), e);
            } else {
                throw new Error("could not get origin", e);
            }
            if (isUnitTestMode()) {
                throw e;
            }
        }

    }

    private void logCompiletimeTiming(List<Either<ImCompiletimeExpr, ImFunction>> toExecute,
                                      long t0, long tCollected, long tSorted, long tExecuted,
                                      long tWriteback, long tDelayed, long tPartitioned) {
        int exprCount = 0;
        int funcCount = 0;
        for (Either<ImCompiletimeExpr, ImFunction> e : toExecute) {
            if (e.isLeft()) {
                exprCount++;
            } else {
                funcCount++;
            }
        }
        WLogger.info(String.format(
            "Compiletime breakdown: total=%dms collect=%dms sort=%dms execute=%dms writeback=%dms delayed=%dms partition=%dms funcs=%d exprs=%d exprEval=%dms",
            ms(tPartitioned - t0),
            ms(tCollected - t0),
            ms(tSorted - tCollected),
            ms(tExecuted - tSorted),
            ms(tWriteback - tExecuted),
            ms(tDelayed - tWriteback),
            ms(tPartitioned - tDelayed),
            funcCount,
            exprCount,
            ms(compiletimeExprNanos)
        ));
        if (!compiletimeFunctionNanos.isEmpty()) {
            List<Map.Entry<String, Long>> top = compiletimeFunctionNanos.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(Collectors.toList());
            StringBuilder sb = new StringBuilder("Top compiletime functions:");
            for (Map.Entry<String, Long> e : top) {
                sb.append("\n  ").append(e.getKey()).append(": ").append(ms(e.getValue())).append("ms");
            }
            WLogger.info(sb.toString());
        }
    }

    private static long ms(long nanos) {
        return nanos / 1_000_000L;
    }

    private void partitionCompiletimeStateInitFunction() {
        if (compiletimeStateInitFunction != null) {
            FunctionSplitter.splitFunc(translator, compiletimeStateInitFunction);
        }
        List<ImFunction> splitTargets = new ArrayList<>(scalarStateSplitTargets);
        splitTargets.addAll(arrayStateSplitTargets);
        splitTargets.sort(Comparator.comparing(ImFunction::getName));
        for (ImFunction stateFunction : splitTargets) {
            if (!stateFunction.getBody().isEmpty()) {
                FunctionSplitter.splitFunc(translator, stateFunction);
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

    private void sendErrors(Element origin, String msg, Throwable ex) {
        gui.sendError(new CompileError(origin.attrSource(),
                msg == null || msg.isBlank() ? describeFailure(ex) : msg,
                CompileError.ErrorType.ERROR, ex));

        // stackframe messages ...
        for (ILStackFrame sf : Utils.iterateReverse(interpreter.getStackFrames().getStackFrames())) {
            gui.sendError(sf.makeCompileError());
        }
    }

    static String describeFailure(Throwable failure) {
        String message = failure.getMessage();
        return message == null || message.isBlank() ? failure.getClass().getSimpleName() : message;
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
        long t0 = System.nanoTime();
        try {
            ProgramState globalState = interpreter.getGlobalState();
            globalState.setLastStatement(cte);
            globalState.resetStackframes();
            globalState.pushStackframe(cte, cte.attrTrace().attrErrorPos());
            LocalState localState = new LocalState();
            ILconst value = cte.evaluate(globalState, localState);
            ImExpr newExpr = constantToExpr(cte.getTrace(), value);
            if(translator.isLuaTarget() && value.toString().equals("0")) {
                // convert 0 to null/nil, if the value is 0 and not a numeric type
                ImExpr expr = cte.getExpr();

                if(expr instanceof ImNull) {
                    newExpr = ImHelper.nullExpr();
                } else {
                    @Nullable ImType exprType = null;
                    if(expr instanceof ImFunctionCall) {
                        exprType = ((ImFunctionCall) expr).getFunc().getReturnType();
                    } else if(expr instanceof ImVarAccess) {
                        exprType = ((ImVarAccess)expr).getVar().getType();
                    } else if(expr instanceof ImVarArrayAccess) {
                        ImType type = ((ImVarArrayAccess)expr).getVar().getType();
                        if(type instanceof ImArrayLikeType) {
                            exprType = ((ImArrayLikeType) type).getEntryType();
                        }
                    }
                    if(exprType != null && !TypesHelper.isIntType(exprType) && !TypesHelper.isRealType(exprType)) {
                        newExpr = ImHelper.nullExpr();
                    }
                }
                // TODO is this complete? Are there more cases where 0 must be replaced?
                // A function can return null
                // null can be a literal
                // null can be a variable
            }
            cte.replaceBy(newExpr);
        } catch (InterpreterException e) {
            String msg = ILInterpreter.buildStacktrace(globalState, e);
            e.setStacktrace(msg);
            e.setTrace(cte.attrTrace());
            throw e;
        } finally {
            compiletimeExprNanos += System.nanoTime() - t0;
        }
    }


    private final GetAForB<ILconstObject, ImVar> globalForObject = new GetAForB<ILconstObject, ImVar>() {
        @Override
        public ImVar initFor(ILconstObject obj) {


            ImVar res = JassIm.ImVar(obj.getTrace(), obj.getType(), obj.getType() + "_compiletime", false);
            imProg.getGlobals().add(res);
            globalState.setValUntracked(res, obj);

            registerCompiletimeObject(obj, res);


            Element trace = obj.getTrace();

            delayedActions.add(() -> {
                for (Map.Entry<ImVar, Map<List<Integer>, ILconst>> entry : obj.getAttributes().rowMap().entrySet()) {
                    ImVar var = entry.getKey();
                    Map<List<Integer>, ILconst> value1 = entry.getValue();
                    for (Map.Entry<List<Integer>, ILconst> entry2 : value1.entrySet()) {
                        List<Integer> indexes = entry2.getKey();
                        ILconst attrValue = entry2.getValue();
                        ImExprs indexesT = JassIm.ImExprs();
                        for (Integer i : indexes) {
                            ImExpr imExpr = constantToExpr(trace, ILconstInt.create(i));
                            indexesT.add(imExpr);
                        }
                        ImExpr value2 = constantToExpr(trace, attrValue);
                        if(translator.isLuaTarget() && value2.toString().equals("0")) {
                            ImType varType = var.getType();
                            if(varType instanceof ImArrayLikeType) {
                                varType = ((ImArrayLikeType) varType).getEntryType();
                            }
                            if (!TypesHelper.isIntType(varType) && !TypesHelper.isRealType(varType)) {
                                value2 = ImHelper.nullExpr();
                            }
                        }
                        addCompiletimeStateInit(JassIm.ImSet(trace, JassIm.ImMemberAccess(trace, JassIm.ImVarAccess(res), JassIm.ImTypeArguments(), var, indexesT), value2));
                    }
                }
            });

            return res;
        }
    };

    private final GetAForB<IlConstHandle, ImVar> globalForHandle = new GetAForB<IlConstHandle, ImVar>() {
        @Override
        public ImVar initFor(IlConstHandle a) {

            Element trace = imProg.getTrace();

            ImExpr init;

            Object obj = a.getObj();
            if (obj instanceof LinkedListMultimap) {
                @SuppressWarnings("unchecked")
                LinkedListMultimap<HashtableProvider.KeyPair, Object> map = (LinkedListMultimap<HashtableProvider.KeyPair, Object>) obj;
                ImType type = TypesHelper.imHashTable();
                ImVar res = JassIm.ImVar(trace, type, type + "_compiletime", false);
                imProg.getGlobals().add(res);
                globalState.setValUntracked(res, a);

                init = constantToExprHashtable(trace, res, a, map);
                addCompiletimeStateInitAlloc(trace, res, init);

                return res;
            } else {
                throw new RuntimeException("Handle value " + obj + " (" + obj.getClass() + ") can not be persistet at compiletime");
            }
        }
    };

    private ImExpr constantToExpr(Element trace, ILconst value) {
        return constantToExpr(trace, value, null);
    }

    /**
     * The text for a string a compiletime expression produced, which becomes a literal in the
     * generated script.
     * <p>
     * A string held by the interpreter is a sequence of bytes and may hold half of a character -
     * slicing one in half is a thing the standard library does deliberately. A literal cannot: the
     * script is written as UTF-8 and neither Jass nor the escaping here can write a byte down
     * numerically, so half a character would go in as the replacement character and come back out
     * three bytes long. Refused rather than carried across at a different length.
     */
    private String literalText(ILconstString value, Element trace) {
        if (!value.isText()) {
            throw new CompileError(trace, "A compiletime expression returned a string holding part of a"
                + " multibyte character, which cannot be written into the generated script. Slice it"
                + " where the program runs rather than at compiletime, or keep whole characters.");
        }
        return value.text();
    }

    private ImExpr constantToExpr(Element trace, ILconst value, @Nullable ImType expectedType) {
        if (value instanceof ILconstBool) {
            return JassIm.ImBoolVal(((ILconstBool) value).getVal());
        } else if (value instanceof ILconstInt) {
            return JassIm.ImIntVal(((ILconstInt) value).getVal());
        } else if (value instanceof ILconstReal) {
            return JassIm.ImRealVal("" + ((ILconstReal) value).getVal());
        } else if (value instanceof ILconstString) {
            return JassIm.ImStringVal(literalText((ILconstString) value, trace));
        } else if (value instanceof ILconstNull) {
            return expectedType == null ? ImHelper.nullExpr() : JassIm.ImNull(expectedType.copy());
        } else if (value instanceof ILconstTuple) {
            List<ImExpr> list = new ArrayList<>();
            ImTupleType tupleType = expectedType instanceof ImTupleType ? (ImTupleType) expectedType : null;
            int index = 0;
            for (ILconst e : ((ILconstTuple) value).values()) {
                ImType elementType = tupleType != null && index < tupleType.getTypes().size()
                    ? tupleType.getTypes().get(index) : null;
                ImExpr imExpr = constantToExpr(trace, e, elementType);
                list.add(imExpr);
                index++;
            }
            return JassIm.ImTupleExpr(
                    JassIm.ImExprs(
                        list
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

    private void registerCompiletimeObject(ILconstObject obj, ImVar targetVar) {
        ClassManagementVars mVars = translator.getClassManagementVarsFor(obj.getType().getClassDef());
        compiletimeObjects.computeIfAbsent(mVars, k -> new ArrayList<>())
            .add(new CompiletimeObjectInit(obj, targetVar));
    }

    private void emitCompiletimeObjectAllocs() {
        if (compiletimeObjects.isEmpty()) {
            return;
        }

        List<ImStmt> objectInits = new ArrayList<>();

        for (Map.Entry<ClassManagementVars, List<CompiletimeObjectInit>> entry : compiletimeObjects.entrySet()) {
            List<CompiletimeObjectInit> objs = entry.getValue();
            if (objs.isEmpty()) {
                continue;
            }

            objs.sort(Comparator.comparingInt(o -> o.object.getObjectId()));

            ClassManagementVars mVars = entry.getKey();
            // Ensure replayed allocations are driven by maxIndex and not by stale free-list state.
            objectInits.add(JassIm.ImSet(objs.get(0).object.getTrace(),
                JassIm.ImVarAccess(mVars.freeCount), JassIm.ImIntVal(0)));

            int currentMax = 0;
            int finalMax = globalState.getMaxAllocatedId(objs.get(0).object.getImClass());

            for (CompiletimeObjectInit init : objs) {
                int desiredId = init.object.getObjectId();
                int targetMax = desiredId - 1;
                if (targetMax > currentMax) {
                    objectInits.add(JassIm.ImSet(init.object.getTrace(),
                        JassIm.ImVarAccess(mVars.maxIndex),
                        JassIm.ImIntVal(targetMax)));
                    currentMax = targetMax;
                }

                ImAlloc alloc = JassIm.ImAlloc(init.object.getTrace(), init.object.getType());
                ImSet assign = JassIm.ImSet(init.object.getTrace(), JassIm.ImVarAccess(init.targetVar), alloc);
                objectInits.add(assign);
                imProg.getGlobalInits().put(init.targetVar, Collections.singletonList(assign));
                currentMax = desiredId;
            }

            if (finalMax > currentMax) {
                objectInits.add(JassIm.ImSet(objs.get(0).object.getTrace(),
                    JassIm.ImVarAccess(mVars.maxIndex), JassIm.ImIntVal(finalMax)));
            }
        }

        getCompiletimeStateInitFunction().getBody().addAll(0, objectInits);
    }

    private static class CompiletimeObjectInit {
        private final ILconstObject object;
        private final ImVar targetVar;

        private CompiletimeObjectInit(ILconstObject object, ImVar targetVar) {
            this.object = object;
            this.targetVar = targetVar;
        }
    }

    private static class StateReplayLocation {
        private final @Nullable ImFunction target;
        private final Set<ImSet> initializers;

        private StateReplayLocation(@Nullable ImFunction target, Set<ImSet> initializers) {
            this.target = target;
            this.initializers = initializers;
        }
    }

    private static class PackageStateReplay {
        private final ImFunction target;
        private final Set<ImSet> initializers;
        private final ImFunction replay;

        private PackageStateReplay(ImFunction target, Set<ImSet> initializers, ImFunction replay) {
            this.target = target;
            this.initializers = initializers;
            this.replay = replay;
        }
    }

    private ImFunction compiletimeStateInitFunction = null;
    private ImFunction compiletimeScalarStateInitFunction = null;
    private ImFunction compiletimeArrayStateInitFunction = null;
    private final List<PackageStateReplay> packageScalarStateReplays = new ArrayList<>();
    private final List<PackageStateReplay> packageArrayStateReplays = new ArrayList<>();
    private final List<ImFunction> scalarStateSplitTargets = new ArrayList<>();
    private final List<ImFunction> arrayStateSplitTargets = new ArrayList<>();
    private int genericScalarStateInitCounter;
    private int genericArrayStateInitCounter;

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
        ImSet imSet = JassIm.ImSet(trace, JassIm.ImVarAccess(v), init.copy());
        imProg.getGlobalInits().put(v, Collections.singletonList(imSet));
        getCompiletimeStateInitFunction().getBody().add(0, imSet);
    }

    // insert at the end
    private void addCompiletimeStateInit(ImStmt stmt) {
        getCompiletimeStateInitFunction().getBody().add(stmt);
    }

    private ImFunction getCompiletimeScalarStateInitFunction(StateReplayLocation location) {
        if (location.target == null && compiletimeScalarStateInitFunction != null) {
            return compiletimeScalarStateInitFunction;
        }
        Element trace = imProg.getTrace();
        String name = location.target == null
            ? "initCompiletimeScalarState"
            : "initCompiletimeScalarState_" + genericScalarStateInitCounter++;
        ImFunction result = JassIm.ImFunction(trace, name, JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        imProg.getFunctions().add(result);
        scalarStateSplitTargets.add(result);
        if (location.target == null) {
            compiletimeScalarStateInitFunction = result;
        } else {
            packageScalarStateReplays.add(new PackageStateReplay(
                location.target, location.initializers, result));
        }
        return result;
    }

    private ImFunction getCompiletimeArrayStateInitFunction(StateReplayLocation location) {
        if (location.target == null && compiletimeArrayStateInitFunction != null) {
            return compiletimeArrayStateInitFunction;
        }
        Element trace = imProg.getTrace();
        String name = location.target == null
            ? "initCompiletimeArrayState"
            : "initCompiletimeArrayState_" + genericArrayStateInitCounter++;
        ImFunction result = JassIm.ImFunction(trace, name, JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        imProg.getFunctions().add(result);
        arrayStateSplitTargets.add(result);
        if (location.target == null) {
            compiletimeArrayStateInitFunction = result;
        } else {
            packageArrayStateReplays.add(new PackageStateReplay(
                location.target, location.initializers, result));
        }
        return result;
    }

    private void insertCompiletimeScalarStateInitCalls() {
        insertCompiletimeMigratedStateInitCalls(packageScalarStateReplays, compiletimeScalarStateInitFunction);
    }

    private void insertCompiletimeArrayStateInitCalls() {
        insertCompiletimeMigratedStateInitCalls(packageArrayStateReplays, compiletimeArrayStateInitFunction);
    }

    private void insertCompiletimeMigratedStateInitCalls(List<PackageStateReplay> packageStateReplays,
                                                         @Nullable ImFunction mainStateReplay) {
        if (packageStateReplays.isEmpty() && mainStateReplay == null) {
            return;
        }
        ImFunction globalInitFunction = translator.getGlobalInitFunc();
        List<PackageStateReplay> packageReplays = new ArrayList<>(packageStateReplays);
        packageReplays.sort(Comparator
            .comparing((PackageStateReplay replay) -> replay.target.getName())
            .thenComparing(replay -> replay.replay.getName()));
        for (PackageStateReplay packageReplay : packageReplays) {
            if (packageReplay.replay.getBody().isEmpty()) {
                continue;
            }
            int insertionIndex = findLastInitializer(packageReplay.target, packageReplay.initializers);
            if (insertionIndex >= 0) {
                packageReplay.target.getBody().add(
                    insertionIndex + 1, newCompiletimeStateInitCall(packageReplay.replay));
            }
        }

        ImFunction mainReplay = mainStateReplay;
        if (mainReplay != null && !mainReplay.getBody().isEmpty()) {
            ImStmts mainBody = translator.getMainFunc().getBody();
            ImFunction stateInit = compiletimeStateInitFunction;
            if (stateInit != null) {
                for (int i = 0; i < mainBody.size(); i++) {
                    ImStmt stmt = mainBody.get(i);
                    if (stmt instanceof ImFunctionCall && ((ImFunctionCall) stmt).getFunc() == stateInit) {
                        mainBody.add(i + 1, newCompiletimeStateInitCall(mainReplay));
                        return;
                    }
                }
            }
            for (int i = 0; i < mainBody.size(); i++) {
                ImStmt stmt = mainBody.get(i);
                if (stmt instanceof ImFunctionCall && ((ImFunctionCall) stmt).getFunc() == globalInitFunction) {
                    mainBody.add(i + 1, newCompiletimeStateInitCall(mainReplay));
                    return;
                }
            }
            mainBody.add(0, newCompiletimeStateInitCall(mainReplay));
        }
    }

    private int findLastInitializer(ImFunction function, Set<ImSet> modifiedInitializers) {
        if (function == null || function.getBody().isEmpty()) {
            return -1;
        }
        int insertionIndex = -1;
        for (int i = 0; i < function.getBody().size(); i++) {
            if (function.getBody().get(i) instanceof ImSet
                && modifiedInitializers.contains(function.getBody().get(i))) {
                insertionIndex = i;
            }
        }
        return insertionIndex;
    }

    private ImFunctionCall newCompiletimeStateInitCall(ImFunction replayFunction) {
        return JassIm.ImFunctionCall(imProg.getTrace(), replayFunction,
            JassIm.ImTypeArguments(), JassIm.ImExprs(), true, CallType.NORMAL);
    }

    private void emitCompiletimeState() {
        // constantToExpr may materialize object handles as additional globals.
        // Iterate over a snapshot to avoid modifying the collection in-flight.
        Set<ImVar> runtimeScalarWrites = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<RuntimeArrayWrite> runtimeArrayWrites = findRuntimeWrites(runtimeScalarWrites);
        List<ImVar> modifiedScalars = new ArrayList<>(globalState.getModifiedScalars());
        List<ImVar> modifiedArrays = new ArrayList<>(globalState.getModifiedArrays());
        Map<ImVar, Integer> globalOrder = new IdentityHashMap<>();
        for (int i = 0; i < imProg.getGlobals().size(); i++) {
            globalOrder.put(imProg.getGlobals().get(i), i);
        }
        Comparator<ImVar> stableGlobalOrder = Comparator
            .comparingInt((ImVar var) -> globalOrder.getOrDefault(var, Integer.MAX_VALUE))
            .thenComparing(ImVar::getName);
        modifiedScalars.sort(stableGlobalOrder);
        for (ImVar var : modifiedScalars) {
            if (!imProg.getGlobals().contains(var) || var.getType() instanceof ImArrayLikeType
                || !isCompiletimeStateMigrationTarget(var)) {
                continue;
            }
            StateReplayLocation replayLocation = findReplayTarget(var);
            ImFunction replayFunction = getCompiletimeScalarStateInitFunction(replayLocation);
            for (ProgramState.ScalarState state : globalState.getScalarStates(var)) {
                if (!isPersistableCompiletimeValue(state.getValue())) {
                    String message = "Unsupported compiletime scalar value for " + var.getName()
                        + ": " + state.getValue();
                    if (runtimeScalarWrites.contains(var)) {
                        WLogger.warning(message + "; runtime initialization remains authoritative ("
                            + sourceDiagnostic(var) + ")");
                        continue;
                    }
                    throw new InterpreterException(var.getTrace(), message);
                }
                if (!state.isGeneric()) {
                    replayFunction.getBody().add(JassIm.ImSet(var.getTrace(), JassIm.ImVarAccess(var),
                        constantToExpr(var.getTrace(), state.getValue(), var.getType())));
                } else if (state.getTypeArguments().isEmpty()) {
                    throw new InterpreterException(var.getTrace(),
                        "Could not determine the generic specialization for compiletime scalar " + var.getName());
                } else {
                    emitCompiletimeGenericScalarState(replayFunction, var, state);
                }
            }
        }
        modifiedArrays.sort(stableGlobalOrder);
        for (ImVar var : modifiedArrays) {
            if (!imProg.getGlobals().contains(var) || !isCompiletimeStateMigrationTarget(var)) {
                continue;
            }
            if (!(var.getType() instanceof ImArrayLikeType)) {
                continue;
            }
            StateReplayLocation replayLocation = findReplayTarget(var);
            ImFunction replayFunction = getCompiletimeArrayStateInitFunction(replayLocation);
            UnsupportedArrayEntries unsupportedEntries = new UnsupportedArrayEntries();
            for (ProgramState.ArrayState state : globalState.getArrayStates(var)) {
                if (!state.isGeneric()) {
                    emitCompiletimeArrayEntries(replayFunction, var, state.getValue(),
                        new ArrayList<>(), ((ImArrayLikeType) var.getType()).getEntryType(), runtimeArrayWrites,
                        state.getModifiedIndexes(), unsupportedEntries);
                } else if (state.getTypeArguments().isEmpty()) {
                    throw new InterpreterException(var.getTrace(),
                        "Could not determine the generic specialization for compiletime array " + var.getName());
                } else {
                    emitCompiletimeGenericArrayState(replayFunction, var, state,
                        ((ImArrayLikeType) var.getType()).getEntryType(), runtimeArrayWrites, unsupportedEntries);
                }
            }
            if (!unsupportedEntries.isEmpty()) {
                WLogger.warning("Compiletime array '" + var.getName() + "' contains "
                    + unsupportedEntries.count + " unsupported compiletime entries"
                    + " (first at index " + unsupportedEntries.firstIndexes
                    + ", value type " + unsupportedEntries.firstValueType + "); "
                    + "runtime initialization remains authoritative (" + sourceDiagnostic(var) + ")");
            }
        }
    }

    private boolean isCompiletimeStateMigrationTarget(ImVar var) {
        // Compiletime state is deliberately opt-in per global. This keeps
        // compiletime-only scratch values and incidental writes out of runtime init.
        return var.getTrace() instanceof GlobalVarDef
            && ((GlobalVarDef) var.getTrace()).hasAnnotation("@compiletime");
    }

    private String sourceDiagnostic(ImVar var) {
        return var.getTrace().attrSource().printShort();
    }

    private StateReplayLocation findReplayTarget(ImVar var) {
        List<ImSet> initializers = imProg.getGlobalInits().getOrDefault(var, Collections.emptyList());
        if (initializers.isEmpty()) {
            return new StateReplayLocation(null, Collections.emptySet());
        }
        List<ImFunction> candidates = new ArrayList<>();
        candidates.add(translator.getGlobalInitFunc());
        candidates.addAll(translator.initFuncMap.values());
        for (ImFunction candidate : candidates) {
            Set<ImSet> matching = Collections.newSetFromMap(new IdentityHashMap<>());
            for (ImSet initializer : initializers) {
                for (ImStmt statement : candidate.getBody()) {
                    if (statement == initializer) {
                        matching.add(initializer);
                        break;
                    }
                }
            }
            if (!matching.isEmpty()) {
                if (candidate != translator.getGlobalInitFunc()) {
                    return new StateReplayLocation(candidate, matching);
                }
                return new StateReplayLocation(null, Collections.emptySet());
            }
        }
        return new StateReplayLocation(null, Collections.emptySet());
    }

    private void emitCompiletimeGenericScalarState(ImFunction replayFunction, ImVar var,
                                                    ProgramState.ScalarState state) {
        List<ImTypeVar> typeVars = new ArrayList<>();
        for (int i = 0; i < state.getTypeArguments().size(); i++) {
            typeVars.add(JassIm.ImTypeVar("T" + i));
        }
        ImFunction replay = JassIm.ImFunction(var.getTrace(),
            "initCompiletimeScalarState_" + genericScalarStateInitCounter++,
            JassIm.ImTypeVars(typeVars), JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImSet(var.getTrace(), JassIm.ImVarAccess(var),
                constantToExpr(var.getTrace(), state.getValue(), var.getType()))), Collections.emptyList());
        imProg.getFunctions().add(replay);
        scalarStateSplitTargets.add(replay);
        replayFunction.getBody().add(JassIm.ImFunctionCall(
            var.getTrace(), replay, JassIm.ImTypeArguments(state.getTypeArguments()), JassIm.ImExprs(), true, CallType.NORMAL));
    }

    private void emitCompiletimeGenericArrayState(ImFunction replayFunction, ImVar var,
                                                   ProgramState.ArrayState state, ImType entryType,
                                                  Set<RuntimeArrayWrite> runtimeArrayWrites,
                                                  UnsupportedArrayEntries unsupportedEntries) {
        List<ImTypeVar> typeVars = new ArrayList<>();
        for (int i = 0; i < state.getTypeArguments().size(); i++) {
            typeVars.add(JassIm.ImTypeVar("T" + i));
        }
        ImFunction replay = JassIm.ImFunction(var.getTrace(),
            "initCompiletimeArrayState_" + genericArrayStateInitCounter++,
            JassIm.ImTypeVars(typeVars), JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(),
            JassIm.ImStmts(), Collections.emptyList());
        imProg.getFunctions().add(replay);
        arrayStateSplitTargets.add(replay);
        emitCompiletimeArrayEntries(replay, var, state.getValue(), new ArrayList<>(), entryType,
            runtimeArrayWrites, state.getModifiedIndexes(), unsupportedEntries);
        if (!replay.getBody().isEmpty()) {
            replayFunction.getBody().add(JassIm.ImFunctionCall(
                var.getTrace(), replay, JassIm.ImTypeArguments(state.getTypeArguments()), JassIm.ImExprs(), true, CallType.NORMAL));
        }
    }

    private void emitCompiletimeArrayEntries(ImFunction target, ImVar var, ILconstArray values, List<Integer> indexes,
                                             ImType entryType, Set<RuntimeArrayWrite> runtimeArrayWrites,
                                             Set<List<Integer>> modifiedIndexes,
                                             UnsupportedArrayEntries unsupportedEntries) {
        for (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<ILconst> entry : values.entries()) {
            List<Integer> nextIndexes = new ArrayList<>(indexes);
            nextIndexes.add(entry.getIntKey());
            if (entry.getValue() instanceof ILconstArray && entryType instanceof ImArrayLikeType) {
                emitCompiletimeArrayEntries(target, var, (ILconstArray) entry.getValue(), nextIndexes,
                    ((ImArrayLikeType) entryType).getEntryType(), runtimeArrayWrites, modifiedIndexes,
                    unsupportedEntries);
            } else if (!modifiedIndexes.contains(nextIndexes)) {
                continue;
            } else if (isPersistableCompiletimeValue(entry.getValue())) {
                ImExprs indexExpressions = JassIm.ImExprs();
                for (Integer index : nextIndexes) {
                    indexExpressions.add(JassIm.ImIntVal(index));
                }
                target.getBody().add(JassIm.ImSet(var.getTrace(),
                    JassIm.ImVarArrayAccess(var.getTrace(), var, indexExpressions),
                    constantToExpr(var.getTrace(), entry.getValue(), entryType)));
            } else {
                String message = "Unsupported compiletime array entry at index " + entry.getIntKey()
                    + " (" + entry.getValue() + ")";
                List<ImExpr> indexExpressions = nextIndexes.stream()
                    .map(JassIm::ImIntVal)
                    .collect(Collectors.toList());
                RuntimeArrayWrite runtimeWrite = runtimeArrayWrite(var, indexExpressions);
                if (runtimeWrite != null && runtimeArrayWrites.stream().anyMatch(runtimeWrite::matches)) {
                    unsupportedEntries.add(nextIndexes, entry.getValue());
                } else {
                    throw new InterpreterException(var.getTrace(), message);
                }
            }
        }
    }

    private static final class UnsupportedArrayEntries {
        private int count;
        private List<Integer> firstIndexes;
        private String firstValueType;

        private void add(List<Integer> indexes, ILconst value) {
            count++;
            if (firstIndexes == null) {
                firstIndexes = new ArrayList<>(indexes);
                firstValueType = value.getClass().getSimpleName();
            }
        }

        private boolean isEmpty() {
            return count == 0;
        }
    }

    private Set<RuntimeArrayWrite> findRuntimeWrites(Set<ImVar> runtimeScalarWrites) {
        Set<RuntimeArrayWrite> runtimeArrayWrites = new HashSet<>();
        Set<ImFunction> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<ImFunction> pending = new ArrayDeque<>(translator.initFuncMap.values());
        pending.add(translator.getMainFunc());
        while (!pending.isEmpty()) {
            ImFunction function = pending.removeFirst();
            if (!visited.add(function)) {
                continue;
            }
            function.accept(new ImFunction.DefaultVisitor() {
                @Override
                public void visit(ImSet set) {
                    super.visit(set);
                    if (set.getLeft() instanceof ImVarAccess) {
                        runtimeScalarWrites.add(((ImVarAccess) set.getLeft()).getVar());
                    } else if (set.getLeft() instanceof ImVarArrayAccess) {
                        ImVarArrayAccess access = (ImVarArrayAccess) set.getLeft();
                        List<Integer> indexes = new ArrayList<>();
                        for (ImExpr index : access.getIndexes()) {
                            indexes.add(index instanceof ImIntVal ? ((ImIntVal) index).getValI() : null);
                        }
                        runtimeArrayWrites.add(new RuntimeArrayWrite(access.getVar(), indexes));
                    }
                }
            });
            pending.addAll(UsedFunctions.calculate(function));
        }
        return runtimeArrayWrites;
    }

    private static final class RuntimeArrayWrite {
        private final ImVar var;
        private final List<Integer> indexes;

        private RuntimeArrayWrite(ImVar var, List<Integer> indexes) {
            this.var = var;
            this.indexes = new ArrayList<>(indexes);
        }

        private boolean matches(RuntimeArrayWrite other) {
            if (var != other.var || indexes.size() != other.indexes.size()) {
                return false;
            }
            for (int i = 0; i < indexes.size(); i++) {
                Integer expected = indexes.get(i);
                Integer actual = other.indexes.get(i);
                if (expected != null && actual != null && !expected.equals(actual)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof RuntimeArrayWrite)) return false;
            RuntimeArrayWrite that = (RuntimeArrayWrite) other;
            return var == that.var && indexes.equals(that.indexes);
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(var) + indexes.hashCode();
        }
    }

    private static RuntimeArrayWrite runtimeArrayWrite(ImVar var, List<ImExpr> indexes) {
        List<Integer> constantIndexes = new ArrayList<>();
        for (ImExpr index : indexes) {
            constantIndexes.add(index instanceof ImIntVal ? ((ImIntVal) index).getValI() : null);
        }
        return new RuntimeArrayWrite(var, constantIndexes);
    }

    private boolean isPersistableCompiletimeValue(ILconst value) {
        if (value instanceof ILconstBool || value instanceof ILconstInt || value instanceof ILconstReal
            || value instanceof ILconstString || value instanceof ILconstNull || value instanceof ILconstObject) {
            return true;
        }
        if (value instanceof ILconstTuple) {
            for (ILconst element : ((ILconstTuple) value).values()) {
                if (!isPersistableCompiletimeValue(element)) return false;
            }
            return true;
        }
        if (value instanceof IlConstHandle) {
            return ((IlConstHandle) value).getObj() instanceof LinkedListMultimap;
        }
        return false;
    }

    /**
     * Stores a hashtable value in a compiletime expression
     * by generating the respective native calls
     */
    private ImExpr constantToExprHashtable(Element trace, ImVar htVar, IlConstHandle handle, LinkedListMultimap<HashtableProvider.KeyPair, Object> map) {
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
                            JassIm.ImStringVal(literalText(iv, trace))
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
        for (ImFunction func : imProg.getFunctions()) {
            if (func.isNative()) {
                if (func.getName().equals(funcName)) {
                    return Optional.of(func)
                        .orElseGet(() -> {
                            throw new CompileError(trace, "Could not find native 'InitHashtable'");
                        });
                }
            }
        }
        return Optional.<ImFunction>empty()
                .orElseThrow(() -> new CompileError(trace, "Could not find native 'InitHashtable'"));
    }


    private void executeCompiletimeFunction(ImFunction f) {
        if (functionFlag.matches(f)) {
            long t0 = System.nanoTime();
            try {
                if (!f.getBody().isEmpty()) {
                    interpreter.getGlobalState().setLastStatement(f.getBody().get(0));
                }
                WLogger.trace(() -> "running " + functionFlag + " function " + f.getName());
                interpreter.runVoidFunc(f, null);
                successTests.add(f);
            } catch (TestSuccessException e) {
                successTests.add(f);
            } catch (TestFailException e) {
                failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
            } catch (Throwable e) {
                failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
                throw e;
            } finally {
                compiletimeFunctionNanos.merge(f.getName(), System.nanoTime() - t0, Long::sum);
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

    /**
     * Releases any resources held by the interpreter or global state (such as open SQLite connections and statements)
     * created during compiletime function execution.
     */
    @Override
    public void close() {
        if (interpreter != null) {
            interpreter.close();
        } else if (globalState != null) {
            globalState.close();
        }
    }

}
