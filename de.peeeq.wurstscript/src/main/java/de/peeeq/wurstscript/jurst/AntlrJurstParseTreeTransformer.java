package de.peeeq.wurstscript.jurst;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompilationUnitInfo;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.jass.AntlrJassParseTreeTransformer;
import de.peeeq.wurstscript.jurst.antlr.JurstParser;
import de.peeeq.wurstscript.jurst.antlr.JurstParser.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

public class AntlrJurstParseTreeTransformer {

    private String file;
    private ErrorHandler cuErrorHandler;
    private LineOffsets lineOffsets;
    private boolean isJassCode = true;

    public AntlrJurstParseTreeTransformer(String file,
                                          ErrorHandler cuErrorHandler, LineOffsets lineOffsets) {
        this.file = file;
        this.cuErrorHandler = cuErrorHandler;
        this.lineOffsets = lineOffsets;
    }

    public CompilationUnit transform(CompilationUnitContext cu) {
        JassToplevelDeclarations jassDecls = Ast.JassToplevelDeclarations();
        WPackages packages = Ast.WPackages();
        try {
            for (TopLevelDeclarationContext decl : cu.decls) {

                if (decl.jassTopLevelDeclaration() != null) {
                    jassDecls.add(transformJassToplevelDecl(decl
                            .jassTopLevelDeclaration()));
                } else if (decl.wpackage() != null) {
                    packages.add(transformPackage(decl.wpackage()));
                }
            }

        } catch (CompileError e) {
            cuErrorHandler.sendError(e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            // ignore
        }

        return Ast
                .CompilationUnit(new CompilationUnitInfo(this.cuErrorHandler), jassDecls, packages);
    }

    private JassToplevelDeclaration transformJassToplevelDecl(
            JassTopLevelDeclarationContext d) {
        if (d.jassFuncDef() != null) {
            return transformJassFuncDef(d.jassFuncDef());
        } else if (d.jassGlobalsBlock() != null) {
            return transformJassGlobalsBlock(d.jassGlobalsBlock());
        } else if (d.jassNativeDecl() != null) {
            return transformJassNativeDecl(d.jassNativeDecl());
        } else if (d.jassTypeDecl() != null) {
            return transformJassTypeDecl(d.jassTypeDecl());
        }
        throw error(d, "unhandled case: " + text(d));
    }

    private Identifier text(@Nullable ParserRuleContext c) {
        if (c == null) {
            return Ast.Identifier(new WPos(file, lineOffsets, 1, 0), "");
        }
        return Ast.Identifier(source(c), c.getText());
    }

    private String rawText(@Nullable ParserRuleContext c) {
        if (c == null) {
            return "";
        }
        return c.getText();
    }

    private JassToplevelDeclaration transformJassTypeDecl(JassTypeDeclContext t) {
        Modifiers modifiers = Ast.Modifiers();
        Identifier name = text(t.name);
        OptTypeExpr optTyp = transformOptionalType(t.typeExpr());
        return Ast.NativeType(source(t), modifiers, name, optTyp);
    }

    private JassToplevelDeclaration transformJassNativeDecl(
            JassNativeDeclContext n) {
        Modifiers modifiers = Ast.Modifiers();
        FuncSig sig = transformFuncSig(n.jassFuncSignature());
        return Ast.NativeFunc(source(n), modifiers, sig.name,
                sig.formalParameters, sig.returnType);
    }

    private JassToplevelDeclaration transformJassGlobalsBlock(
            JassGlobalsBlockContext g) {
        JassGlobalBlock result = Ast.JassGlobalBlock();
        for (JassGlobalDeclContext v : g.jassGlobalDecl()) {
            Modifiers modifiers = Ast.Modifiers();
            if (v.constant != null) {
                modifiers.add(Ast.ModConstant(source(v.constant)));
            }
            OptTypeExpr optTyp = transformOptionalType(v.typeExpr());
            Identifier name = text(v.name);
            OptExpr initialExpr = transformOptionalExpr(v.initial);
            result.add(Ast.GlobalVarDef(source(v), modifiers, optTyp, name,
                    initialExpr));
        }
        return result;
    }

    private JassToplevelDeclaration transformJassFuncDef(JassFuncDefContext f) {
        Modifiers modifiers = Ast.Modifiers();
        FuncSig sig = transformFuncSig(f.jassFuncSignature());
        WStatements body = transformJassLocals(f.jassLocals);
        body.addAll(transformJassStatements(f.jassStatements()).removeAll());
        return Ast.FuncDef(source(f), modifiers, sig.name, sig.typeParams,
                sig.formalParameters, sig.returnType, body);
    }

    private WStatements transformJassLocals(List<JassLocalContext> jassLocals) {
        WStatements result = Ast.WStatements();
        for (JassLocalContext l : jassLocals) {
            Modifiers modifiers = Ast.Modifiers();
            OptTypeExpr optTyp = transformOptionalType(l.typeExpr());
            Identifier name = text(l.name);
            OptExpr initialExpr = transformOptionalExpr(l.initial);
            result.add(Ast.LocalVarDef(source(l), modifiers, optTyp, name,
                    initialExpr));
        }
        return result;
    }

    private WStatements transformJassStatements(JassStatementsContext stmts) {
        WStatements result = Ast.WStatements();
        for (JassStatementContext s : stmts.jassStatement()) {
            result.add(transformJassStatement(s));
        }
        return result;
    }

    private WStatement transformJassStatement(JassStatementContext s) {
        if (s.jassStatementCall() != null) {
            return transformJassStatementCall(s.jassStatementCall());
        } else if (s.jassStatementExithwhen() != null) {
            return transformJassStatementExitwhen(s.jassStatementExithwhen());
        } else if (s.jassStatementIf() != null) {
            return transformJassStatementIf(s.jassStatementIf());
        } else if (s.jassStatementLoop() != null) {
            return transformJassStatementLoop(s.jassStatementLoop());
        } else if (s.jassStatementReturn() != null) {
            return transformJassStatementReturn(s.jassStatementReturn());
        } else if (s.jassStatementSet() != null) {
            return transformJassStatementSet(s.jassStatementSet());
        }
        throw error(s, "unhandled case: " + text(s));
    }

    private WStatement transformJassStatementSet(JassStatementSetContext s) {
        return Ast.StmtSet(source(s), transformAssignable(s.left), transformExpr(s.right));
    }

    private WStatement transformJassStatementReturn(JassStatementReturnContext s) {
        return Ast.StmtReturn(source(s), transformOptionalExpr(s.expr()));
    }

    private WStatement transformJassStatementLoop(JassStatementLoopContext s) {
        return Ast.StmtLoop(source(s), transformJassStatements(s.jassStatements()));
    }

    private WStatement transformJassStatementIf(JassStatementIfContext s) {
        WStatements thenBlock = transformJassStatements(s.thenStatements);
        WStatements elseBlock = transformJassElseIfs(s.jassElseIfs());
        return Ast.StmtIf(source(s), transformExpr(s.cond), thenBlock, elseBlock);
    }

    private WStatements transformJassElseIfs(@Nullable JassElseIfsContext s) {
        if (s == null) {
            return Ast.WStatements();
        }
        if (s.cond != null) {
            return Ast.WStatements(Ast.StmtIf(source(s),
                    transformExpr(s.cond),
                    transformJassStatements(s.thenStatements),
                    transformJassElseIfs(s.jassElseIfs())));
        } else if (s.elseStmts != null) {
            return transformJassStatements(s.elseStmts);
        } else {
            return Ast.WStatements();
        }
    }

    private WStatement transformJassStatementExitwhen(JassStatementExithwhenContext s) {
        return Ast.StmtExitwhen(source(s), transformExpr(s.cond));
    }

    private WStatement transformJassStatementCall(JassStatementCallContext s) {
        return transformFunctionCall(s.exprFunctionCall());
    }

    private WPackage transformPackage(WpackageContext p) {
        // entering Jurst package
        isJassCode = false;


        WPos source = source(p);
        Modifiers modifiers = Ast.Modifiers();

        WImports imports = Ast.WImports();

        for (IdContext i : p.requires) {
            imports.add(Ast.WImport(source(i), true, false, text(i)));
        }

        for (WImportContext i : p.imports) {
            imports.add(transformImport(i));
        }

        WEntities elements = Ast.WEntities();
        for (EntityContext e : p.entities) {
            if (e.globalsBlock() != null) {
                for (VarDefContext v : e.globalsBlock().vars) {
                    WEntity en = transformVardef(v);
                    elements.add(en);
                }
            } else {
                WEntity en = transformEntity(e);
                if (en != null) {
                    elements.add(en);
                }
            }
        }

        if (p.initializer != null) {
            // add initializer
            WPos src = source(p.initializer);
            Identifier funcName = text(p.initializer);
            elements.add(Ast.InitBlock(src, Ast.WStatements(Ast.ExprFunctionCall(src, funcName, Ast.TypeExprList(), Ast.Arguments()))));
        }

        // leaving jass code
        isJassCode = true;
        return Ast.WPackage(source, modifiers, text(p.name), imports, elements);
    }


    private @Nullable WEntity transformEntity(EntityContext e) {
        try {
            if (e.nativeType() != null) {
                return transformNativeType(e.nativeType());
            } else if (e.funcDef() != null) {
                return transformFuncDef(e.funcDef());
            } else if (e.varDef() != null) {
                return transformVardef(e.varDef());
            } else if (e.initBlock() != null) {
                return transformInit(e.initBlock());
            } else if (e.nativeDef() != null) {
                return transformNativeDef(e.nativeDef());
            } else if (e.classDef() != null) {
                return transformClassDef(e.classDef());
            } else if (e.enumDef() != null) {
                return transformEnumDef(e.enumDef());
            } else if (e.moduleDef() != null) {
                return transformModuleDef(e.moduleDef());
            } else if (e.interfaceDef() != null) {
                return transformInterfaceDef(e.interfaceDef());
            } else if (e.tupleDef() != null) {
                return transformTupleDef(e.tupleDef());
            } else if (e.extensionFuncDef() != null) {
                return transformExtensionFuncDef(e.extensionFuncDef());
            } else if (e.functionInterfaceDef() != null) {
                return null; // TODO
            }

            if (e.exception != null) {
                return null;
            }
            // TODO Auto-generated method stub
            throw error(e, "entity not implemented " + text(e));
        } catch (NullPointerException npe) {
            // TODO
            npe.printStackTrace();
            return null;
        }
    }

    private WEntity transformExtensionFuncDef(ExtensionFuncDefContext f) {
        WPos src = source(f);
        Modifiers modifiers = transformModifiers(f.modifiersWithDoc());
        TypeExpr extendedType = transformTypeExpr(f.receiverType);
        FuncSig sig = transformFuncSig(f.funcSignature());
        WStatements body = transformStatements(f.statementsBlock());
        return Ast.ExtensionFuncDef(src, modifiers, extendedType, sig.name,
                sig.typeParams, sig.formalParameters, sig.returnType, body);
    }

    private Modifiers transformModifiers(ModifiersWithDocContext ms) {
        Modifiers result = Ast.Modifiers();
        if (ms.hotdocComment() != null) {
            result.add(Ast.WurstDoc(source(ms.hotdocComment()), ms
                    .hotdocComment().getText()));
        }
        for (ModifierContext m : ms.modifiers) {
            result.add(transformModifier(m));
        }
        return result;
    }

    private Modifier transformModifier(ModifierContext m) {
        WPos src = source(m);
        if (m.annotation() != null) {
            AnnotationContext a = m.annotation();
            return Ast.Annotation(src,
                    Ast.Identifier(source(a.name), a.name.getText().substring(1)),
                    Ast.Arguments(Ast.ExprStringVal(source(a.message), a.message.getText())));
        }
        switch (m.modType.getType()) {
            case JurstParser.PUBLIC:
                return Ast.VisibilityPublic(src);
            case JurstParser.PRIVATE:
                return Ast.VisibilityPrivate(src);
            case JurstParser.PROTECTED:
                return Ast.VisibilityProtected(src);
            case JurstParser.PULBICREAD:
            case JurstParser.READONLY:
                return Ast.VisibilityPublicread(src);
            case JurstParser.STATIC:
                return Ast.ModStatic(src);
            case JurstParser.OVERRIDE:
                return Ast.ModOverride(src);
            case JurstParser.ABSTRACT:
                return Ast.ModAbstract(src);
            case JurstParser.CONSTANT:
                return Ast.ModConstant(src);
            case JurstParser.DELEGATE:
                return annotation(src, "delegate", "internal");
            case JurstParser.STUB:
                return annotation(src, "stub", "internal");
        }
        throw error(m, "modifier not implemented");
    }

    private Modifier annotation(WPos src, String name, String message) {
        return Ast.Annotation(src, Ast.Identifier(src, name), Ast.Arguments(Ast.ExprStringVal(src, message)));
    }

    private WEntity transformTupleDef(TupleDefContext t) {

        WPos src = source(t);
        Modifiers modifiers = transformModifiers(t.modifiersWithDoc());
        Identifier name = text(t.name);
        WParameters parameters = transformFormalParameters(
                t.formalParameters(), false);
        OptTypeExpr returnTyp = Ast.NoTypeExpr();
        return Ast.TupleDef(src, modifiers, name, parameters, returnTyp);
    }

    private WEntity transformInterfaceDef(InterfaceDefContext i) {
        WPos src = source(i);
        Modifiers modifiers = transformModifiers(i.modifiersWithDoc());
        Identifier name = text(i.name);
        TypeParamDefs typeParameters = transformTypeParams(i.typeParams());
        TypeExprList extendsList = Ast.TypeExprList();
        for (TypeExprContext ex : i.extended) {
            extendsList.add(transformTypeExpr(ex));
        }
        ClassSlotResult slots = transformClassSlots(src, i.classSlots());
        return Ast.InterfaceDef(src, modifiers, name, typeParameters,
                extendsList, slots.methods, slots.vars, slots.constructors,
                slots.moduleInstanciations, slots.moduleUses, slots.onDestroy);
    }

    private ClassSlotResult transformClassSlots(WPos src,
                                                @Nullable ClassSlotsContext slots) {
        ClassSlotResult result = new ClassSlotResult();
        if (slots != null && slots.slots != null) {
            for (ClassSlotContext slot : slots.slots) {
                ClassSlot s = transformClassSlot(slot);
                if (s instanceof ConstructorDef) {
                    result.constructors.add((ConstructorDef) s);
                } else if (s instanceof FuncDef) {
                    result.methods.add((FuncDef) s);
                } else if (s instanceof ModuleUse) {
                    result.moduleUses.add((ModuleUse) s);
                } else if (s instanceof OnDestroyDef) {
                    if (result.onDestroy == null) {
                        result.onDestroy = (OnDestroyDef) s;
                    } else {
                        throw new CompileError(s.attrSource(),
                                "ondestroy already defined.");
                    }
                } else if (s instanceof GlobalVarDef) {
                    result.vars.add((GlobalVarDef) s);
                } else if (s instanceof ClassDef) {
                    result.innerClasses.add((ClassDef) s);
                } else if (s != null) {
                    throw error(slot, "unexpected classslot: "
                            + s.getClass().getSimpleName());
                }
            }
        }
        if (result.onDestroy == null) {
            result.onDestroy = Ast.OnDestroyDef(src.artificial(), Ast.WStatements());
        }
        return result;
    }

    private @Nullable ClassSlot transformClassSlot(ClassSlotContext s) {
        try {
            if (s.constructorDef() != null) {
                return transformConstructorDef(s.constructorDef());
            } else if (s.moduleUse() != null) {
                return transformModuleUse(s.moduleUse());
            } else if (s.ondestroyDef() != null) {
                return transformOndestroyDef(s.ondestroyDef());
            } else if (s.varDef() != null) {
                return transformVardef(s.varDef());
            } else if (s.funcDef() != null) {
                return transformFuncDef(s.funcDef());
            }
            if (s.exception != null) {
                return null;
            }
            throw error(s, "not matched: " + text(s));
        } catch (NullPointerException npe) {
            // TODO
            npe.printStackTrace();
            return null;
        }
    }

    private OnDestroyDef transformOndestroyDef(OndestroyDefContext o) {
        return Ast.OnDestroyDef(source(o),
                transformStatements(o.statementsBlock()));
    }

    private ClassSlot transformModuleUse(ModuleUseContext u) {
        return Ast.ModuleUse(source(u), text(u.moduleName),
                transformTypeArgs(u.typeArgs()));
    }

    private ConstructorDef transformConstructorDef(ConstructorDefContext c) {
        WPos source = source(c);
        Modifiers modifiers = transformModifiers(c.modifiersWithDoc());
        WParameters parameters = transformFormalParameters(
                c.formalParameters(), false);
        WStatements body = transformStatementList(c.stmts);
        boolean isExplicit = c.superArgs != null;
        Arguments superArgs = transformExprs(c.superArgs);
        SuperConstructorCall superCall;
        if (isExplicit) {
            superCall = Ast.SomeSuperConstructorCall(source(c.superArgs), source(c.superArgs), superArgs);
        } else {
            superCall = Ast.NoSuperConstructorCall();
        }
        return Ast.ConstructorDef(source, modifiers, parameters, superCall, body);
    }

    private WStatements transformStatementList(List<StatementContext> stmts) {
        WStatements result = Ast.WStatements();
        for (StatementContext s : stmts) {
            result.add(transformStatement(s));
        }
        return result;
    }

    private WEntity transformModuleDef(ModuleDefContext i) {
        WPos src = source(i);
        Modifiers modifiers = transformModifiers(i.modifiersWithDoc());
        Identifier name = text(i.name);
        TypeParamDefs typeParameters = transformTypeParams(i.typeParams());
        ClassSlotResult slots = transformClassSlots(src, i.classSlots());
        return Ast.ModuleDef(src, modifiers, name, typeParameters,
                slots.innerClasses, slots.methods, slots.vars, slots.constructors,
                slots.moduleInstanciations, slots.moduleUses, slots.onDestroy);
    }

    private WEntity transformEnumDef(EnumDefContext i) {
        WPos src = source(i);
        Modifiers modifiers = transformModifiers(i.modifiersWithDoc());
        Identifier name = text(i.name);
        EnumMembers members = Ast.EnumMembers();
        for (IdContext m : i.enumMembers) {
            members.add(Ast.EnumMember(source(m), Ast.Modifiers(), text(m)));
        }
        return Ast.EnumDef(src, modifiers, name, members);
    }

    private WEntity transformClassDef(ClassDefContext i) {
        WPos src = source(i);
        Modifiers modifiers = transformModifiers(i.modifiersWithDoc());
        Identifier name = text(i.name);
        TypeParamDefs typeParameters = transformTypeParams(i.typeParams());
        OptTypeExpr extendedClass = transformOptionalType(i.extended);
        TypeExprList implementsList = Ast.TypeExprList();
        for (TypeExprContext ex : i.implemented) {
            implementsList.add(transformTypeExpr(ex));
        }
        ClassSlotResult slots = transformClassSlots(src, i.classSlots());
        return Ast.ClassDef(src, modifiers, name, typeParameters,
                extendedClass, implementsList, slots.innerClasses, slots.methods, slots.vars,
                slots.constructors, slots.moduleInstanciations,
                slots.moduleUses, slots.onDestroy);
    }

    private NativeType transformNativeType(NativeTypeContext n) {
        OptTypeExpr extended;
        if (n.extended != null) {
            extended = Ast.TypeExprSimple(source(n.extended),
                    Ast.NoTypeExpr(), rawText(n.extended), Ast.TypeExprList());
        } else {
            extended = Ast.NoTypeExpr();
        }
        return Ast.NativeType(source(n), Ast.Modifiers(), text(n.name),
                extended);
    }

    private FuncDef transformFuncDef(FuncDefContext f) {
        FuncSig sig = transformFuncSig(f.funcSignature());
        Modifiers modifiers = transformModifiers(f.modifiersWithDoc());
        WStatements body = transformStatements(f.statementsBlock());
        return Ast.FuncDef(source(f), modifiers, sig.name, sig.typeParams,
                sig.formalParameters, sig.returnType, body);
    }

    private GlobalVarDef transformVardef(VarDefContext v) {
        WPos source = source(v);
        Modifiers modifiers = transformModifiers(v.modifiersWithDoc());
        if (v.constant != null) {
            modifiers.add(Ast.ModConstant(source(v.constant)));
        }
        OptExpr initialExpr = transformOptionalExpr(v.initial);
        Identifier name = text(v.name);
        OptTypeExpr optTyp = transformOptionalType(v.varType);
        if (v.arraySizes != null && !v.arraySizes.isEmpty()) {
            if (optTyp instanceof TypeExprArray) {
                TypeExprArray arType = (TypeExprArray) optTyp;
                arType.setArraySize(transformOptionalExpr(v.arraySizes.get(0)));
                if (v.arraySizes.size() > 1) {
                    throw error(v.arraySizes.get(1), "Only one-dimensional arrays are supported currently.");
                }
            } else {
                throw error(v.arraySizes.get(0), "Array size can only be given for array types.");
            }
        }
        return Ast.GlobalVarDef(source, modifiers, optTyp, name, initialExpr);
    }

    private InitBlock transformInit(InitBlockContext i) {
        return Ast.InitBlock(source(i),
                transformStatements(i.statementsBlock()));
    }

    private WStatements transformStatements(@Nullable StatementsBlockContext b) {
        WStatements result = Ast.WStatements();
        if (b != null) {
            for (StatementContext s : b.statement()) {
                result.add(transformStatement(s));
            }
        }
        return result;
    }

    private WStatement transformStatement(StatementContext s) {
        if (s.stmtIf() != null) {
            return transformIf(s.stmtIf());
        } else if (s.stmtWhile() != null) {
            return transformWhile(s.stmtWhile());
        } else if (s.localVarDef() != null) {
            return transformLocalVarDef(s.localVarDef());
        } else if (s.stmtSet() != null) {
            return transformStmtSet(s.stmtSet());
        } else if (s.stmtCall() != null) {
            return transformCall(s.stmtCall());
        } else if (s.stmtReturn() != null) {
            return transformReturn(s.stmtReturn());
        } else if (s.stmtForLoop() != null) {
            return transformForLoop(s.stmtForLoop());
        } else if (s.stmtBreak() != null) {
            return Ast
                    .StmtExitwhen(source(s), Ast.ExprBoolVal(source(s), true));
        } else if (s.stmtSkip() != null) {
            return Ast.StmtSkip(source(s));
        } else if (s.stmtSwitch() != null) {
            return transformSwitch(s.stmtSwitch());
        } else if (s.stmtLoop() != null) {
            return transformLoop(s.stmtLoop());
        } else if (s.stmtExitwhen() != null) {
            return transformExitwhen(s.stmtExitwhen());
        }

        if (s.exception != null) {
            return Ast.StmtErr(source(s));
        }

        // TODO Auto-generated method stub
        throw error(s, "not implemented: " + text(s) + "\n"
                + s.toStringTree());
    }

    private WStatement transformExitwhen(StmtExitwhenContext s) {
        return Ast.StmtExitwhen(source(s), transformExpr(s.expr()));
    }

    private WStatement transformLoop(StmtLoopContext s) {
        return Ast.StmtLoop(source(s), transformStatements(s.statementsBlock()));
    }

    private WStatement transformSwitch(StmtSwitchContext s) {
        Expr expr = transformExpr(s.expr());
        SwitchCases cases = Ast.SwitchCases();
        for (SwitchCaseContext c : s.switchCase()) {
            Expr e = transformExpr(c.expr());
            WStatements stmts = transformStatements(c.statementsBlock());
            cases.add(Ast.SwitchCase(source(c), Ast.ExprList(e), stmts));
        }
        SwitchDefaultCase switchDefault;
        if (s.switchDefaultCase() != null) {
            switchDefault = Ast.SwitchDefaultCaseStatements(source(s.switchDefaultCase()),
                    transformStatements(s.switchDefaultCase().statementsBlock()));
        } else {
            switchDefault = Ast.NoDefaultCase();
        }
        return Ast.SwitchStmt(source(s), expr, cases, switchDefault);
    }

    private WStatement transformWhile(StmtWhileContext s) {
        return Ast.StmtWhile(source(s), transformExpr(s.cond),
                transformStatements(s.statementsBlock()));
    }

    private ExprDestroy transformExprDestroy(ExprDestroyContext e) {
        return transformExprDestroyObject(source(e), e.expr());
    }

    private ExprDestroy transformExprDestroyObject(WPos source, ExprContext e) {
        return Ast.ExprDestroy(source, transformExpr(e));
    }

    private StmtReturn transformReturn(StmtReturnContext s) {
        return Ast.StmtReturn(source(s), transformOptionalExpr(s.expr()));
    }

    private WStatement transformStmtSet(StmtSetContext s) {
        NameRef updatedExpr = transformAssignable(s.left);
        WPos src = source(s);
        if (s.assignOp != null) {
            Expr right = transformExpr(s.right);
            WurstOperator op = getAssignOp(s.assignOp);
            if (op != null) {
                right = Ast.ExprBinary(src, (Expr) updatedExpr.copy(), op,
                        right);
            }
            return Ast.StmtSet(src, updatedExpr, right);
        } else if (s.incOp != null) {
            Expr right = Ast.ExprBinary(src, (Expr) updatedExpr.copy(),
                    WurstOperator.PLUS, Ast.ExprIntVal(src, "1"));
            return Ast.StmtSet(src, updatedExpr, right);
        } else if (s.decOp != null) {
            Expr right = Ast.ExprBinary(src, (Expr) updatedExpr.copy(),
                    WurstOperator.MINUS, Ast.ExprIntVal(src, "1"));
            return Ast.StmtSet(src, updatedExpr, right);
        }
        if (s.exception != null) {
            return Ast.StmtErr(src);
        } else {
            throw error(s, "set-statment not implemented");
        }
    }

    private @Nullable WurstOperator getAssignOp(Token assignOp) {
        switch (assignOp.getType()) {
            case JurstParser.PLUS_EQ:
                return WurstOperator.PLUS;
            case JurstParser.MINUS_EQ:
                return WurstOperator.MINUS;
            case JurstParser.MULT_EQ:
                return WurstOperator.MULT;
            case JurstParser.DIV_EQ:
                return WurstOperator.DIV_REAL;
            case JurstParser.EQ:
                return null;
            default:
                throw error(source(assignOp), "unhandled assign op: " + text(assignOp));
        }
    }

    private NameRef transformAssignable(ExprAssignableContext e) {
        if (e.exprMemberVar() != null) {
            return transformExprMemberVar(e.exprMemberVar());
        } else if (e.exprVarAccess() != null) {
            return transformExprVarAccess(e.exprVarAccess());
        }
        return Ast.ExprVarAccess(source(e), text(e));
    }

    private NameRef transformExprVarAccess(ExprVarAccessContext e) {
        if (e.indexes() == null || e.indexes().isEmpty()) {
            return Ast.ExprVarAccess(source(e), text(e.varname));
        } else {
            return Ast.ExprVarArrayAccess(source(e), text(e.varname),
                    transformIndexes(e.indexes()));
        }
    }

    private NameRef transformExprMemberVar(ExprMemberVarContext e) {
        return transformExprMemberVarAccess2(source(e), e.expr(), e.dots,
                e.varname, e.indexes());
    }

    private NameRef transformExprMemberVarAccess2(WPos source,
                                                  ExprContext e_expr, Token e_dots, IdContext varname2,
                                                  @Nullable List<IndexesContext> indexList) {
        Expr left = transformExpr(e_expr);

        if (left instanceof ExprEmpty) {
            left = Ast.ExprThis(left.getSource());
        }

        Identifier varName = text(varname2);
        if (indexList != null && !indexList.isEmpty()) {
            Indexes indexes = transformIndexes(indexList);
            if (e_dots.getType() == JurstParser.DOT) {
                return Ast
                        .ExprMemberArrayVarDot(source, left, varName, indexes);
            } else {
                return Ast.ExprMemberArrayVarDotDot(source, left, varName,
                        indexes);
            }
        } else {
            if (e_dots.getType() == JurstParser.DOT) {
                return Ast.ExprMemberVarDot(source, left, varName);
            } else {
                return Ast.ExprMemberVarDotDot(source, left, varName);
            }
        }
    }

    private String text(@Nullable Token t) {
        if (t == null) {
            return "";
        }
        return t.getText();
    }

    private WStatement transformForLoop(StmtForLoopContext s) {
        if (s.forRangeLoop() != null) {
            return transformForRangeLoop(s.forRangeLoop());
        } else if (s.forIteratorLoop() != null) {
            return transformForIteratorLoop(s.forIteratorLoop());
        }
        throw error(s, "for loop not implemented: " + text(s));
    }

    private WStatement transformForRangeLoop(ForRangeLoopContext s) {
        WPos source = source(s);
        LocalVarDef loopVar = transformLocalVarDef(s.loopVar);
        loopVar.setInitialExpr(transformExpr(s.start));
        Expr to = transformExpr(s.end);
        Expr step;
        if (s.step == null) {
            step = Ast.ExprIntVal(source(s.direction), "1");
        } else {
            step = transformExpr(s.step);
        }
        WStatements body = transformStatements(s.statementsBlock());
        if (s.direction.getType() == JurstParser.TO) {
            return Ast.StmtForRangeUp(source, loopVar, to, step, body);
        } else if (s.direction.getType() == JurstParser.DOWNTO) {
            return Ast.StmtForRangeDown(source, loopVar, to, step, body);
        }
        throw error(s, "for range loop not implemented: " + text(s));
    }

    private LocalVarDef transformLocalVarDef(LocalVarDefInlineContext v) {
        Modifiers modifiers = Ast.Modifiers();
        OptTypeExpr optTyp = transformOptionalType(v.typeExpr());
        Identifier name = text(v.name);
        OptExpr initialExpr = Ast.NoExpr();
        return Ast.LocalVarDef(source(v), modifiers, optTyp, name, initialExpr);
    }

    private WStatement transformForIteratorLoop(ForIteratorLoopContext s) {
        WPos source = source(s);
        LocalVarDef loopVar = transformLocalVarDef(s.loopVar);
        Expr in = transformExpr(s.iteratorExpr);
        WStatements body = transformStatements(s.statementsBlock());
        if (s.iterStyle.getType() == JurstParser.IN) {
            return Ast.StmtForIn(source, loopVar, in, body);
        } else {
            return Ast.StmtForFrom(source, loopVar, in, body);
        }
    }

    private LocalVarDef transformLocalVarDef(LocalVarDefContext l) {
        Modifiers modifiers = Ast.Modifiers();
        if (l.let != null) {
            modifiers.add(Ast.ModConstant(source(l.let)));
        }
        OptTypeExpr optTyp = transformOptionalType(l.type);
        Identifier name = text(l.name);
        OptExpr initialExpr = transformOptionalExpr(l.initial);
        return Ast.LocalVarDef(source(l), modifiers, optTyp, name, initialExpr);
    }

    private OptExpr transformOptionalExpr(@Nullable ExprContext e) {
        if (e == null) {
            return Ast.NoExpr();
        }
        Expr r = transformExpr(e);
        if (r instanceof ExprEmpty) {
            return Ast.NoExpr();
        }
        return r;
    }

    private WStatement transformCall(StmtCallContext c) {
        if (c.exprFunctionCall() != null) {
            return transformFunctionCall(c.exprFunctionCall());
        } else if (c.exprMemberMethod() != null) {
            return transformMemberMethodCall(c.exprMemberMethod());
        } else if (c.exprNewObject() != null) {
            return transformExprNewObject(c.exprNewObject());
        } else if (c.exprDestroy() != null) {
            return transformExprDestroy(c.exprDestroy());
        }
        return Ast.StmtErr(source(c));
    }

    private ExprNewObject transformExprNewObject(ExprNewObjectContext e) {
        Identifier typeName = text(e.className);
        TypeExprList typeArgs = transformTypeArgs(e.typeArgs());
        Arguments args = transformExprs(e.exprList());
        return Ast.ExprNewObject(source(e), typeName, typeArgs, args);
    }

    private WStatement transformMemberMethodCall(ExprMemberMethodContext e) {
        WPos source = source(e);
        ExprContext receiver = e.receiver;
        Token dots = e.dots;
        IdContext funcName = e.funcName;
        TypeArgsContext typeArgs = e.typeArgs();
        ExprListContext args = e.exprList();
        return transformMemberMethodCall2(source, receiver, dots, funcName,
                typeArgs, args);
    }

    private ExprMemberMethod transformMemberMethodCall2(WPos source,
                                                        ExprContext receiver, Token dots, IdContext funcName,
                                                        TypeArgsContext typeArgs, ExprListContext args) {
        Expr left = transformExpr(receiver);

        if (left instanceof ExprEmpty) {
            left = Ast.ExprThis(left.getSource());
        }

        if (dots.getType() == JurstParser.DOT) {
            return Ast.ExprMemberMethodDot(source, left, text(funcName),
                    transformTypeArgs(typeArgs), transformExprs(args));
        } else {
            return Ast.ExprMemberMethodDotDot(source, left, text(funcName),
                    transformTypeArgs(typeArgs), transformExprs(args));
        }

    }

    private ExprFunctionCall transformFunctionCall(ExprFunctionCallContext c) {
        return Ast.ExprFunctionCall(source(c), text(c.funcName),
                transformTypeArgs(c.typeArgs()), transformExprs(c.exprList()));
    }

    private Arguments transformExprs(@Nullable ExprListContext es) {
        Arguments result = Ast.Arguments();
        if (es != null) {
            for (ExprContext e : es.exprs) {
                result.add(transformExpr(e));
            }
        }
        if (result.size() == 1 && result.get(0) instanceof ExprEmpty) {
            result.clear();
        }
        return result;
    }

    private WStatement transformIf(StmtIfContext i) {
        Expr cond = transformExpr(i.cond);
        WStatements thenBlock = transformStatements(i.thenStatements);
        WStatements elseBlock = transformElseBlock(i.elseStatements());
        return Ast.StmtIf(source(i), cond, thenBlock, elseBlock);
    }

    private WStatements transformElseBlock(@Nullable ElseStatementsContext es) {
        if (es == null) {
            return Ast.WStatements();
        }
        if (es.cond != null) {
            // elseif block
            WStatements thenBlock = transformStatements(es.thenStatements);
            WStatements elseBlock = transformElseBlock(es.elseStatements());
            return Ast.WStatements(Ast.StmtIf(source(es), transformExpr(es.cond), thenBlock, elseBlock));
        } else if (es.statementsBlock() != null) {
            // 'else' block
            return transformStatements(es.statementsBlock());
        } else {
            // just an 'endif'
            return Ast.WStatements();
        }
    }

    private Expr transformExpr(ExprContext e) {
        WPos source = source(e);
        if (e.exprPrimary() != null) {
            return transformExprPrimary(e.exprPrimary());
        } else if (e.left != null && e.right != null && e.op != null) {
            return Ast.ExprBinary(source, transformExpr(e.left),
                    transformOp(e.op), transformExpr(e.right));
        } else if (e.op != null && e.op.getType() == JurstParser.NOT) {
            return Ast.ExprUnary(source, WurstOperator.NOT,
                    transformExpr(e.right));
        } else if (e.op != null && e.op.getType() == JurstParser.MINUS) {
            return Ast.ExprUnary(source, WurstOperator.UNARY_MINUS,
                    transformExpr(e.right));
        } else if (e.castToType != null) {
            return Ast.ExprCast(source, transformTypeExpr(e.castToType),
                    transformExpr(e.left));
        } else if (e.dotsVar != null) {
            return transformExprMemberVarAccess2(source, e.receiver, e.dotsVar,
                    e.varName, e.indexes());
        } else if (e.dotsCall != null) {
            return transformMemberMethodCall2(source, e.receiver, e.dotsCall,
                    e.funcName, e.typeArgs(), e.exprList());
        } else if (e.instaneofType != null) {
            return Ast.ExprInstanceOf(source, transformTypeExpr(e.instaneofType),
                    transformExpr(e.left));
        } else if (e.destroyedObject != null) {
            return transformExprDestroyObject(source(e), e.destroyedObject);
        }

        ParseTree left = getLeftParseTree(e);
        if (left != null) {
            source = source.withLeftPos(1 + stopPos(left));
        }
        ParseTree right = getRightParseTree(e);
        if (right != null) {
            source = source.withRightPos(beginPos(right));
        }
        return Ast.ExprEmpty(source);
    }


    private int beginPos(ParseTree left) {
        if (left instanceof ParserRuleContext) {
            ParserRuleContext left2 = (ParserRuleContext) left;
            return left2.getStart().getStartIndex();
        } else if (left instanceof TerminalNode) {
            TerminalNode left2 = (TerminalNode) left;
            return left2.getSymbol().getStartIndex();
        }
        throw new Error("unhandled case: " + left.getClass() + "  // " + left);
    }

    private int stopPos(ParseTree left) {
        if (left instanceof ParserRuleContext) {
            ParserRuleContext left2 = (ParserRuleContext) left;
            return left2.getStop().getStopIndex();
        } else if (left instanceof TerminalNode) {
            TerminalNode left2 = (TerminalNode) left;
            return left2.getSymbol().getStopIndex();
        }
        throw new Error("unhandled case: " + left.getClass() + "  // " + left);
    }

    private @Nullable ParseTree getLeftParseTree(@Nullable ParserRuleContext e) {
        if (e == null || e.getParent() == null) {
            return null;
        }
        ParserRuleContext parent = e.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) == e) {
                if (i > 0) {
                    return parent.getChild(i - 1);
                } else {
                    return getLeftParseTree(parent);
                }
            }
        }
        return null;
    }

    private @Nullable ParseTree getRightParseTree(@Nullable ParserRuleContext e) {
        if (e == null || e.getParent() == null) {
            return null;
        }
        ParserRuleContext parent = e.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) == e) {
                if (i < parent.getChildCount() - 1) {
                    return parent.getChild(i + 1);
                } else {
                    return getRightParseTree(parent);
                }
            }
        }
        return null;
    }

    private WurstOperator transformOp(Token op) {
        switch (op.getType()) {
            case JurstParser.OR:
                return WurstOperator.OR;
            case JurstParser.AND:
                return WurstOperator.AND;
            case JurstParser.EQEQ:
                return WurstOperator.EQ;
            case JurstParser.NOT_EQ:
                return WurstOperator.NOTEQ;
            case JurstParser.LESS_EQ:
                return WurstOperator.LESS_EQ;
            case JurstParser.LESS:
                return WurstOperator.LESS;
            case JurstParser.GREATER_EQ:
                return WurstOperator.GREATER_EQ;
            case JurstParser.GREATER:
                return WurstOperator.GREATER;
            case JurstParser.PLUS:
                return WurstOperator.PLUS;
            case JurstParser.MINUS:
                return WurstOperator.MINUS;
            case JurstParser.MULT:
                return WurstOperator.MULT;
            case JurstParser.DIV_REAL:
                return WurstOperator.DIV_REAL;
            case JurstParser.DIV:
                return WurstOperator.DIV_INT;
            case JurstParser.MOD_REAL:
                return WurstOperator.MOD_REAL;
            case JurstParser.MOD:
                return WurstOperator.MOD_INT;
            case JurstParser.NOT:
                return WurstOperator.NOT;
        }
        throw error(source(op), "operator not implemented: " + text(op));
    }

    private Expr transformExprPrimary(ExprPrimaryContext e) {
        if (e.atom != null) {
            return transformAtom(e.atom);
        } else if (e.varname != null) {
            if (e.indexes() != null && !e.indexes().isEmpty()) {
                Indexes index = transformIndexes(e.indexes());
                return Ast.ExprVarArrayAccess(source(e), text(e.varname),
                        index);
            } else {
                return Ast.ExprVarAccess(source(e), text(e.varname));
            }
        } else if (e.expr() != null) {
            return transformExpr(e.expr());
        } else if (e.exprFunctionCall() != null) {
            return transformFunctionCall(e.exprFunctionCall());
        } else if (e.exprNewObject() != null) {
            return transformExprNewObject(e.exprNewObject());
        } else if (e.exprClosure() != null) {
            return transformClosure(e.exprClosure());
        } else if (e.exprStatementsBlock() != null) {
            return transformExprStatementsBlock(e.exprStatementsBlock());
        } else if (e.exprFuncRef() != null) {
            return transformExprFuncRef(e.exprFuncRef());
        }
        // TODO Auto-generated method stub
        throw error(e, "primary expr not implemented " + text(e));
    }

    private ExprFuncRef transformExprFuncRef(ExprFuncRefContext e) {
        String scopeName = e.scopeName == null ? "" : rawText(e.scopeName);
        Identifier funcName = text(e.funcName);
        return Ast.ExprFuncRef(source(e), scopeName, funcName);
    }

    private ExprStatementsBlock transformExprStatementsBlock(
            ExprStatementsBlockContext e) {
        return Ast.ExprStatementsBlock(source(e),
                transformStatements(e.statementsBlock()));
    }

    private ExprClosure transformClosure(ExprClosureContext e) {
        WParameters parameters = transformFormalParameters(
                e.formalParameters(), true);
        Expr implementation = transformExpr(e.expr());
        WShortParameters sparameters = Ast.WShortParameters();
        for (WParameter p : parameters) {
            sparameters.add(Ast.WShortParameter(
                    p.getSource(),
                    p.getModifiers().copy(),
                    p.getTyp().copy(),
                    p.getNameId().copy()
            ));
        }
        return Ast.ExprClosure(source(e), source(e.arrow), sparameters, implementation);
    }

    private Indexes transformIndexes(List<IndexesContext> indexList) {
        Indexes result = Ast.Indexes();
        for (IndexesContext i : indexList) {
            result.add(transformExpr(i.expr()));
        }
        return result;
    }

    private Expr transformAtom(Token a) {
        WPos source = source(a);
        switch (a.getType()) {
            case JurstParser.INT:
                return Ast.ExprIntVal(source, text(a));
            case JurstParser.REAL:
                return Ast.ExprRealVal(source, text(a));
            case JurstParser.STRING:
                return Ast.ExprStringVal(source, getStringVal(source, text(a)));
            case JurstParser.NULL:
                return Ast.ExprNull(source);
            case JurstParser.TRUE:
                return Ast.ExprBoolVal(source, true);
            case JurstParser.FALSE:
                return Ast.ExprBoolVal(source, false);
            case JurstParser.THIS:
                if (isJassCode) {
                    return Ast.ExprVarAccess(source, Ast.Identifier(source, "this"));
                } else {
                    return Ast.ExprThis(source);
                }
            case JurstParser.SUPER:
                return Ast.ExprSuper(source);
        }
        // TODO Auto-generated method stub
        throw error(source(a), "atom not implemented: " + text(a));
    }

    private String getStringVal(WPos source, String text) {
        StringBuilder res = new StringBuilder();
        AntlrJassParseTreeTransformer.buildStringVal(source, text, res);
        return res.toString();
    }

    private WEntity transformNativeDef(NativeDefContext n) {
        Modifiers modifiers = transformModifiers(n.modifiersWithDoc());
        FuncSig sig = transformFuncSig(n.funcSignature());
        return Ast.NativeFunc(source(n), modifiers, sig.name,
                sig.formalParameters, sig.returnType);
    }

    private FuncSig transformFuncSig(FuncSignatureContext s) {
        TypeParamDefs typeParams = transformTypeParams(s.typeParams());
        WParameters formalParameters = transformFormalParameters(
                s.formalParameters(), false);
        OptTypeExpr returnType = transformOptionalType(s.returnType);
        return new FuncSig(text(s.name), typeParams, formalParameters,
                returnType);
    }

    private FuncSig transformFuncSig(JassFuncSignatureContext s) {
        TypeParamDefs typeParams = Ast.TypeParamDefs();
        WParameters formalParameters = Ast.WParameters();
        for (FormalParameterContext p : s.args) {
            formalParameters.add(transformFormalParameter(p, false));
        }
        OptTypeExpr returnType = transformOptionalType(s.returnType);
        return new FuncSig(text(s.name), typeParams, formalParameters,
                returnType);
    }

    private OptTypeExpr transformOptionalType(@Nullable TypeExprContext t) {
        if (t == null) {
            return Ast.NoTypeExpr();
        }
        return transformTypeExpr(t);
    }

    private TypeExpr transformTypeExpr(TypeExprContext t) throws Error {
        OptTypeExpr scopeType;
        if (t.typeExpr() != null) {
            scopeType = transformTypeExpr(t.typeExpr());
        } else {
            scopeType = Ast.NoTypeExpr();
        }

        if (t.thistype != null) {
            return Ast.TypeExprThis(source(t), scopeType);
        } else if (t.typeName != null) {
            return Ast.TypeExprSimple(source(t), scopeType, text(t.typeName),
                    transformTypeArgs(t.typeArgs()));
        } else if (t.typeExpr() != null) {
            ExprContext arrSize = null;
            if (t.arraySizes != null && !t.arraySizes.isEmpty()) {
                arrSize = t.arraySizes.get(0);
                if (t.arraySizes.size() > 1) {
                    throw error(t.arraySizes.get(1), "Currently only one dimension is allowed for arrays.");
                }
            }

            return Ast
                    .TypeExprArray(source(t), (TypeExpr) scopeType, transformOptionalExpr(arrSize));
        } else {
            return Ast.TypeExprSimple(source(t), scopeType, "", Ast.TypeExprList());
        }
    }


    private CompileError error(WPos source, String msg) {
        return new CompileError(source, msg);
    }

    private CompileError error(ParserRuleContext source, String msg) {
        return new CompileError(source(source), msg);
    }

    private TypeExprList transformTypeArgs(@Nullable TypeArgsContext typeArgs) {
        TypeExprList result = Ast.TypeExprList();
        if (typeArgs != null) {
            for (TypeExprContext e : typeArgs.args) {
                result.add(transformTypeExpr(e));
            }
        }
        return result;
    }

    private WParameters transformFormalParameters(FormalParametersContext ps,
                                                  boolean makeConstant) {
        WParameters result = Ast.WParameters();
        for (FormalParameterContext p : ps.params) {
            result.add(transformFormalParameter(p, makeConstant));
        }
        return result;
    }

    private WParameter transformFormalParameter(FormalParameterContext p,
                                                boolean makeConstant) {
        Modifiers modifiers = Ast.Modifiers();
        if (makeConstant) {
            modifiers.add(Ast.ModConstant(source(p).artificial()));
        }
        return Ast.WParameter(source(p), modifiers,
                transformTypeExpr(p.typeExpr()), text(p.name));
    }

    private TypeParamDefs transformTypeParams(@Nullable TypeParamsContext typeParams) {
        TypeParamDefs result = Ast.TypeParamDefs();
        if (typeParams != null) {
            for (TypeParamContext p : typeParams.params) {
                result.add(transformTypeParam(p));
            }
        }
        return result;
    }

    private TypeParamDef transformTypeParam(TypeParamContext p) {
        Modifiers modifiers = Ast.Modifiers();
        return Ast.TypeParamDef(source(p), modifiers, text(p.name), Ast.NoTypeParamConstraints());
    }

    private WImport transformImport(WImportContext i) {
        // TODO initlater
        return Ast.WImport(source(i), i.isPublic != null,
                i.isInitLater != null, text(i.importedPackage));
    }

    private WPos source(ParserRuleContext p) {
        return new WPos(file, lineOffsets, p.start.getStartIndex(),
                p.stop.getStopIndex() + 1);
    }

    private WPos source(Token p) {
        return new WPos(file, lineOffsets, p.getStartIndex(), p.getStopIndex() + 1);
    }

    class FuncSig {
        Identifier name;
        TypeParamDefs typeParams;
        WParameters formalParameters;
        OptTypeExpr returnType;

        public FuncSig(Identifier name, TypeParamDefs typeParams,
                       WParameters formalParameters, OptTypeExpr returnType) {
            this.name = name;
            this.typeParams = typeParams;
            this.formalParameters = formalParameters;
            this.returnType = returnType;
        }

    }

    class ClassSlotResult {

        public ClassDefs innerClasses = Ast.ClassDefs();
        public ConstructorDefs constructors = Ast.ConstructorDefs();
        public ModuleInstanciations moduleInstanciations = Ast
                .ModuleInstanciations();
        public GlobalVarDefs vars = Ast.GlobalVarDefs();
        public FuncDefs methods = Ast.FuncDefs();
        public ModuleUses moduleUses = Ast.ModuleUses();
        public @Nullable OnDestroyDef onDestroy = null;

    }

}
