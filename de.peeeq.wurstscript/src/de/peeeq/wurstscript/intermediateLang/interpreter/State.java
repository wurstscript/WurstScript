package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.jassIm.ImVar;

public abstract class State {

	private Map<ImVar, ILconst> values = Maps.newHashMap();
	private Map<ImVar, Map<Integer, ILconst>> arrayValues = Maps.newHashMap();


	public void setVal(ImVar v, ILconst val) {
		values.put(v, val);
	}

	public ILconst getVal(ImVar v) {
		return values.get(v);
	}
	
	private Map<Integer, ILconst> getArray(ImVar v) {
		Map<Integer, ILconst> r = arrayValues.get(v);
		if (r == null) {
			r = Maps.newHashMap();
			arrayValues.put(v, r);
		}
		return r;
	}

	public void setArrayVal(ImVar v, int index, ILconst val) {
		getArray(v).put(index, val);
	}

	public ILconst getArrayVal(ImVar v, int index) {
		return getArray(v).get(index);
	}
	
}