package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.Map;

import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.LocalVarDefPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.ast.VisibilityPrivatePos;
import de.peeeq.wurstscript.ast.VisibilityPublicPos;
import de.peeeq.wurstscript.ast.WParameterPos;

public class ScopeForVars {
	
	private Map<String, VarDefPos> vars = new HashMap<String, VarDefPos>();
	private Map<String, VarDefPos> publicVars = new HashMap<String, VarDefPos>();
	private Map<String, VarDefPos> packageVars = new HashMap<String, VarDefPos>();
	
	private void addVar(VarDefPos v) {
		String name = v.name().term();
		if (vars.containsKey(name)) {
			VarDefPos firstDefinition = vars.get(name);
		}
		vars.put(name, v);
	}
	
	
	public void add(LocalVarDefPos v) {
		addVar(v);
	}
	
	public void add(WParameterPos v) {
		addVar(v);
	}
	
	public void add(GlobalVarDefPos v) {
		addVar(v);
		if (v.visibility() instanceof VisibilityPublicPos) {
			publicVars.put(v.name().term(), v);
		}
		if (! (v.visibility() instanceof VisibilityPrivatePos)) {
			packageVars.put(v.name().term(), v);
		}
	}
}
