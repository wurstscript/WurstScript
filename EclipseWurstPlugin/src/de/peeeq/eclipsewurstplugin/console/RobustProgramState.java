package de.peeeq.eclipsewurstplugin.console;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.utils.Pair;

/**
 * a more robust global state, because it does not use vars as keys
 * but uses the varname + the linenumber + the file as key. 
 */
public class RobustProgramState extends ProgramState {

	
	// (varname, line) -> value)
	private Map<String, ILconst> values = Maps.newHashMap();
	
	// (varname, line) -> (index -> value))
	private Map<String, Map<Integer, ILconst>> arrayValues = Maps.newHashMap();
	
	public RobustProgramState(File mapFile, WurstGui gui) {
		super(mapFile, gui);
	}
	
	
	public void setVal(ImVar v, ILconst val) {
		values.put(key(v), val);
	}

	private String key(ImVar v) {
		return v.attrTrace().attrPathDescription() + "^" + v.getName();
	}


	public ILconst getVal(ImVar v) {
		return values.get(key(v));
	}
	
	private Map<Integer, ILconst> getArray(String key) {
		Map<Integer, ILconst> r = arrayValues.get(key);
		if (r == null) {
			r = Maps.newHashMap();
			arrayValues.put(key, r);
		}
		return r;
	}

	public void setArrayVal(ImVar v, int index, ILconst val) {
		getArray(key(v)).put(index, val);
	}

	public ILconst getArrayVal(ImVar v, int index) {
		return getArray(key(v)).get(index);
	}
	
	public ILconst getVarValue(String varName) {
		for (Entry<String, ILconst> e : values.entrySet()) {
			if (e.getKey().endsWith("^"+varName)) {
				return e.getValue();
			}
		}
		return null;
	}
}
