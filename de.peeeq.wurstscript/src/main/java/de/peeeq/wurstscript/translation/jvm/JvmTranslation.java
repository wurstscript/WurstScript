package de.peeeq.wurstscript.translation.jvm;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Element;
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

    /**
     * target version for translation
     */
    public static final int JAVA_VERSION = V11;
    private static Label currentEndLoopLabel;
    private ImProg prog;
    private final Path outputFolder;
    private ImmutableMultimap<ImClass, ImMethod> methodByClass;
    private Map<ImVar, Integer> localVars = new HashMap<>();
    // signatures of functions already added to the current class
    private Set<String> currentClassFunctions = new HashSet<>();

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

        @Override
        public String toString() {
            return name;
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

    private ImClass getElemClass(de.peeeq.wurstscript.jassIm.Element element) {
        while (element != null) {
            if (element instanceof ImClass) {
                return (ImClass) element;
            }
            element = element.getParent();
        }
        return null;
    }

    private void translatePackage(JPackage p, Collection<ImFunction> imFunctions, Collection<ImVar> imVars, Collection<ImClass> imClasses) throws IOException {
        ClassWriter classWriter = new WurstClassWriter(); // ClassWriter.COMPUTE_MAXS
        classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, p.name, null, "java/lang/Object", null);

        for (ImClass c : imClasses) {
            translateClass(p, classWriter, c);
        }

        for (ImVar v : imVars) {
            translateStaticVar(classWriter, v);
        }
        for (ImFunction f : imFunctions) {
            translateFunc(classWriter, f, ACC_PUBLIC | ACC_STATIC);
        }


        classWriter.visitEnd();
        byte[] bytes = classWriter.toByteArray();
        Files.write(outputFolder.resolve(p.name + ".class"), bytes);

        for (ImClass c : imClasses) {
            translateInnerClass(p, c);
        }
    }

    private void translateClass(JPackage p, ClassWriter outerClassWriter, ImClass c) {
        String name = className(c, p);
        outerClassWriter.visitNestMember(name);
        outerClassWriter.visitInnerClass(name, p.name, c.getName(), ACC_STATIC);
    }

    private void translateInnerClass(JPackage p, ImClass c) throws IOException {
        currentClassFunctions.clear();
        String name = className(c, p);
        ClassWriter classWriter = new WurstClassWriter();
        classWriter.visit(JAVA_VERSION, ACC_PUBLIC | ACC_SUPER, name, null, "java/lang/Object", null);
        classWriter.visitNestHost(p.name);
        classWriter.visitInnerClass(name, p.name, c.getName(), ACC_PUBLIC | ACC_STATIC);
        for (ImVar v : c.getFields()) {
            translateField(classWriter, v);
        }

        for (ImMethod method : c.getMethods()) {
            translateMethod(classWriter, method);
        }

        for (ImFunction func : c.getFunctions()) {
            translateFunc(classWriter, func, ACC_PUBLIC | ACC_STATIC);
        }

        classWriter.visitEnd();
        byte[] bytes = classWriter.toByteArray();
        Files.write(outputFolder.resolve(name + ".class"), bytes);
    }

    private void translateMethod(ClassWriter classWriter, ImMethod method) {
        translateFunc(classWriter, method.getImplementation(), ACC_PUBLIC);
    }

    private void translateField(ClassWriter classWriter, ImVar v) {
        FieldVisitor fieldVisitor = classWriter.visitField(0, v.getName(), translateType(v.getType()), null, null);
        fieldVisitor.visitEnd();
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
                ImClass cd = c.getClassDef();
                return "L" + getPackage(cd) + "$" + cd.getName() + ";";
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
                    case "boolean":
                        return "Z";
                    default:
                        return "L" + s.getTypename() + ";";
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

    private void translateFunc(ClassWriter classWriter, ImFunction func, int accesss) {
        String sig = getSignatureDescriptor(func);
        boolean changed = currentClassFunctions.add(func.getName() + sig);
        if (!changed) {
            return;
        }
        MethodVisitor methodVisitor = classWriter.visitMethod(accesss, func.getName(), sig, null, null);
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

    public void translateStatement(MethodVisitor methodVisitor, ImStmt s) {
        s.match(new ImStmt.MatcherVoid() {
            @Override
            public void case_ImTypeVarDispatch(ImTypeVarDispatch imTypeVarDispatch) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImDealloc(ImDealloc imDealloc) {
                // we have garbage collection in Java so nothing to do
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
            public void case_ImVarAccess(ImVarAccess va) {
                if (localVars.containsKey(va.getVar())) {
                    int index = localVars.get(va.getVar());
                    methodVisitor.visitVarInsn(getLoadInstruction(va.getVar().getType()), index);
                    return;
                }
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
                methodVisitor.visitLdcInsn(new Float(imRealVal.getValR()));
            }

            @Override
            public void case_ImFunctionCall(ImFunctionCall fc) {
                for (ImExpr arg : fc.getArguments()) {
                    translateStatement(methodVisitor, arg);
                }
                String signatureDescriptor = getSignatureDescriptor(fc.getFunc());
                methodVisitor.visitMethodInsn(INVOKESTATIC, getClassName(fc.getFunc()), fc.getFunc().getName(), signatureDescriptor, false);
            }

            @Override
            public void case_ImReturn(ImReturn imReturn) {
                if (imReturn.getReturnValue() instanceof ImExpr) {
                    ImExpr re = (ImExpr) imReturn.getReturnValue();
                    translateStatement(methodVisitor, re);
                    re.attrTyp().match(new ImType.MatcherVoid() {
                        @Override
                        public void case_ImTupleType(ImTupleType imTupleType) {
                            methodVisitor.visitInsn(ARETURN);
                        }

                        @Override
                        public void case_ImVoid(ImVoid imVoid) {
                            methodVisitor.visitInsn(RETURN);
                        }

                        @Override
                        public void case_ImClassType(ImClassType imClassType) {
                            methodVisitor.visitInsn(ARETURN);
                        }

                        @Override
                        public void case_ImArrayTypeMulti(ImArrayTypeMulti imArrayTypeMulti) {
                            methodVisitor.visitInsn(ARETURN);
                        }

                        @Override
                        public void case_ImSimpleType(ImSimpleType t) {
                            int returnIns = ARETURN;
                            switch (t.getTypename()) {
                                case "integer":
                                    returnIns = IRETURN;
                                    break;
                                case "real":
                                    returnIns = FRETURN;
                                    break;
                            }
                            methodVisitor.visitInsn(returnIns);
                        }

                        @Override
                        public void case_ImArrayType(ImArrayType imArrayType) {

                        }

                        @Override
                        public void case_ImTypeVarRef(ImTypeVarRef imTypeVarRef) {

                        }
                    });
                } else {
                    methodVisitor.visitInsn(RETURN);
                }
            }

            @Override
            public void case_ImTupleSelection(ImTupleSelection imTupleSelection) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImOperatorCall(ImOperatorCall oc) {
                if (oc.getOp().equals(WurstOperator.AND)) {
                    Label ifTrue = new Label();
                    Label afterAnd = new Label();
                    translateStatement(methodVisitor, oc.getArguments().get(0));
                    methodVisitor.visitJumpInsn(IFNE, ifTrue);
                    methodVisitor.visitInsn(ICONST_0);
                    methodVisitor.visitJumpInsn(GOTO, afterAnd);
                    methodVisitor.visitLabel(ifTrue);
                    translateStatement(methodVisitor, oc.getArguments().get(1));
                    methodVisitor.visitLabel(afterAnd);
                    return;
                } else if (oc.getOp().equals(WurstOperator.OR)) {
                    Label ifFalse = new Label();
                    Label afterOr = new Label();
                    translateStatement(methodVisitor, oc.getArguments().get(0));
                    methodVisitor.visitJumpInsn(IFEQ, ifFalse);
                    methodVisitor.visitInsn(ICONST_1);
                    methodVisitor.visitJumpInsn(GOTO, afterOr);
                    methodVisitor.visitLabel(ifFalse);
                    translateStatement(methodVisitor, oc.getArguments().get(1));
                    methodVisitor.visitLabel(afterOr);
                    return;
                }
                boolean intOperation = isIntOperation(oc);
                boolean floatOperation = isFloatOperation(oc);
                for (ImExpr a : oc.getArguments()) {
                    translateStatement(methodVisitor, a);
                    if (floatOperation && isIntType(a.attrTyp())) {
                        // convert to float
                        methodVisitor.visitInsn(I2F);
                    }
                }
                switch (oc.getOp()) {
                    case OR:
                        break;
                    case AND:
                        break;
                    case EQ: {
                        if (intOperation) {
                            makeCompare(methodVisitor, IF_ICMPNE, ICONST_1, ICONST_0);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FCMPL);
                            makeCompare(methodVisitor, IFNE, ICONST_1, ICONST_0);
                            return;
                        } else {
                            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    }
                    case NOTEQ: {
                        if (intOperation) {
                            makeCompare(methodVisitor, IF_ICMPEQ, ICONST_1, ICONST_0);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FCMPL);
                            makeCompare(methodVisitor, IFEQ, ICONST_1, ICONST_0);
                            return;
                        } else {
                            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
                            makeCompare(methodVisitor, IFNE, ICONST_1, ICONST_0);
                            return;
                        }
                    }
                    case LESS_EQ: {
                        if (intOperation) {
                            makeCompare(methodVisitor, IF_ICMPLE, ICONST_0, ICONST_1);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FCMPL);
                            makeCompare(methodVisitor, IFLE, ICONST_0, ICONST_1);
                            return;
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    }
                    case LESS: {
                        if (intOperation) {
                            makeCompare(methodVisitor, IF_ICMPLT, ICONST_0, ICONST_1);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FCMPL);
                            makeCompare(methodVisitor, IFLE, ICONST_0, ICONST_1);
                            return;
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    }
                    case GREATER_EQ: {
                        if (intOperation) {
                            makeCompare(methodVisitor, IF_ICMPGE, ICONST_0, ICONST_1);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FCMPL);
                            makeCompare(methodVisitor, IFGE, ICONST_0, ICONST_1);
                            return;
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    }
                    case GREATER: {
                        if (intOperation) {
                            makeCompare(methodVisitor, IF_ICMPGT, ICONST_0, ICONST_1);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FCMPL);
                            makeCompare(methodVisitor, IFGT, ICONST_0, ICONST_1);
                            return;
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    }
                    case PLUS:
                        if (intOperation) {
                            methodVisitor.visitInsn(IADD);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FADD);
                            return;
                        } else if (isStringOperation(oc)) {
                            methodVisitor.visitInvokeDynamicInsn(
                                    "makeConcatWithConstants",
                                    "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                                    new Handle(Opcodes.H_INVOKESTATIC,
                                            "java/lang/invoke/StringConcatFactory",
                                            "makeConcatWithConstants",
                                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"
                                            , false)
                                    , "\u0001\u0001");
                            return;
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    case MINUS:
                        if (intOperation) {
                            methodVisitor.visitInsn(ISUB);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FSUB);
                            return;
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    case MULT:
                        if (intOperation) {
                            methodVisitor.visitInsn(IMUL);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FMUL);
                            return;
                        }
                        throw new RuntimeException("unhandled case " + oc);
                    case DIV_REAL:
                        methodVisitor.visitInsn(FDIV);
                        return;
                    case DIV_INT:
                        methodVisitor.visitInsn(IDIV);
                        return;
                    case MOD_REAL:
                        methodVisitor.visitInsn(FREM);
                        return;
                    case MOD_INT:
                        methodVisitor.visitInsn(IREM);
                        return;
                    case NOT:
                        methodVisitor.visitInsn(ICONST_1);
                        methodVisitor.visitInsn(IXOR);
                        return;
                    case UNARY_MINUS:
                        if (intOperation) {
                            methodVisitor.visitInsn(INEG);
                            return;
                        } else if (floatOperation) {
                            methodVisitor.visitInsn(FNEG);
                            return;
                        }
                        throw new RuntimeException("TODO " + s + " " + oc.getArguments().get(0).attrTyp());
                }
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImVarArrayAccess(ImVarArrayAccess imVarArrayAccess) {
                throw new RuntimeException("TODO " + s);
            }

            @Override
            public void case_ImAlloc(ImAlloc imAlloc) {
                ImClass cd = imAlloc.getClazz().getClassDef();
                String className = className(cd, getPackage(cd));
                methodVisitor.visitTypeInsn(NEW, className);
            }

            @Override
            public void case_ImIntVal(ImIntVal imIntVal) {
                methodVisitor.visitIntInsn(SIPUSH, imIntVal.getValI());
            }

            @Override
            public void case_ImExitwhen(ImExitwhen e) {
                translateStatement(methodVisitor, e.getCondition());
                methodVisitor.visitJumpInsn(IFNE, currentEndLoopLabel);
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
                Label beginLoop = new Label();
                Label endLoop = new Label();
                methodVisitor.visitLabel(beginLoop);
                JvmTranslation.currentEndLoopLabel = endLoop;
                translateStatements(methodVisitor, imLoop.getBody());
                methodVisitor.visitLabel(endLoop);
            }

            @Override
            public void case_ImMemberAccess(ImMemberAccess ma) {
                ImVar var = ma.getVar();
                translateStatement(methodVisitor, ma.getReceiver());
                methodVisitor.visitFieldInsn(GETFIELD, getClassName(var), var.getName(), translateType(var.getType()));
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
                left.match(new ImLExpr.MatcherVoid() {
                    @Override
                    public void case_ImTupleSelection(ImTupleSelection imTupleSelection) {
                        throw new RuntimeException("TODO " + s);
                    }

                    @Override
                    public void case_ImVarAccess(ImVarAccess leftVar) {
                        ImVar var = leftVar.getVar();
                        if (localVars.containsKey(var)) {
                            int varIndex = localVars.get(var);
                            translateStatement(methodVisitor, imSet.getRight());
                            methodVisitor.visitVarInsn(getStoreInstruction(var.getType()), varIndex);
                        } else {
                            // if it is not a local variable it must be a static field / global variable
                            methodVisitor.visitFieldInsn(GETSTATIC, getPackage(var).name, var.getName(), translateType(var.getType()));
                        }
                    }

                    @Override
                    public void case_ImVarArrayAccess(ImVarArrayAccess imVarArrayAccess) {
                        throw new RuntimeException("TODO " + s);
                    }

                    @Override
                    public void case_ImMemberAccess(ImMemberAccess e) {
                        translateStatement(methodVisitor, e.getReceiver());
                        methodVisitor.visitFieldInsn(PUTFIELD, getClassName(e.getVar()), e.getVar().getName(), translateType(e.getVar().getType()));
                    }

                    @Override
                    public void case_ImStatementExpr(ImStatementExpr imStatementExpr) {
                        throw new RuntimeException("TODO " + s);
                    }

                    @Override
                    public void case_ImTupleExpr(ImTupleExpr imTupleExpr) {
                        throw new RuntimeException("TODO " + s);
                    }
                });
            }

            @Override
            public void case_ImStatementExpr(ImStatementExpr se) {
                translateStatements(methodVisitor, se.getStatements());
                translateStatement(methodVisitor, se.getExpr());
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

    @NotNull
    private String className(ImClass cd, JPackage aPackage) {
        return aPackage.name + "$" + cd.getName();
    }

    private String getClassName(ImVar var) {
        ImClass elemClass = getElemClass(var);
        if (elemClass == null) {
            return getPackage(var).name;
        }
        return getPackage(var).name + "$" + elemClass.getName();
    }

    private String getClassName(ImFunction f) {
        ImClass elemClass = getElemClass(f);
        if (elemClass == null) {
            return getPackage(f).name;
        }
        return getPackage(f).name + "$" + elemClass.getName();
    }

    private boolean isIntOperation(ImOperatorCall oc) {
        for (ImExpr a : oc.getArguments()) {
            if (!isIntType(a.attrTyp())) {
                return false;
            }
        }
        return true;
    }

    private boolean isFloatOperation(ImOperatorCall oc) {
        for (ImExpr a : oc.getArguments()) {
            if (isFloatType(a.attrTyp())) {
                return true;
            }
        }
        return false;
    }

    private boolean isStringOperation(ImOperatorCall oc) {
        for (ImExpr a : oc.getArguments()) {
            if (isStringType(a.attrTyp())) {
                return true;
            }
        }
        return false;
    }

    private boolean isIntType(ImType t) {
        return t instanceof ImSimpleType
                && ((ImSimpleType) t).getTypename().equals("integer");
    }

    private boolean isFloatType(ImType t) {
        return t instanceof ImSimpleType
                && ((ImSimpleType) t).getTypename().equals("real");
    }

    private boolean isStringType(ImType t) {
        return t instanceof ImSimpleType
                && ((ImSimpleType) t).getTypename().equals("string");
    }

    private void makeCompare(MethodVisitor methodVisitor, int jumpInstruction, int ifFalse, int ifTrue) {
        Label equal = new Label();
        Label afterCompare = new Label();
        methodVisitor.visitJumpInsn(jumpInstruction, equal);
        methodVisitor.visitInsn(ifFalse);
        methodVisitor.visitJumpInsn(GOTO, afterCompare);
        methodVisitor.visitLabel(equal);
        methodVisitor.visitInsn(ifTrue);
        methodVisitor.visitLabel(afterCompare);
    }


    private int getLoadInstruction(ImType type) {
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
                return ALOAD;
            }

            @Override
            public Integer case_ImArrayTypeMulti(ImArrayTypeMulti imArrayTypeMulti) {
                throw new RuntimeException("TODO " + type);
            }

            @Override
            public Integer case_ImSimpleType(ImSimpleType t) {
                switch (t.getTypename()) {
                    case "integer":
                        return ILOAD;
                    case "real":
                        return FLOAD;
                    default:
                        return ALOAD;
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
                return ASTORE;
            }

            @Override
            public Integer case_ImArrayTypeMulti(ImArrayTypeMulti imArrayTypeMulti) {
                throw new RuntimeException("TODO " + type);
            }

            @Override
            public Integer case_ImSimpleType(ImSimpleType t) {
                switch (t.getTypename()) {
                    case "integer":
                        return ISTORE;
                    case "real":
                        return FSTORE;
                    default:
                        return ASTORE;
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
        MethodVisitor methodVisitor;

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
