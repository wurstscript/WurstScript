package de.peeeq.wurstscript.translation.jvm;

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

import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public class JvmTranslation {

    private ImProg prog;
    private final Path outputFolder;
    private ImmutableMultimap<ImClass, ImMethod> methodByClass;
    private Map<ImVar, Integer> localVars = new HashMap<>();

    public JvmTranslation(ImProg prog, Path outputFolder) {
        this.prog = prog;
        this.outputFolder = outputFolder;
    }

    static class JPackage {
        final String name;

        JPackage(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JPackage jPackage = (JPackage) o;
            return Objects.equals(name, jPackage.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public void translate() {
        try {
            Multimap<JPackage, ImFunction> functionsByPackage =
                    prog.getFunctions().stream()
                            .collect(Utils.groupBy(this::getPackage));
            Multimap<JPackage, ImVar> varsByPackage =
                    prog.getGlobals().stream()
                            .collect(Utils.groupBy(this::getPackage));
            Multimap<JPackage, ImClass> classesByPackage =
                    prog.getClasses().stream()
                            .collect(Utils.groupBy(this::getPackage));
            methodByClass =
                    prog.getMethods().stream()
                            .collect(Utils.groupBy(f -> f.getMethodClass().getClassDef()));
            Set<JPackage> packages = new LinkedHashSet<>(functionsByPackage.keys());
            packages.addAll(varsByPackage.keys());
            packages.addAll(classesByPackage.keys());

            // create one class per package
            for (JPackage p : packages) {
                translatePackage(p, functionsByPackage.get(p), varsByPackage.get(p), classesByPackage.get(p));
            }


            // group functions and globals by package

            // translate functions and globals as statics in the package

            // group methods by class

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private JPackage getPackage(de.peeeq.wurstscript.jassIm.Element element) {
        return getPackage(element.attrTrace());
    }

    private JPackage getPackage(Element element) {
        while (element != null) {
            if (element instanceof WPackage) {
                return new JPackage(((WPackage) element).getName());
            }
            element = element.getParent();
        }
        return new JPackage("WurstMain");
    }

    private void translatePackage(JPackage p, Collection<ImFunction> imFunctions, Collection<ImVar> imVars, Collection<ImClass> imClasses) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES); // ClassWriter.COMPUTE_MAXS
        classWriter.visit(V1_6, ACC_PUBLIC | ACC_SUPER, p.name, null, "java/lang/Object", null);
        for (ImVar v : imVars) {
            translateStaticVar(classWriter, v);
        }
        for (ImFunction f : imFunctions) {
            translateFunc(classWriter, f);
        }

        classWriter.visitEnd();
        byte[] bytes = classWriter.toByteArray();
        Files.write(outputFolder.resolve(p.name + ".class"), bytes);
    }

    private void translateStaticVar(ClassWriter classWriter, ImVar v) {
        FieldVisitor fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_STATIC, v.getName(), translateType(v.getType()), null, null);
        fieldVisitor.visitEnd();
    }

    private String translateType(ImType type) {
        return type.match(new ImType.Matcher<String>() {
            @Override
            public String case_ImTupleType(ImTupleType imTupleType) {
                throw new RuntimeException("TODO " + imTupleType);
            }

            @Override
            public String case_ImVoid(ImVoid imVoid) {
                return "V";
            }

            @Override
            public String case_ImClassType(ImClassType c) {
                return c.getClassDef().getName();
            }

            @Override
            public String case_ImArrayTypeMulti(ImArrayTypeMulti a) {
                String t = translateType(a.getEntryType());
                for (Integer i : a.getArraySize()) {
                    t = "[" + t;
                }
                return t;
            }

            @Override
            public String case_ImSimpleType(ImSimpleType s) {
                switch (s.getTypename()) {
                    case "integer":
                        return "I";
                    default:
                        return "L"+s.getTypename() + ";";
                }
            }

            @Override
            public String case_ImArrayType(ImArrayType a) {
                return "[" + translateType(a.getEntryType());
            }

            @Override
            public String case_ImTypeVarRef(ImTypeVarRef imTypeVarRef) {
                throw new RuntimeException("TODO " + imTypeVarRef);
            }
        });
    }

    private void translateFunc(ClassWriter classWriter, ImFunction func) {
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, func.getName(), getSignatureDescriptor(func), null, null);
        methodVisitor.visitCode();
        Label start = new Label();
        methodVisitor.visitLabel(start);
        methodVisitor.visitLineNumber(line(func), start);

        localVars.clear();
        for (int i = 0; i < func.getParameters().size(); i++) {
            localVars.put(func.getParameters().get(i), i);
        }
        if (func.isNative()) {
            NativeFuncsJvm.generateCode(methodVisitor, func);
        } else {
            for (int i = 0; i < func.getLocals().size(); i++) {
                localVars.put(func.getLocals().get(i), i + func.getParameters().size());
            }

            translateStatements(methodVisitor, func.getBody());

            if (func.getReturnType() instanceof ImVoid) {
                methodVisitor.visitInsn(RETURN);
            }
        }

        Label end = new Label();
        localVars.forEach((v, i) -> {
            methodVisitor.visitLocalVariable(v.getName(), translateType(v.getType()), null, start, end, i);
        });
        // TODO do correctly ...
        methodVisitor.visitMaxs(100, 200);
        methodVisitor.visitEnd();

    }

    private void translateStatements(MethodVisitor methodVisitor, ImStmts body) {
        for (ImStmt s : body) {
            if (s instanceof ImNull) {
                continue;
            }
            translateStatement(methodVisitor, s);
        }
    }

    private void translateStatement(MethodVisitor methodVisitor, ImStmt s) {
        s.match(new ImStmt.MatcherVoid() {
            @Override
            public void case_ImTypeVarDispatch(ImTypeVarDispatch imTypeVarDispatch) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImDealloc(ImDealloc imDealloc) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImBoolVal(ImBoolVal imBoolVal) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImTypeIdOfClass(ImTypeIdOfClass imTypeIdOfClass) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImVarAccess(ImVarAccess imVarAccess) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImStringVal(ImStringVal imStringVal) {
                methodVisitor.visitLdcInsn(imStringVal.getValS());
            }

            @Override
            public void case_ImMethodCall(ImMethodCall imMethodCall) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImRealVal(ImRealVal imRealVal) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImFunctionCall(ImFunctionCall fc) {
                for (ImExpr arg : fc.getArguments()) {
                    translateStatement(methodVisitor, arg);
                }
                methodVisitor.visitMethodInsn(INVOKESTATIC, getPackage(fc.getFunc()).name, fc.getFunc().getName(), getSignatureDescriptor(fc.getFunc()), false);
            }

            @Override
            public void case_ImReturn(ImReturn imReturn) {

            }

            @Override
            public void case_ImTupleSelection(ImTupleSelection imTupleSelection) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImOperatorCall(ImOperatorCall oc) {
                for (ImExpr a : oc.getArguments()) {
                    translateStatement(methodVisitor, a);
                }
                switch (oc.getOp()) {
                    case OR:
                        break;
                    case AND:
                        break;
                    case EQ:
                        Label notEqual = new Label();
                        Label afterCompare = new Label();
                        methodVisitor.visitJumpInsn(IF_ICMPNE, notEqual);
                        methodVisitor.visitInsn(ICONST_1);
                        methodVisitor.visitJumpInsn(GOTO, afterCompare);
                        methodVisitor.visitLabel(notEqual);
                        methodVisitor.visitInsn(ICONST_0);
                        methodVisitor.visitLabel(afterCompare);
                        return;
                    case NOTEQ:
                        break;
                    case LESS_EQ:
                        break;
                    case LESS:
                        break;
                    case GREATER_EQ:
                        break;
                    case GREATER:
                        break;
                    case PLUS:
                        methodVisitor.visitInsn(IADD);
                        return;
                    case MINUS:
                        break;
                    case MULT:
                        break;
                    case DIV_REAL:
                        break;
                    case DIV_INT:
                        break;
                    case MOD_REAL:
                        break;
                    case MOD_INT:
                        break;
                    case NOT:
                        break;
                    case UNARY_MINUS:
                        break;
                }
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImVarArrayAccess(ImVarArrayAccess imVarArrayAccess) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImAlloc(ImAlloc imAlloc) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImIntVal(ImIntVal imIntVal) {
                methodVisitor.visitIntInsn(SIPUSH, imIntVal.getValI());
            }

            @Override
            public void case_ImExitwhen(ImExitwhen imExitwhen) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImVarargLoop(ImVarargLoop imVarargLoop) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImNull(ImNull imNull) {
                methodVisitor.visitInsn(ACONST_NULL);
            }

            @Override
            public void case_ImLoop(ImLoop imLoop) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImMemberAccess(ImMemberAccess imMemberAccess) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImGetStackTrace(ImGetStackTrace imGetStackTrace) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImTupleExpr(ImTupleExpr imTupleExpr) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImTypeIdOfObj(ImTypeIdOfObj imTypeIdOfObj) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImSet(ImSet imSet) {
                ImLExpr left = imSet.getLeft();
                if (left instanceof ImVarAccess) {
                    ImVarAccess leftVar = (ImVarAccess) left;
                    if (localVars.containsKey(leftVar.getVar())) {
                        int varIndex = localVars.get(leftVar.getVar());
                        translateStatement(methodVisitor, imSet.getRight());
                        methodVisitor.visitVarInsn(getStoreInstruction(leftVar.getVar().getType()), varIndex);
                        return;
                    }

                }
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImStatementExpr(ImStatementExpr imStatementExpr) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImCompiletimeExpr(ImCompiletimeExpr imCompiletimeExpr) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImIf(ImIf imIf) {
                translateStatement(methodVisitor, imIf.getCondition());
                Label elseLabel = new Label();
                Label endifLabel = new Label();
                methodVisitor.visitJumpInsn(IFEQ, elseLabel);
                translateStatements(methodVisitor, imIf.getThenBlock());
                methodVisitor.visitJumpInsn(GOTO, endifLabel);
                methodVisitor.visitLabel(elseLabel);
                translateStatements(methodVisitor, imIf.getElseBlock());
                methodVisitor.visitLabel(endifLabel);
            }

            @Override
            public void case_ImCast(ImCast imCast) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImFuncRef(ImFuncRef imFuncRef) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImInstanceof(ImInstanceof imInstanceof) {
                throw new RuntimeException("TODO " + s);
            }
        });
    }

    private int getStoreInstruction(ImType type) {
        return type.match(new ImType.Matcher<Integer>() {
            @Override
            public Integer case_ImTupleType(ImTupleType imTupleType) {
                throw new RuntimeException("TODO " + type);
            }

            @Override
            public Integer case_ImVoid(ImVoid imVoid) {
                throw new RuntimeException("TODO " + type);
            }

            @Override
            public Integer case_ImClassType(ImClassType imClassType) {
                throw new RuntimeException("TODO " + type);
            }

            @Override
            public Integer case_ImArrayTypeMulti(ImArrayTypeMulti imArrayTypeMulti) {
                throw new RuntimeException("TODO " + type);
            }

            @Override
            public Integer case_ImSimpleType(ImSimpleType t) {
                switch (t.getTypename()) {
                    case "integer": return ISTORE;
                    default: return ASTORE;
                }
            }

            @Override
            public Integer case_ImArrayType(ImArrayType imArrayType) {
                throw new RuntimeException("TODO " + type);
            }

            @Override
            public Integer case_ImTypeVarRef(ImTypeVarRef imTypeVarRef) {
                throw new RuntimeException("TODO " + type);
            }
        });
    }

    private int line(de.peeeq.wurstscript.jassIm.Element e) {
        return e.attrTrace().attrSource().getLine();
    }

    @NotNull
    private String getSignatureDescriptor(ImFunction func) {
        StringBuilder sb = new StringBuilder("(");
        if (func.getName().equals("main")) {
            // add args parameter:
            sb.append("[Ljava/lang/String;");
        }
        for (ImVar v : func.getParameters()) {
            sb.append(translateType(v.getType()));
        }
        sb.append(")");
        sb.append(translateType(func.getReturnType()));
        return sb.toString();
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
