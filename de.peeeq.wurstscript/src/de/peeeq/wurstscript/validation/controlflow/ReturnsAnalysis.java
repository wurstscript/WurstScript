package de.peeeq.wurstscript.validation.controlflow;

import java.util.Collection;

import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.EndFunctionStatement;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.WStatement;

public class ReturnsAnalysis extends ForwardMethod<Boolean> {


	@Override
	Boolean calculate(WStatement s, Boolean incoming) {
		if (s instanceof StmtReturn) {
			return true;
		} else {
			return incoming;
		}
	}

	@Override
	Boolean merge(Collection<Boolean> values) {
		if (values.isEmpty()) {
			// does not return
			return false;
		}
		for (Boolean v : values) {
			if (!v) {
				return false;
			}
		}
		return true;
	}

	@Override
	boolean equality(Boolean a, Boolean b) {
		return a.booleanValue() == b.booleanValue();
	}


	@Override
	void checkFinal(Boolean incoming) {
		FunctionImplementation f = getFuncDef();
		if (f.getReturnTyp() instanceof TypeExpr && !(f.attrNearestStructureDef() instanceof InterfaceDef)) {
			if (!incoming) {
				f.addError("Function " + f.getName() + " is missing a return statement.");
			}
		}
		
	}

	@Override
	public Boolean inital() {
		return true; 
	}


}
