package de.peeeq.wurstscript.translation.jvm;

import de.peeeq.wurstscript.jassIm.ImFunction;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static de.peeeq.wurstscript.translation.jvm.JvmTranslation.getReturnInstruction;
import static de.peeeq.wurstscript.translation.jvm.JvmTranslation.pushDefaultValue;
import static org.objectweb.asm.Opcodes.*;


/**
 * Native functions directly implemented in the compiler such that the same unit
 * tests can be run for the Jass and JVM backends.
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
        codeGen.put("SquareRoot", methodVisitor -> {
            methodVisitor.visitVarInsn(FLOAD, 0);
            methodVisitor.visitInsn(F2D);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
            methodVisitor.visitInsn(D2F);
            methodVisitor.visitInsn(FRETURN);
        });
        codeGen.put("GetRandomReal", methodVisitor -> {
            {
                methodVisitor.visitTypeInsn(NEW, "java/util/Random");
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Random", "<init>", "()V", false);
                methodVisitor.visitVarInsn(ASTORE, 2);
                methodVisitor.visitVarInsn(FLOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 2);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Random", "nextFloat", "()F", false);
                methodVisitor.visitVarInsn(FLOAD, 1);
                methodVisitor.visitVarInsn(FLOAD, 0);
                methodVisitor.visitInsn(FSUB);
                methodVisitor.visitInsn(FMUL);
                methodVisitor.visitInsn(FADD);
                methodVisitor.visitInsn(FRETURN);
            }
        });

        codeGen.put("GetRandomInt", methodVisitor -> {
            methodVisitor.visitTypeInsn(NEW, "java/util/Random");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Random", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ASTORE, 2);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitInsn(ISUB);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Random", "nextInt", "(I)I", false);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitInsn(IRETURN);
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
//        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        methodVisitor.visitLdcInsn("testSuccess");
//        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitTypeInsn(NEW, "java/lang/RuntimeException");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitLdcInsn("testSuccess");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
        methodVisitor.visitInsn(ATHROW);
    }
}
