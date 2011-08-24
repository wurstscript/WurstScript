package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.utils.Utils;

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
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("set " + getResultVar().getName() + " = " + var.getName() + "[" + index.getName() + "]\n");
	}

}
