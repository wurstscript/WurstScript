package de.peeeq.pscript.intermediateLang;

import java.util.List;

public class ILloop extends ILStatement {

	private List<ILStatement> loopBody;

	public ILloop(List<ILStatement> loopBody) {
		this.loopBody = loopBody;
	}

	public List<ILStatement> getLoopBody() {
		return loopBody;
	}

}
