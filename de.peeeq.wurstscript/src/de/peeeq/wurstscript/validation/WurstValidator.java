package de.peeeq.wurstscript.validation;

import katja.common.NE;
import de.peeeq.wurstscript.ast.CompilationUnitPos;
import de.peeeq.wurstscript.ast.ExprFunctionCallPos;
import de.peeeq.wurstscript.ast.ExprMemberMethodPos;
import de.peeeq.wurstscript.ast.ExprNewObjectPos;
import de.peeeq.wurstscript.ast.ExprPos;
import de.peeeq.wurstscript.ast.FuncDefPos;
import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.LocalVarDefPos;
import de.peeeq.wurstscript.ast.StmtIfPos;
import de.peeeq.wurstscript.ast.StmtSetPos;
import de.peeeq.wurstscript.ast.StmtWhilePos;
import de.peeeq.wurstscript.ast.TypeExprPos;
import de.peeeq.wurstscript.ast.WPosPos;
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
public class WurstValidator extends CompilationUnitPos.DefaultVisitor<NE> {

	private Attributes attr;
	private CompilationUnitPos prog;
	private int functionCount;
	private int visitedFunctions;

	public WurstValidator(CompilationUnitPos prog, Attributes attr) {
		this.prog = prog;
		this.attr = attr;
	}
	
	public void validate() {
		functionCount = countFunctions();
		visitedFunctions = 0;
		
		visit(prog);
	}

	private int countFunctions() {
		final int functionCount[] = new int[1];
		new CompilationUnitPos.DefaultVisitor<NE>() {
			@Override
			public void visit(FuncDefPos f) {
				functionCount[0]++;
			}
		}.visit(prog);
		return functionCount[0];
	}
	
	@Override
	public
	void visit(StmtSetPos s) {
		super.visit(s);
		
		PscriptType leftType = attr.exprType.get(s.left());
		PscriptType rightType = attr.exprType.get(s.right());
		
		checkAssignment(Utils.isJassCode(s), s.source(), leftType, rightType);
		
		
	}
	
	private void checkAssignment(boolean isJassCode, WPosPos pos, PscriptType leftType, PscriptType rightType) {
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
	public void visit(LocalVarDefPos s) {
		super.visit(s);
		if (s.initialExpr() instanceof ExprPos) {
			ExprPos initial = (ExprPos) s.initialExpr();
			PscriptType leftType = attr.varDefType.get(s);
			PscriptType rightType = attr.exprType.get(initial);
			
			checkAssignment(Utils.isJassCode(s), s.source(), leftType, rightType);
		}		
	}
	

	@Override
	public void visit(GlobalVarDefPos s) {
		super.visit(s);
		if (s.initialExpr() instanceof ExprPos) {
			ExprPos initial = (ExprPos) s.initialExpr();
			PscriptType leftType = attr.varDefType.get(s);
			PscriptType rightType = attr.exprType.get(initial);
			checkAssignment(Utils.isJassCode(s), s.source(), leftType, rightType);
		}
	}
	
	
	@Override
	public void visit(StmtIfPos stmtIf) {
		super.visit(stmtIf);
		PscriptType condType = attr.exprType.get(stmtIf.cond()); 
		if (!(condType instanceof PScriptTypeBool)) {
			attr.addError(stmtIf.cond().source(), "If condition must be a boolean but found " + condType);
		}
	}

	
	@Override
	public void visit(StmtWhilePos stmtWhile) {
		super.visit(stmtWhile);
		PscriptType condType = attr.exprType.get(stmtWhile.cond()); 
		if (!(condType instanceof PScriptTypeBool)) {
			attr.addError(stmtWhile.cond().source(), "While condition must be a boolean but found " + condType);
		}
	}
	

	
	@Override public void visit(FuncDefPos func) {
		super.visit(func);
		visitedFunctions++;
		attr.setProgress(null, ProgressHelper.getValidatorPercent(visitedFunctions, functionCount));
		
		
		if (func.signature().typ() instanceof TypeExprPos) {
			if (!attr.doesReturn.get(func.body())) {
				attr.addError(func.source(), "Function " + func.signature().name().term() + " is missing a return statement.");
			}
		}
	}
	
	@Override public void visit(ExprFunctionCallPos stmtCall) {
		super.visit(stmtCall);
		// calculating the exprType should reveal all errors:
		attr.exprType.get(stmtCall);
	}
	
	@Override public void visit(ExprMemberMethodPos stmtCall) {
		super.visit(stmtCall);
		// calculating the exprType should reveal all errors:
		attr.exprType.get(stmtCall);
	}
	
	@Override public void visit(ExprNewObjectPos stmtCall) {
		super.visit(stmtCall);
		// calculating the exprType should reveal all errors:
		attr.exprType.get(stmtCall);
	}
	
	
}
