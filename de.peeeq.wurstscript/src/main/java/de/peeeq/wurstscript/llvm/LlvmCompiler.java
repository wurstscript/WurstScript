package de.peeeq.wurstscript.llvm;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstCompiler;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.llvm.ast.Prog;
import de.peeeq.wurstscript.llvm.printer.PrettyPrinter;
import de.peeeq.wurstscript.llvm.tollvm.LlvmTranslator;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class LlvmCompiler {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            args = new String[] {"testscripts/test.wurst"};
        }
        WurstGui gui = new WurstGuiCliImpl();
        RunArgs runArgs = new RunArgs(args);
        WurstCompilerJassImpl com = new WurstCompilerJassImpl(gui, null, runArgs);
        com.loadFiles(runArgs.getFiles().toArray(new String[0]));
        @Nullable WurstModel model = com.parseFiles();
        com.checkProg(model);
        if (gui.getErrorCount() > 0) {
            for (CompileError compileError : gui.getErrorList()) {
                System.out.println(compileError);
            }
            System.out.println("Errors in input");
            return;
        }

        @Nullable ImProg im = com.translateProgToIm(model);

        Files.write(Paths.get("test-output", "test.im"), im.toString().getBytes());


        LlvmTranslator llvmTr = new LlvmTranslator(im);
        Prog prog = llvmTr.translateProg();

        String llvmStr = PrettyPrinter.elementToString(prog);

        Files.write(Paths.get("test-output", "test.ll"), llvmStr.getBytes());

    }


}
