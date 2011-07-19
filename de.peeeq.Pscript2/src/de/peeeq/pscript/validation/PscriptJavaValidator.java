package de.peeeq.pscript.validation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.validation.Check;

import com.google.inject.Inject;

import de.peeeq.pscript.attributes.AttrExprType;
import de.peeeq.pscript.attributes.AttrTypeExprType;
import de.peeeq.pscript.attributes.AttrVarDefType;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprFunctioncall;
import de.peeeq.pscript.pscript.ExprMember;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.ParameterDef;
import de.peeeq.pscript.pscript.Program;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.StmtSet;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.pscript.util.ClassMemberSwitchVoid;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeError;
import de.peeeq.pscript.utils.NotNullList;

public class PscriptJavaValidator extends AbstractPscriptJavaValidator {

	public static final String INVALID_TYPE_NAME = "INVALID_TYPE_NAME";
	public static final String OTHER_ERROR = "OTHER_ERROR";
	private static final String INVALID_TYPE = "INVALID_TYPE";
	private static final String NO_CATEGORY = "NO_CATEGORY";
	private static final String TOO_FEW_PARAMETERS = "TOO_FEW_PARAMETERS";
	private static final String TOO_MANY_PARAMETERS = "TOO_MANY_PARAMETERS";
	private static final String WRONG_PARAMETER_TYPE = "WRONG_PARAMETER_TYPE";
	private static final String FUNC_NOT_FOUND = "FUNC_NOT_FOUND";

	@Inject
	private AttributeManager attrManager;

	@Check
	public void checkCompilationUnit(Program p) {
		// if everything is checked we better reset the cache to caching
		// problems
		// maybe this could be done more intelligently to increase performance
		attrManager.reset();
	}

	@Check
	public void checkTypes(Expr e) {
		PscriptType t = attrManager.getAttValue(AttrExprType.class, e);
		if (t == null) {
			this.error("Could not determine type of expression",
					PscriptPackage.Literals.EXPR.getEStructuralFeature(0),
					INVALID_TYPE, e.toString());
		}
		if (t instanceof PscriptTypeError) {
			error(t.getName(),
					PscriptPackage.Literals.EXPR.getEStructuralFeature(0),
					INVALID_TYPE, e.toString());
		}
	}

	@Check
	public void checkVarDefs(VarDef v) {
		PscriptType leftType = attrManager.getAttValue(AttrVarDefType.class, v);
		if (leftType instanceof PscriptTypeError) {
			error(leftType.toString(), PscriptPackage.Literals.VAR_DEF__E,
					NO_CATEGORY);
			return;
		}
		if (v.getE() != null) {
			PscriptType rightType = attrManager.getAttValue(AttrExprType.class,
					v.getE());
			if (!leftType.isSupertypeOf(rightType)) {
				error("Cannot assign value of type " + rightType
						+ " to variable of type " + leftType + ".",
						PscriptPackage.Literals.VAR_DEF__E, NO_CATEGORY);
				return;
			}
		} else { // right side is null
			if (v.isConstant()) {
				error("Constant variables must be initialized.",
						PscriptPackage.Literals.VAR_DEF__E, NO_CATEGORY);
				return;
			}
		}
	}

	@Check
	public void checkAssignments(StmtSet e) {
		PscriptType leftType = attrManager.getAttValue(AttrExprType.class, e
				.getLeft().getE());
		PscriptType rightType = attrManager.getAttValue(AttrExprType.class,
				e.getRight());
		// TODO check if left side is variable and not constant
		if (!leftType.isSupertypeOf(rightType)) {
			error("Cannot assign value of type " + rightType
					+ " to variable of type " + leftType + ".",
					PscriptPackage.Literals.STMT_SET__OP_ASSIGNMENT,
					NO_CATEGORY);
			return;
		}

	}

	@Check
	public void checkClassNames(ClassDef c) {
		if (!Character.isUpperCase(c.getName().charAt(0))) {
			error("Type names must start with a capital letter",
					PscriptPackage.Literals.ENTITY__NAME
					/* PscriptPackage.CLASS_DEF__NAME */, INVALID_TYPE_NAME,
					c.getName());
			// error("Type names must start with a capital letter", TYPE_NAME,
			// INVALID_TYPE_NAME, c.getName());
		}

	}

	@Check
	void checkExprMember(final ExprMember e) {
		final Expr left = e.getLeft();
		if (e.getMessage().getNameVal() == null) {
			return; // should already give an syntax error
		}
		
		new ClassMemberSwitchVoid() {
			
			@Override
			public void caseVarDef(VarDef varDef) {
				// TODO meber vars
				throw new Error("not implemented");
			}
			
			@Override
			public void caseFuncDef(FuncDef funcDef) {
				List<Expr> args = new NotNullList<Expr>();
				
				args.add(left);
				if (e.getMessage().getParams() != null) {
					args.addAll(e.getMessage().getParams());
				}
				
				boolean hasImplicitArgument = true;				
				helper_checkFunctionCall(funcDef, hasImplicitArgument, args);
			}
		}.doSwitch(e.getMessage().getNameVal());
		
		
	}
	
	
	@Check
	void checkFunctionCall(ExprFunctioncall fc) {

		FuncDef func = fc.getNameVal();
		if (func == null) {
			error("Function not found.",
					PscriptPackage.Literals.EXPR_FUNCTIONCALL__NAME_VAL,
					FUNC_NOT_FOUND);
			return;
		}

		boolean hasImplicitArgument = false;
		helper_checkFunctionCall(func, hasImplicitArgument, fc.getParams());

	}

	void helper_checkFunctionCall(FuncDef calledFunc, boolean hasImplicitArgument, List<Expr> args) {
		// calculate argument types
		LinkedList<PscriptType> argumentTypes = new NotNullList<PscriptType>();
		for (Expr arg : args) {
			PscriptType argType = attrManager.getAttValue(AttrExprType.class,
					arg);
			argumentTypes.add(argType);
		}

		// Get formal parameter types:
		LinkedList<PscriptType> formalParameterTypes = new LinkedList<PscriptType>();

		for (VarDef x : calledFunc.getParameters()) {
			ParameterDef param = (ParameterDef) x;
			PscriptType paramType = attrManager.getAttValue(
					AttrTypeExprType.class, param.getType());
			formalParameterTypes.add(paramType);
		}

		int missingParameters = formalParameterTypes.size()
				- argumentTypes.size();
		if (missingParameters > 0) {
			error("Function call to " + calledFunc.getName() + " is missing "
					+ missingParameters + " more arguments.",
					PscriptPackage.Literals.EXPR_FUNCTIONCALL__NAME_VAL,
					TOO_FEW_PARAMETERS, "" + missingParameters);

			return;
		}

		if (missingParameters < 0) {
			error("Function call to " + calledFunc.getName() + " has "
					+ -missingParameters + " too many arguments.",
					PscriptPackage.Literals.EXPR_FUNCTIONCALL__NAME_VAL,
					TOO_MANY_PARAMETERS, "" + missingParameters);

			return;
		}

		for (int i = 0; i < formalParameterTypes.size(); i++) {
			if (!argumentTypes.get(i).isSubtypeOf(formalParameterTypes.get(i))) {

				int errorPos = i;
				if (hasImplicitArgument) {
					errorPos--;
				}
				if (errorPos < 0) {
					String message = "The type " + argumentTypes.get(0)
							+ " does not understand the method "
							+ calledFunc.getName() + ". "
							+ "Expected receiver of type "
							+ formalParameterTypes.get(0) + ".";
					error(message, args.get(0),
							null,
							WRONG_PARAMETER_TYPE);
				} else {
					String message = "Argument " + (errorPos + 1) + " of type "
							+ argumentTypes.get(i)
							+ " did not match the expected type "
							+ formalParameterTypes.get(i) + ".";
					error(message, args.get(errorPos), null,
							WRONG_PARAMETER_TYPE);
				}
			}
		}
	}

	@Check
	void checkFunction(FuncDef func) {

		new FunctionChecker(this, func).checkFunction();
	}

	// TODO check return statements
	// TODO check for unique names
	// TODO while, if, exitwhen, etc

}
