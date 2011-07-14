package de.peeeq.pscript.intermediateLang.interpreter;

import java.util.List;

import de.peeeq.pscript.intermediateLang.ILStatement;
import de.peeeq.pscript.intermediateLang.ILconst;
import de.peeeq.pscript.intermediateLang.ILconstBool;
import de.peeeq.pscript.intermediateLang.ILfunction;
import de.peeeq.pscript.intermediateLang.ILif;
import de.peeeq.pscript.intermediateLang.ILprog;
import de.peeeq.pscript.intermediateLang.ILvar;

public class ILInterpreterImpl implements ILInterpreter {

	private ILprog prog;

	@Override
	public void LoadProgram(ILprog prog) {
		this.prog = prog;
		// TODO initialize global variables

	}

	@Override
	public ILconst executeFunction(String name, ILconst... arguments) {
		ILfunction func = searchFunction(name);
		List<ILvar> locals = func.getLocals();
		
		// TODO initialize local variables
		
		List<ILStatement> body = func.getBody();
		
		this.executeStatements(body); // TODO maybe add a parameter for local variables
		
		
		return null; // TODO get return value
	}

	private void executeStatements(List<ILStatement> body) {
		for (ILStatement s : body) {
			executeStatement(s);
		}
	}


	private void executeStatement(ILStatement s) {
		// TODO Auto-generated method stub
		if (s instanceof ILif) {
			translateStatementIf((ILif)s);
		} else {
			// TODO mögliche andere Statements
			throw new Error("not implemented " + s);
		}
	}

	private void translateStatementIf(ILif s) {
		ILvar cond = s.getCond();	
		ILconstBool condValue = (ILconstBool) lookupVarValue(cond);
		
		if (condValue.getVal()) {
			executeStatements(s.getThenBlock());
		} else {
			executeStatements(s.getElseBlock());
		}
		
	}

	private ILconstBool lookupVarValue(ILvar var) {
		// TODO lookup value of var
		throw new Error("not implemetned");
	}

	private ILfunction searchFunction(String name) {
		for (ILfunction f : prog.getFunctions()) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		throw new Error("Function " + name + " not found.");
	}

}
