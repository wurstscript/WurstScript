package de.peeeq.eclipsewurstplugin.console;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;

/**
 * a more robust global state, because it does not use vars as keys
 * but uses the varname + the linenumber + the file as key. 
 */
public class RobustProgramState extends ProgramState {

	
	// (varname, line) -> value)
	private Map<String, ILconst> values = Maps.newLinkedHashMap();
	
	// (varname, line) -> (index -> value))
	private Map<String, Map<Integer, ILconst>> arrayValues = Maps.newLinkedHashMap();
	
	public RobustProgramState(@Nullable File mapFile, WurstGui gui, ImProg prog) {
		super(gui, prog, false);
	}
	
	
	@Override
	public void setVal(ImVar v, ILconst val) {
		values.put(key(v), val);
	}

	private String key(ImVar v) {
		if (v == null) throw new IllegalArgumentException();
		if (v.attrTrace() == null) {
			System.err.println("Variable has no trace: " + v);
			return v.getName();
		}
		return v.attrTrace().attrPathDescription() + "^" + v.getName();
	}


	@Override
	public ILconst getVal(ImVar v) {
		return values.get(key(v));
	}
	
	private Map<Integer, ILconst> getArray(String key) {
		Map<Integer, ILconst> r = arrayValues.get(key);
		if (r == null) {
			r = Maps.newLinkedHashMap();
			arrayValues.put(key, r);
		}
		return r;
	}

	@Override
	public void setArrayVal(ImVar v, int index, ILconst val) {
		getArray(key(v)).put(index, val);
	}

	@Override
	public ILconst getArrayVal(ImVar v, int index) {
		return getArray(key(v)).get(index);
	}
	
	@Override
	public @Nullable ILconst getVarValue(String varName) {
		for (Entry<String, ILconst> e : values.entrySet()) {
			if (e.getKey().endsWith("^"+varName)) {
				return e.getValue();
			}
		}
		return null;
	}
	
	@Override
	protected Object classKey(ImClass clazz) {
		return clazz.getTrace().attrPathDescription() + "^" + clazz.getName();
	}
}
