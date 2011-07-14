package de.peeeq.pscript.intermediateLang.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.peeeq.pscript.intermediateLang.ILStatement;
import de.peeeq.pscript.intermediateLang.ILconst;
import de.peeeq.pscript.intermediateLang.ILconstBool;
import de.peeeq.pscript.intermediateLang.ILconstInt;
import de.peeeq.pscript.intermediateLang.ILcopy;
import de.peeeq.pscript.intermediateLang.ILfunction;
import de.peeeq.pscript.intermediateLang.ILif;
import de.peeeq.pscript.intermediateLang.ILprog;
import de.peeeq.pscript.intermediateLang.ILreturn;
import de.peeeq.pscript.intermediateLang.ILvar;
import de.peeeq.pscript.intermediateLang.Ilbinary;
import de.peeeq.pscript.intermediateLang.Iloperator;
import de.peeeq.pscript.intermediateLang.IlsetConst;
import de.peeeq.pscript.types.PScriptTypeInt;

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
		
		Map<String, ILconst> localVarMap = new HashMap<String, ILconst>();
		
		for (ILvar v : locals) {
			ILconst value = null;
			if (v.getType() instanceof PScriptTypeInt) {
				value  = new ILconstInt(0); // TODO boolean, etc.
			}
			localVarMap.put(v.getName(), value );
		}
		
		int i = 0;
		for (ILvar v : func.getParams()) {
			localVarMap.put(v.getName(), arguments[i] );
			i++;
		}
		try {
			this.executeStatements(localVarMap, body); // TODO maybe add a parameter for local variables
		} catch (ReturnException e) {
			return e.getVal();
		}
		
		
		return null; // TODO get return value
	}

	private void executeStatements(Map<String, ILconst> localVarMap, List<ILStatement> body) {
		for (ILStatement s : body) {
			executeStatement(localVarMap, s);
		}
	}


	private void executeStatement(Map<String, ILconst> localVarMap, ILStatement s) {
		// TODO Auto-generated method stub
		if (s instanceof ILif) {
			translateStatementIf(localVarMap, (ILif)s);
		} else if (s instanceof ILcopy) {
			translateIlcopy( localVarMap, (ILcopy)s);
		} else if (s instanceof IlsetConst) {
			translateIlsetConst( localVarMap, (IlsetConst)s);
		} else if (s instanceof Ilbinary) {
			translateIlbinary( localVarMap, (Ilbinary)s);
		} else if (s instanceof ILreturn) {
			translateReturn(localVarMap, (ILreturn) s);
		} else {
			// TODO mögliche andere Statements

			throw new Error("not implemented " + s);
		}
	}

	private void translateReturn(Map<String, ILconst> localVarMap, ILreturn s) {
		ILconst value = lookupVarValue(localVarMap, s.getVar());
		
		throw new ReturnException(value);
		
	}

	private void translateIlbinary(Map<String, ILconst> localVarMap, Ilbinary s) {
		// set x = a + b
		// set x = a * b
		ILconst leftValue = lookupVarValue(localVarMap, s.getLeft());
		ILconst rightvalue = lookupVarValue(localVarMap, s.getRight());
		ILconst result = null;
		if (s.getOp() == Iloperator.PLUS) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightvalue;
				result  = new ILconstInt(l.getVal() + r.getVal());
			}
		} else if (s.getOp() == Iloperator.LESS) { 
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightvalue;
				result  = new ILconstBool(l.getVal() < r.getVal());
			}
		}
		
//		if (isLocal(localVarMap, s.getResultVar())) {
//			localVarMap.put(s.getResultVar().getName(), result);
//		} else {
//			// TODO global
//			throw new Error("not implemented");
//		}
		
		addVarToProperMap(localVarMap,s.getResultVar(), result);
		
	}

	private void translateIlsetConst(Map<String, ILconst> localVarMap,
			IlsetConst s) {
		// TODO Auto-generated method stub

//		if (isLocal(localVarMap, s.getResultVar())) {
//			localVarMap.put(s.getResultVar().getName(), s.getConstant());
//		} else {
//			// TODO global
//			throw new Error("not implemented");
//		}
		
		addVarToProperMap(localVarMap,s.getResultVar(), s.getConstant());
		
	}

	private void translateIlcopy(Map<String, ILconst> localVarMap, ILcopy s) {
		// TODO Auto-generated method stub
		// set x = y
		ILconst value = lookupVarValue(localVarMap, s.getVar());
//		value2 = lookupVarValue(localVarMap, s.getResultVar());
		
//		if (isLocal(localVarMap, s.getResultVar())) {
//			localVarMap.put(s.getResultVar().getName(), value);
//		} else {
//			// TODO global
//			throw new Error("not implemented");
//		}
		addVarToProperMap(localVarMap,s.getResultVar(), value);
		
		
		
	}
	
	private void translateStatementIf(Map<String, ILconst> localVarMap, ILif s) {
		ILvar cond = s.getCond();	
		ILconstBool condValue = (ILconstBool) lookupVarValue(localVarMap, cond);
		
		if (condValue.getVal()) {
			executeStatements(localVarMap, s.getThenBlock());
		} else {
			executeStatements(localVarMap, s.getElseBlock());
		}
		
	}

	
	private void addVarToProperMap(Map<String, ILconst> localVarMap, ILvar v, ILconst s ) {
		if (isLocal(localVarMap, v)) {
			localVarMap.put(v.getName(), s);
		} else if (true) {
			// TODO globalVarMap 
			
		} else {
			throw new Error("var is neither local nor global?");
		}
	}
	
	
	private boolean isLocal(Map<String, ILconst> localVarMap, ILvar resultVar) {
		return localVarMap.containsKey(resultVar.getName());

	}
	

	private ILconst lookupVarValue(Map<String, ILconst> localVarMap, ILvar var) {
		ILconst value = localVarMap.get(var.getName());
		if (value == null) {
			// TODO bei globalen variablen nachgucken
			throw new Error("Variable " + var.getName() + " not found.");
		}
		return value;
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
