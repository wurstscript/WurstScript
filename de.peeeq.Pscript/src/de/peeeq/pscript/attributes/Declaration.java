package de.peeeq.pscript.attributes;

import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.NameDef;


public abstract class Declaration {

	public static Declaration of(NameDef x) {
		return new DeclarationStub(x);
	}

	public abstract String getName();

	public abstract void caseFuncDef(VoidFunction<FuncDef> callback);

}

class DeclarationStub extends Declaration {
	private NameDef nameDef;

	DeclarationStub(NameDef nameDef) {
		this.nameDef = nameDef;
	}

	@Override
	public String getName() {
		return nameDef.getName();
	}
	
	@Override
	public String toString() {
		if (nameDef instanceof FuncDef) {
			AttrUtil.printFuncDef((FuncDef) nameDef);
		}
		return nameDef.toString();
	}


	@Override
	public void caseFuncDef(VoidFunction<FuncDef> callback) {
		if (nameDef instanceof FuncDef) {
			callback.appy((FuncDef) nameDef);
		}		
	}
}
