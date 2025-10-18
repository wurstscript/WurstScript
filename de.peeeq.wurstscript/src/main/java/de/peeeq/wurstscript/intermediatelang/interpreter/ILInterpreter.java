package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.VarargArray;
import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.HasModifier;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.validation.GlobalCaches;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.translation.imoptimizer.UselessFunctionCallsRemover.isFunctionPure;
import static de.peeeq.wurstscript.validation.GlobalCaches.LOCAL_STATE_CACHE;

public class ILInterpreter implements AbstractInterpreter {
    private ImProg prog;
    private final ProgramState globalState;
    private final TimerMockHandler timerMockHandler = new TimerMockHandler();

    public ILInterpreter(ImProg prog, WurstGui gui, Optional<File> mapFile, ProgramState globalState) {
        this.prog = prog;
        this.globalState = globalState;
        globalState.addNativeProvider(new BuiltinFuncs(globalState));
//        globalState.addNativeProvider(new NativeFunctions());
    }

    public ILInterpreter(ImProg prog, WurstGui gui, Optional<File> mapFile, boolean isCompiletime) {
        this(prog, gui, mapFile, new ProgramState(gui, prog, isCompiletime));
    }

    public static LocalState runFunc(ProgramState globalState, ImFunction f, @Nullable Element caller,
                                     ILconst... args) {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterpreterException(globalState, "Execution interrupted");
        }
        try {
            if (f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
                // for vararg functions, rewrite args and put last argument
                ILconst[] newArgs = new ILconst[f.getParameters().size()];
                if (newArgs.length - 1 >= 0) System.arraycopy(args, 0, newArgs, 0, newArgs.length - 1);

                ILconst[] varargArray = new ILconst[1 + args.length - newArgs.length];
                for (int i = newArgs.length - 1, j = 0; i < args.length; i++, j++) {
                    varargArray[j] = args[i];
                }
                newArgs[newArgs.length - 1] = new VarargArray(varargArray);
                args = newArgs;
            }

            if (f.getParameters().size() != args.length) {
                throw new Error("wrong number of parameters when calling func " + f.getName() + "(" +
                    Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", ")) + ")");
            }

            for (int i = 0; i < f.getParameters().size(); i++) {
                // TODO could do typecheck here
                args[i] = adjustTypeOfConstant(args[i], f.getParameters().get(i).getType());
            }

            if (isCompiletimeNative(f) || f.isNative()) {
                return runBuiltinFunction(globalState, f, args);
            }

            LocalState localState = new LocalState();

            // Set up local variables
            int i = 0;
            for (ImVar p : f.getParameters()) {
                localState.setVal(p, args[i]);
                i++;
            }

            if (f.getBody().isEmpty()) {
                return localState.setReturnVal(ILconstNull.instance());
            } else {
                globalState.setLastStatement(f.getBody().get(0));
            }


            if (!(caller instanceof ImFunctionCall)) {
                if (caller instanceof ImMethodCall) {
                    // Instance method call: bind class T-vars from the *receiver*'s concrete type args
                    final Map<ImTypeVar, ImType> subst = new HashMap<>();

                    // First parameter is the implicit 'this'
                    final ImVar thisParam = f.getParameters().get(0);
                    final ImType thisParamType = thisParam.getType();
                    if (!(thisParamType instanceof ImClassType)) {
                        // Defensive: still push with no substitutions
                        globalState.pushStackframeWithTypes(f, null, args, f.attrTrace().attrErrorPos(), Collections.emptyMap());
                    } else {
                        final ImClassType sigThisType = (ImClassType) thisParamType; // may contain ImTypeVarRefs
                        final ILconstObject thisArg = (ILconstObject) args[0];
                        final ImClassType recvType = thisArg.getType();              // concrete type Box<tuple<int,int>> etc.

                        // Class type variables (on the class definition)
                        final ImClass cls = sigThisType.getClassDef();
                        final ImTypeVars tvars = cls.getTypeVariables();             // e.g., [T74]

                        // Concrete type arguments from receiver (same order)
                        final ImTypeArguments concreteArgs = recvType.getTypeArguments();

                        final int n = Math.min(tvars.size(), concreteArgs.size());
                        for (int i2 = 0; i2 < n; i2++) {
                            subst.put(tvars.get(i2), concreteArgs.get(i2).getType());
                        }

                        globalState.pushStackframeWithTypes(f, thisArg, args, f.attrTrace().attrErrorPos(), subst);
                    }
                } else {
                    // Static function or unknown caller kind
                    globalState.pushStackframeWithTypes(f, null, args, f.attrTrace().attrErrorPos(), Collections.emptyMap());
                }
            }



            try {
                f.getBody().runStatements(globalState, localState);
                globalState.popStackframe();
            } catch (ReturnException e) {
                globalState.popStackframe();
                ILconst retVal = e.getVal();
                retVal = adjustTypeOfConstant(retVal, f.getReturnType());
                return localState.setReturnVal(retVal);
            }

            if (f.getReturnType() instanceof ImVoid) {
                return localState;
            }
            throw new InterpreterException("function " + f.getName() + " did not return any value...");

        } catch (InterpreterException e) {
            String msg = buildStacktrace(globalState, e);
            e.setStacktrace(msg);
            e.setTrace(getTrace(globalState, f));
            throw e;
        } catch (TestSuccessException | TestFailException | DebugPrintError e) {
            throw e;
        } catch (Throwable e) {
            String msg = buildStacktrace(globalState, e);
            de.peeeq.wurstscript.ast.Element trace = getTrace(globalState, f);
            throw new InterpreterException(trace, "You encountered a bug in the interpreter: " + e, e).setStacktrace(msg);
        }
    }

    public static de.peeeq.wurstscript.ast.Element getTrace(ProgramState globalState, ImFunction f) {
        Element lastStatement = globalState.getLastStatement();
        return lastStatement == null ? f.attrTrace() : lastStatement.attrTrace();
    }

    public static String buildStacktrace(ProgramState globalState, Throwable e) {
        StringBuilder err = new StringBuilder();
        try {
            WPos src = globalState.getLastStatement().attrTrace().attrSource();
            err.append("at : ").append(new File(src.getFile()).getName()).append(", line ").append(src.getLine()).append("\n");
        } catch (Exception _e) {
            // ignore
        }
        globalState.getStackFrames().appendTo(err);
        return err.toString();
    }

    @SuppressWarnings("null")
    private static ILconst adjustTypeOfConstant(@Nullable ILconst retVal, ImType expectedType) {
        if (retVal instanceof ILconstInt && isTypeReal(expectedType)) {
            ILconstInt retValI = (ILconstInt) retVal;
            retVal = new ILconstReal(retValI.getVal());
        }
        return retVal;
    }

    private static boolean isTypeReal(ImType t) {
        if (t instanceof ImSimpleType) {
            ImSimpleType st = (ImSimpleType) t;
            return st.getTypename().equals("real");
        }
        return false;
    }


    // Cap per-function cache size to avoid unbounded growth
    private static final int MAX_CACHE_PER_FUNC = 2048;

    private static final LocalState EMPTY_LOCAL_STATE = new LocalState();

    private static LocalState runBuiltinFunction(ProgramState globalState, ImFunction f, ILconst... args) {
        // Delegate to the array overload to avoid double-allocations.
        return runBuiltinFunction(globalState, f, args, /*isVarargs*/ true);
    }

    private static LocalState runBuiltinFunction(ProgramState globalState, ImFunction f, ILconst[] args, boolean isVarargs) {
        // Cache purity + name once
        final String fname = f.getName();
        final boolean pure = isFunctionPure(fname);

        GlobalCaches.ArgumentKey key = null;
        if (pure) {
            key = GlobalCaches.ArgumentKey.forLookup(args);

            final Object2ObjectOpenHashMap<GlobalCaches.ArgumentKey, LocalState> perFn =
                LOCAL_STATE_CACHE.get(f);
            if (perFn != null) {
                final LocalState cached = perFn.get(key);
                if (cached != null) {
                    return cached;
                }
            }
        }

        // Build error text lazily (only if we actually get exceptions)
        StringBuilder errors = null;

        for (NativesProvider natives : globalState.getNativeProviders()) {
            try {
                // Invoke native; TODO: cache method handles per name elsewhere.
                final LocalState localState = new LocalState(natives.invoke(fname, args));

                if (pure) {
                    // insert into per-function cache with bounded size
                    Object2ObjectOpenHashMap<GlobalCaches.ArgumentKey, LocalState> perFn =
                        LOCAL_STATE_CACHE.get(f);
                    if (perFn == null) {
                        perFn = new Object2ObjectOpenHashMap<>(16);
                        LOCAL_STATE_CACHE.put(f, perFn);
                    }
                    perFn.put(key, localState);
                    if (perFn.size() > MAX_CACHE_PER_FUNC) {
                        // evict eldest (insertion order) to bound memory
                        // Object2ObjectOpenHashMap maintains insertion order
                        final GlobalCaches.ArgumentKey eldest = perFn.keySet().iterator().next();
                        perFn.remove(eldest);
                    }
                }

                return localState;

            } catch (NoSuchNativeException e) {
                if (errors == null) errors = new StringBuilder(128);
                errors.append('\n').append(e.getMessage());
                // keep trying next provider
            }
        }

        // If we reach here, none of the providers handled it
        if (errors == null) errors = new StringBuilder(64);
        errors.insert(0, "function ").append(fname).append(" cannot be used from the Wurst interpreter.\n");
        globalState.compilationError(errors.toString());

        // Return a lightweight state
        if (f.getReturnType() instanceof ImVoid) {
            return EMPTY_LOCAL_STATE;
        }
        final ILconst returnValue = f.getReturnType().defaultValue();
        return new LocalState(returnValue);
    }

    /** Zero-allocation combined hash for ILconst[] (order-sensitive). */
    private static int fastHashArgs(ILconst[] args) {
        return Arrays.hashCode(args);
    }


    private static boolean isCompiletimeNative(ImFunction f) {
        if (f.getTrace() instanceof HasModifier) {
            HasModifier f2 = (HasModifier) f.getTrace();
            for (Modifier m : f2.getModifiers()) {
                if (m instanceof Annotation) {
                    Annotation annotation = (Annotation) m;
                    if (annotation.getAnnotationType().equals("@compiletimenative")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public LocalState executeFunction(String funcName, @Nullable Element trace) {
        globalState.resetStackframes();
        for (ImFunction f : prog.getFunctions()) {
            if (f.getName().equals(funcName)) {
                return runFunc(globalState, f, trace);
            }
        }

        throw new Error("no function with name " + funcName + "was found.");
    }

    public void runVoidFunc(ImFunction f, @Nullable Element trace) {
        globalState.resetStackframes();
        ILconst[] args = {};
        if (!f.getParameters().isEmpty()) {
            // this should only happen because of added stacktrace parameter
            args = new ILconstString[]{new ILconstString("initial call")};
        }
        runFunc(globalState, f, trace, args);
    }

    public Element getLastStatement() {
        return globalState.getLastStatement();
    }

    public void writebackGlobalState(boolean injectObjects) {
        globalState.writeBack(injectObjects);

    }

    public ProgramState getGlobalState() {
        return globalState;
    }

    public void addNativeProvider(NativesProvider np) {
        globalState.addNativeProvider(np);
    }

    public void setProgram(ImProg imProg) {
        this.prog = imProg;
        this.getGlobalState().setProg(imProg);
        globalState.resetStackframes();
    }

    public ProgramState.StackTrace getStackFrames() {
        return globalState.getStackFrames();

    }

    @Override
    public void runFuncRef(ILconstFuncRef obj, @Nullable Element trace) {
        runVoidFunc(obj.getFunc(), trace);
    }

    @Override
    public TimerMockHandler getTimerMockHandler() {
        return timerMockHandler;
    }

    @Override
    public void completeTimers() {
        timerMockHandler.completeTimers();
    }

    @Override
    public ImProg getImProg() {
        return prog;
    }

    @Override
    public int getInstanceCount(int val) {
        long count = 0L;
        for (ILconstObject o : globalState.getAllObjects()) {
            if (o.getType().getClassDef().attrTypeId() == val) {
                if (!o.isDestroyed()) {
                    count++;
                }
            }
        }
        return (int) count;
    }

    @Override
    public int getMaxInstanceCount(int val) {
        long count = 0L;
        for (ILconstObject o : globalState.getAllObjects()) {
            if (o.getType().getClassDef().attrTypeId() == val) {
                count++;
            }
        }
        return (int) count;
    }
}
