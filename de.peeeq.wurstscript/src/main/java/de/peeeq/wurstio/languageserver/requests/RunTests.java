package de.peeeq.wurstio.languageserver.requests;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.peeeq.wurstio.jassinterpreter.NativeFunctionsIO;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstLanguageServer;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;

/**
 * Created by peter on 05.05.16.
 */
public class RunTests extends UserRequest<Object> {

    private final WFile filename;
    private final int line;
    private final int column;
    private WurstLanguageServer server;


    public RunTests(WurstLanguageServer server, String filename, int line, int column) {
        this.server = server;
        this.filename = filename == null ? null : WFile.create(filename);
        this.line = line;
        this.column = column;
    }


    @Override
    public Object execute(ModelManager modelManager) {
        WLogger.info("Starting tests " + filename + ", " + line + ", " + column);
        println("Running unit tests..\n");

        CompilationUnit cu = filename == null ? null : modelManager.getCompilationUnit(filename);
        WLogger.info("test.cu = " + Utils.printElement(cu));
        FuncDef funcToTest = getFunctionToTest(cu);
        WLogger.info("test.funcToTest = " + Utils.printElement(funcToTest));


        ImProg imProg = translateProg(modelManager);
        if (imProg == null) {
            println("Could not run tests, because program did not compile.\n");
            return "Could not translate program";
        }

        WurstGui gui = new TestGui();
        ProgramState globalState = new ProgramState(gui, imProg, true);
        ILInterpreter interpreter = new ILInterpreter(null, gui, null, globalState);
        interpreter.addNativeProvider(new NativeFunctionsIO());

        redirectInterpreterOutput(globalState);


        List<ImFunction> successTests = Lists.newArrayList();
        Map<ImFunction, Pair<ImStmt, String>> failTests = Maps.newLinkedHashMap();
        for (ImFunction f : imProg.getFunctions()) {
            if (f.hasFlag(FunctionFlagEnum.IS_TEST)) {
                Element trace = f.attrTrace();

                if (cu != null && !Utils.elementContained(trace, cu)) {
                    continue;
                }
                if (funcToTest != null && trace != funcToTest) {
                    continue;
                }


                print("Running test <" + f.attrTrace().attrNearestPackage().tryGetNameDef().getName() + "." + f.getName() + "> .. ");
                try {
                    Callable run = () -> {
                        interpreter.runVoidFunc(f, null);
                        successTests.add(f);
                        println("success!");
                        return null;
                    };
                    RunnableFuture future = new FutureTask(run);
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.execute(future);
                    try {
                        future.get(10, TimeUnit.SECONDS); // Wait 10 seconds for test to complete
                    } catch (TimeoutException ex) {
                        future.cancel(true);
                        throw new TestTimeOutException();
                    }
                    service.shutdown();

                } catch (TestSuccessException e) {
                    println("success!");
                } catch (TestFailException e) {
                    failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
                    println("FAILED");
                } catch (TestTimeOutException e) {
                    failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
                    println("FAILED - TIMEOUT (This test did not complete in 10 seconds, it might contain an endless loop)");
                } catch (Exception e) {
                    failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
                    println("FAILED with exception:");
                    println("\t" + e.getMessage());
                }
            }
        }
        println("Tests succeeded: " + successTests.size() + "/" + (successTests.size()+failTests.size()));
        if(failTests.size() == 0) {
            println(">> All tests have passed successfully!");
        } else {
            println(">> The following tests failed:");
            for (Entry<ImFunction, Pair<ImStmt, String>> e : failTests.entrySet()) {
                println(Utils.printElementWithSource(e.getKey().attrTrace())
                        + "\n\t" + e.getValue().getB()
                        + "\n\tat " + Utils.printElementWithSource(e.getValue().getA().attrTrace()) + "\n");
            }
        }
        WLogger.info("finished tests");
        return "ok";
    }


    private void redirectInterpreterOutput(ProgramState globalState) {
        OutputStream os = new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                if (b > 0) {
                    println("" + (char) b);
                }
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                println(new String(b, off, len));
            }


        };
        globalState.setOutStream(new PrintStream(os));
    }

    private void println(String message) {
        System.err.println(message);
    }

    private void print(String message) {
        System.err.print(message);
    }

    private ImProg translateProg(ModelManager modelManager) {
        ImTranslator imTranslator = new ImTranslator(modelManager.getModel(), false);
        // will ignore udg_ variables which are not found
        imTranslator.setEclipseMode(true);
        return imTranslator.translateProg();
    }


    private FuncDef getFunctionToTest(CompilationUnit cu) {
        if (filename == null || cu == null || line < 0) {
            return null;
        }
        Element e = Utils.getAstElementAtPos(cu, line, column, false);
        while (e != null) {
            if (e instanceof FuncDef) {
                return (FuncDef) e;
            }
            e = e.getParent();
        }
        return null;
    }

    public class TestGui extends WurstGui {

        @Override
        public void sendProgress(String whatsRunningNow) {
            // ignore
        }

        @Override
        public void sendFinished() {
            // ignore
        }

        @Override
        public void showInfoMessage(String message) {
            println(message + "\n");
        }

    }

    private class TestTimeOutException extends Throwable {
    }
}
