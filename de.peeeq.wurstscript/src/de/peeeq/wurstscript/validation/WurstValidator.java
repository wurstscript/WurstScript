package de.peeeq.wurstscript.validation;

import java.util.Map;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprAssignable;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FuncSignature;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModStatic;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.VisibilityModifier;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.gui.ProgressHelper;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.utils.Utils;

/**
 * this class validates a pscript program
 * 
 * it has visit methods for different elements in the AST and checks whether
 * these are correct
 * 
 * the validation phase might not find all errors, code transformation and
 * optimization phases might detect other errors because they do a more
 * sophisticated analysis of the program
 * 
 * also note that many cases are already caught by the calculation of the
 * attributes
 * 
 */
public class WurstValidator extends CompilationUnit.DefaultVisitor {

	private CompilationUnit prog;
	private int functionCount;
	private int visitedFunctions;

	public WurstValidator(CompilationUnit prog) {
		this.prog = prog;
	}

	public void validate() {
		functionCount = countFunctions();
		visitedFunctions = 0;

		prog.accept(this);
	}

	private int countFunctions() {
		final int functionCount[] = new int[1];
		prog.accept(new CompilationUnit.DefaultVisitor() {
			@Override
			public void visit(FuncDef f) {
				functionCount[0]++;
			}
		});
		return functionCount[0];
	}

	@Override
	public void visit(StmtSet s) {
		super.visit(s);

		PscriptType leftType = s.getUpdatedExpr().attrTyp();
		PscriptType rightType = s.getRight().attrTyp();

		checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);
		
		checkIfAssigningToConstant(s.getUpdatedExpr());
		
		

	}

	private void checkIfAssigningToConstant(final ExprAssignable left) {
		left.match(new ExprAssignable.MatcherVoid() {
			
			@Override
			public void case_ExprVarArrayAccess(ExprVarArrayAccess e) {
				
			}
			
			@Override
			public void case_ExprVarAccess(ExprVarAccess e) {
				checkVar(e.attrNameDef());
			}
			
			private void checkVar(NameDef var) {
				if (var instanceof GlobalVarDef) {
					GlobalVarDef g = (GlobalVarDef) var;
					if (g.attrIsConstant()) {
						attr.addError(left.getSource(), "Cannot assign a new value to constant variable " + g.getName());
					}
				}
			}

			@Override
			public void case_ExprMemberVar(ExprMemberVar e) {
				checkVar(e.attrNameDef());
			}
			
			@Override
			public void case_ExprMemberArrayVar(ExprMemberArrayVar e) {
				
			}
		});
	}

	private void checkAssignment(boolean isJassCode, WPos pos, PscriptType leftType, PscriptType rightType) {
		if (!rightType.isSubtypeOf(leftType)) {
			if (isJassCode) {
				if (leftType instanceof PScriptTypeReal && rightType instanceof PScriptTypeInt) {
					// special case: jass allows to assign an integer to a real
					// variable
					return;
				}
			}
			attr.addError(pos, "Cannot assign " + rightType + " to " + leftType);
		}
	}

	@Override
	public void visit(LocalVarDef s) {
		checkVarName(s, false);
		super.visit(s);
		if (s.getInitialExpr() instanceof Expr) {
			Expr initial = (Expr) s.getInitialExpr();
			PscriptType leftType = s.attrTyp();
			PscriptType rightType = initial.attrTyp();

			checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);
		}
	}

	private void checkVarName(VarDef s, boolean isConstant) {
		String varName = s.getName(); 
		if (!Utils.isJassCode(s)) {
			if (!Character.isLowerCase(varName.charAt(0))) {
				if (!varName.matches("[A-Z0-9_]+")) {
					attr.addError(s.getSource(), "Variable names must start with a lower case character. (" + varName + ")" );
				}
			}
		}
	}
	
	@Override
	public void visit(WParameter wParameter) {
		checkVarName(wParameter, false);
	}

	@Override
	public void visit(GlobalVarDef s) {
		checkVarName(s, s.attrIsConstant());
		super.visit(s);
		if (s.getInitialExpr() instanceof Expr) {
			Expr initial = (Expr) s.getInitialExpr();
			PscriptType leftType = s.attrTyp();
			PscriptType rightType = initial.attrTyp();
			checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);
		}
	}

	@Override
	public void visit(StmtIf stmtIf) {
		super.visit(stmtIf);
		PscriptType condType = stmtIf.getCond().attrTyp();
		if (!(condType instanceof PScriptTypeBool)) {
			attr.addError(stmtIf.getCond().getSource(), "If condition must be a boolean but found " + condType);
		}
	}

	@Override
	public void visit(StmtWhile stmtWhile) {
		super.visit(stmtWhile);
		PscriptType condType = stmtWhile.getCond().attrTyp();
		if (!(condType instanceof PScriptTypeBool)) {
			attr.addError(stmtWhile.getCond().getSource(), "While condition must be a boolean but found " + condType);
		}
	}

	@Override
	public void visit(ExtensionFuncDef func) {
		checkFunctionName(func);
		
		
		checkReturn(func);
		UninitializedVars.checkFunc(func.attrDefinedNames().values(), func.getBody());
	}

	private void checkFunctionName(FunctionDefinition f) {
		if (!Utils.isJassCode(f)) {
			if (Character.isUpperCase(f.getName().charAt(0))) {
				attr.addError(f.getSource(), "Function names must start with an lower case character.");
			}
		}
	}

	

	

	

	private void checkReturn(FunctionImplementation func) {
		String functionName = func.getName();
		if (func.getReturnTyp() instanceof TypeExpr) {
			if (!func.getBody().attrDoesReturn()) {
				attr.addError(func.getSource(), "Function " + functionName + " is missing a return statement.");
			}
		}
	}

	@Override
	public void visit(FuncDef func) {
		visitedFunctions++;
		attr.setProgress(null, ProgressHelper.getValidatorPercent(visitedFunctions, functionCount));

		checkFunctionName(func);
		
		String functionName = func.getName();
		if (func.attrIsAbstract()) {
			if (func.getBody().size() > 0) {
				attr.addError(func.getBody().get(0).getSource(), "The abstract function " + functionName
						+ " must not have any statements.");
			}
		} else { // not abstract
			checkReturn(func);
			UninitializedVars.checkFunc(func.attrDefinedNames().values(), func.getBody());
		}

		if (func.attrNearestClassOrModule() instanceof ModuleDef) {
			// function is in module -> must be private or public
			if (!func.attrIsPrivate() && !func.attrIsPublic()) {
				attr.addError(func.getSource(), "Function " + functionName + " must be declared " + "public or private.\n");
			}
		}

	}
	
	@Override
	public void visit(InitBlock initBlock) {
		UninitializedVars.checkFunc(initBlock.attrDefinedNames().values(), initBlock.getBody());
	}
	
	@Override
	public void visit(OnDestroyDef onDestroyDef) {
		UninitializedVars.checkFunc(onDestroyDef.attrDefinedNames().values(), onDestroyDef.getBody());
	}
	
	@Override
	public void visit(ConstructorDef constructorDef) {
		UninitializedVars.checkFunc(constructorDef.attrDefinedNames().values(), constructorDef.getBody());
	}

	@Override
	public void visit(ExprFunctionCall stmtCall) {
		String funcName = stmtCall.getFuncName();
		// calculating the exprType should reveal most errors:
		stmtCall.attrTyp();
		
		FunctionImplementation nearestFunc = stmtCall.attrNearestFuncDef();
		if (stmtCall.attrFuncDef() != null) {
			
			FunctionDefinition calledFunc = stmtCall.attrFuncDef();
			if (calledFunc.attrIsDynamicClassMember()) {
				if (!stmtCall.attrIsDynamicContext()) {
							attr.addError(stmtCall.getSource(), "Cannot call dynamic function " + funcName  +
									" from static function " + nearestFunc.getName());
				}
			}
		}
		
		// special check for filter & condition:
		if (Utils.oneOf(funcName, "Condition", "Filter")) {
			Expr firstArg = stmtCall.getArgs().get(0);
			if (firstArg instanceof ExprFuncRef) {
				ExprFuncRef exprFuncRef = (ExprFuncRef) firstArg;
				FunctionDefinition f = exprFuncRef.attrFuncDef();
				if (f != null) {
					if (f.getParameters().size() > 0) {
						attr.addError(firstArg.getSource(), "Functions passed to Filter or Condition must have no parameters.");
					}
					if (!(f.getReturnTyp().attrTyp() instanceof PScriptTypeBool)) {
						attr.addError(firstArg.getSource(), "Functions passed to Filter or Condition must return boolean.");
					}
				}
			}
		}
	}

	@Override
	public void visit(ExprMemberMethod stmtCall) {
		// calculating the exprType should reveal all errors:
		stmtCall.attrTyp();
	}

	@Override
	public void visit(ExprNewObject stmtCall) {
		stmtCall.attrTyp();
		stmtCall.attrConstructorDef();
	}

	@Override
	public void visit(Modifiers modifiers) {
		boolean hasVis = false;
		boolean isStatic = false;
		for (Modifier m : modifiers) {
			if (m instanceof VisibilityModifier) {
				if (hasVis) {
					attr.addError(m.getSource(), "Each element can only have one visibility modfifier (public, private, ...)");
				}
				hasVis = true;
			} else if (m instanceof ModStatic) {
				if (isStatic) {
					attr.addError(m.getSource(), "double static? - what r u trying to do?");
				}
				isStatic = true;
			}
		}
	}

	@Override
	public void visit(StmtReturn s) {
		FunctionImplementation func = s.attrNearestFuncDef();
		if (func == null) {
			attr.addError(s.getSource(), "return statements can only be used inside functions");
			return;
		}
		PscriptType returnType = func.getReturnTyp().attrTyp();
		if (s.getReturnedObj() instanceof Expr) {
			Expr returned = (Expr) s.getReturnedObj();
			if (returnType.isSubtypeOf(PScriptTypeVoid.instance())) {
				attr.addError(s.getSource(), "Cannot return a value from a function which returns nothing");
			} else {
				PscriptType returnedType = returned.attrTyp();
				if (!returnedType.isSubtypeOf(returnType)) {
					attr.addError(s.getSource(), "Cannot return " + returnedType + ", expected expression of type "
							+ returnType);
				}
			}
		} else { // empty return
			if (!returnType.isSubtypeOf(PScriptTypeVoid.instance())) {
				attr.addError(s.getSource(), "Missing return value");
			}
		}
	}

	@Override
	public void visit(ClassDef classDef) {
		checkTypeName(classDef.getSource(), classDef.getName());
		
		// calculate all functions to find possible errors
		Map<String, FuncDef> functions = classDef.attrAllFunctions();
		
		// check that there are no abstract functions in a class
		for (FunctionDefinition f : functions.values()) {
			if (f.attrIsAbstract()) {
				attr.addError(classDef.getSource(), "The abstract method " + f.getName()
						+ " must be implemented in class " + classDef.getName() + ".");
			}
		}
	}

	private void checkTypeName(WPos source, String name) {
		if (!Character.isUpperCase(name.charAt(0))) {
			attr.addError(source, "Type names must start with upper case characters.");
		}
	}

	@Override
	public void visit(ModuleDef moduleDef) {
		checkTypeName(moduleDef.getSource(), moduleDef.getName());
		// calculate all functions to find possible errors
		moduleDef.attrAllFunctions();
	}
	

	@Override
	public void visit(StmtDestroy stmtDestroy) {
		PscriptType typ = stmtDestroy.getDestroyedObj().attrTyp();
		if (!(typ instanceof PscriptTypeModule || typ instanceof PscriptTypeClass)) {
			attr.addError(stmtDestroy.getSource(), "Cannot destroy objects of type " + typ);
		}
	}
	
	@Override 
	public void visit(ExprVarAccess e) {
		checkVarRef(e, e.attrIsDynamicContext());
	}

	
	
	@Override
	public void visit(WImport wImport) {
		if (wImport.attrImportedPackage() == null) {
			attr.addError(wImport.getSource(), "Could not find imported package " + wImport.getPackagename());
		}
	}

	/**
	 * check if the nameRef e is accessed correctly
	 * i.e. not using a dynamic variable from a static context
	 * @param e
	 * @param dynamicContext
	 */
	private void checkVarRef(NameRef e, boolean dynamicContext) {
		NameDef def = e.attrNameDef();
		if (def instanceof GlobalVarDef) {
			GlobalVarDef g = (GlobalVarDef) def;
			if (g.attrIsDynamicClassMember() && !dynamicContext) {
				attr.addError(e.getSource(), "Cannot reference dynamic variable " +e.getVarName() + " from static context.");
			}
		}
	}
}
