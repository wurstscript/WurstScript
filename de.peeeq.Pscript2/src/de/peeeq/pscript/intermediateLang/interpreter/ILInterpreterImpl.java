package de.peeeq.pscript.intermediateLang.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.peeeq.pscript.intermediateLang.ILStatement;
import de.peeeq.pscript.intermediateLang.ILconst;
import de.peeeq.pscript.intermediateLang.ILconstBool;
import de.peeeq.pscript.intermediateLang.ILconstInt;
import de.peeeq.pscript.intermediateLang.ILconstNum;
import de.peeeq.pscript.intermediateLang.ILconstString;
import de.peeeq.pscript.intermediateLang.ILcopy;
import de.peeeq.pscript.intermediateLang.ILexitwhen;
import de.peeeq.pscript.intermediateLang.ILfunction;
import de.peeeq.pscript.intermediateLang.ILfunctionCall;
import de.peeeq.pscript.intermediateLang.ILif;
import de.peeeq.pscript.intermediateLang.ILloop;
import de.peeeq.pscript.intermediateLang.ILprog;
import de.peeeq.pscript.intermediateLang.ILreturn;
import de.peeeq.pscript.intermediateLang.ILvar;
import de.peeeq.pscript.intermediateLang.Ilbinary;
import de.peeeq.pscript.intermediateLang.IlbuildinFunctionCall;
import de.peeeq.pscript.intermediateLang.Iloperator;
import de.peeeq.pscript.intermediateLang.IlsetConst;
import de.peeeq.pscript.intermediateLang.Ilunary;
import de.peeeq.pscript.types.PScriptTypeInt;

public class ILInterpreterImpl implements ILInterpreter {

	private ILprog prog;
	private static ExitwhenException staticExitwhenException = new ExitwhenException();

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
		} else if (s instanceof Ilunary) {
			translateIlunary(localVarMap, (Ilunary) s);
		} else if (s instanceof ILreturn) {
			translateReturn(localVarMap, (ILreturn) s);
		} else if (s instanceof ILloop) {
			translateLoop(localVarMap, (ILloop) s);
		} else if (s instanceof ILexitwhen) {
			translateExitwhen(localVarMap, (ILexitwhen) s);
		} else if (s instanceof ILfunctionCall) {
			translateFunctionCall(localVarMap, (ILfunctionCall) s);
		} else if (s instanceof IlbuildinFunctionCall) {
			translateBuildinFunction(localVarMap, (IlbuildinFunctionCall) s);
		} else {
			// TODO mögliche andere Statements

			throw new Error("not implemented " + s);
		}
	}

	private void translateIlunary(Map<String, ILconst> localVarMap,
			Ilunary s) {
		ILconst rightValue = lookupVarValue(localVarMap, s.getRight());
		ILconst result = null;
		if (s.getOp() == Iloperator.MINUS) {
			if (rightValue instanceof ILconstInt) {
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(r.negate());
			} else if (rightValue instanceof ILconstNum) {
				ILconstNum r = (ILconstNum) rightValue;
				result  = new ILconstNum(r.negate());
			}
		}else if (s.getOp() == Iloperator.NOT) {
			if (rightValue instanceof ILconstBool) {
				ILconstBool r = (ILconstBool) rightValue;
				result  = new ILconstBool(r.not());
			}
		}
		
		addVarToProperMap(localVarMap,s.getResultVar(), result);
		
	}

	private void translateExitwhen(Map<String, ILconst> localVarMap,
			ILexitwhen s) {
		ILconst value = lookupVarValue(localVarMap, s.getVar());
		if (((ILconstBool)value).getVal())
		{
			throw staticExitwhenException;
		}
		
	}

	private void translateLoop(Map<String, ILconst> localVarMap, ILloop s) {
		try{
			while (true) { executeStatements(localVarMap, s.getLoopBody() ); }
		} catch (ExitwhenException e){}
		
	}

	private void translateBuildinFunction(Map<String, ILconst> localVarMap,
			IlbuildinFunctionCall s) {
		String name = s.getFuncName();
		
		// native methods:
		if (name.equals("print")) {
			// Example: print method
			ILconstString msg = (ILconstString) lookupVarValue(localVarMap, s.getArgs().get(0));
			System.out.println(msg.getVal());
		} else {
			throw new Error("Function " + name 
					+ " not implemented.");
			// TODO other methods
		}
		
	}

	private void translateFunctionCall(Map<String, ILconst> localVarMap,
			ILfunctionCall s) {
		ILconst[] arguments = new ILconst[s.getArgs().size()];
		for (int i=0; i< arguments.length; i++) {
			arguments[i] = lookupVarValue(localVarMap, s.getArgs().get(i));
		}
		executeFunction(s.getName(), arguments );
	}

	private void translateReturn(Map<String, ILconst> localVarMap, ILreturn s) {
		ILconst value = lookupVarValue(localVarMap, s.getVar());
		
		throw new ReturnException(value);
		
	}

	private void translateIlbinary(Map<String, ILconst> localVarMap, Ilbinary s) {
		ILconst leftValue = lookupVarValue(localVarMap, s.getLeft());
		ILconst rightValue = lookupVarValue(localVarMap, s.getRight());
		ILconst result = null;

		if (s.getOp() == Iloperator.PLUS) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() + r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstNum(l + r);
			} else if (leftValue instanceof ILconstString) {
				String l = ((ILconstString) leftValue).getVal();
				String r = ((ILconstString) rightValue).getVal();
				result  = new ILconstString(l + r);
			}
		}else if (s.getOp() == Iloperator.MINUS) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() - r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstNum(l - r);
			}
		}else if (s.getOp() == Iloperator.MULT) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() * r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstNum(l * r);
			}
		}else if ( s.getOp() == Iloperator.DIV_REAL ) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() / r.getVal());
			}else if(leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				float sum = l + r;
				result  = new ILconstNum( sum );
			}
			// TODO Reals
		}else if (s.getOp() == Iloperator.LESS) { 
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() < r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l < r);
			}
		}else if (s.getOp() == Iloperator.LESS_EQ) { 
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() <= r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l <= r);
			}
		}else if (s.getOp() == Iloperator.GREATER) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() > r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l > r);
			}
		}else if (s.getOp() == Iloperator.GREATER_EQ) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() >= r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l >= r);
			}
		}else if (s.getOp() == Iloperator.EQUALITY) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() == r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l == r);
			}  else if (leftValue instanceof ILconstBool) {
				boolean l = ((ILconstBool) leftValue).getVal();
				boolean r = ((ILconstBool) rightValue).getVal();
				result  = new ILconstBool(l == r);
			} else if (leftValue instanceof ILconstString) {
				String l = ((ILconstString) leftValue).getVal();
				String r = ((ILconstString) rightValue).getVal();
				result  = new ILconstBool(l == r);
			}
		}else if (s.getOp() == Iloperator.UNEQUALITY) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() != r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l != r);
			} else if (leftValue instanceof ILconstBool) {
				boolean l = ((ILconstBool) leftValue).getVal();
				boolean r = ((ILconstBool) rightValue).getVal();
				result  = new ILconstBool(l != r);
			} else if (leftValue instanceof ILconstString) {
				String l = ((ILconstString) leftValue).getVal();
				String r = ((ILconstString) rightValue).getVal();
				result  = new ILconstBool(l != r);
			}
		}else if (s.getOp() == Iloperator.AND ) {
			if (leftValue instanceof ILconstBool) {
				boolean l = ((ILconstBool) leftValue).getVal();
				boolean r = ((ILconstBool) rightValue).getVal();
				result  = new ILconstBool(l && r);
			}
		}else if (s.getOp() == Iloperator.OR ) {
			if (leftValue instanceof ILconstBool) {
				boolean l = ((ILconstBool) leftValue).getVal();
				boolean r = ((ILconstBool) rightValue).getVal();
				result  = new ILconstBool(l || r);
			}
		}else if (s.getOp() == Iloperator.MOD_INT ) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() % r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstNum(l % r);
			}
		}
		// TODO DIV_INT, MOD_REAL (MOD ansich?)
		

		
		addVarToProperMap(localVarMap,s.getResultVar(), result);
		
	}

	private void translateIlsetConst(Map<String, ILconst> localVarMap,
			IlsetConst s) {
		// TODO Auto-generated method stub
		
		addVarToProperMap(localVarMap,s.getResultVar(), s.getConstant());
		
	}

	private void translateIlcopy(Map<String, ILconst> localVarMap, ILcopy s) {
		// TODO Auto-generated method stub
		// set x = y
		ILconst value = lookupVarValue(localVarMap, s.getVar());

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
			throw new Error("not yet implemented");
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
