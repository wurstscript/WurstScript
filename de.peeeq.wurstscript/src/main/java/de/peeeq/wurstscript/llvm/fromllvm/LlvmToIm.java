package de.peeeq.wurstscript.llvm.fromllvm;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.Prog;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transform Llvm programs to our own intermediate language
 *
 * Could first make Im basic block based, which would also simplify translation
 * of something like 'continue'.
 *
 * Then translation to Jass or an Im-transform has to handle the complicated stuff.
 *
 * Maybe useful resources:
 * https://www2.cs.arizona.edu/~collberg/Teaching/553/2011/Resources/cifuentes96structuring.pdf
 *
 *
 */
public class LlvmToIm {


    public ImProg transformProg(Prog prog) {

        ImVars globals = JassIm.ImVars();
        ImFunctions functions = JassIm.ImFunctions();
        ImClasses classes = JassIm.ImClasses();
        Map<ImVar, ImExpr> globalInits = new LinkedHashMap<>();
        return JassIm.ImProg(globals, functions, classes, globalInits);
    }

}
