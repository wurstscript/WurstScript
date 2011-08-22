package de.peeeq.wurstscript.intermediateLang;

/**
 * set resultVar = var[index] 
 */
public class ILsetVarArray extends ILStatementSet {

	private ILvar var;
	private ILvar index;

	public ILsetVarArray(ILvar resultVar, ILvar var, ILvar index) {
		super(resultVar);
		this.var = var;
		this.index = index;
	}

	public ILvar getVar() {
		return var;
	}

	public ILvar getIndex() {
		return index;
	}

	@Override
	public void printJass(StringBuilder sb) {
		sb.append("set " + getResultVar().getName() + " = " + var.getName() + "[" + index.getName() + "]\n");
	}

}
