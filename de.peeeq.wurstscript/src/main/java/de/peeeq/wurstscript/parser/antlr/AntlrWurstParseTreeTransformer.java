package de.peeeq.wurstscript.parser.antlr;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.antlr.WurstParser.*;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompilationUnitInfo;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.jass.AntlrJassParseTreeTransformer;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

public class AntlrWurstParseTreeTransformer {

    private String file;
    private ErrorHandler cuErrorHandler;
    private LineOffsets lineOffsets;

    public AntlrWurstParseTreeTransformer(String file,
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
            WLogger.warning("Compilation error in parse tree transformer", e);
        } catch (NullPointerException e) {
            WLogger.warning("Error transforming compilation unit " + line(cu), e);
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

    private Identifier text(@Nullable Token t) {
        if (t == null) {
            return Ast.Identifier(new WPos(file, lineOffsets, 1, 0), "");
        }
        return Ast.Identifier(source(t), t.getText());
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

    private String rawText(@Nullable Token c) {
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
        if (stmts != null && stmts.jassStatement() != null) {
            for (JassStatementContext s : stmts.jassStatement()) {
                result.add(transformJassStatement(s));
            }
        }
        return result;
    }

    private WStatement transformJassStatement(JassStatementContext s) {
        try {
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
        } catch (NullPointerException t) {
            WLogger.warning("Error when transforming statement " + line(s), t);
            return Ast.StmtErr(source(s));
        }
        throw error(s, "unhandled case: " + text(s));
    }

    private String line(ParserRuleContext s) {
        if (s == null) {
            return "file " + file;
        }
        Token start = s.start;
        if (start == null) {
            return "file " + file;
        }
        return "file " + file + ", line " + start.getLine();
    }

    private WStatement transformJassStatementSet(JassStatementSetContext s) {
        return Ast.StmtSet(source(s), transformAssignable(s.left), transformExpr(s.right));
    }

    private WStatement transformJassStatementReturn(JassStatementReturnContext s) {
        OptExpr r = transformOptionalExpr(s.expr());
        if (r instanceof ExprEmpty) {
            r = Ast.NoExpr();
        }
        return Ast.StmtReturn(source(s), r);
    }

    private WStatement transformJassStatementLoop(JassStatementLoopContext s) {
        return Ast.StmtLoop(source(s), transformJassStatements(s.jassStatements()));
    }

    private WStatement transformJassStatementIf(JassStatementIfContext s) {
        WStatements thenBlock = transformJassStatements(s.thenStatements);
        WStatements elseBlock = transformJassElseIfs(s.jassElseIfs());
        return Ast.StmtIf(source(s), transformExpr(s.cond), thenBlock, elseBlock);
    }

    private WStatements transformJassElseIfs(JassElseIfsContext s) {
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
        WPos source = source(p);
        Modifiers modifiers = Ast.Modifiers();

        WImports imports = Ast.WImports();

        for (WImportContext i : p.imports) {
            imports.add(transformImport(i));
        }

        WEntities elements = Ast.WEntities();
        for (EntityContext e : p.entities) {
            WEntity en = transformEntity(e);
            if (en != null) {
                elements.add(en);
            }
        }

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
            }

            if (e.exception != null) {
                return null;
            }
            // TODO Auto-generated method stub
            throw error(e, "not implemented " + text(e));
        } catch (NullPointerException npe) {
            WLogger.warning("Error transforming entity in line " + line(e), npe);
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
            return Ast.Annotation(src,
                    Ast.Identifier(source(m.annotation().name), m.annotation().name.getText().substring(1).toLowerCase()),
                    transformArgumentList(m.annotation().argumentList()));
        }
        switch (m.modType.getType()) {
            case WurstParser.PUBLIC:
                return Ast.VisibilityPublic(src);
            case WurstParser.PRIVATE:
                return Ast.VisibilityPrivate(src);
            case WurstParser.PROTECTED:
                return Ast.VisibilityProtected(src);
            case WurstParser.PULBICREAD:
                return Ast.VisibilityPublicread(src);
            case WurstParser.STATIC:
                return Ast.ModStatic(src);
            case WurstParser.OVERRIDE:
                return Ast.ModOverride(src);
            case WurstParser.ABSTRACT:
                return Ast.ModAbstract(src);
            case WurstParser.CONSTANT:
                return Ast.ModConstant(src);
            case WurstParser.VARARG:
                return Ast.ModVararg(src);
        }
        throw error(m, "not implemented");
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
                                                ClassSlotsContext slots) {
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

    private ClassSlot transformClassSlot(ClassSlotContext s) {
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
            } else if (s.classDef() != null) {
                return transformClassDef(s.classDef());
            }
            if (s.exception != null) {
                return null;
            }
            throw error(s, "not matched: " + text(s));
        } catch (NullPointerException npe) {
            WLogger.warning("Error transforming classlot in " + line(s), npe);
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
                c.formalParameters(), true);
        WStatements body = transformStatementList(c.stmts);
        SuperConstructorCall superCall = transformSuperCall(c.superCall());
        return Ast.ConstructorDef(source, modifiers, parameters,
                superCall, body);
    }

    private SuperConstructorCall transformSuperCall(SuperCallContext sc) {
        if (sc == null) {
            return Ast.NoSuperConstructorCall();
        }
        return Ast.SomeSuperConstructorCall(source(sc), source(sc.superKeyword), transformExprs(sc.superArgs));
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
        for (Token m : i.enumMembers) {
            members.add(Ast.EnumMember(source(m), Ast.Modifiers(), text(m)));
        }
        return Ast.EnumDef(src, modifiers, name, members);
    }

    private ClassDef transformClassDef(ClassDefContext i) {
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
        VarInitialization initialExpr = transformVarInit(v.variableInit());
        Identifier name = text(v.name);
        OptTypeExpr optTyp = transformOptionalType(v.varType);
        return Ast.GlobalVarDef(source, modifiers, optTyp, name, initialExpr);
    }

    private VarInitialization transformVarInit(VariableInitContext e) {
        if (e == null) {
            return Ast.NoExpr();
        }
        if (e.arrayInit() != null) {
            return transformArrayInit(e.arrayInit());
        } else {
            return transformExpr(e.initial);
        }
    }

    private VarInitialization transformArrayInit(ArrayInitContext e) {
        return Ast.ArrayInitializer(source(e), transformExprlist(e.exprList()));
    }

    private ExprList transformExprlist(ExprListContext es) {
        return transformExprlist(es.exprs);
    }

    private ExprList transformExprlist(List<ExprContext> es) {
        ExprList result = Ast.ExprList();
        if (es != null) {
            for (ExprContext e : es) {
                result.add(transformExpr(e));
            }
        }
        if (result.size() == 1 && result.get(0) instanceof ExprEmpty) {
            result.clear();
        }
        return result;
    }

    private InitBlock transformInit(InitBlockContext i) {
        return Ast.InitBlock(source(i),
                transformStatements(i.statementsBlock()));
    }

    private WStatements transformStatements(StatementsBlockContext b) {
        WStatements result = Ast.WStatements();
        if (b != null) {
            for (StatementContext s : b.statement()) {
                result.add(transformStatement(s));
            }
        }
        return result;
    }

    private WStatement transformStatement(StatementContext s) {
        WStatement ws = transformStatement2(s);
        if (s.externalLambda() != null) {
            Expr lambda = transformExternalLambda(s.externalLambda());

            Element e = ws;
            while (true) {
                if (e instanceof AstElementWithSource) {
                    AstElementWithSource sourced = (AstElementWithSource) e;
                    sourced.setSource(sourced.getSource().withRightPos(lambda.getSource().getRightPos()));
                }
                if (e instanceof AstElementWithArgs) {
                    // add as last arg in function call
                    AstElementWithArgs fc = (AstElementWithArgs) e;
                    fc.getArgs().add(lambda);
                    return ws;
                }
                if (e instanceof ExprEmpty) {
                    // replace empty right-hand-side of assignment
                    e.replaceBy(lambda);
                    return ws;
                }
                if (e.size() == 0) {
                    break;
                }
                // continue with rightmost expression
                e = e.get(e.size() - 1);
            }
            cuErrorHandler.sendError(new CompileError(source(s.externalLambda()),
                    "External Lambda-block can only be used after function calls."));
        }
        return ws;
    }

    private Expr transformExternalLambda(ExternalLambdaContext el) {
        WShortParameters closureParams = transformShortFormalParameters(el.shortFormalParameters());
        WPos paramSource = source(el.shortFormalParameters());
        WPos source = source(el.statementsBlock()).withLeftPos(paramSource.getLeftPos());
        return Ast.ExprClosure(
                source, source(el.arrow), closureParams,
                Ast.ExprStatementsBlock(source(el.statementsBlock()),
                        transformStatements(el.statementsBlock()))
        );
    }

    private WStatement transformStatement2(StatementContext s) {
        if (s.stmtIf() != null) {
            return transformIf(s.stmtIf());
        } else if (s.stmtWhile() != null) {
            return transformWhile(s.stmtWhile());
        } else if (s.localVarDef() != null) {
            return transformLocalVarDef(s.localVarDef());
        } else if (s.stmtSet() != null) {
            return transformStmtSet(s.stmtSet());
        } else if (s.expr() != null) {
            Expr e = transformExpr(s.expr());
            if (e instanceof WStatement) {
                return (WStatement) e;
            } else {
                cuErrorHandler.sendError(new CompileError(source(s), Utils.printElement(e) + " cannot be used here. A full statement is required."));
                return Ast.StmtErr(source(s));
            }
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
        }

        if (s.exception != null) {
            return Ast.StmtErr(source(s));
        }

        // TODO Auto-generated method stub
        throw error(s, "not implemented: " + text(s) + "\n"
                + s.toStringTree());
    }

    private WStatement transformSwitch(StmtSwitchContext s) {
        Expr expr = transformExpr(s.expr());
        SwitchCases cases = Ast.SwitchCases();
        for (SwitchCaseContext c : s.switchCase()) {
            ExprList e = transformExprlist(c.expr());
            WStatements stmts = transformStatements(c.statementsBlock());
            cases.add(Ast.SwitchCase(source(c), e, stmts));
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
        return Ast.ExprDestroy(source(e), transformExpr(e.expr()));
    }

    private StmtReturn transformReturn(StmtReturnContext s) {
        OptExpr r = transformOptionalExpr(s.expr());
        if (r instanceof ExprEmpty) {
            r = Ast.NoExpr();
        }
        return Ast.StmtReturn(source(s), r);
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
        throw error(s, "not implemented");
    }

    private WurstOperator getAssignOp(Token assignOp) {
        switch (assignOp.getType()) {
            case WurstParser.PLUS_EQ:
                return WurstOperator.PLUS;
            case WurstParser.MINUS_EQ:
                return WurstOperator.MINUS;
            case WurstParser.MULT_EQ:
                return WurstOperator.MULT;
            case WurstParser.DIV_EQ:
                return WurstOperator.DIV_REAL;
            case WurstParser.EQ:
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
        throw error(e, "not implemented: " + text(e));
    }

    private NameRef transformExprVarAccess(ExprVarAccessContext e) {
        if (e.indexes() == null) {
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
                                                  ExprContext e_expr, Token e_dots, Token e_varname,
                                                  @Nullable IndexesContext e_indexes) {
        Expr left = transformExpr(e_expr);
        Identifier varName = text(e_varname);
        if (e_indexes != null) {
            Indexes indexes = transformIndexes(e_indexes);
            if (e_dots.getType() == WurstParser.DOT) {
                return Ast
                        .ExprMemberArrayVarDot(source, left, varName, indexes);
            } else {
                return Ast.ExprMemberArrayVarDotDot(source, left, varName,
                        indexes);
            }
        } else {
            if (e_dots.getType() == WurstParser.DOT) {
                return Ast.ExprMemberVarDot(source, left, varName);
            } else {
                return Ast.ExprMemberVarDotDot(source, left, varName);
            }
        }
    }


    private WStatement transformForLoop(StmtForLoopContext s) {
        if (s.forRangeLoop() != null) {
            return transformForRangeLoop(s.forRangeLoop());
        } else if (s.forIteratorLoop() != null) {
            return transformForIteratorLoop(s.forIteratorLoop());
        } else {
            return Ast.StmtErr(source(s));
        }
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
        if (s.direction.getType() == WurstParser.TO) {
            return Ast.StmtForRangeUp(source, loopVar, to, step, body);
        } else if (s.direction.getType() == WurstParser.DOWNTO) {
            return Ast.StmtForRangeDown(source, loopVar, to, step, body);
        }
        throw error(s, "not implemented: " + text(s));
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
        if (s.iterStyle.getType() == WurstParser.IN) {
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
        VarInitialization initialExpr = transformVarInit(l.variableInit());
        return Ast.LocalVarDef(source(l), modifiers, optTyp, name, initialExpr);
    }

    private OptExpr transformOptionalExpr(ExprContext e) {
        if (e == null) {
            return Ast.NoExpr();
        }
        Expr r = transformExpr(e);
//		if (r instanceof ExprEmpty) {
//			return Ast.NoExpr();
//		}
        return r;
    }

    private ExprNewObject transformExprNewObject(ExprNewObjectContext e) {
        Identifier typeName = text(e.className);
        TypeExprList typeArgs = transformTypeArgs(e.typeArgs());
        Arguments args = transformArgumentList(e.argumentList());
        return Ast.ExprNewObject(source(e), typeName, typeArgs, args);
    }

    private Arguments transformArgumentList(ArgumentListContext al) {
        if (al == null) {
            return Ast.Arguments();
        }
        return transformExprs(al.exprList());
    }


    private ExprMemberMethod transformMemberMethodCall2(WPos source,
                                                        ExprContext receiver, Token dots, Token funcName,
                                                        TypeArgsContext typeArgs, ArgumentListContext args) {
        Expr left = transformExpr(receiver);
        if (dots.getType() == WurstParser.DOT) {
            return Ast.ExprMemberMethodDot(source, left, text(funcName),
                    transformTypeArgs(typeArgs), transformArgumentList(args));
        } else {
            return Ast.ExprMemberMethodDotDot(source, left, text(funcName),
                    transformTypeArgs(typeArgs), transformArgumentList(args));
        }

    }

    private ExprFunctionCall transformFunctionCall(ExprFunctionCallContext c) {
        return Ast.ExprFunctionCall(source(c), text(c.funcName),
                transformTypeArgs(c.typeArgs()), transformArgumentList(c.argumentList()));
    }

    private Arguments transformExprs(ExprListContext es) {
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
        WStatements elseBlock;
        if (i.elseStatements() != null) {
            if (i.elseStatements().stmtIf() != null) {
                elseBlock = Ast.WStatements(transformIf(i.elseStatements()
                        .stmtIf()));
            } else {
                elseBlock = transformStatements(i.elseStatements()
                        .statementsBlock());
            }
        } else {
            elseBlock = Ast.WStatements();
        }
        return Ast.StmtIf(source(i), cond, thenBlock, elseBlock);
    }

    private Expr transformExpr(ExprContext e) {
        try {
            WPos source = source(e);
            if (e.exprPrimary() != null) {
                return transformExprPrimary(e.exprPrimary());
            } else if (e.left != null && e.right != null && e.op != null) {
                return Ast.ExprBinary(source, transformExpr(e.left),
                        transformOp(e.op), transformExpr(e.right));
            } else if (e.op != null && e.op.getType() == WurstParser.NOT) {
                return Ast.ExprUnary(source, WurstOperator.NOT,
                        transformExpr(e.right));
            } else if (e.op != null && e.op.getType() == WurstParser.MINUS) {
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
                        e.funcName, e.typeArgs(), e.argumentList());
            } else if (e.instaneofType != null) {
                return Ast.ExprInstanceOf(source, transformTypeExpr(e.instaneofType),
                        transformExpr(e.left));
            } else if (e.cond != null) {
                return Ast.ExprIfElse(source, transformExpr(e.cond), transformExpr(e.ifTrueExpr), transformExpr(e.ifFalseExpr));
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
        } catch (NullPointerException t) {
            WLogger.warning("Error transforming expression in line " + line(e), t);
            return Ast.ExprIncomplete(source(e), "Incomplete expression.");
        }

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

    private ParseTree getLeftParseTree(ParserRuleContext e) {
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

    private ParseTree getRightParseTree(ParserRuleContext e) {
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
            case WurstParser.OR:
                return WurstOperator.OR;
            case WurstParser.AND:
                return WurstOperator.AND;
            case WurstParser.EQEQ:
                return WurstOperator.EQ;
            case WurstParser.NOT_EQ:
                return WurstOperator.NOTEQ;
            case WurstParser.LESS_EQ:
                return WurstOperator.LESS_EQ;
            case WurstParser.LESS:
                return WurstOperator.LESS;
            case WurstParser.GREATER_EQ:
                return WurstOperator.GREATER_EQ;
            case WurstParser.GREATER:
                return WurstOperator.GREATER;
            case WurstParser.PLUS:
                return WurstOperator.PLUS;
            case WurstParser.MINUS:
                return WurstOperator.MINUS;
            case WurstParser.MULT:
                return WurstOperator.MULT;
            case WurstParser.DIV_REAL:
                return WurstOperator.DIV_REAL;
            case WurstParser.DIV:
                return WurstOperator.DIV_INT;
            case WurstParser.MOD_REAL:
                return WurstOperator.MOD_REAL;
            case WurstParser.MOD:
                return WurstOperator.MOD_INT;
            case WurstParser.NOT:
                return WurstOperator.NOT;
        }
        throw error(source(op), "not implemented: " + text(op));
    }

    private Expr transformExprPrimary(ExprPrimaryContext e) {
        if (e.atom != null) {
            return transformAtom(e.atom);
        } else if (e.varname != null) {
            if (e.indexes() != null) {
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
        } else if (e.exprDestroy() != null) {
            return transformExprDestroy(e.exprDestroy());
        }
        // TODO Auto-generated method stub
        throw error(e, "not implemented " + text(e));
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
        WShortParameters parameters = transformShortFormalParameters(
                e.shortFormalParameters());
        Expr implementation;
        if (e.expr() != null) {
            implementation = transformExpr(e.expr());
        } else if (e.skip != null) {
            implementation = Ast.ExprStatementsBlock(source(e.skip), Ast.WStatements(
                    Ast.StmtSkip(source(e.skip))
            ));
        } else {
            throw new RuntimeException("not implemented: " + text(e));
        }
        return Ast.ExprClosure(source(e), source(e.arrow), parameters, implementation);
    }

    private Indexes transformIndexes(IndexesContext indexes) {
        Indexes result = Ast.Indexes();
        result.add(transformExpr(indexes.expr()));
        return result;
    }

    private Expr transformAtom(Token a) {
        WPos source = source(a);
        switch (a.getType()) {
            case WurstParser.INT:
                return Ast.ExprIntVal(source, rawText(a));
            case WurstParser.REAL:
                return Ast.ExprRealVal(source, rawText(a));
            case WurstParser.STRING:
                return Ast.ExprStringVal(source, getStringVal(source, rawText(a)));
            case WurstParser.NULL:
                return Ast.ExprNull(source);
            case WurstParser.TRUE:
                return Ast.ExprBoolVal(source, true);
            case WurstParser.FALSE:
                return Ast.ExprBoolVal(source, false);
            case WurstParser.THIS:
                return Ast.ExprThis(source);
            case WurstParser.SUPER:
                return Ast.ExprSuper(source);
        }
        throw error(source(a), "not implemented: " + text(a));
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
                s.formalParameters(), true);
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

    private OptTypeExpr transformOptionalType(TypeExprContext t) {
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
            String typeName = rawText(t.typeName);
            TypeExprList typeArgs = transformTypeArgs(t.typeArgs());
            return Ast.TypeExprSimple(source(t), scopeType, typeName, typeArgs);
        } else if (t.typeExpr() != null) {
            return Ast
                    .TypeExprArray(source(t), (TypeExpr) scopeType, transformOptionalExpr(t.arraySize));
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

    private TypeExprList transformTypeArgs(TypeArgsContext typeArgs) {
        TypeExprList result = Ast.TypeExprList();
        for (TypeExprContext e : typeArgs.args) {
            result.add(transformTypeExpr(e));
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

    private WShortParameters transformShortFormalParameters(ShortFormalParametersContext ps) {
        if (ps.singleParam != null) {
            return Ast.WShortParameters(
                    Ast.WShortParameter(source(ps.singleParam).artificial(),
                            Ast.Modifiers(Ast.ModConstant(source(ps.singleParam).artificial())),
                            Ast.NoTypeExpr(),
                            text(ps.singleParam)
                            )
            );
        }
        WShortParameters result = Ast.WShortParameters();
        for (ShortFormalParameterContext p : ps.params) {
            result.add(transformShortFormalParameter(p));
        }
        return result;
    }

    private WParameter transformFormalParameter(FormalParameterContext p,
                                                boolean makeConstant) {
        Modifiers modifiers = Ast.Modifiers();
        if(p.vararg != null) {
            modifiers.add(Ast.ModVararg(source(p).artificial()));
        }
        if (makeConstant) {
            modifiers.add(Ast.ModConstant(source(p).artificial()));
        }
        return Ast.WParameter(source(p), modifiers,
                transformTypeExpr(p.typeExpr()), text(p.name));
    }

    private WShortParameter transformShortFormalParameter(ShortFormalParameterContext p) {
        Modifiers modifiers = Ast.Modifiers(Ast.ModConstant(source(p).artificial()));
        return Ast.WShortParameter(source(p), modifiers,
                transformOptionalType(p.typeExpr()), text(p.name));
    }

    private TypeParamDefs transformTypeParams(TypeParamsContext typeParams) {
        TypeParamDefs result = Ast.TypeParamDefs();
        if (typeParams != null && typeParams.params != null) {
            for (TypeParamContext p : typeParams.params) {
                result.add(transformTypeParam(p));
            }
        }
        return result;
    }

    private TypeParamDef transformTypeParam(TypeParamContext p) {
        Modifiers modifiers = Ast.Modifiers();
        TypeParamConstraints typeParamClasses = tranformTypeParamConstraints(p.typeParamConstraints());
        return Ast.TypeParamDef(source(p), modifiers, text(p.name), typeParamClasses);
    }

    private TypeParamConstraints tranformTypeParamConstraints(TypeParamConstraintsContext tc) {
        if (tc == null) {
            return Ast.NoTypeParamConstraints();
        }
        TypeExprList res = Ast.TypeExprList();
        for (TypeExprContext t : tc.constraints) {
            res.add(transformTypeExpr(t));
        }
        return res;
    }

    private WImport transformImport(WImportContext i) {
        // TODO initlater
        return Ast.WImport(source(i), i.isPublic != null,
                i.isInitLater != null, text(i.importedPackage));
    }

    private WPos source(ParserRuleContext p) {
        int stopIndex;
        if (p.stop.getType() == WurstParser.NL) {
            stopIndex = p.stop.getStartIndex() + 1;
            if (p.stop.getText().contains("\r")) {
                stopIndex++;
            }
        } else {
            stopIndex = p.stop.getStopIndex() + 1;
        }
        return new WPos(file, lineOffsets, p.start.getStartIndex(), stopIndex);
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
        public OnDestroyDef onDestroy = null;

    }

}
