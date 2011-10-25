package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.Attributes;
import de.peeeq.wurstscript.gui.ProgressHelper;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
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

	private Attributes attr;
	private CompilationUnit prog;
	private int functionCount;
	private int visitedFunctions;

	public WurstValidator(CompilationUnit prog, Attributes attr) {
		this.prog = prog;
		this.attr = attr;
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
	

	
	@Override public void visit(FuncDef func) {
		super.visit(func);
		visitedFunctions++;
		attr.setProgress(null, ProgressHelper.getValidatorPercent(visitedFunctions, functionCount));
		
		
		if (func.getSignature().getTyp() instanceof TypeExpr) {
			if (!func.getBody().attrDoesReturn()) {
				attr.addError(func.getSource(), "Function " + func.getSignature().getName() + " is missing a return statement.");
			}
		}
	}
	
	@Override public void visit(ExprFunctionCall stmtCall) {
		super.visit(stmtCall);
		// calculating the exprType should reveal all errors:
		stmtCall.attrTyp();
	}
	
	@Override public void visit(ExprMemberMethod stmtCall) {
		super.visit(stmtCall);
		// calculating the exprType should reveal all errors:
		stmtCall.attrTyp();
	}
	
	@Override public void visit(ExprNewObject stmtCall) {
		super.visit(stmtCall);
		// calculating the exprType should reveal all errors:
		stmtCall.attrTyp();
	}
	
	
}
