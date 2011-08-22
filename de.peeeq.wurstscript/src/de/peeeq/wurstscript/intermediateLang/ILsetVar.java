package de.peeeq.wurstscript.intermediateLang;


public class ILsetVar extends ILStatementSet {

	private ILvar var;

	public ILsetVar(ILvar resultVar, ILvar var) {
		super(resultVar);
		this.var = var;
	}


	public ILvar getVar() {
		return var;
	}


	@Override
	public void printJass(StringBuilder sb) {
		sb.append("set " + getResultVar().getName() + " = " + var.getName() + "\n");
	}
	
	

}
