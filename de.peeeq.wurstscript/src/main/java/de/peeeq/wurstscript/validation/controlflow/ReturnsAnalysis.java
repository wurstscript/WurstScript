package de.peeeq.wurstscript.validation.controlflow;

import java.util.Collection;

import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.StartFunctionStatement;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;

public class ReturnsAnalysis extends ForwardMethod<Boolean, FunctionLike> {


	@Override
	Boolean calculate(WStatement s, Boolean incoming) {
		if (s instanceof StartFunctionStatement) {
			return false;
		} else if (s instanceof StmtReturn) {
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
		FunctionLike f = getFuncDef();

		if (!(f.attrReturnType() instanceof WurstTypeVoid) && !(f.attrNearestStructureDef() instanceof InterfaceDef)) {
			if (!incoming) {
				f.addError(Utils.printElement(f) + " is missing a return statement.");
			}
		}
	}

	@Override
	public Boolean startValue() {
		return true; 
	}


}
