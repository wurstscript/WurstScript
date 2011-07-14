package de.peeeq.pscript.validation;

import org.eclipse.emf.common.util.EList;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.immutablecollections.ImmutableMap;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.Statement;
import de.peeeq.pscript.pscript.Statements;
import de.peeeq.pscript.pscript.StmtExpr;
import de.peeeq.pscript.pscript.StmtIf;
import de.peeeq.pscript.pscript.StmtReturn;
import de.peeeq.pscript.pscript.StmtWhile;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.pscript.util.StatementSwitch;
import de.peeeq.pscript.types.PscriptType;

public class FunctionChecker {

	private PscriptJavaValidator pscriptJavaValidator;
	private FuncDef func;

	public FunctionChecker(PscriptJavaValidator pscriptJavaValidator,	FuncDef func) {
		this.pscriptJavaValidator = pscriptJavaValidator;
		this.func = func;
	}

	public void checkFunction() {
		// We have several things todo here
		// 1) check if function returns (if it does not return void)
		// 2) check if no uninitialized vars are used
		// 3) check that no vars are defined twice
		
		checkStatements(func.getBody());
		
	}

	private CheckResult checkStatements(Statements body) {
		return checkStatements(body.getStatements());
	}

	private CheckResult checkStatements(EList<Statement> statements) {
		for (Statement s : statements) {
			checkStatement(s);
		}
		return null;
		
	}

	private CheckResult checkStatement(Statement s) {
		return new StatementSwitch<CheckResult>() {

			@Override
			public CheckResult caseStmtWhile(StmtWhile stmtWhile) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CheckResult caseStmtExpr(StmtExpr stmtExpr) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CheckResult caseVarDef(VarDef varDef) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CheckResult caseStmtReturn(StmtReturn stmtReturn) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CheckResult caseStmtIf(StmtIf stmtIf) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}.doSwitch(s);
		
	}

}

// immutable class
class CheckResult {
	private final boolean returns;
	private final ImmutableMap<String, PscriptType> definedVariableTypes; 
	
	
	
	public CheckResult() {
		this.returns = false;
		this.definedVariableTypes = ImmutableMap.emptyMap();
	}
	
	public CheckResult(boolean returns, ImmutableMap<String, PscriptType> definedVariableTypes) {
		this.returns = returns;
		this.definedVariableTypes = definedVariableTypes;
	}

	CheckResult setReturns() {
		return new CheckResult(true, definedVariableTypes);
	}
	
	
	CheckResult addVar(String name, PscriptType type) {
		return new CheckResult(returns, definedVariableTypes.put(name, type));
	}
	
	/**
	 * merges two CheckResults
	 * 
	 * meant to be used after an if
	 * 
	 * - if both paths of the if return, then the whole if returns
	 * - if both paths define a variable x, then x is defined afterwards
	 * 
	 * @param other
	 * @return
	 * @throws MergeException
	 */
	CheckResult tryMerge(CheckResult other) throws MergeException {
		
		
		
		ImmutableMap<String, PscriptType> merged_definedVariableTypes = ImmutableMap.emptyMap();
		boolean merged_returns = returns && other.returns;
		
		for ( String name : other.definedVariableTypes.keys()) {
			if (definedVariableTypes.containsKey(name)) {
				PscriptType typeOther = other.definedVariableTypes.get(name);
				PscriptType type = definedVariableTypes.get(name);
				if (type.equals(typeOther)) {
					// same name, same type -> merge
					merged_definedVariableTypes = merged_definedVariableTypes.put(name, type);
				} else {
					// different types -> error
					throw new MergeException(name, type, typeOther);
				}				
			}
		}
		
		return new CheckResult(merged_returns, merged_definedVariableTypes);
	}
	
	
	
}

class MergeException extends Exception {

	public MergeException(String name, PscriptType type, PscriptType typeOther) {
		// TODO Auto-generated constructor stub
	}

	
}
