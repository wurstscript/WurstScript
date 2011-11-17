package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.Map;

import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WParameter;

public class ScopeForVars {
	
	private Map<String, VarDef> vars = new HashMap<String, VarDef>();
	private Map<String, VarDef> publicVars = new HashMap<String, VarDef>();
	private Map<String, VarDef> packageVars = new HashMap<String, VarDef>();
	
	private void addVar(VarDef v) {
		String name = v.getName();
		if (vars.containsKey(name)) {
			VarDef firstDefinition = vars.get(name);
		}
		vars.put(name, v);
	}
	
	
	public void add(LocalVarDef v) {
		addVar(v);
	}
	
	public void add(WParameter v) {
		addVar(v);
	}
	
	public void add(GlobalVarDef v) {
		addVar(v);
		if (v.attrIsPublic()) {
			publicVars.put(v.getName(), v);
		}
		if (! (v.attrIsPrivate())) {
			packageVars.put(v.getName(), v);
		}
	}
}
