package de.peeeq.wurstscript.frotty.jassValidator;

import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassNative;
import de.peeeq.wurstscript.jassAst.JassProgs;
import de.peeeq.wurstscript.jassAst.JassTypeDef;
import de.peeeq.wurstscript.jassAst.JassVar;

public class JassProgsAttr {

	private static Map<String, JassFunction> functionsMap = Maps.newHashMap();
	private static Map<String, JassNative> nativesMap = Maps.newHashMap();
	private static Map<String, JassVar> globalsMap = Maps.newHashMap();
	private static Map<String, JassTypeDef> typeDefsMap = Maps.newHashMap();
	
	public static void addFunction(JassProgs jassProgs, JassFunction f) {
		functionsMap.put(f.getName(), f);
		
	}
	public static void addGlobal(JassProgs jassProgs, JassVar v) {
		globalsMap.put(v.getName(), v);
		
	}
	public static void addNative(JassProgs jassProgs, JassNative n) {
		nativesMap.put(n.getName(), n);
		
	}
	public static void addTypeDef(JassProgs jassProgs, JassTypeDef t) {
		typeDefsMap.put(t.getName(), t);
		
	}
	
	public static JassFunction getFunction(JassProgs jassProgsImpl, String name) {
		return functionsMap.get(name);
	}
	public static JassVar getGlobal(JassProgs jassProgsImpl, String name) {
		// TODO Auto-generated method stub
		return globalsMap.get(name);
	}
	public static JassNative getNative(JassProgs jassProgsImpl, String name) {
		// TODO Auto-generated method stub
		return nativesMap.get(name);
	}
	public static JassTypeDef getTypeDef(JassProgs jassProgsImpl, String name) {
		// TODO Auto-generated method stub
		return typeDefsMap.get(name);
	}
	


}
