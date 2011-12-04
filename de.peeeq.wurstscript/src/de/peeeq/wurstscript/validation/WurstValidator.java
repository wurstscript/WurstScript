package de.peeeq.wurstscript.validation;

import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModStatic;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.VisibilityModifier;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.FuncDefInstance;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.gui.ProgressHelper;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.Utils;

/**
 * this class validates a pscript program
 * 
 * it has visit methods for different elements in the AST and checks whether these are correct
 * 
 * the validation phase might not find all errors, code transformation and optimization phases
 * might detect other errors because they do a more sophisticated analysis of the program
 * 
 * also note that many cases are already caught by the calculation of the attributes
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
	public
	void visit(StmtSet s) {
		super.visit(s);

		PscriptType leftType = s.getLeft().attrTyp();
		PscriptType rightType = s.getRight().attrTyp();

		checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);


	}

	private void checkAssignment(boolean isJassCode, WPos pos, PscriptType leftType, PscriptType rightType) {
		if (!rightType.isSubtypeOf(leftType)) {
			if (isJassCode) {
				if (leftType instanceof PScriptTypeReal && rightType instanceof PScriptTypeInt) {
					// special case: jass allows to assign an integer to a real variable
					return;
				}
			}
			attr.addError(pos, "Cannot assign " + rightType + " to " + leftType);
		}
	}

	@Override
	public void visit(LocalVarDef s) {
		super.visit(s);
		if (s.getInitialExpr() instanceof Expr) {
			Expr initial = (Expr) s.getInitialExpr();
			PscriptType leftType = s.attrTyp();
			PscriptType rightType = initial.attrTyp();

			checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);
		}		
	}


	@Override
	public void visit(GlobalVarDef s) {
		super.visit(s);
		if (s.getInitialExpr() instanceof Expr) {
			Expr initial = (Expr) s.getInitialExpr();
			PscriptType leftType =s.attrTyp();
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


	@Override public void visit(ExtensionFuncDef func) {
		
		
		checkReturn(func);
		
		
	}

	private void checkReturn(FunctionImplementation func) {
		String functionName = func.getSignature().getName();
		if (func.getSignature().getTyp() instanceof TypeExpr) {
			if (!func.getBody().attrDoesReturn()) {
				attr.addError(func.getSource(), "Function " + functionName + " is missing a return statement.");
			}
		}
	}

	@Override public void visit(FuncDef func) {
		super.visit(func);
		visitedFunctions++;
		attr.setProgress(null, ProgressHelper.getValidatorPercent(visitedFunctions, functionCount));

		String functionName = func.getSignature().getName();
		if (func.attrIsAbstract()) {
			if (func.getBody().size() > 0) {
				attr.addError(func.getBody().get(0).getSource(), "The abstract function " + functionName + " must not have any statements.");
			}
		} else { // not abstract
			checkReturn(func);
		}


		if (func.attrNearestClassOrModule() instanceof ModuleDef) {
			// function is in module -> must be private or public
			if (!func.attrIsPrivate() && !func.attrIsPublic()) {
				attr.addError(func.getSource(), "Function " + functionName + " must be declared " +
						"public or private.\n");
			}
		}

	}

	@Override public void visit(ExprFunctionCall stmtCall) {
		// calculating the exprType should reveal all errors:
		stmtCall.attrTyp();
	}

	@Override public void visit(ExprMemberMethod stmtCall) {
		// calculating the exprType should reveal all errors:
		stmtCall.attrTyp();
	}

	@Override public void visit(ExprNewObject stmtCall) {
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
		PscriptType returnType = func.getSignature().getTyp().attrTyp();
		if (s.getObj() instanceof Expr) {
			Expr returned = (Expr) s.getObj();
			if (returnType.isSubtypeOf(PScriptTypeVoid.instance())) {
				attr.addError(s.getSource(), "Cannot return a value from a function which returns nothing");
			} else {
				PscriptType returnedType = returned.attrTyp();
				if (! returnedType.isSubtypeOf(returnType)) {
					attr.addError(s.getSource(), "Cannot return " + returnedType + ", expected expression of type " + returnType);
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
		// calculate all functions to find possible errors
		Multimap<String, FuncDefInstance> functions = classDef.attrAllFunctions();
		for (FuncDefInstance f : functions.values()) {
			FunctionDefinition funcDef = f.getDef();
			if (funcDef.attrIsAbstract()) {
				attr.addError(classDef.getSource(), "The abstract method " + funcDef.getSignature().getName() +
						" must be implemented in class "+ classDef.getName() + ".");
			}
		}
	}

	@Override
	public void visit(ModuleDef moduleDef) {
		// calculate all functions to find possible errors
		moduleDef.attrAllFunctions();
	}

}
