package de.peeeq.pscript.ast;

import static de.peeeq.pscript.ast.pscriptAST.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import katja.common.NE;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;


import de.peeeq.pscript.pscript.*;
import de.peeeq.pscript.pscript.util.PscriptSwitch;

/**
 * converts the concrete syntax tree to the abstract syntax tree 
 */
public class CST2AST {
	
	/**
	 * Converts the concrete syntax tree given by a resource in an abstract syntax tree
	 * @param resource
	 * @return
	 */
	public ACompilationUnit convert(Resource resource) {
		EList<EObject> programs = resource.getContents();
		List<PackageDeclaration> packages = new LinkedList<PackageDeclaration>();
		for (EObject p : programs) {
			if (p instanceof Program) {
				Program program = (Program) p;
				for (PackageDeclaration pack : program.getPackages()) {
					packages.add(pack);
				}
			} else {
				throw new Error("A resource should only consist of programs, but " + p + " is of type " + p.getClass());
			}
		}
		
		APackage[] convertPackages = new APackage[packages.size()];
		int pos = 0;
		for (PackageDeclaration p : packages) {
			APackage compiledP = compilePackage(p);
			convertPackages[pos] = compiledP;
			pos++;
		}
		
		
		return ACompilationUnit(convertPackages);
	}

	private APackage compilePackage(PackageDeclaration p) {
		return APackage(p.getName(), convertImports(p.getImports()), convertPackageElements(p.getElements()));
	}

	/**
	 * @param elements
	 * @return
	 */
	private AElements convertPackageElements(EList<NameDef> elements) {
		AElement[] elements2 = new AElement[elements.size()];
		int pos  = 0;
		for (NameDef n : elements) {
			if (n instanceof ClassDef) {
				elements2[pos] = convertClassDef((ClassDef) n);
			} else if (n instanceof FuncDef ) {
				elements2[pos] = convertFuncDef((FuncDef) n);
			} else if (n instanceof NativeType ) {
				elements2[pos] = convertNativeType((NativeType) n);
			} else if (n instanceof de.peeeq.pscript.pscript.VarDef ) {
				elements2[pos] = convertVarDef((VarDef) n);
			} else if (n instanceof ParameterDef ) {
				throw new Error("Parameter on package level - impossible.");
				// TODO should not be a sublclass...
			} else {
				throw new Error("Unknown subtype of NameDef ...");
			}
			pos++;
		}
		
		return AElements(elements2);
	}

	private AVarDef convertVarDef(VarDef n) {
		return AVarDef(n, n.getName(), convertTypeExpr(n.getType()), n.isConstant(), convertExpr(n.getE()));
	}

	private AExpr convertExpr(Expr e) {
		return new PscriptSwitch<AExpr>() {
			
			@Override
			public AExpr caseExprAssignment(ExprAssignment e) {
				error(e, "Assignment only allowed on top level");
				return ANoExpr(e);
			}
			
			@Override
			public AExpr caseExprOr(ExprOr e) {
				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprAnd(ExprAnd e) {
				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprEquality(ExprEquality e) {
				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprComparison(ExprComparison e) {
				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprAdditive(ExprAdditive e) {
				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprMult(ExprMult e) {
				return AInfix(e, convertExpr(e.getLeft()), convertOp(e.getOp()), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprSign(ExprSign e) {
				return APrefix(e, convertPrefixOp(e.getOp()), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprNot(ExprNot e) {
				return APrefix(e, ANot(), convertExpr(e.getRight()));
			}

			@Override
			public AExpr caseExprMember(ExprMember e) {
				AExpr left = convertExpr(e.getLeft());
				AExpr right = convertExpr(e.getRight());
				
				AExpr result;
				if (right instanceof AFunctionCall) {
					// Something like u.kill() where u is left and kill() is the function call on the right
					// this is represented as a simple function call kill(u) in the ast
					AFunctionCall call = (AFunctionCall) right;
					result = AFunctionCall(e, call.name(), call.args().appFront(left));
				} else if (right instanceof AVariableAccess) {
					AVariableAccess va = (AVariableAccess) right;
					result = AFieldAccess(e, left, va.ident());
				} else {
					error(e, "Dot operator can only used with functions or identifiers on right side.");
					result = ANoExpr(e);
				}
				return result;
			}

			@Override
			public AExpr caseExprFunctioncall(ExprFunctioncall e) {
				return AFunctionCall(e, e.getNameVal().getName(), convertExprList(e.getParameters()));
			}

			@Override
			public AExpr caseExprIdentifier(ExprIdentifier e) {
				if (e.getNameVal() instanceof VarDef) {
					VarDef v = (VarDef) e.getNameVal();
					return AVariableAccess(e, AIdentifier(e,v.getName()));
				}
				error(e, "Expected link to Variable");
				return ANoExpr(e);
			}

			@Override
			public AExpr caseExprIntVal(ExprIntVal e) {
				return AIntegerLiteral(e, e.getIntVal());
			}

			@Override
			public AExpr caseExprNumVal(ExprNumVal e) {
				BigDecimal d = new BigDecimal(e.getNumVal());
				return ARealLiteral(e, d);
			}

			@Override
			public AExpr caseExprStrval(ExprStrval e) {
				return AStringLiteral(e, e.getStrVal());
			}

			@Override
			public AExpr caseExprBoolVal(ExprBoolVal e) {
				if (e.getBoolVal().equals("true")) {
					return ABooleanLiteral(e, true);
				} else if (e.getBoolVal().equals("false")) {
					return ABooleanLiteral(e, false);
				}
				throw new Error("Invalid boolean value: " + e.getBoolVal());
			}

			@Override
			public AExpr caseExprBuildinFunction(ExprBuildinFunction e) {
				return ABuildinCall(e, e.getName(), convertExprList(e.getParameters()));
			}

		}.doSwitch(e);
	}

	protected AArguments convertExprList(ExprList parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	protected APrefixOp convertPrefixOp(String op) {
		// TODO Auto-generated method stub
		return null;
	}

	protected AInfixOp convertOp(String op) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void error(EObject e, String msg) {
		// TODO Auto-generated method stub
		
	}

	private ATypeExpr convertTypeExpr(TypeExpr type) {
		// TODO Auto-generated method stub
		return null;
	}

	private AElement convertNativeType(NativeType n) {
		// TODO Auto-generated method stub
		return null;
	}

	private AElement convertFuncDef(FuncDef n) {
		// TODO Auto-generated method stub
		return null;
	}

	private AElement convertClassDef(ClassDef n) {
		// TODO Auto-generated method stub
		return null;
	}

	private AImports convertImports(EList<Import> imports) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
