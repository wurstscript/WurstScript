package de.peeeq.wurstscript.intermediateLang;

import java.util.List;

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
	public void printJass(StringBuilder sb) {
		sb.append("if " + cond.getName() + " then\n");
		for (ILStatement s : thenBlock) {
			s.printJass(sb);
		}
		if (elseBlock.size() > 0) {
			sb.append("else\n");
			for (ILStatement s : elseBlock) {
				s.printJass(sb);
			}
		}
		sb.append("endif\n");
		
		
	}
	
}
