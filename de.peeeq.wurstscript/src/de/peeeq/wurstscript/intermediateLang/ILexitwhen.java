package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.utils.Utils;


public class ILexitwhen extends ILStatement {

	private ILvar var;

	public ILexitwhen(ILvar var) {
		this.var = var;
	}

	public ILvar getVar() {
		return var;
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("exitwhen " + var.getName() + "\n");
	}

	
	
}
