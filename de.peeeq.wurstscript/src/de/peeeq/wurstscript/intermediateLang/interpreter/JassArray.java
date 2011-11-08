package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.util.Map;

import org.testng.collections.Maps;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstAbstract;
import de.peeeq.wurstscript.types.PscriptType;

public class JassArray extends ILconstAbstract {

	private String type;
	private Map<Integer, ILconst> values = Maps.newHashMap();
	
	public JassArray(String type) {
		this.type = type;
	}

	@Override
	public PscriptType getType() {
		throw new Error("Not implemented.");
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		throw new Error("Not implemented yet.");
	}

	@Override
	public String print() {
		throw new Error("Not implemented yet.");
	}

	public void set(int key, ILconst value) {
		values.put(key, value);
	}
	
	public ILconst get(int key) {
		if (values.containsKey(key)) {
			return values.get(key);
		} else {
			return JassInterpreter.getDefaultValue(type);
		}
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		throw new Error("Cannot compare arrays.");
	}

}
