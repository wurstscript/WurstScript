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
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class ILInterpreter implements AbstractInterpreter {
    private ImProg prog;
    private final ProgramState globalState;
    private final TimerMockHandler timerMockHandler = new TimerMockHandler();

    public ILInterpreter(ImProg prog, WurstGui gui, Optional<File> mapFile, ProgramState globalState) {
        this.prog = prog;
        this.globalState = globalState;
        globalState.addNativeProvider(new BuiltinFuncs(globalState));
        globalState.addNativeProvider(new NativeFunctions());
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
                for (int i = 0; i < newArgs.length - 1; i++) {
                    newArgs[i] = args[i];
                }

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

            if (isCompiletimeNative(f)) {
                return runBuiltinFunction(globalState, f, args);
            }

            if (f.isNative()) {
                return runBuiltinFunction(globalState, f, args);
            }

            LocalState localState = new LocalState();
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

            globalState.pushStackframe(f, args, (caller == null ? f : caller).attrTrace().attrErrorPos());

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

    private static LocalState runBuiltinFunction(ProgramState globalState, ImFunction f, ILconst... args) {
        StringBuilder errors = new StringBuilder();
        for (NativesProvider natives : globalState.getNativeProviders()) {
            try {
                return new LocalState(natives.invoke(f.getName(), args));
            } catch (NoSuchNativeException e) {
                errors.append("\n").append(e.getMessage());
                // ignore
            }
        }
        globalState.compilationError("function " + f.getName() + " cannot be used from the Wurst interpreter.\n" + errors);
        if (f.getReturnType() instanceof ImVoid) {
            return new LocalState();
        }
        ILconst returnValue = ImHelper.defaultValueForComplexType(f.getReturnType()).evaluate(globalState, new LocalState());
        return new LocalState(returnValue);
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
        return (int) globalState.getAllObjects()
            .stream()
            .filter(o -> o.getType().getClassDef().attrTypeId() == val)
            .filter(o -> !o.isDestroyed())
            .count();
    }

    @Override
    public int getMaxInstanceCount(int val) {
        return (int) globalState.getAllObjects()
            .stream()
            .filter(o -> o.getType().getClassDef().attrTypeId() == val)
            .count();
    }
}
