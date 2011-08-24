package de.peeeq.wurstscript.intermediateLang;

import java.util.List;

import de.peeeq.wurstscript.utils.Utils;

public class ILif extends ILStatement {

	private ILvar cond;
	private List<ILStatement> thenBlock;
	private List<ILStatement> elseBlock;

	public ILif(ILvar cond, List<ILStatement> thenBlock, List<ILStatement> elseBlock) {
		this.cond = cond;
		this.thenBlock = thenBlock;
		this.elseBlock = elseBlock;
	}

	public ILvar getCond() {
		return cond;
	}

	public List<ILStatement> getThenBlock() {
		return thenBlock;
	}

	public List<ILStatement> getElseBlock() {
		return elseBlock;
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("if " + cond.getName() + " then\n");
		for (ILStatement s : thenBlock) {
			s.printJass(sb, indent+1);
		}
		if (elseBlock.size() > 0) {
			Utils.printIndent(sb, indent);
			sb.append("else\n");
			for (ILStatement s : elseBlock) {
				s.printJass(sb, indent+1);
			}
		}
		Utils.printIndent(sb, indent);
		sb.append("endif\n");
		
		
	}
	
}
