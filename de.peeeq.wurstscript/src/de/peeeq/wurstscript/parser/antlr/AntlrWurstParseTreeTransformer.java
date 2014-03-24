package de.peeeq.wurstscript.parser.antlr;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.antlr.WurstParser.ClassDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.ClassSlotContext;
import de.peeeq.wurstscript.antlr.WurstParser.ClassSlotsContext;
import de.peeeq.wurstscript.antlr.WurstParser.CompilationUnitContext;
import de.peeeq.wurstscript.antlr.WurstParser.ConstructorDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.EntityContext;
import de.peeeq.wurstscript.antlr.WurstParser.EnumDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprAssignableContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprClosureContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprDestroyContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprFuncRefContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprFunctionCallContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprListContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprMemberMethodContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprMemberVarContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprNewObjectContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprPrimaryContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprStatementsBlockContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExprVarAccessContext;
import de.peeeq.wurstscript.antlr.WurstParser.ExtensionFuncDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.ForIteratorLoopContext;
import de.peeeq.wurstscript.antlr.WurstParser.ForRangeLoopContext;
import de.peeeq.wurstscript.antlr.WurstParser.FormalParameterContext;
import de.peeeq.wurstscript.antlr.WurstParser.FormalParametersContext;
import de.peeeq.wurstscript.antlr.WurstParser.FuncDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.FuncSignatureContext;
import de.peeeq.wurstscript.antlr.WurstParser.IndexesContext;
import de.peeeq.wurstscript.antlr.WurstParser.InitBlockContext;
import de.peeeq.wurstscript.antlr.WurstParser.InterfaceDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.LocalVarDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.LocalVarDefInlineContext;
import de.peeeq.wurstscript.antlr.WurstParser.ModifierContext;
import de.peeeq.wurstscript.antlr.WurstParser.ModifiersWithDocContext;
import de.peeeq.wurstscript.antlr.WurstParser.ModuleDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.ModuleUseContext;
import de.peeeq.wurstscript.antlr.WurstParser.NativeDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.NativeTypeContext;
import de.peeeq.wurstscript.antlr.WurstParser.OndestroyDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.StatementContext;
import de.peeeq.wurstscript.antlr.WurstParser.StatementsBlockContext;
import de.peeeq.wurstscript.antlr.WurstParser.StmtCallContext;
import de.peeeq.wurstscript.antlr.WurstParser.StmtForLoopContext;
import de.peeeq.wurstscript.antlr.WurstParser.StmtIfContext;
import de.peeeq.wurstscript.antlr.WurstParser.StmtReturnContext;
import de.peeeq.wurstscript.antlr.WurstParser.StmtSetContext;
import de.peeeq.wurstscript.antlr.WurstParser.StmtWhileContext;
import de.peeeq.wurstscript.antlr.WurstParser.TupleDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.TypeArgsContext;
import de.peeeq.wurstscript.antlr.WurstParser.TypeExprContext;
import de.peeeq.wurstscript.antlr.WurstParser.TypeParamContext;
import de.peeeq.wurstscript.antlr.WurstParser.TypeParamsContext;
import de.peeeq.wurstscript.antlr.WurstParser.VarDefContext;
import de.peeeq.wurstscript.antlr.WurstParser.WImportContext;
import de.peeeq.wurstscript.antlr.WurstParser.WpackageContext;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ConstructorDefs;
import de.peeeq.wurstscript.ast.EnumMembers;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprDestroy;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarDot;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprStatementsBlock;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FuncDefs;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.GlobalVarDefs;
import de.peeeq.wurstscript.ast.Indexes;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassToplevelDeclarations;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.ModuleInstanciations;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.ModuleUses;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprList;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.ast.WEntities;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WImports;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WPackages;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;

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
			packages.add(transformPackage(cu.wpackage()));
		} catch (NullPointerException e) {
			e.printStackTrace();
			// ignore
		}

		return Ast.CompilationUnit("", this.cuErrorHandler, jassDecls,
				packages);
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

		String name = "UnknownName";
		if (p.name != null) {
			name = p.name.getText();
		}
		return Ast.WPackage(source, modifiers, name, imports, elements);
	}

	private WEntity transformEntity(EntityContext e) {
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
			throw new Error("not implemented " + e.getText());
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
			result.add(Ast.WurstDoc(source(ms.hotdocComment()), ms.hotdocComment().getText()));
		}
		for (ModifierContext m : ms.modifiers) {
			result.add(transformModifier(m));
		}
		return result;
	}

	private Modifier transformModifier(ModifierContext m) {
		WPos src = source(m);
		if (m.annotation() != null) {
			return Ast.Annotation(src, m.annotation().getText());
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
		}
		throw new Error("not implemented");
	}

	private WEntity transformTupleDef(TupleDefContext t) {

		WPos src = source(t);
		Modifiers modifiers = transformModifiers(t.modifiersWithDoc());
		String name = t.name.getText();
		WParameters parameters = transformFormalParameters(t.formalParameters(), false);
		OptTypeExpr returnTyp = Ast.NoTypeExpr();
		return Ast.TupleDef(src, modifiers, name, parameters, returnTyp);
	}

	private WEntity transformInterfaceDef(InterfaceDefContext i) {
		WPos src = source(i);
		Modifiers modifiers = transformModifiers(i.modifiersWithDoc());
		String name = i.name.getText();
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
				} else if (s != null) {
					throw new Error("unexpected classslot: "
							+ s.getClass().getSimpleName());
				}
			}
		}
		if (result.onDestroy == null) {
			result.onDestroy = Ast.OnDestroyDef(src, Ast.WStatements());
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
			}
			if (s.exception != null) {
				return null;
			}
			throw new Error("not matched: " + s.getText());
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
		return Ast.ModuleUse(source(u), u.moduleName.getText(),
				transformTypeArgs(u.typeArgs()));
	}

	private ConstructorDef transformConstructorDef(ConstructorDefContext c) {
		WPos source = source(c);
		Modifiers modifiers = transformModifiers(c.modifiersWithDoc());
		WParameters parameters = transformFormalParameters(c.formalParameters(), true);
		WStatements body = transformStatementList(c.stmts);
		boolean isExplicit = c.superArgs != null;
		Arguments superArgs = transformExprs(c.superArgs);
		return Ast.ConstructorDef(source, modifiers, parameters, isExplicit,
				superArgs, body);
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
		String name = i.name.getText();
		TypeParamDefs typeParameters = transformTypeParams(i.typeParams());
		ClassSlotResult slots = transformClassSlots(src, i.classSlots());
		return Ast.ModuleDef(src, modifiers, name, typeParameters,
				slots.methods, slots.vars, slots.constructors,
				slots.moduleInstanciations, slots.moduleUses, slots.onDestroy);
	}

	private WEntity transformEnumDef(EnumDefContext i) {
		WPos src = source(i);
		Modifiers modifiers = transformModifiers(i.modifiersWithDoc());
		String name = i.name.getText();
		EnumMembers members = Ast.EnumMembers();
		for (Token m : i.enumMembers) {
			members.add(Ast.EnumMember(source(m), Ast.Modifiers(), m.getText()));
		}
		return Ast.EnumDef(src, modifiers, name, members);
	}

	private WEntity transformClassDef(ClassDefContext i) {
		WPos src = source(i);
		Modifiers modifiers = transformModifiers(i.modifiersWithDoc());
		String name = i.name.getText();
		TypeParamDefs typeParameters = transformTypeParams(i.typeParams());
		OptTypeExpr extendedClass = transformOptionalType(i.extended);
		TypeExprList implementsList = Ast.TypeExprList();
		for (TypeExprContext ex : i.implemented) {
			implementsList.add(transformTypeExpr(ex));
		}
		ClassSlotResult slots = transformClassSlots(src, i.classSlots());
		return Ast.ClassDef(src, modifiers, name, typeParameters,
				extendedClass, implementsList, slots.methods, slots.vars,
				slots.constructors, slots.moduleInstanciations,
				slots.moduleUses, slots.onDestroy);
	}

	private NativeType transformNativeType(NativeTypeContext n) {
		OptTypeExpr extended;
		if (n.extended != null) {
			extended = Ast.TypeExprSimple(source(n.extended), n.extended.getText(), Ast.TypeExprList());
		} else {
			extended = Ast.NoTypeExpr();
		}
		return Ast.NativeType(source(n), Ast.Modifiers(), n.name.getText(), extended);
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
		String name = v.name.getText();
		OptTypeExpr optTyp = transformOptionalType(v.varType);
		return Ast.GlobalVarDef(source, modifiers, optTyp, name, initialExpr);
	}

	private InitBlock transformInit(InitBlockContext i) {
		return Ast.InitBlock(source(i),
				transformStatements(i.statementsBlock()));
	}

	private WStatements transformStatements(StatementsBlockContext b) {
		WStatements result = Ast.WStatements();
		for (StatementContext s : b.statement()) {
			result.add(transformStatement(s));
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
		} else if (s.exprDestroy() != null) {
			return transformExprDestroy(s.exprDestroy());
		} else if (s.stmtForLoop() != null) {
			return transformForLoop(s.stmtForLoop());
		} else if (s.stmtBreak() != null) {
			return Ast
					.StmtExitwhen(source(s), Ast.ExprBoolVal(source(s), true));
		} else if (s.stmtSkip() != null) {
			return Ast.StmtSkip(source(s));
		} else if (s.stmtSwitch() != null) {
			throw new Error("not implemented: " + s.getText() + "\n"
					+ s.toStringTree());
		}

		if (s.exception != null) {
			return Ast.StmtErr(source(s));
		}

		// TODO Auto-generated method stub
		throw new Error("not implemented: " + s.getText() + "\n"
				+ s.toStringTree());
	}

	private WStatement transformWhile(StmtWhileContext s) {
		return Ast.StmtWhile(source(s), transformExpr(s.cond),
				transformStatements(s.statementsBlock()));
	}

	private ExprDestroy transformExprDestroy(ExprDestroyContext e) {
		return Ast.ExprDestroy(source(e), transformExpr(e.expr()));
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
		throw new Error("not implemented");
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
			throw new Error("unhandled assign op: " + assignOp.getText());
		}
	}

	private NameRef transformAssignable(ExprAssignableContext e) {
		if (e.exprMemberVar() != null) {
			return transformExprMemberVar(e.exprMemberVar());
		} else if (e.exprVarAccess() != null) {
			return transformExprVarAccess(e.exprVarAccess());
		}
		throw new Error("not implemented: " + e.getText());
	}

	private NameRef transformExprVarAccess(ExprVarAccessContext e) {
		if (e.indexes() == null) {
			return Ast.ExprVarAccess(source(e), e.varname.getText());
		} else {
			return Ast.ExprVarArrayAccess(source(e), e.varname.getText(),
					transformIndexes(e.indexes()));
		}
	}

	private NameRef transformExprMemberVar(ExprMemberVarContext e) {
		return transformExprMemberVarAccess2(source(e), e.expr(), e.dots,
				e.varname, e.indexes());
	}

	private NameRef transformExprMemberVarAccess2(WPos source,
			ExprContext e_expr, Token e_dots, Token e_varname,
			IndexesContext e_indexes) {
		Expr left = transformExpr(e_expr);
		String varName = e_varname.getText();
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
		}
		throw new Error("not implemented: " + s.getText());
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
		throw new Error("not implemented: " + s.getText());
	}

	private LocalVarDef transformLocalVarDef(LocalVarDefInlineContext v) {
		Modifiers modifiers = Ast.Modifiers();
		OptTypeExpr optTyp = transformOptionalType(v.typeExpr());
		String name = v.name.getText();
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
		String name = l.name.getText();
		OptExpr initialExpr = transformOptionalExpr(l.initial);
		return Ast.LocalVarDef(source(l), modifiers, optTyp, name, initialExpr);
	}

	private OptExpr transformOptionalExpr(ExprContext e) {
		if (e == null) {
			return Ast.NoExpr();
		}
		return transformExpr(e);
	}

	private WStatement transformCall(StmtCallContext c) {
		if (c.exprFunctionCall() != null) {
			return transformFunctionCall(c.exprFunctionCall());
		} else if (c.exprMemberMethod() != null) {
			return transformMemberMethodCall(c.exprMemberMethod());
		} else if (c.exprNewObject() != null) {
			return transformExprNewObject(c.exprNewObject());
		}
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	private ExprNewObject transformExprNewObject(ExprNewObjectContext e) {
		String typeName = e.className.getText();
		TypeExprList typeArgs = transformTypeArgs(e.typeArgs());
		Arguments args = transformExprs(e.exprList());
		return Ast.ExprNewObject(source(e), typeName, typeArgs, args);
	}

	private WStatement transformMemberMethodCall(ExprMemberMethodContext e) {
		WPos source = source(e);
		ExprContext receiver = e.receiver;
		Token dots = e.dots;
		Token funcName = e.funcName;
		TypeArgsContext typeArgs = e.typeArgs();
		ExprListContext args = e.exprList();
		return transformMemberMethodCall2(source, receiver, dots, funcName,
				typeArgs, args);
	}

	private ExprMemberMethod transformMemberMethodCall2(WPos source,
			ExprContext receiver, Token dots, Token funcName,
			TypeArgsContext typeArgs, ExprListContext args) {
		Expr left = transformExpr(receiver);
		if (dots.getType() == WurstParser.DOT) {
			return Ast.ExprMemberMethodDot(source, left, funcName.getText(),
					transformTypeArgs(typeArgs), transformExprs(args));
		} else {
			return Ast.ExprMemberMethodDotDot(source, left, funcName.getText(),
					transformTypeArgs(typeArgs), transformExprs(args));
		}

	}

	private ExprFunctionCall transformFunctionCall(ExprFunctionCallContext c) {
		return Ast.ExprFunctionCall(source(c), c.funcName.getText(),
				transformTypeArgs(c.typeArgs()), transformExprs(c.exprList()));
	}

	private Arguments transformExprs(ExprListContext es) {
		Arguments result = Ast.Arguments();
		if (es != null) {
			for (ExprContext e : es.exprs) {
				result.add(transformExpr(e));
			}
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
		if (e.exprPrimary() != null) {
			return transformExprPrimary(e.exprPrimary());
		} else if (e.left != null && e.right != null && e.op != null) {
			return Ast.ExprBinary(source(e), transformExpr(e.left),
					transformOp(e.op), transformExpr(e.right));
		} else if (e.op != null && e.op.getType() == WurstParser.NOT) {
			return Ast.ExprUnary(source(e), WurstOperator.NOT,
					transformExpr(e.right));
		} else if (e.op != null && e.op.getType() == WurstParser.MINUS) {
			return Ast.ExprUnary(source(e), WurstOperator.UNARY_MINUS,
					transformExpr(e.right));
		} else if (e.castToType != null) {
			return Ast.ExprCast(source(e), transformTypeExpr(e.castToType),
					transformExpr(e.left));
		} else if (e.varName != null) {
			return transformExprMemberVarAccess2(source(e), e.receiver, e.dots,
					e.varName, e.indexes());
		} else if (e.funcName != null) {
			return transformMemberMethodCall2(source(e), e.receiver, e.dots,
					e.funcName, e.typeArgs(), e.exprList());
		}

		if (e.exception != null) {
			return Ast.ExprNull(source(e));
		}
		// TODO Auto-generated method stub
		throw new Error("not implemented: " + e.getText());
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
		throw new Error("not implemented: " + op.getText());
	}

	private Expr transformExprPrimary(ExprPrimaryContext e) {
		if (e.atom != null) {
			return transformAtom(e.atom);
		} else if (e.varname != null) {
			if (e.indexes() != null) {
				Indexes index = transformIndexes(e.indexes());
				return Ast.ExprVarArrayAccess(source(e), e.varname.getText(),
						index);
			} else {
				return Ast.ExprVarAccess(source(e), e.varname.getText());
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
		throw new Error("not implemented " + e.getText());
	}

	private ExprFuncRef transformExprFuncRef(ExprFuncRefContext e) {
		String scopeName = e.scopeName == null ? "" : e.scopeName.getText();
		String funcName = e.funcName.getText();
		return Ast.ExprFuncRef(source(e), scopeName, funcName);
	}

	private ExprStatementsBlock transformExprStatementsBlock(
			ExprStatementsBlockContext e) {
		return Ast.ExprStatementsBlock(source(e), transformStatements(e.statementsBlock()));
	}

	private ExprClosure transformClosure(ExprClosureContext e) {
		WParameters parameters = transformFormalParameters(e.formalParameters(), true);
		Expr implementation = transformExpr(e.expr());
		return Ast.ExprClosure(source(e), parameters, implementation);
	}

	private Indexes transformIndexes(IndexesContext indexes) {
		Indexes result = Ast.Indexes();
		result.add(transformExpr(indexes.expr()));
		return result;
	}

	private Expr transformAtom(Token a) {
		WPos source = source(a);
		if (a.getType() == WurstParser.INT) {
			return Ast.ExprIntVal(source, a.getText());
		} else if (a.getType() == WurstParser.REAL) {
			return Ast.ExprRealVal(source, a.getText());
		} else if (a.getType() == WurstParser.STRING) {
			return Ast.ExprStringVal(source, getStringVal(source, a.getText()));
		} else if (a.getType() == WurstParser.NULL) {
			return Ast.ExprNull(source);
		} else if (a.getType() == WurstParser.TRUE) {
			return Ast.ExprBoolVal(source, true);
		} else if (a.getType() == WurstParser.FALSE) {
			return Ast.ExprBoolVal(source, false);
		} else if (a.getType() == WurstParser.THIS) {
			return Ast.ExprThis(source);
		} else if (a.getType() == WurstParser.SUPER) {
			return Ast.ExprSuper(source);
		}
		// TODO Auto-generated method stub
		throw new Error("not implemented: " + a.getText());
	}

	private String getStringVal(WPos source, String text) {
		StringBuilder res = new StringBuilder();
		for (int i=1; i<text.length()-1; i++) {
			char c = text.charAt(i);
			if (c == '\\') {
				i++;
				switch (text.charAt(i)) {
				case '\\': res.append('\\'); break;
				case 'n': res.append('\n'); break;
				case 'r': res.append('\r'); break;
				case 't': res.append('\t'); break;
				case '"': res.append('"'); break;
				case '\'': res.append('\''); break;
				default: 
					throw new CompileError(source, "Invalid escape sequence: " + text.charAt(i));
				}
			} else {
				res.append(c);
			}
		}
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
		WParameters formalParameters = transformFormalParameters(s.formalParameters(), true);
		OptTypeExpr returnType = transformOptionalType(s.returnType);
		return new FuncSig(s.name.getText(), typeParams, formalParameters,
				returnType);
	}

	private OptTypeExpr transformOptionalType(TypeExprContext t) {
		if (t == null) {
			return Ast.NoTypeExpr();
		}
		return transformTypeExpr(t);
	}

	private TypeExpr transformTypeExpr(TypeExprContext t) throws Error {
		if (t.thistype != null) {
			return Ast.TypeExprThis(source(t));
		} else if (t.typeName != null) {
			return Ast.TypeExprSimple(source(t), t.typeName.getText(),
					transformTypeArgs(t.typeArgs()));
		} else if (t.typeExpr() != null) {
			return Ast
					.TypeExprArray(source(t), transformTypeExpr(t.typeExpr()));
		}
		throw new Error("not implemented " + t.toStringTree());
	}

	private TypeExprList transformTypeArgs(TypeArgsContext typeArgs) {
		TypeExprList result = Ast.TypeExprList();
		for (TypeExprContext e : typeArgs.args) {
			result.add(transformTypeExpr(e));
		}
		return result;
	}

	private WParameters transformFormalParameters(FormalParametersContext ps, boolean makeConstant) {
		WParameters result = Ast.WParameters();
		for (FormalParameterContext p : ps.params) {
			result.add(transformFormalParameter(p, makeConstant));
		}
		return result;
	}

	private WParameter transformFormalParameter(FormalParameterContext p, boolean makeConstant) {
		Modifiers modifiers = Ast.Modifiers();
		if (makeConstant) {
			modifiers.add(Ast.ModConstant(source(p)));
		}
		return Ast.WParameter(source(p), modifiers,
				transformTypeExpr(p.typeExpr()), p.name.getText());
	}

	private TypeParamDefs transformTypeParams(TypeParamsContext typeParams) {
		TypeParamDefs result = Ast.TypeParamDefs();
		for (TypeParamContext p : typeParams.params) {
			result.add(transformTypeParam(p));
		}
		return result;
	}

	private TypeParamDef transformTypeParam(TypeParamContext p) {
		Modifiers modifiers = Ast.Modifiers();
		return Ast.TypeParamDef(source(p), modifiers, p.name.getText());
	}

	private WImport transformImport(WImportContext i) {
		// TODO initlater
		return Ast.WImport(source(i), i.isPublic != null,
				i.isInitLater != null, i.importedPackage.getText());
	}

	private WPos source(ParserRuleContext p) {
		System.out.println("p = " + p);
		System.out.println(p.start);
		System.out.println(p.stop);
		return new WPos(file, lineOffsets, p.start.getStartIndex(),
				p.stop.getStopIndex());
	}

	private WPos source(Token p) {
		return new WPos(file, lineOffsets, p.getStartIndex(), p.getStopIndex());
	}

	class FuncSig {
		String name;
		TypeParamDefs typeParams;
		WParameters formalParameters;
		OptTypeExpr returnType;

		public FuncSig(String name, TypeParamDefs typeParams,
				WParameters formalParameters, OptTypeExpr returnType) {
			this.name = name;
			this.typeParams = typeParams;
			this.formalParameters = formalParameters;
			this.returnType = returnType;
		}

	}

	class ClassSlotResult {

		public ConstructorDefs constructors = Ast.ConstructorDefs();
		public ModuleInstanciations moduleInstanciations = Ast
				.ModuleInstanciations();
		public GlobalVarDefs vars = Ast.GlobalVarDefs();
		public FuncDefs methods = Ast.FuncDefs();
		public ModuleUses moduleUses = Ast.ModuleUses();
		public OnDestroyDef onDestroy = null;

	}

}
