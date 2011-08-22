package de.peeeq.wurstscript.intermediateLang;

public class ILreturn extends ILStatement {

	private ILvar var;

	public ILreturn(ILvar var) {
		this.var = var;
	}

	public ILvar getVar() {
		return var;
	}

	@Override
	public void printJass(StringBuilder sb) {
		sb.append("return " + var.getName());
	}
	
	

}
