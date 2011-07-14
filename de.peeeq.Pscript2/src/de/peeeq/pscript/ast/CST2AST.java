//package de.peeeq.pscript.ast;
//
//import static de.peeeq.pscript.ast.pscriptAST.*;
//
//import java.math.BigDecimal;
//import java.util.Collection;
//import java.util.LinkedList;
//import java.util.List;
//
//import katja.common.NE;
//
//import org.eclipse.emf.common.util.EList;
//import org.eclipse.emf.ecore.EObject;
//import org.eclipse.emf.ecore.EStructuralFeature;
//import org.eclipse.emf.ecore.resource.Resource;
//
//import com.google.inject.Inject;
//
//
//import de.peeeq.pscript.pscript.*;
//import de.peeeq.pscript.pscript.util.PscriptSwitch;
//import de.peeeq.pscript.validation.ErrorSink;
//import de.peeeq.pscript.validation.PscriptJavaValidator;
//
///**
// * converts the concrete syntax tree to the abstract syntax tree 
// */
//public class CST2AST {
//	
//	// used to point to a near location when an exception is thrown
//	private EObject lastElement = null;
//	
//	
//	private ErrorSink errorSink;
//	
//	@Inject
//	public CST2AST(ErrorSink errorSink) {
//		this.errorSink = errorSink;
//	}
//	
//	ErrorSink getErrorSink() {
//		return errorSink;
//	}
//	
//	protected void error(EObject source, EStructuralFeature feature, String type, String message) {
//		errorSink.addError(source, feature, type, message);
//		//validator.acceptError(message, source, feature, 0, type);
//		
//		// TODO Auto-generated method stubS
//		System.out.println("Error: " + message);
//		
//	}
//	
//	/**
//	 * Converts the concrete syntax tree given by a resource in an abstract syntax tree
//	 * @param resource
//	 * @return
//	 */
//	public ACompilationUnit convert(Resource resource) {
//		try {
//			EList<EObject> programs = resource.getContents();
//			List<PackageDeclaration> packages = new LinkedList<PackageDeclaration>();
//			for (EObject p : programs) {
//				if (p instanceof Program) {
//					Program program = (Program) p;
//					for (NameDef pack : program.getPackages()) {
//						packages.add((PackageDeclaration) pack);
//					}
//				} else {
//					throw new Error("A resource should only consist of programs, but " + p + " is of type " + p.getClass());
//				}
//			}
//			
//			APackage[] convertPackages = new APackage[packages.size()];
//			int pos = 0;
//			for (PackageDeclaration p : packages) {
//				APackage compiledP = convertPackage(p);
//				convertPackages[pos] = compiledP;
//				pos++;
//			}
//			return ACompilationUnit(convertPackages);
//		} catch (Throwable t) {
//			System.err.println("Unexpected Error while constructing AST: ");
//			t.printStackTrace();
//			if (lastElement != null) {
//				error(lastElement, PscriptPackage.Literals.PROGRAM__PACKAGES, null, "Unexpected error: " + t.getMessage());
//			}
//		}		
//		return ACompilationUnit();
//	}
//
//	public APackage convertPackage(PackageDeclaration p) {
//		setLastElement(p);
//		return APackage(p.getName(), convertImports(p.getImports()), convertPackageElements(p.getElements()));
//	}
//
//	private void setLastElement(EObject obj) {
//		this.lastElement = obj;		
//	}
//
//	/**
//	 * @param elements
//	 * @return
//	 */
//	private AElements convertPackageElements(EList<NameDef> elements) {
//		AElement[] elements2 = new AElement[elements.size()];
//		int pos  = 0;
//		for (NameDef n : elements) {
//			if (n instanceof ClassDef) {
//				elements2[pos] = convertClassDef((ClassDef) n);
//			} else if (n instanceof FuncDef ) {
//				elements2[pos] = convertFuncDef((FuncDef) n);
//			} else if (n instanceof NativeType ) {
//				elements2[pos] = convertNativeType((NativeType) n);
//			} else if (n instanceof de.peeeq.pscript.pscript.VarDef ) {
//				elements2[pos] = convertVarDef((VarDef) n);
//			} else if (n instanceof ParameterDef ) {
//				throw new Error("Parameter on package level - impossible.");
//				// TODO should not be a sublclass...
//			} else {
//				throw new Error("Unknown subtype of NameDef ...");
//			}
//			pos++;
//		}
//		
//		return AElements(elements2);
//	}
//
//	private AVarDef convertVarDef(VarDef n) {
//		setLastElement(n);
//		return AVarDef(n, n.getName(), convertTypeExpr(n.getType()), n.isConstant(), convertExpr(n.getE()));
//	}
//
//	private AExpr convertExpr(Expr e) {
//		setLastElement(e);
//		return new PscriptSwitch<AExpr>() {
//			
//			@Override
//			public AExpr caseExprAssignment(ExprAssignment e) {
//				error(e, PscriptPackage.Literals.EXPR_ASSIGNMENT__OP, null, "Assignment only allowed on top level");
//				return ANoExpr(e);
//			}
//			
//			@Override
//			public AExpr caseExprOr(ExprOr e) {
//				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprAnd(ExprAnd e) {
//				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprEquality(ExprEquality e) {
//				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprComparison(ExprComparison e) {
//				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprAdditive(ExprAdditive e) {
//				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprMult(ExprMult e) {
//				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprSign(ExprSign e) {
//				return APrefix(e, convertPrefixOp(e.getOp()), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprNot(ExprNot e) {
//				return APrefix(e, ANot(), convertExpr(e.getRight()));
//			}
//
//			@Override
//			public AExpr caseExprMember(ExprMember e) {
//				
//				// TODO left side might be package
//				
//				AExpr left = convertExpr(e.getLeft());
//				AExpr right = convertExpr(e.getRight());
//				
//				AExpr result;
//				if (right instanceof AFunctionCall) {
//					// Something like u.kill() where u is left and kill() is the function call on the right
//					// this is represented as a simple function call kill(u) in the ast
//					AFunctionCall call = (AFunctionCall) right;
//					result = AFunctionCall(e, call.name(), call.args().appFront(left));
//				} else if (right instanceof AVariableAccess) {
//					AVariableAccess va = (AVariableAccess) right;
//					result = AFieldAccess(e, left, va.ident());
//				} else {
//					error(e, PscriptPackage.Literals.EXPR_MEMBER__RIGHT, null, "Dot operator can only used with functions or identifiers on right side.");
//					result = ANoExpr(e);
//				}
//				return result;
//			}
//
//			@Override
//			public AExpr caseExprFunctioncall(ExprFunctioncall e) {
//				return AFunctionCall(e, e.getNameVal().getName(), convertExprList(e.getParameters()));
//			}
//
//			@Override
//			public AExpr caseExprIdentifier(ExprIdentifier e) {
//				if (e.getNameVal() instanceof VarDef) {
//					VarDef v = (VarDef) e.getNameVal();
//					return AVariableAccess(e, AIdentifier(e,v.getName()));
//				}
//				if (e.getNameVal() instanceof ParameterDef) {
//					ParameterDef p = (ParameterDef) e.getNameVal();
//					return AVariableAccess(e, AIdentifier(e,p.getName()));
//				}
//				
//				error(e,PscriptPackage.Literals.EXPR_IDENTIFIER__NAME_VAL, null, "Expected link to Variable");
//				return ANoExpr(e);
//			}
//
//			@Override
//			public AExpr caseExprIntVal(ExprIntVal e) {
//				return AIntegerLiteral(e, e.getIntVal());
//			}
//
//			@Override
//			public AExpr caseExprNumVal(ExprNumVal e) {
//				BigDecimal d = new BigDecimal(e.getNumVal());
//				return ARealLiteral(e, d);
//			}
//
//			@Override
//			public AExpr caseExprStrval(ExprStrval e) {
//				return AStringLiteral(e, e.getStrVal());
//			}
//
//			@Override
//			public AExpr caseExprBoolVal(ExprBoolVal e) {
//				if (e.getBoolVal().equals("true")) {
//					return ABooleanLiteral(e, true);
//				} else if (e.getBoolVal().equals("false")) {
//					return ABooleanLiteral(e, false);
//				}
//				throw new Error("Invalid boolean value: " + e.getBoolVal());
//			}
//
//			@Override
//			public AExpr caseExprBuildinFunction(ExprBuildinFunction e) {
//				return ABuildinCall(e, e.getName(), convertExprList(e.getParameters()));
//			}
//
//		}.doSwitch(e);
//	}
//
//	protected AArguments convertExprList(ExprList parameters) {
//		setLastElement(parameters);
//		AExpr[] expressions = new AExpr[parameters.getParams().size()];
//		int pos = 0;
//		for (Expr p : parameters.getParams()) {
//			expressions[pos] = convertExpr(p);
//			pos++;
//		}
//		return AArguments(expressions);
//	}
//
//	protected APrefixOp convertPrefixOp(String op) {
//		if (op.equals("+")) {
//			return APlus();
//		} else if (op.equals("-")) {
//			return AMinus();
//		} else if (op.equals("not")) {
//			return ANot();
//		}
//		throw new Error("unknown prefix op");
//	}
//
//	protected AInfixOp convertOp(String op) {
//		if (op.equals("+")) {
//			return APlus();
//		} else if (op.equals("-")) {
//			return AMinus();
//		} else if (op.equals("*")) {
//			return AMult();
//		} else if (op.equals("/")) {
//			return ADiv();
//		} else if (op.equals("div")) {
//			return ADivInt();
//		} else if (op.equals("mod")) {
//			return AModulo();
//		} else if (op.equals("and")) {
//			return AAnd();
//		} else if (op.equals("or")) {
//			return AOr();
//		} else if (op.equals("%")) {
//			return AModuloReal();
//		} else if (op.equals("==")) {
//			return AEquals();
//		} else if (op.equals("!=")) {
//			return AUnequal();
//		} else if (op.equals("<=")) {
//			return ALtEq();
//		} else if (op.equals("<")) {
//			return ALt();
//		} else if (op.equals(">=")) {
//			return AGtEq();		
//		} else if (op.equals(">")) {
//			return AGt();
//		}
//		throw new Error("unknown infix op");
//	}
//
//	
//
//	private ATypeExpr convertTypeExpr(TypeExpr type) {
//		setLastElement(type);
//		if (type == null) {
//			return ATypeExprInfer();
//		}
//		return ATypeExprSimple(type.getName().getName());
//	}
//
//	private AElement convertNativeType(NativeType n) {
//		setLastElement(n);
//		TypeExpr superType = n.getSuperName();
//		String superName = "";
//		if (superType != null) {
//			superName = superType.getName().getName();
//		} 
//		return ANativeType(n, n.getName(), n.getOrigName(), superName);
//	}
//
//	private AElement convertFuncDef(FuncDef n) {
//		setLastElement(n);
//		
//		AReturnType retType = convertReturnType(n.getType());
//		AFormalParameters params = convertParams(n.getParameters());
//		ABlock body = convertStatements(n.getBody());
//		return AFuncDef(n, n.getName(), retType, params, body);
//	}
//
//	private ABlock convertStatements(Statements body) {
//		setLastElement(body);
//		
//		List<AStatement> statements = new LinkedList<AStatement>();
//		for (Statement s : body.getStatements()) {
//			statements.addAll(convertStatement(s));
//		}
//		return ABlock(statements.toArray(new AStatement[0]));
//	}
//
//	private Collection<? extends AStatement> convertStatement(Statement s) {
//		setLastElement(s);
//		List<AStatement> result = new LinkedList<AStatement>();
//		AStatement r = new PscriptSwitch<AStatement>() {
//			@Override
//			public AStatement caseStmtIf(StmtIf e) {
//				AExpr cond = convertExpr(e.getCond());
//				ABlock thenBlock = convertStatements(e.getThenBlock());
//				ABlock elseBlock = convertStatements(e.getElseBlock());
//				return AIf(e, cond, thenBlock, elseBlock);
//			}
//			@Override
//			public AStatement caseStmtWhile(StmtWhile e) {
//				AExpr cond = convertExpr(e.getCond());
//				AStatement body = convertStatements(e.getBody());
//				return AWhile(e, cond, body);
//			}
//			@Override
//			public AStatement caseVarDef(VarDef e) {
//				
//				AExpr initial = convertExpr(e.getE());
//				ATypeExpr typeExpr = convertTypeExpr(e.getType());
//				return AVarDef(e, e.getName(), typeExpr, e.isConstant(), initial);
//			}
//			@Override
//			public AStatement caseStmtExpr(StmtExpr e) {
//				if (e.getE() instanceof ExprAssignment) {
//					ExprAssignment a = (ExprAssignment) e.getE();
//					if (a.getOp().equals("=")) {
//						return AAssignment(e, convertExpr(a.getLeft()), convertExpr(a.getRight()));
//					}
//					throw new Error("unsupported assignment operator: " + a.getOp());					
//				}
//				return convertExpr(e.getE());
//			}
//			@Override
//			public AStatement caseStmtReturn(StmtReturn e) {
//				if (e.getE() != null) {
//					return AReturn(e, convertExpr(e.getE()));
//				} else {
//					return AVoidReturn(e);
//				}
//			}
//		}.doSwitch(s);
//		if (r!=null) {
//			result.add(r);
//		}
//		return result;
//	}
//
//	private AFormalParameters convertParams(EList<NameDef> parameters) {
//		AFormalParameter[] result = new AFormalParameter[parameters.size()];
//		int pos = 0;
//		for (NameDef p: parameters) {
//			result[pos] = convertFormalParameter((ParameterDef)p);
//			pos++;
//		}
//		return AFormalParameters(result);
//	}
//
//	private AFormalParameter convertFormalParameter(ParameterDef p) {
//		setLastElement(p);
//		return AFormalParameter(p,p.getName(), convertTypeExpr(p.getType()));
//	}
//
//	private AReturnType convertReturnType(TypeExpr type) {
//		setLastElement(type);
//		if (type == null) {
//			return AReturnsNothing();
//		}
//		return convertTypeExpr(type);
//	}
//
//	private AElement convertClassDef(ClassDef n) {
//		setLastElement(n);
//		// TODO convertClassDef
//		throw new Error("Classes not implemented yet.");
//	}
//
//	private AImports convertImports(EList<Import> imports) {
//		AImport[] result = new AImport[imports.size()];
//		int pos = 0;
//		for (Import x: imports) {
//			result[pos] = convertImport(x);
//			pos++;
//		}
//		return AImports(result);
//	}
//
//	private AImport convertImport(Import x) {
//		setLastElement(x);
//		PackageDeclaration p = x.getImportedNamespace();
//		return AImport(x, p.getName());
//	}
//
//	
//		
//	
//}
