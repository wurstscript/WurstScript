package de.peeeq.wurstscript.translation.jvm;

import de.peeeq.wurstscript.jassIm.ImFunction;
import org.objectweb.asm.MethodVisitor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import static de.peeeq.wurstscript.translation.jvm.JvmTranslation.getReturnInstruction;
import static de.peeeq.wurstscript.translation.jvm.JvmTranslation.pushDefaultValue;
import static org.objectweb.asm.Opcodes.*;


/**
 *
 */
public class NativeFuncsJvm {

    private static Map<String, Consumer<MethodVisitor>> codeGen = new HashMap<>();

    static {
        codeGen.put("testSuccess", NativeFuncsJvm::testSuccess);
        codeGen.put("I2S", NativeFuncsJvm::I2S);
        codeGen.put("println", NativeFuncsJvm::println);
        codeGen.put("Sin", methodVisitor -> {
            methodVisitor.visitVarInsn(FLOAD, 0);
            methodVisitor.visitInsn(F2D);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
            methodVisitor.visitInsn(D2F);
            methodVisitor.visitInsn(FRETURN);
        });
        codeGen.put("Cos", methodVisitor -> {
            methodVisitor.visitVarInsn(FLOAD, 0);
            methodVisitor.visitInsn(F2D);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
            methodVisitor.visitInsn(D2F);
            methodVisitor.visitInsn(FRETURN);
        });

    }


    public static void generateCode(MethodVisitor methodVisitor, ImFunction func) {
        Consumer<MethodVisitor> code = codeGen.get(func.getName());
        if (code != null) {
            code.accept(methodVisitor);
        } else {
            genericStub(methodVisitor, func);
        }
    }

    private static void println(MethodVisitor methodVisitor) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
        methodVisitor.visitInsn(RETURN);
    }

    private static void I2S(MethodVisitor methodVisitor) {
        methodVisitor.visitVarInsn(ILOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false);
        methodVisitor.visitInsn(ARETURN);
    }

    private static void genericStub(MethodVisitor methodVisitor, ImFunction func) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("Called native " + func.getName());
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);


        pushDefaultValue(func.getReturnType(), methodVisitor);
        methodVisitor.visitInsn(getReturnInstruction(func.getReturnType()));
    }

    private static void testSuccess(MethodVisitor methodVisitor) {
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("testSuccess");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        methodVisitor.visitInsn(RETURN);
    }
}
