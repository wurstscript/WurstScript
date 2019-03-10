package de.peeeq.wurstscript.translation.jvm;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public class WurstStandardFunctions {


    private final JvmTranslation.JPackage wurstMain;

    public WurstStandardFunctions(JvmTranslation.JPackage wurstMain) {
        this.wurstMain = wurstMain;
    }

    void addStandardFunctions(ClassWriter classWriter) {
        createIndexMap(classWriter);
        castToIndex(classWriter);
        castFromIndex(classWriter);
        stringConcat(classWriter);
    }

    private void castFromIndex(ClassWriter classWriter) {
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_STATIC, "castFromIndex", "(I)Ljava/lang/Object;", "<T:Ljava/lang/Object;>(I)TT;", null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(17, label0);
        methodVisitor.visitFieldInsn(GETSTATIC, wurstMain.name, "indexMap", "Ljava/util/Map;");
        methodVisitor.visitVarInsn(ILOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        methodVisitor.visitInsn(ARETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("i", "I", null, label0, label1, 0);
        methodVisitor.visitMaxs(2, 1);
        methodVisitor.visitEnd();
    }

    private void castToIndex(ClassWriter classWriter) {
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_STATIC, "castToIndex", "(Ljava/lang/Object;)I", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(8, label0);
        methodVisitor.visitFieldInsn(GETSTATIC, wurstMain.name, "indexMap", "Ljava/util/Map;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Integer");
        methodVisitor.visitVarInsn(ASTORE, 1);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(9, label1);
        methodVisitor.visitVarInsn(ALOAD, 1);
        Label label2 = new Label();
        methodVisitor.visitJumpInsn(IFNONNULL, label2);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLineNumber(10, label3);
        methodVisitor.visitFieldInsn(GETSTATIC, wurstMain.name, "indexMap", "Ljava/util/Map;");
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "size", "()I", true);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        methodVisitor.visitVarInsn(ASTORE, 1);
        Label label4 = new Label();
        methodVisitor.visitLabel(label4);
        methodVisitor.visitLineNumber(11, label4);
        methodVisitor.visitFieldInsn(GETSTATIC, wurstMain.name, "indexMap", "Ljava/util/Map;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
        methodVisitor.visitInsn(POP);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(13, label2);
        methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Integer"}, 0, null);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        methodVisitor.visitInsn(IRETURN);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitLocalVariable("o", "Ljava/lang/Object;", null, label0, label5, 0);
        methodVisitor.visitLocalVariable("i", "Ljava/lang/Integer;", null, label1, label5, 1);
        methodVisitor.visitMaxs(3, 2);
        methodVisitor.visitEnd();
    }

    private void createIndexMap(ClassWriter classWriter) {
        FieldVisitor fieldVisitor = classWriter.visitField(ACC_STATIC, "indexMap", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/Object;Ljava/lang/Integer;>;", null);
        fieldVisitor.visitEnd();
    }

    public void stringConcat(ClassWriter classWriter) {
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_STATIC | ACC_PUBLIC, "stringAppend", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(27, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        Label label1 = new Label();
        methodVisitor.visitJumpInsn(IFNONNULL, label1);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(28, label2);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(29, label1);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        methodVisitor.visitVarInsn(ALOAD, 1);
        Label label3 = new Label();
        methodVisitor.visitJumpInsn(IFNONNULL, label3);
        Label label4 = new Label();
        methodVisitor.visitLabel(label4);
        methodVisitor.visitLineNumber(30, label4);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLineNumber(31, label3);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitInvokeDynamicInsn("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false), "\u0001\u0001");
        methodVisitor.visitInsn(ARETURN);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitLocalVariable("a", "Ljava/lang/String;", null, label0, label5, 0);
        methodVisitor.visitLocalVariable("b", "Ljava/lang/String;", null, label0, label5, 1);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }
}
