package de.peeeq.wurstscript.translation.jvm;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.utils.Utils;
import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public class JvmTranslation {

    private ImProg prog;
    private ImmutableMultimap<ImClass, ImMethod> methodByClass;

    public JvmTranslation(ImProg prog) {
        this.prog = prog;
    }

    public void translate() {
        Multimap<PackageOrGlobal, ImFunction> functionsByPackage =
                prog.getFunctions().stream()
                .collect(Utils.groupBy(f -> f.attrTrace().attrNearestPackage()));
        Multimap<PackageOrGlobal, ImVar> varsByPackage =
                prog.getGlobals().stream()
                        .collect(Utils.groupBy(v -> v.attrTrace().attrNearestPackage()));
        Multimap<PackageOrGlobal, ImClass> classesByPackage =
                prog.getClasses().stream()
                        .collect(Utils.groupBy(f -> f.attrTrace().attrNearestPackage()));
        methodByClass =
                prog.getMethods().stream()
                        .collect(Utils.groupBy(f -> f.getMethodClass().getClassDef()));
        Set<PackageOrGlobal> packages = new LinkedHashSet<>(functionsByPackage.keys());
        packages.addAll(varsByPackage.keys());
        packages.addAll(classesByPackage.keys());

        // create one class per package
        for (PackageOrGlobal p : packages) {
            translatePackage(p, functionsByPackage.get(p), varsByPackage.get(p), classesByPackage.get(p));
        }


        // group functions and globals by package

        // translate functions and globals as statics in the package

        // group methods by class

        for (ImFunction func : prog.getFunctions()) {
            translateFunc(func);
        }


    }

    private void translatePackage(PackageOrGlobal p, Collection<ImFunction> imFunctions, Collection<ImVar> imVars, Collection<ImClass> imClasses) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, getName(p), null, "java/lang/Object", null);
    }

    private String getName(PackageOrGlobal p) {
        return p.match(new PackageOrGlobal.Matcher<String>() {
            @Override
            public String case_WPackage(WPackage wPackage) {
                return wPackage.getName();
            }

            @Override
            public String case_CompilationUnit(CompilationUnit compilationUnit) {
                return "WurstMain";
            }
        });
    }

    private void translateFunc(ImFunction func) {

    }


    public static void main(String[] args) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, "Blub", null, "java/lang/Object", null);

        classWriter.visitSource("Hello.java", null);

        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(1, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("Hello World!!!");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(4, label1);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();


        byte[] bytes = classWriter.toByteArray();
        Files.write(Paths.get("/home/peter/Desktop/Blub.class"), bytes);
    }

}
