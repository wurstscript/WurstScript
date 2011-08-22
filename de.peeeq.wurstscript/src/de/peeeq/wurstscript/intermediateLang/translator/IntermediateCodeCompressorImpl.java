package de.peeeq.wurstscript.intermediateLang.translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.peeeq.wurstscript.intermediateLang.ILStatement;
import de.peeeq.wurstscript.intermediateLang.ILStatementSet;
import de.peeeq.wurstscript.intermediateLang.ILexitwhen;
import de.peeeq.wurstscript.intermediateLang.ILfunction;
import de.peeeq.wurstscript.intermediateLang.ILfunctionCall;
import de.peeeq.wurstscript.intermediateLang.ILif;
import de.peeeq.wurstscript.intermediateLang.ILloop;
import de.peeeq.wurstscript.intermediateLang.ILprog;
import de.peeeq.wurstscript.intermediateLang.ILreturn;
import de.peeeq.wurstscript.intermediateLang.ILsetBinary;
import de.peeeq.wurstscript.intermediateLang.ILsetVar;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.intermediateLang.IlbuildinFunctionCall;
import de.peeeq.wurstscript.intermediateLang.IlsetUnary;

public class IntermediateCodeCompressorImpl implements
		IntermediateCodeCompressor {

	@Override
	public void compress(ILprog prog) {
		
		for (ILfunction f : prog.getFunctions()) {
			new FunctionCompressor(f).compressFunction();
		}
		
	}


}

class FunctionCompressor {

	private ILfunction f;
	private Map<ILvar,Integer> varUses = new HashMap<ILvar, Integer>();
	private Map<ILvar,Integer> varAssignments = new HashMap<ILvar, Integer>();
	
	
	public FunctionCompressor(ILfunction f) {
		this.f = f;
		
	}

	public void compressFunction() {
		// count how often each variable is used and assigned
		getVarUsesAndVarAssignments(f.getBody());
		
		// TODO if a variable "temp" is used and assigned only once we probably have something like this:
		// 1) set temp = a + b
		// 2) set blub = temp + x
		// in this case we can remove line 1 and variable temp and compress it to:
		// set blub = (a+b) + x
		// 
		// there are still some border cases where this is not so easy. for example the following code should not be
		// compressed, even if "temp" is only used here:
		// 1) set temp = callSomeFunc(a,b)
		// 2) if cond then
		// 3)    set blub = temp + x
		// 4) endif
		// 
		// if we moved the function call into the if this would change the semantics of the code so function calls must 
		// never be moved into ifs
		//
		// loops: nothing should ever be moved into loops as it would slow down the code
		//
		// for some statements (function calls) the order is also important. example:
		// 1) set temp1 = foo()
		// 2) set temp2 = bar()
		// 3) set blub = temp2 + temp1
		//
		// this must not be optimized to "set blub = bar() + foo()" as it would probably change the semantics of the code
		// 
		// order is also important in other cases where the value of the expression changes in the meantime. example:
		// 1) set temp1 = x + y
		// 2) set y = 4
		// 3) set blub = temp1 + q
		//
		// if we compressed the expression from temp 1 into line3 this would change the semantics of the code.
		
		
		
		
		
	}

	private void getVarUsesAndVarAssignments(List<ILStatement> statements) {
		for (ILStatement stmt : statements) {
			if (stmt instanceof ILStatementSet) {
				incVarAssignments(((ILStatementSet) stmt).getResultVar());
				if (stmt instanceof ILsetBinary) {
					ILsetBinary s = (ILsetBinary) stmt;
					incVarUses(s.getLeft());
					incVarUses(s.getRight());
				} else if (stmt instanceof ILsetVar) {
					ILsetVar s = (ILsetVar) stmt;
					incVarUses(s.getVar());
				} else if (stmt instanceof IlbuildinFunctionCall) {
					IlbuildinFunctionCall s = (IlbuildinFunctionCall) stmt;
					for (ILvar v : s.getArgs()) {
						incVarUses(v);
					}
				} else if (stmt instanceof ILfunctionCall) {
					ILfunctionCall s = (ILfunctionCall) stmt;
					for (ILvar v : s.getArgs()) {
						incVarUses(v);
					}
				} else if (stmt instanceof IlsetUnary) {
					IlsetUnary s = (IlsetUnary) stmt;
					incVarUses(s.getRight());
				}
			}
			if (stmt instanceof ILexitwhen) {
				incVarUses(((ILexitwhen) stmt).getVar());
			} else if (stmt instanceof ILreturn) {
				incVarUses(((ILreturn) stmt).getVar());
			} else if (stmt instanceof ILif) {
				ILif s = (ILif) stmt;
				incVarUses(s.getCond());
				getVarUsesAndVarAssignments(s.getThenBlock());
				getVarUsesAndVarAssignments(s.getElseBlock());
			} else if (stmt instanceof ILloop) {
				ILloop s = (ILloop) stmt;
				getVarUsesAndVarAssignments(s.getLoopBody());
			} 
		}
		
	}

	private void incVarUses(ILvar v) {
		int count = 0;
		if (varUses.containsKey(v)) {
			count = varUses.get(v);
		}
		count++;
		varUses.put(v, count);		
	}
	
	private void incVarAssignments(ILvar v) {
		int count = 0;
		if (varAssignments.containsKey(v)) {
			count = varAssignments.get(v);
		}
		count++;
		varAssignments.put(v, count);		
	}
}
