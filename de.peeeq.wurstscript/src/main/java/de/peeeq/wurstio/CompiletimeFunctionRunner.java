package de.peeeq.wurstio;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstio.intermediateLang.interpreter.CompiletimeNatives;
import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.NativeFunctionsIO;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILStackFrame;
import de.peeeq.wurstscript.intermediatelang.interpreter.LocalState;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class CompiletimeFunctionRunner {

    private final ImProg imProg;
    private ILInterpreter interpreter;
    private WurstGui gui;
    private FunctionFlag functionFlag;
    private List<ImFunction> successTests = Lists.newArrayList();
    private Map<ImFunction, Pair<ImStmt, String>> failTests = Maps.newLinkedHashMap();
    private boolean injectObjects;


    public CompiletimeFunctionRunner(ImProg imProg, File mapFile, MpqEditor mpqEditor, WurstGui gui, FunctionFlag flag) {
        Preconditions.checkNotNull(imProg);
        this.imProg = imProg;
        ProgramStateIO globalState = new ProgramStateIO(mapFile, mpqEditor, gui, imProg, true);
        this.interpreter = new ILInterpreter(imProg, gui, mapFile, globalState);

        interpreter.addNativeProvider(new NativeFunctionsIO());
        interpreter.addNativeProvider(new CompiletimeNatives(globalState));
        this.gui = gui;
        this.functionFlag = flag;
    }


    public void run() {
//		interpreter.executeFunction("main");
//		interpreter.executeFunction("initGlobals");
        try {
            executeCompiletimeExpressions();
            executeCompiletimeFunctions();

            if (functionFlag == FunctionFlagEnum.IS_COMPILETIME) {
                interpreter.writebackGlobalState(isInjectObjects());
            }
        } catch (Throwable e) {
            WLogger.severe(e);
            ImStmt s = interpreter.getLastStatement();
            Element origin = s == null ? null : s.attrTrace();
            if (origin != null) {
                gui.sendError(new CompileError(origin.attrSource(), e.getMessage()));

                // stackframe messages ...
                for (ILStackFrame sf : Utils.iterateReverse(interpreter.getStackFrames())) {
                    gui.sendError(sf.makeCompileError());
                }

            } else {
                throw new Error("could not get origin", e);
            }
        }

    }

    private void executeCompiletimeExpressions() {
        List<ImCompiletimeExpr> compiletimeExprs = new ArrayList<>();
        imProg.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
            @Override
            public void visit(ImCompiletimeExpr e) {
                compiletimeExprs.add(e);
            }
        });

        for (ImCompiletimeExpr cte : compiletimeExprs) {
            LocalState localState = new LocalState();
            ILconst value = cte.evaluate(interpreter.getGlobalState(), localState);
            ImExpr newExpr = constantToExpr(cte, value);
            cte.replaceBy(newExpr);
        }

    }

    private ImExpr constantToExpr(ImCompiletimeExpr cte, ILconst value) {
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
        } else {
            throw new InterpreterException(cte.attrTrace(), "Compiletime expression returned unsupported value " + value);
        }
    }

    private void executeCompiletimeFunctions() {
        for (ImFunction f : imProg.getFunctions()) {
            if (f.hasFlag(functionFlag)) {
                try {
                    WLogger.info("running " + functionFlag + " function " + f.getName());
                    interpreter.runVoidFunc(f, null);
                    successTests.add(f);
                } catch (TestSuccessException e) {
                    successTests.add(f);
                } catch (TestFailException e) {
                    failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
                }
            }
        }
    }

    public List<ImFunction> getSuccessTests() {
        return successTests;
    }


    public Map<ImFunction, Pair<ImStmt, String>> getFailTests() {
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
