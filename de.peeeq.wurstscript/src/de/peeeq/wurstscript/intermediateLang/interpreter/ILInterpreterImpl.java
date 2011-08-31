package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.peeeq.wurstscript.intermediateLang.ILStatement;
import de.peeeq.wurstscript.intermediateLang.ILarraySetVar;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstNum;
import de.peeeq.wurstscript.intermediateLang.ILconstString;
import de.peeeq.wurstscript.intermediateLang.ILexitwhen;
import de.peeeq.wurstscript.intermediateLang.ILfunction;
import de.peeeq.wurstscript.intermediateLang.ILfunctionCall;
import de.peeeq.wurstscript.intermediateLang.ILif;
import de.peeeq.wurstscript.intermediateLang.ILloop;
import de.peeeq.wurstscript.intermediateLang.ILprog;
import de.peeeq.wurstscript.intermediateLang.ILreturn;
import de.peeeq.wurstscript.intermediateLang.ILsetBinary;
import de.peeeq.wurstscript.intermediateLang.ILsetBinaryCL;
import de.peeeq.wurstscript.intermediateLang.ILsetBinaryCR;
import de.peeeq.wurstscript.intermediateLang.ILsetVar;
import de.peeeq.wurstscript.intermediateLang.ILsetVarArray;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.intermediateLang.IlbuildinFunctionCall;
import de.peeeq.wurstscript.intermediateLang.Iloperator;
import de.peeeq.wurstscript.intermediateLang.IlsetConst;
import de.peeeq.wurstscript.intermediateLang.IlsetUnary;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeHandle;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;

public class ILInterpreterImpl implements ILInterpreter {

	private ILprog prog;
	private static ExitwhenException staticExitwhenException = new ExitwhenException();
	private Map<String, ILconst> globalVarMap;

	@Override
	public void LoadProgram(ILprog prog) {
		this.prog = prog;
		globalVarMap = new HashMap<String, ILconst>();

		List<ILvar> globals = prog.getGlobals();
		// TODO globals initialisieren
		for (ILvar v : globals) {
			ILconst value = null;
			if (v.getType() instanceof PScriptTypeInt) {
				value  = new ILconstInt(0);
			}else if (v.getType() instanceof PScriptTypeBool) {
				value  = new ILconstBool(false);
			}else if (v.getType() instanceof PScriptTypeReal) {
				value  = new ILconstNum(0.0f);
			}else if (v.getType() instanceof PScriptTypeString) {
				value  = new ILconstString("");
			} else if (v.getType() instanceof PScriptTypeArray) {
				PScriptTypeArray arType = (PScriptTypeArray) v.getType();
				value = new ILarray(arType.getBaseType());
			} else if (v.getType().isSubtypeOf(PScriptTypeHandle.instance())) {
				// initialize handles with null
				value = new ILconstNull();
			}else {
				// TODO Andere Types
				throw new Error("not implemented: initialisation of " + v);
			}
			globalVarMap.put(v.getName(), value );
		}

	}

	@Override
	public ILconst executeFunction(String name, ILconst... arguments) {




		ILfunction func = searchFunction(name);
		List<ILvar> locals = func.getLocals();


		List<ILStatement> body = func.getBody();

		Map<String, ILconst> localVarMap = new HashMap<String, ILconst>();
		// TODO locals initialisieren
		for (ILvar v : locals) {
			ILconst value = null;
			if (v.getType() instanceof PScriptTypeInt) {
				value  = new ILconstInt(0);
			}else if (v.getType() instanceof PScriptTypeBool) {
				value  = new ILconstBool(false);
			}else if (v.getType() instanceof PScriptTypeReal) {
				value  = new ILconstNum(0.0f);
			}else if (v.getType() instanceof PScriptTypeString) {
				value  = new ILconstString("");
			} else if (v.getType() instanceof PScriptTypeArray) {
				// arrays are not initialized...
			}else {
				throw new Error("not implemented " + v.getType());
			}
			localVarMap.put(v.getName(), value );
		}

		int i = 0;
		for (ILvar v : func.getParams()) {
			localVarMap.put(v.getName(), arguments[i] );
			i++;
		}
		try {
			this.executeStatements(localVarMap, body);
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
		if (s instanceof ILif) {
			translateStatementIf(localVarMap, (ILif)s);
		} else if (s instanceof ILsetVar) {
			translateIlcopy( localVarMap, (ILsetVar)s);
		} else if (s instanceof IlsetConst) {
			translateIlsetConst( localVarMap, (IlsetConst)s);
		} else if (s instanceof ILsetBinary) {
			translateIlbinary( localVarMap, (ILsetBinary)s);
		} else if (s instanceof IlsetUnary) {
			translateIlunary(localVarMap, (IlsetUnary) s);
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
		} else if (s instanceof ILsetBinaryCR) {
			translateILsetBinaryCR(localVarMap, (ILsetBinaryCR) s);
		} else if (s instanceof ILsetBinaryCL) {
			translateILsetBinaryCL(localVarMap, (ILsetBinaryCL) s);
		} else if (s instanceof ILarraySetVar) {
			translateILarraySetVar(localVarMap, (ILarraySetVar) s);
		} else if (s instanceof ILsetVarArray) {
			translateILsetVarArray(localVarMap, (ILsetVarArray) s);
		} else {

			throw new Error("not implemented " + s);
		}
	}

	

	private void translateILsetVarArray(Map<String, ILconst> localVarMap,
			ILsetVarArray s) {
		ILarray ar = (ILarray) lookupVarValue(localVarMap, s.getVar());
		ILconstInt indexValue = (ILconstInt) lookupVarValue(localVarMap, s.getIndex());
		
		
		int index = indexValue.getVal();
		ILconst value = ar.get(index );
		
		addVarToProperMap(localVarMap, s.getResultVar(), value);
	}

	private void translateILarraySetVar(Map<String, ILconst> localVarMap, ILarraySetVar s) {
		
		ILconst rightValue = lookupVarValue(localVarMap, s.getVar());
		ILconstInt indexValue = (ILconstInt) lookupVarValue(localVarMap, s.getIndex());
		ILarray ar = (ILarray) lookupVarValue(localVarMap, s.getResultVar());
		ar.set(indexValue.getVal(),	rightValue);
		
	}

	private void translateIlunary(Map<String, ILconst> localVarMap,
			IlsetUnary s) {
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
		} else if (name.equals("testFail")) {
			ILconstString msg = (ILconstString) lookupVarValue(localVarMap, s.getArgs().get(0));
			throw new TestFailException(msg.getVal());
		} else if (name.equals("testSuccess")) {
			throw TestSuccessException.instance;
		} else if (name.equals("toString")) {
			ILconstInt i = (ILconstInt) lookupVarValue(localVarMap, s.getArgs().get(0));
			addVarToProperMap(localVarMap, s.getResultVar(), new ILconstString(""+i.getVal()));
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

	
	private void translateILsetBinaryCL(Map<String, ILconst> localVarMap,
			ILsetBinaryCL s) {
		ILconst leftValue = s.getLeft();
		ILconst rightValue = lookupVarValue(localVarMap, s.getRight());
		
		
		ILconst result = translateBinaryExpr(localVarMap, leftValue, s.getOp(), rightValue);
		addVarToProperMap(localVarMap,s.getResultVar(), result );
		
	}

	private void translateILsetBinaryCR(Map<String, ILconst> localVarMap,
			ILsetBinaryCR s) {
		ILconst leftValue = lookupVarValue(localVarMap, s.getLeft());
		ILconst rightValue = s.getRight();
		
		
		ILconst result = translateBinaryExpr(localVarMap, leftValue, s.getOp(), rightValue);
		addVarToProperMap(localVarMap,s.getResultVar(), result );
		
	}
	
	private void translateIlbinary(Map<String, ILconst> localVarMap, ILsetBinary s) {
		ILconst leftValue = lookupVarValue(localVarMap, s.getLeft());
		ILconst rightValue = lookupVarValue(localVarMap, s.getRight());
		
		
		ILconst result = translateBinaryExpr(localVarMap, leftValue, s.getOp(), rightValue);
		addVarToProperMap(localVarMap,s.getResultVar(), result );

	}
	
	private ILconst translateBinaryExpr(Map<String, ILconst> localVarMap, ILconst leftValue, Iloperator op, ILconst rightValue) {
		ILconst result = null;


		if (op == Iloperator.PLUS) {
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
		}else if (op == Iloperator.MINUS) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() - r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstNum(l - r);
			}
		}else if (op == Iloperator.MULT) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() * r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstNum(l * r);
			}
		}else if ( op == Iloperator.DIV_REAL ) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstNum((1.0*l.getVal()) / r.getVal());
			}else if(leftValue instanceof ILconstNum) {
				double l = ((ILconstNum) leftValue).getVal().floatValue();
				double r = ((ILconstNum) rightValue).getVal().floatValue();
				double sum = l + r;
				result  = new ILconstNum( sum );
			}
		}else if ( op == Iloperator.DIV_INT ) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstInt(l.getVal() / r.getVal());
			}
		}else if (op == Iloperator.LESS) { 
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() < r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l < r);
			}
		}else if (op == Iloperator.LESS_EQ) { 
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() <= r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l <= r);
			}
		}else if (op == Iloperator.GREATER) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() > r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l > r);
			}
		}else if (op == Iloperator.GREATER_EQ) {
			if (leftValue instanceof ILconstInt) {
				ILconstInt l = (ILconstInt) leftValue;
				ILconstInt r = (ILconstInt) rightValue;
				result  = new ILconstBool(l.getVal() >= r.getVal());
			} else if (leftValue instanceof ILconstNum) {
				float l = ((ILconstNum) leftValue).getVal().floatValue();
				float r = ((ILconstNum) rightValue).getVal().floatValue();
				result  = new ILconstBool(l >= r);
			}
		}else if (op == Iloperator.EQUALITY) {
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
		}else if (op == Iloperator.UNEQUALITY) {
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
		}else if (op == Iloperator.AND ) {
			if (leftValue instanceof ILconstBool) {
				boolean l = ((ILconstBool) leftValue).getVal();
				boolean r = ((ILconstBool) rightValue).getVal();
				result  = new ILconstBool(l && r);
			}
		}else if (op == Iloperator.OR ) {
			if (leftValue instanceof ILconstBool) {
				boolean l = ((ILconstBool) leftValue).getVal();
				boolean r = ((ILconstBool) rightValue).getVal();
				result  = new ILconstBool(l || r);
			}
		}else if (op == Iloperator.MOD_INT || op == Iloperator.MOD_REAL) {
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
		return result;
	}
	


	private void translateIlsetConst(Map<String, ILconst> localVarMap,
			IlsetConst s) {

		addVarToProperMap(localVarMap,s.getResultVar(), s.getConstant());

	}

	private void translateIlcopy(Map<String, ILconst> localVarMap, ILsetVar s) {
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
		} else if (isGlobal( v )) {
			globalVarMap.put(v.getName(), s);
		} else {
			throw new Error("var " + v + " is neither local nor global?");
		}
	}


	private boolean isLocal(Map<String, ILconst> localVarMap, ILvar resultVar) {
		return localVarMap.containsKey(resultVar.getName());

	}

	private boolean isGlobal( ILvar resultVar) {
		return globalVarMap.containsKey(resultVar.getName());

	}

	private ILconst lookupVarValue(Map<String, ILconst> localVarMap, ILvar var) {
		ILconst value = localVarMap.get(var.getName());
		if (value == null) {
			value = globalVarMap.get(var.getName());
			if (value == null) {
				throw new Error("Variable " + var.getName() + " not found.");
			}
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
